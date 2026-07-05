package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.AnalyticsResponse;
import com.placement.placement_intelligence.repository.DepartmentAnalyticsRepository;
import com.placement.placement_intelligence.repository.StudentAnswerRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final DepartmentAnalyticsRepository departmentAnalyticsRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public AnalyticsService(StudentRepository studentRepository,
                            DepartmentAnalyticsRepository departmentAnalyticsRepository,
                            StudentAnswerRepository studentAnswerRepository) {
        this.studentRepository = studentRepository;
        this.departmentAnalyticsRepository = departmentAnalyticsRepository;
        this.studentAnswerRepository = studentAnswerRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse buildAnalytics() {
        AnalyticsResponse response = new AnalyticsResponse();

        response.setTestScoreVsPlacement(mapPlacementCorrelations(studentRepository.averageFinalScoreByPlacementStatus()));
        response.setDepartmentAverageScores(mapDepartmentAverages(departmentAnalyticsRepository.averageScoresByDepartment()));
        response.setDifficultyPerformance(mapDifficulty(studentAnswerRepository.aggregateDifficultyPerformance()));
        response.setWeakTopics(mapTopics(studentAnswerRepository.aggregateTopicPerformance()));
        response.setTestEligibilityCorrelation(mapPlacementCorrelations(studentRepository.averageFinalScoreByPlacementStatus()));

        return response;
    }

    private List<AnalyticsResponse.PlacementCorrelation> mapPlacementCorrelations(List<Object[]> rows) {
        List<AnalyticsResponse.PlacementCorrelation> results = new ArrayList<>();
        for (Object[] row : rows) {
            String label = row[0] == null ? "UNKNOWN" : row[0].toString();
            double avg = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            results.add(new AnalyticsResponse.PlacementCorrelation(label, avg));
        }
        return results;
    }

    private List<AnalyticsResponse.DepartmentAverage> mapDepartmentAverages(List<Object[]> rows) {
        List<AnalyticsResponse.DepartmentAverage> results = new ArrayList<>();
        for (Object[] row : rows) {
            String department = row[0].toString();
            double aptitude = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            double dsa = row[2] == null ? 0.0 : ((Number) row[2]).doubleValue();
            double mock = row[3] == null ? 0.0 : ((Number) row[3]).doubleValue();
            results.add(new AnalyticsResponse.DepartmentAverage(department, aptitude, dsa, mock));
        }
        return results;
    }

    private List<AnalyticsResponse.DifficultyPerformance> mapDifficulty(List<Object[]> rows) {
        List<AnalyticsResponse.DifficultyPerformance> results = new ArrayList<>();
        for (Object[] row : rows) {
            String difficulty = row[0].toString();
            double correct = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            double total = row[2] == null ? 0.0 : ((Number) row[2]).doubleValue();
            double accuracy = total == 0.0 ? 0.0 : (correct / total) * 100.0;
            results.add(new AnalyticsResponse.DifficultyPerformance(difficulty, accuracy));
        }
        return results;
    }

    private List<AnalyticsResponse.TopicPerformance> mapTopics(List<Object[]> rows) {
        List<AnalyticsResponse.TopicPerformance> results = new ArrayList<>();
        for (Object[] row : rows) {
            String topic = row[0].toString();
            double correct = row[1] == null ? 0.0 : ((Number) row[1]).doubleValue();
            double total = row[2] == null ? 0.0 : ((Number) row[2]).doubleValue();
            double accuracy = total == 0.0 ? 0.0 : (correct / total) * 100.0;
            results.add(new AnalyticsResponse.TopicPerformance(topic, accuracy));
        }
        return results;
    }
}
