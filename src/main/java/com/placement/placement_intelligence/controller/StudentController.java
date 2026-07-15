package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.ProgressResponse;
import com.placement.placement_intelligence.dto.CodeExecutionRequest;
import com.placement.placement_intelligence.dto.CodeExecutionResponse;
import com.placement.placement_intelligence.dto.StudentDashboardResponse;
import com.placement.placement_intelligence.dto.TestHistoryItem;
import com.placement.placement_intelligence.dto.TestQuestionResponse;
import com.placement.placement_intelligence.dto.TestSubmissionRequest;
import com.placement.placement_intelligence.dto.TestSubmissionResponse;
import com.placement.placement_intelligence.model.Question;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentSkill;
import com.placement.placement_intelligence.model.StudentTestAttempt;
import com.placement.placement_intelligence.model.TestSession;
import com.placement.placement_intelligence.model.TestType;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentSkillRepository;
import com.placement.placement_intelligence.repository.StudentTestAttemptRepository;
import com.placement.placement_intelligence.service.CurrentUserService;
import com.placement.placement_intelligence.service.CodeExecutionService;
import com.placement.placement_intelligence.service.StudentDashboardService;
import com.placement.placement_intelligence.service.TestService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {
    private static final Path RESUME_DIR = Paths.get("uploads", "resumes");

    private final StudentRepository studentRepository;
    private final StudentTestAttemptRepository attemptRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final TestService testService;
    private final CodeExecutionService codeExecutionService;
    private final CurrentUserService currentUserService;
    private final StudentDashboardService dashboardService;

    public StudentController(StudentRepository studentRepository,
                             StudentTestAttemptRepository attemptRepository,
                             StudentSkillRepository studentSkillRepository,
                             StudentAnswerRepository studentAnswerRepository,
                             TestService testService,
                             CodeExecutionService codeExecutionService,
                             CurrentUserService currentUserService,
                             StudentDashboardService dashboardService) {
        this.studentRepository = studentRepository;
        this.attemptRepository = attemptRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.testService = testService;
        this.codeExecutionService = codeExecutionService;
        this.currentUserService = currentUserService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> dashboard() {
        return ResponseEntity.ok(dashboardService.buildDashboard(currentUserService.currentStudentId()));
    }

    @GetMapping("/{studentId}/dashboard")
    public ResponseEntity<StudentDashboardResponse> dashboardById(@PathVariable Long studentId) {
        return ResponseEntity.ok(dashboardService.buildDashboard(studentId));
    }

    @GetMapping("/tests")
    public ResponseEntity<TestQuestionResponse> tests(@RequestParam(defaultValue = "aptitude") String section,
                                                      @RequestParam(required = false) String topic) {
        Long studentId = currentUserService.currentStudentId();
        TestType type = toTestType(section);
        int questionCount = switch (type) {
            case CODING -> 5;
            case SOFT_SKILLS -> 10;
            case MOCK -> 22; // 20 Aptitude + 2 Coding
            default -> 20;
        };
        List<Question> questions = testService.getOrGenerateQuestions(type, topic, questionCount);
        int duration = 20;
        if (type == TestType.CODING) {
            duration = 60;
        } else if (type == TestType.MOCK) {
            duration = 90; // 90 minutes for Mock Test
        } else if (type == TestType.SOFT_SKILLS) {
            duration = 15;
        }
        TestSession session = testService.startSession(studentId, type, questions.size(), duration);
        return ResponseEntity.ok(toQuestionResponse(type, questions.size(), duration, session.getId(), questions));
    }

    @GetMapping("/test-history")
    public ResponseEntity<List<TestHistoryItem>> testHistory() {
        Long studentId = currentUserService.currentStudentId();
        List<StudentTestAttempt> attempts = attemptRepository.findByStudent_IdOrderByTestDateDesc(studentId);
        return ResponseEntity.ok(toHistory(attempts));
    }

    @PostMapping("/submit-test")
    public ResponseEntity<TestSubmissionResponse> submitTest(@RequestBody TestSubmissionRequest request) {
        Long studentId = currentUserService.currentStudentId();
        StudentTestAttempt attempt = testService.recordTestResult(studentId, request.getTestType(),
                request.getSessionId(), request.getAnswers());
        double accuracy = attempt.getTotalQuestions() == 0 ? 0.0 :
                (attempt.getScore() * 100.0 / attempt.getTotalQuestions());
        TestSubmissionResponse response = new TestSubmissionResponse(
                attempt.getId(),
                attempt.getTestType(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                round(accuracy),
                request.getTimeTakenSeconds() == null ? 0L : request.getTimeTakenSeconds(),
                weakAreas(studentId),
                attempt.getTestDate()
        );
        // Build per-question review items (correct answers revealed after submission)
        if (request.getAnswers() != null && !request.getAnswers().isEmpty()) {
            List<TestSubmissionResponse.QuestionReview> reviewItems = new ArrayList<>();
            for (Map.Entry<Long, String> entry : request.getAnswers().entrySet()) {
                com.placement.placement_intelligence.model.Question q = testService.findQuestionById(entry.getKey());
                if (q != null) {
                    TestSubmissionResponse.QuestionReview rev = new TestSubmissionResponse.QuestionReview();
                    rev.setQuestionId(q.getId());
                    rev.setQuestionText(q.getQuestionText());
                    rev.setOptionA(q.getOptionA());
                    rev.setOptionB(q.getOptionB());
                    rev.setOptionC(q.getOptionC());
                    rev.setOptionD(q.getOptionD());
                    rev.setCorrectOption(q.getCorrectOption());
                    rev.setSelectedOption(entry.getValue());
                    rev.setTopic(q.getTopic());
                    rev.setDifficultyLevel(q.getDifficultyLevel());
                    reviewItems.add(rev);
                }
            }
            response.setReviewItems(reviewItems);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<ProgressResponse> progress() {
        Long studentId = currentUserService.currentStudentId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        ProgressResponse response = new ProgressResponse();
        List<ProgressResponse.ProgressPoint> timeline = new ArrayList<>();
        List<StudentTestAttempt> attempts = attemptRepository.findByStudent_IdOrderByTestDateDesc(studentId);
        attempts.stream()
                .sorted(Comparator.comparing(StudentTestAttempt::getTestDate))
                .forEach(attempt -> timeline.add(new ProgressResponse.ProgressPoint(
                        attempt.getTestDate().toLocalDate().toString(),
                        round(student.getReadinessScore() == null ? 0.0 : student.getReadinessScore()))));
        if (timeline.isEmpty()) {
            timeline.add(new ProgressResponse.ProgressPoint(LocalDate.now().toString(),
                    round(student.getReadinessScore() == null ? 0.0 : student.getReadinessScore())));
        }
        response.setTimeline(timeline);
        response.setSectionPerformance(new ProgressResponse.SectionPerformance(
                round(defaultScore(student.getAptitudeScore())),
                round(defaultScore(student.getDsaScore())),
                round(defaultScore(student.getSoftSkillScore()))
        ));
        response.setSuggestions(buildRecommendations(student));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> profile() {
        Long studentId = currentUserService.currentStudentId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Map<String, Object> payload = new HashMap<>();
        payload.put("studentId", student.getId());
        payload.put("name", student.getName());
        payload.put("email", student.getUser() == null ? "" : student.getUser().getEmail());
        payload.put("department", student.getDepartment().getName());
        payload.put("cgpa", student.getCgpa());
        payload.put("interests", student.getInterests());
        payload.put("level", student.getLevel());
        payload.put("phone", student.getPhone());
        payload.put("achievements", student.getAchievements());
        payload.put("resumeUploaded", student.getResumePath() != null && !student.getResumePath().isBlank());
        payload.put("resumeFileName", extractFileName(student.getResumePath()));
        payload.put("profileStrength", calculateProfileStrength(student));
        payload.put("skills", studentSkillRepository.findByStudent_Id(studentId).stream()
                .map(StudentSkill::getSkill)
                .map(skill -> skill.getName())
                .toList());
        payload.put("history", toHistory(attemptRepository.findByStudent_IdOrderByTestDateDesc(studentId)));
        return ResponseEntity.ok(payload);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(@RequestBody Map<String, Object> request) {
        Long studentId = currentUserService.currentStudentId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (request.get("name") instanceof String name) {
            student.setName(name);
        }
        if (request.get("interests") instanceof String interests) {
            student.setInterests(interests);
        }
        if (request.get("level") instanceof String level) {
            student.setLevel(level);
        }
        if (request.get("cgpa") instanceof Number cgpa) {
            student.setCgpa(cgpa.doubleValue());
        }
        if (request.get("phone") instanceof String phone) {
            student.setPhone(phone);
        }
        if (request.get("achievements") instanceof String achievements) {
            student.setAchievements(achievements);
        }
        studentRepository.save(student);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    @PostMapping(path = "/profile/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }
        Long studentId = currentUserService.currentStudentId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Files.createDirectories(RESUME_DIR);
        String original = file.getOriginalFilename() == null ? "resume.pdf" : file.getOriginalFilename();
        String safe = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String filename = studentId + "_" + LocalDateTime.now().toString().replace(":", "-") + "_" + safe;
        Path target = RESUME_DIR.resolve(filename).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        student.setResumePath(target.toString());
        studentRepository.save(student);
        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded successfully",
                "resumeFileName", safe
        ));
    }

    @GetMapping("/profile/resume/download")
    public ResponseEntity<ByteArrayResource> downloadResume() throws IOException {
        Long studentId = currentUserService.currentStudentId();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getResumePath() == null || student.getResumePath().isBlank()) {
            throw new IllegalArgumentException("Resume not uploaded yet");
        }
        Path path = Paths.get(student.getResumePath());
        byte[] bytes = Files.readAllBytes(path);
        String filename = extractFileName(student.getResumePath());
        ByteArrayResource resource = new ByteArrayResource(bytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PostMapping("/code/execute")
    public ResponseEntity<CodeExecutionResponse> executeCode(@RequestBody CodeExecutionRequest request) {
        currentUserService.currentStudentId();
        return ResponseEntity.ok(codeExecutionService.execute(request));
    }

    private List<String> weakAreas(Long studentId) {
        List<String> weak = new ArrayList<>();
        for (Object[] row : studentAnswerRepository.aggregateTopicPerformanceByStudent(studentId)) {
            double correct = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            double total = row[2] == null ? 0.0 : ((Number) row[2]).doubleValue();
            double accuracy = total == 0 ? 0 : (correct * 100.0 / total);
            if (accuracy < 60) {
                weak.add(row[0].toString());
            }
        }
        if (weak.isEmpty()) {
            weak.add("Maintain consistency in mixed practice");
        }
        return weak;
    }

    private List<String> buildRecommendations(Student student) {
        List<String> suggestions = new ArrayList<>();
        if (defaultScore(student.getDsaScore()) < 60) {
            suggestions.add("Improve in Arrays");
            suggestions.add("Solve two medium coding problems daily");
        }
        if (defaultScore(student.getAptitudeScore()) < 60) {
            suggestions.add("Practice Logical Reasoning");
        }
        if (defaultScore(student.getSoftSkillScore()) < 60) {
            suggestions.add("Take Communication and HR scenario modules");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Keep up mock tests to maintain readiness");
        }
        return suggestions;
    }

    private List<TestHistoryItem> toHistory(List<StudentTestAttempt> attempts) {
        List<TestHistoryItem> items = new ArrayList<>();
        for (StudentTestAttempt attempt : attempts) {
            items.add(new TestHistoryItem(
                    attempt.getId(),
                    attempt.getTestType(),
                    attempt.getScore(),
                    attempt.getTotalQuestions(),
                    attempt.getTestDate()
            ));
        }
        return items;
    }

    private double calculateProfileStrength(Student student) {
        int total = 9;
        int completed = 0;
        if (student.getName() != null && !student.getName().isBlank()) completed++;
        if (student.getUser() != null && student.getUser().getEmail() != null && !student.getUser().getEmail().isBlank()) completed++;
        if (student.getCgpa() != null && student.getCgpa() > 0) completed++;
        if (student.getLevel() != null && !student.getLevel().isBlank()) completed++;
        if (student.getInterests() != null && !student.getInterests().isBlank()) completed++;
        if (student.getPhone() != null && !student.getPhone().isBlank()) completed++;
        if (student.getAchievements() != null && !student.getAchievements().isBlank()) completed++;
        if (student.getResumePath() != null && !student.getResumePath().isBlank()) completed++;
        if (!studentSkillRepository.findByStudent_Id(student.getId()).isEmpty()) completed++;
        return round((completed * 100.0) / total);
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return Paths.get(path).getFileName().toString();
    }

    private TestType toTestType(String section) {
        String value = section.toLowerCase();
        if ("coding".equals(value)) {
            return TestType.CODING;
        }
        if ("softskills".equals(value) || "soft_skills".equals(value) || "soft-skills".equals(value)) {
            return TestType.SOFT_SKILLS;
        }
        if ("mock".equals(value)) {
            return TestType.MOCK;
        }
        return TestType.APTITUDE;
    }

    private TestQuestionResponse toQuestionResponse(TestType type, int totalQuestions, int durationMinutes, Long sessionId, List<Question> questions) {
        List<TestQuestionResponse.QuestionPayload> payloads = new ArrayList<>();
        for (Question question : questions) {
            payloads.add(new TestQuestionResponse.QuestionPayload(
                    question.getId(),
                    question.getQuestionText(),
                    question.getOptionA(),
                    question.getOptionB(),
                    question.getOptionC(),
                    question.getOptionD(),
                    question.getDifficultyLevel(),
                    question.getTopic()
            ));
        }
        return new TestQuestionResponse(type.name(), totalQuestions, durationMinutes, sessionId, payloads);
    }

    private double defaultScore(Double score) {
        return score == null ? 0.0 : score;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
