package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.DepartmentStatsResponse;
import com.placement.placement_intelligence.dto.StudentDetailsResponse;
import com.placement.placement_intelligence.dto.TestHistoryItem;
import com.placement.placement_intelligence.dto.TopStudentsResponse;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentTestAttempt;
import com.placement.placement_intelligence.repository.DepartmentRepository;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentTestAttemptRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentTestAttemptRepository attemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final UserRepository userRepository;

    public StaffController(StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           StudentTestAttemptRepository attemptRepository,
                           StudentAnswerRepository studentAnswerRepository,
                           UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.attemptRepository = attemptRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/department-stats")
    public ResponseEntity<DepartmentStatsResponse> departmentStats() {
        DepartmentStatsResponse response = new DepartmentStatsResponse();
        response.setTotalStudents(studentRepository.totalStudents());
        response.setActiveUsers(userRepository.countActiveUsers());
        response.setLoggedInToday(userRepository.countByLastLoginAtAfter(LocalDate.now().atStartOfDay()));
        List<DepartmentStatsResponse.DepartmentPerformance> performance = new ArrayList<>();
        for (Object[] row : studentRepository.averageFinalScoreByDepartment()) {
            Long departmentId = ((Number) row[0]).longValue();
            String departmentName = departmentRepository.findById(departmentId)
                    .map(d -> d.getName())
                    .orElse("Unknown");
            Double average = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            performance.add(new DepartmentStatsResponse.DepartmentPerformance(departmentName, round(average)));
        }
        response.setDepartmentPerformance(performance);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-students")
    public ResponseEntity<TopStudentsResponse> topStudents() {
        TopStudentsResponse response = new TopStudentsResponse();
        Map<String, List<TopStudentsResponse.TopStudent>> result = new HashMap<>();
        departmentRepository.findAll().forEach(department -> {
            List<TopStudentsResponse.TopStudent> top = studentRepository.findByDepartmentIdOrderByFinalScoreDesc(department.getId())
                    .stream()
                    .limit(3)
                    .map(student -> new TopStudentsResponse.TopStudent(
                            student.getId(),
                            student.getName(),
                            round(defaultScore(student.getReadinessScore())),
                            round(defaultScore(student.getFinalScore()))
                    ))
                    .toList();
            result.put(department.getName(), top);
        });
        response.setTopByDepartment(result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student-details")
    public ResponseEntity<StudentDetailsResponse> studentDetails(@RequestParam Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        StudentDetailsResponse response = new StudentDetailsResponse();
        response.setStudentId(student.getId());
        response.setName(student.getName());
        response.setDepartment(student.getDepartment().getName());
        response.setReadiness(round(defaultScore(student.getReadinessScore())));
        response.setAptitude(round(defaultScore(student.getAptitudeScore())));
        response.setCoding(round(defaultScore(student.getDsaScore())));
        response.setSoftSkills(round(defaultScore(student.getSoftSkillScore())));
        response.setWeakAreas(weakTopics(studentId));

        List<TestHistoryItem> history = new ArrayList<>();
        List<StudentTestAttempt> attempts = attemptRepository.findByStudent_IdOrderByTestDateDesc(studentId);
        attempts.stream().sorted(Comparator.comparing(StudentTestAttempt::getTestDate).reversed()).forEach(attempt ->
                history.add(new TestHistoryItem(attempt.getId(), attempt.getTestType(),
                        attempt.getScore(), attempt.getTotalQuestions(), attempt.getTestDate())));
        response.setActivity(history);
        return ResponseEntity.ok(response);
    }

    private List<String> weakTopics(Long studentId) {
        List<String> weak = new ArrayList<>();
        for (Object[] row : studentAnswerRepository.aggregateTopicPerformanceByStudent(studentId)) {
            double correct = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            double total = row[2] == null ? 0.0 : ((Number) row[2]).doubleValue();
            if (total > 0 && (correct * 100.0 / total) < 60) {
                weak.add(row[0].toString());
            }
        }
        if (weak.isEmpty()) {
            weak.add("No major weak area found");
        }
        return weak;
    }

    private double defaultScore(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
