package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.StudentDashboardResponse;
import com.placement.placement_intelligence.model.CourseEnrollment;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.StudentSkill;
import com.placement.placement_intelligence.model.StudentTestAttempt;
import com.placement.placement_intelligence.repository.CourseEnrollmentRepository;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.StudentSkillRepository;
import com.placement.placement_intelligence.repository.StudentTestAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for building comprehensive student dashboard with readiness analytics.
 * Implements Requirement 11: Performance Analytics Engine
 */
@Service
public class StudentDashboardService {

    private final StudentRepository studentRepository;
    private final StudentTestAttemptRepository attemptRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final CourseEnrollmentRepository courseEnrollmentRepository;

    public StudentDashboardService(StudentRepository studentRepository,
                                   StudentTestAttemptRepository attemptRepository,
                                   StudentSkillRepository studentSkillRepository,
                                   StudentAnswerRepository studentAnswerRepository,
                                   CourseEnrollmentRepository courseEnrollmentRepository) {
        this.studentRepository = studentRepository;
        this.attemptRepository = attemptRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.courseEnrollmentRepository = courseEnrollmentRepository;
    }

    /**
     * Build comprehensive dashboard for a student.
     * Calculates readiness score based on:
     * - Profile completion (20%)
     * - Test performance (30%)
     * - Coding success rate (30%)
     * - Course progress (20%)
     */
    @Transactional(readOnly = true)
    public StudentDashboardResponse buildDashboard(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        StudentDashboardResponse response = new StudentDashboardResponse();
        response.setStudentId(student.getId());
        response.setName(student.getName());
        response.setDepartment(student.getDepartment().getName());
        response.setCgpa(student.getCgpa());

        // Calculate comprehensive readiness score
        double readinessScore = calculateComprehensiveReadinessScore(student);
        response.setReadinessScore(round(readinessScore));

        // Set progress scores
        response.setAptitudeProgress(round(defaultScore(student.getAptitudeScore())));
        response.setCodingProgress(round(defaultScore(student.getDsaScore())));
        response.setSoftSkillsProgress(round(defaultScore(student.getSoftSkillScore())));

        // Identify weak areas from test history
        response.setWeakAreas(identifyWeakAreas(studentId));

        // Generate personalized recommendations
        response.setRecommendations(generateRecommendations(student, studentId));

        return response;
    }

    /**
     * Calculate comprehensive readiness score based on multiple factors.
     * Formula:
     * - Profile Completion: 20%
     * - Test Performance: 30%
     * - Coding Success Rate: 30%
     * - Course Progress: 20%
     */
    private double calculateComprehensiveReadinessScore(Student student) {
        double profileScore = calculateProfileCompletionScore(student);
        double testScore = calculateTestPerformanceScore(student);
        double codingScore = calculateCodingSuccessRate(student);
        double courseScore = calculateCourseProgressScore(student.getId());

        double readiness = (profileScore * 0.20) + (testScore * 0.30) + (codingScore * 0.30) + (courseScore * 0.20);
        return Math.max(0, Math.min(100, readiness)); // Ensure score is between 0-100
    }

    /**
     * Calculate profile completion percentage (0-100).
     * Checks: name, email, cgpa, level, interests, phone, achievements, resume, skills
     */
    private double calculateProfileCompletionScore(Student student) {
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
        
        List<StudentSkill> skills = studentSkillRepository.findByStudent_Id(student.getId());
        if (!skills.isEmpty()) completed++;

        return (completed * 100.0) / total;
    }

    /**
     * Calculate test performance score (0-100).
     * Average of aptitude, soft skills scores.
     */
    private double calculateTestPerformanceScore(Student student) {
        double aptitude = defaultScore(student.getAptitudeScore());
        double softSkills = defaultScore(student.getSoftSkillScore());
        
        // If no tests taken, return 0
        if (aptitude == 0 && softSkills == 0) {
            return 0;
        }
        
        // Average of available test scores
        int count = 0;
        double sum = 0;
        if (aptitude > 0) { sum += aptitude; count++; }
        if (softSkills > 0) { sum += softSkills; count++; }
        
        return count > 0 ? sum / count : 0;
    }

    /**
     * Calculate coding success rate (0-100).
     * Based on DSA score and recent coding test attempts.
     */
    private double calculateCodingSuccessRate(Student student) {
        double dsaScore = defaultScore(student.getDsaScore());
        
        // If no coding tests taken, return 0
        if (dsaScore == 0) {
            return 0;
        }
        
        return dsaScore;
    }

    /**
     * Calculate average course progress (0-100).
     * Based on completion percentage of enrolled courses.
     */
    private double calculateCourseProgressScore(Long studentId) {
        List<CourseEnrollment> enrollments = courseEnrollmentRepository.findByStudent_Id(studentId);
        
        if (enrollments.isEmpty()) {
            return 0;
        }
        
        double totalProgress = enrollments.stream()
                .map(CourseEnrollment::getCompletionPercentage)
                .map(BigDecimal::doubleValue)
                .reduce(0.0, Double::sum);
        
        return totalProgress / enrollments.size();
    }

