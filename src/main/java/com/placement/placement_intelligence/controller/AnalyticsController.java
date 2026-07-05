package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.AnalyticsResponse;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final StudentRepository studentRepository;

    public AnalyticsController(AnalyticsService analyticsService,
                               StudentRepository studentRepository) {
        this.analyticsService = analyticsService;
        this.studentRepository = studentRepository;
    }

    /**
     * Full analytics overview — accessible to STAFF and RECRUITER roles.
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsResponse> overview() {
        return ResponseEntity.ok(analyticsService.buildAnalytics());
    }

    /**
     * Per-student analytics — accessible to STUDENT (own data) and STAFF/RECRUITER.
     * Returns key scores and placement status from the Student entity.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> studentAnalytics(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        Map<String, Object> result = new HashMap<>();
        result.put("studentId", student.getId());
        result.put("name", student.getName());
        result.put("department", student.getDepartment() != null ? student.getDepartment().getName() : null);
        result.put("aptitudeScore", student.getAptitudeScore() != null ? student.getAptitudeScore() : 0.0);
        result.put("dsaScore", student.getDsaScore() != null ? student.getDsaScore() : 0.0);
        result.put("softSkillScore", student.getSoftSkillScore() != null ? student.getSoftSkillScore() : 0.0);
        result.put("mockTestScore", student.getMockTestScore() != null ? student.getMockTestScore() : 0.0);
        result.put("finalScore", student.getFinalScore() != null ? student.getFinalScore() : 0.0);
        result.put("readinessScore", student.getReadinessScore() != null ? student.getReadinessScore() : 0.0);
        result.put("placementStatus", student.getPlacementStatus() != null ? student.getPlacementStatus().name() : "PENDING");
        result.put("cgpa", student.getCgpa() != null ? student.getCgpa() : 0.0);
        result.put("rank", student.getRank());
        return ResponseEntity.ok(result);
    }

    /**
     * Department-wise average scores — accessible to STAFF and RECRUITER.
     */
    @GetMapping("/department-summary")
    public ResponseEntity<java.util.List<AnalyticsResponse.DepartmentAverage>> departmentSummary() {
        AnalyticsResponse analytics = analyticsService.buildAnalytics();
        return ResponseEntity.ok(analytics.getDepartmentAverageScores());
    }

    /**
     * Topic accuracy list — accessible to STAFF and RECRUITER.
     */
    @GetMapping("/topic-performance")
    public ResponseEntity<java.util.List<AnalyticsResponse.TopicPerformance>> topicPerformance() {
        AnalyticsResponse analytics = analyticsService.buildAnalytics();
        return ResponseEntity.ok(analytics.getWeakTopics());
    }
}
