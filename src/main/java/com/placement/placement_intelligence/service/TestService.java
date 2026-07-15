package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.model.Question;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentAnswer;
import com.placement.placement_intelligence.model.StudentTestAttempt;
import com.placement.placement_intelligence.model.TestType;
import com.placement.placement_intelligence.model.TestSession;
import com.placement.placement_intelligence.model.SessionStatus;
import com.placement.placement_intelligence.repository.QuestionRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentTestAttemptRepository;
import com.placement.placement_intelligence.repository.TestSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestService {

    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;
    private final StudentTestAttemptRepository attemptRepository;
    private final StudentAnswerRepository answerRepository;
    private final TestSessionRepository testSessionRepository;
    private final GroqQuestionGeneratorService groqQuestionGeneratorService;

    public TestService(QuestionRepository questionRepository,
                       StudentRepository studentRepository,
                       StudentTestAttemptRepository attemptRepository,
                       StudentAnswerRepository answerRepository,
                       TestSessionRepository testSessionRepository,
                       GroqQuestionGeneratorService groqQuestionGeneratorService) {
        this.questionRepository = questionRepository;
        this.studentRepository = studentRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.testSessionRepository = testSessionRepository;
        this.groqQuestionGeneratorService = groqQuestionGeneratorService;
    }

    @Transactional(readOnly = true)
    public Question findQuestionById(Long questionId) {
        return questionRepository.findById(questionId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Question> getRandomQuestions(String topic, int count) {
        List<Question> questions = new ArrayList<>(questionRepository.findByTopicIgnoreCase(topic));
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.shuffle(questions);
        return questions.subList(0, Math.min(count, questions.size()));
    }

    @Transactional(readOnly = true)
    public List<Question> getRandomQuestionsForCompany(String topic, Long companyId, int count) {
        List<Question> questions = new ArrayList<>(questionRepository.findByTopicIgnoreCaseAndCompanyId(topic, companyId));
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.shuffle(questions);
        return questions.subList(0, Math.min(count, questions.size()));
    }

    @Transactional
    public List<Question> getOrGenerateQuestions(TestType type, String requestedTopic, int count) {
        if (type == TestType.MOCK) {
            List<Question> combined = new ArrayList<>();
            combined.addAll(getOrGenerateQuestions(TestType.APTITUDE, "Aptitude", 20));
            combined.addAll(getOrGenerateQuestions(TestType.CODING, "DSA", 2));
            return combined;
        }

        String canonicalTopic = canonicalTopic(type);
        String topic = normalizeTopic(requestedTopic, canonicalTopic);

        List<Question> available = new ArrayList<>(questionRepository.findByTopicIgnoreCase(topic));
        if (available.size() >= count) {
            Collections.shuffle(available);
            return available.subList(0, count);
        }

        int missing = count - available.size();
        List<Question> generated = groqQuestionGeneratorService.generateQuestions(type, topic, missing);
        if (!generated.isEmpty()) {
            questionRepository.saveAll(generated);
            available = new ArrayList<>(questionRepository.findByTopicIgnoreCase(topic));
        }

        if (available.size() < count && !topic.equalsIgnoreCase(canonicalTopic)) {
            List<Question> fallback = new ArrayList<>(questionRepository.findByTopicIgnoreCase(canonicalTopic));
            available.addAll(fallback);
        }

        if (available.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.shuffle(available);
        return available.subList(0, Math.min(count, available.size()));
    }

    @Transactional(readOnly = true)
    public int evaluateAnswers(Map<Long, String> answers) {
        Map<Long, Question> questionsById = loadQuestionsById(answers);
        int score = 0;
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Question question = questionsById.get(entry.getKey());
            if (question.getCorrectOption().equalsIgnoreCase(entry.getValue())) {
                score++;
            }
        }
        return score;
    }

    @Transactional
    public StudentTestAttempt recordTestResult(Long studentId, TestType testType, Long sessionId, Map<Long, String> answers) {
        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        validateSession(session, studentId, testType);

        Map<Long, Question> questionsById = loadQuestionsById(answers);
        int score = calculateScore(answers, questionsById);
        int totalQuestions = session.getTotalQuestions() > 0 ? session.getTotalQuestions() : answers.size();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        double scaledScore = totalQuestions == 0 ? 0.0 : (score * 100.0) / totalQuestions;
        updateStudentTestScore(student, testType, scaledScore);
        student.setReadinessScore(calculateReadiness(student));
        student.setFinalScore(student.getReadinessScore());
        studentRepository.save(student);

        StudentTestAttempt attempt = new StudentTestAttempt();
        attempt.setStudent(student);
        attempt.setTestType(testType);
        attempt.setSession(session);
        attempt.setScore(score);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setTestDate(LocalDateTime.now());
        attempt = attemptRepository.save(attempt);

        saveStudentAnswers(attempt, answers, questionsById);

        return attempt;
    }

    private double calculateReadiness(Student student) {
        // Simple weighted average of test scores for quick updates
        // The comprehensive calculation is done in StudentDashboardService
        double aptitude = defaultScore(student.getAptitudeScore());
        double coding = defaultScore(student.getDsaScore());
        double softSkills = defaultScore(student.getSoftSkillScore());
        return Math.round(((aptitude * 0.35) + (coding * 0.45) + (softSkills * 0.20)) * 100.0) / 100.0;
    }

    private double defaultScore(Double value) {
        return value == null ? 0.0 : value;
    }

    private Map<Long, Question> loadQuestionsById(Map<Long, String> answers) {
        Map<Long, Question> questionsById = new HashMap<>();
        for (Long questionId : answers.keySet()) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found"));
            questionsById.put(questionId, question);
        }
        return questionsById;
    }

    private int calculateScore(Map<Long, String> answers, Map<Long, Question> questionsById) {
        int score = 0;
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Question question = questionsById.get(entry.getKey());
            if (question.getCorrectOption().equalsIgnoreCase(entry.getValue())) {
                score++;
            }
        }
        return score;
    }

    private void updateStudentTestScore(Student student, TestType testType, double scaledScore) {
        if (testType == TestType.APTITUDE) {
            student.setAptitudeScore(scaledScore);
            return;
        }
        if (testType == TestType.CODING) {
            student.setDsaScore(scaledScore);
            return;
        }
        if (testType == TestType.SOFT_SKILLS) {
            student.setSoftSkillScore(scaledScore);
            return;
        }
        student.setMockTestScore(scaledScore);
    }

    private void saveStudentAnswers(StudentTestAttempt attempt, Map<Long, String> answers, Map<Long, Question> questionsById) {
        for (Map.Entry<Long, String> entry : answers.entrySet()) {
            Question question = questionsById.get(entry.getKey());
            StudentAnswer answer = new StudentAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(entry.getValue());
            answer.setCorrect(question.getCorrectOption().equalsIgnoreCase(entry.getValue()));
            answerRepository.save(answer);
        }
    }

    @Transactional
    public TestSession startSession(Long studentId, TestType testType, int totalQuestions, int durationMinutes) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        TestSession session = new TestSession();
        session.setStudent(student);
        session.setTestType(testType);
        session.setStartTime(LocalDateTime.now());
        session.setDurationMinutes(durationMinutes);
        session.setTotalQuestions(totalQuestions);
        session.setStatus(SessionStatus.ACTIVE);
        return testSessionRepository.save(session);
    }

    private void validateSession(TestSession session, Long studentId, TestType testType) {
        if (!session.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Session does not belong to student");
        }
        if (session.getTestType() != testType) {
            throw new IllegalArgumentException("Session type mismatch");
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Session is not active");
        }
        LocalDateTime expiry = session.getStartTime().plusMinutes(session.getDurationMinutes());
        if (LocalDateTime.now().isAfter(expiry)) {
            session.setStatus(SessionStatus.EXPIRED);
            testSessionRepository.save(session);
            throw new IllegalArgumentException("Session has expired");
        }
        session.setStatus(SessionStatus.SUBMITTED);
        testSessionRepository.save(session);
    }

    private String canonicalTopic(TestType type) {
        if (type == TestType.CODING) {
            return "DSA";
        }
        if (type == TestType.SOFT_SKILLS) {
            return "SOFT_SKILLS";
        }
        if (type == TestType.MOCK) {
            return "APTITUDE";
        }
        return "APTITUDE";
    }

    private String normalizeTopic(String topic, String fallback) {
        if (topic == null || topic.isBlank()) {
            return fallback;
        }
        String cleaned = topic.trim().replaceAll("\\s+", " ");
        if (cleaned.length() > 60) {
            return cleaned.substring(0, 60);
        }
        return cleaned;
    }
}