    /**
     * Identify weak areas from test history.
     * Analyzes topic-level performance and flags areas with <60% accuracy.
     */
    private List<String> identifyWeakAreas(Long studentId) {
        List<String> weakAreas = new ArrayList<>();
        
        // Get topic-level performance from student answers
        List<Object[]> topicPerformance = studentAnswerRepository.aggregateTopicPerformanceByStudent(studentId);
        
        Map<String, TopicStats> topicStatsMap = new HashMap<>();
        
        for (Object[] row : topicPerformance) {
            String topic = row[0] != null ? row[0].toString() : "General";
            double correct = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
            double total = row[2] != null ? ((Number) row[2]).doubleValue() : 0;
            
            topicStatsMap.put(topic, new TopicStats(correct, total));
        }
        
        // Identify topics with <60% accuracy
        for (Map.Entry<String, TopicStats> entry : topicStatsMap.entrySet()) {
            TopicStats stats = entry.getValue();
            double accuracy = stats.total > 0 ? (stats.correct * 100.0 / stats.total) : 0;
            
            if (accuracy < 60 && stats.total >= 3) { // At least 3 questions attempted
                weakAreas.add(String.format("%s (%.0f%% accuracy)", entry.getKey(), accuracy));
            }
        }
        
        // If no specific weak areas, provide general guidance
        if (weakAreas.isEmpty()) {
            Student student = studentRepository.findById(studentId).orElse(null);
            if (student != null) {
                if (defaultScore(student.getAptitudeScore()) < 70) {
                    weakAreas.add("Aptitude - needs improvement");
                }
                if (defaultScore(student.getDsaScore()) < 70) {
                    weakAreas.add("Coding - practice more problems");
                }
                if (defaultScore(student.getSoftSkillScore()) < 70) {
                    weakAreas.add("Soft Skills - focus on communication");
                }
            }
            
            if (weakAreas.isEmpty()) {
                weakAreas.add("Keep practicing to maintain consistency");
            }
        }
        
        return weakAreas;
    }

    /**
     * Generate personalized recommendations based on analytics.
     * Provides actionable suggestions based on weak areas and performance.
     */
    private List<String> generateRecommendations(Student student, Long studentId) {
        List<String> recommendations = new ArrayList<>();
        
        double dsaScore = defaultScore(student.getDsaScore());
        double aptitudeScore = defaultScore(student.getAptitudeScore());
        double softSkillScore = defaultScore(student.getSoftSkillScore());
        double profileCompletion = calculateProfileCompletionScore(student);
        double courseProgress = calculateCourseProgressScore(studentId);
        
        // Profile completion recommendations
        if (profileCompletion < 80) {
            recommendations.add("Complete your profile to improve visibility to recruiters");
        }
        
        // Course progress recommendations
        if (courseProgress < 50) {
            recommendations.add("Focus on completing enrolled courses to build strong fundamentals");
        }
        
        // Coding recommendations
        if (dsaScore < 50) {
            recommendations.add("Start with basic data structures: Arrays, Strings, and Linked Lists");
            recommendations.add("Practice 2-3 easy coding problems daily on the platform");
        } else if (dsaScore < 70) {
            recommendations.add("Move to intermediate topics: Trees, Graphs, and Dynamic Programming");
            recommendations.add("Solve at least one medium-level problem daily");
        } else if (dsaScore < 85) {
            recommendations.add("Master advanced algorithms and optimize your solutions");
        }
        
        // Aptitude recommendations
        if (aptitudeScore < 60) {
            recommendations.add("Focus on Logical Reasoning and Quantitative Aptitude basics");
            recommendations.add("Practice aptitude tests regularly to improve speed and accuracy");
        } else if (aptitudeScore < 75) {
            recommendations.add("Work on time management in aptitude tests");
        }
        
        // Soft skills recommendations
        if (softSkillScore < 60) {
            recommendations.add("Improve communication skills through mock interviews");
            recommendations.add("Take HR scenario modules to prepare for behavioral questions");
        } else if (softSkillScore < 75) {
            recommendations.add("Practice situational judgment questions for better responses");
        }
        
        // Overall readiness recommendations
        double overallReadiness = calculateComprehensiveReadinessScore(student);
        if (overallReadiness >= 75) {
            recommendations.add("Great progress! Take mock tests to simulate real interview conditions");
            recommendations.add("Focus on maintaining consistency across all skill areas");
        } else if (overallReadiness >= 50) {
            recommendations.add("You're on track! Focus on your weak areas for faster improvement");
        } else {
            recommendations.add("Build a daily practice routine: 1 hour coding + 30 min aptitude");
        }
        
        // Ensure at least one recommendation
        if (recommendations.isEmpty()) {
            recommendations.add("Keep up the excellent work! Continue with regular practice and mock tests");
        }
        
        return recommendations;
    }

    private double defaultScore(Double score) {
        return score == null ? 0.0 : score;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Helper class to track topic statistics
     */
    private static class TopicStats {
        final double correct;
        final double total;

        TopicStats(double correct, double total) {
            this.correct = correct;
            this.total = total;
        }
    }
}
