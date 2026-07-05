package com.placement.placement_intelligence.dto;

import java.util.List;

public class AnalyticsResponse {
    private List<PlacementCorrelation> testScoreVsPlacement;
    private List<DepartmentAverage> departmentAverageScores;
    private List<DifficultyPerformance> difficultyPerformance;
    private List<TopicPerformance> weakTopics;
    private List<PlacementCorrelation> testEligibilityCorrelation;

    public AnalyticsResponse() {
    }

    public List<PlacementCorrelation> getTestScoreVsPlacement() {
        return testScoreVsPlacement;
    }

    public void setTestScoreVsPlacement(List<PlacementCorrelation> testScoreVsPlacement) {
        this.testScoreVsPlacement = testScoreVsPlacement;
    }

    public List<DepartmentAverage> getDepartmentAverageScores() {
        return departmentAverageScores;
    }

    public void setDepartmentAverageScores(List<DepartmentAverage> departmentAverageScores) {
        this.departmentAverageScores = departmentAverageScores;
    }

    public List<DifficultyPerformance> getDifficultyPerformance() {
        return difficultyPerformance;
    }

    public void setDifficultyPerformance(List<DifficultyPerformance> difficultyPerformance) {
        this.difficultyPerformance = difficultyPerformance;
    }

    public List<TopicPerformance> getWeakTopics() {
        return weakTopics;
    }

    public void setWeakTopics(List<TopicPerformance> weakTopics) {
        this.weakTopics = weakTopics;
    }

    public List<PlacementCorrelation> getTestEligibilityCorrelation() {
        return testEligibilityCorrelation;
    }

    public void setTestEligibilityCorrelation(List<PlacementCorrelation> testEligibilityCorrelation) {
        this.testEligibilityCorrelation = testEligibilityCorrelation;
    }

    public static class PlacementCorrelation {
        private String label;
        private double averageFinalScore;

        public PlacementCorrelation() {
        }

        public PlacementCorrelation(String label, double averageFinalScore) {
            this.label = label;
            this.averageFinalScore = averageFinalScore;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getAverageFinalScore() {
            return averageFinalScore;
        }

        public void setAverageFinalScore(double averageFinalScore) {
            this.averageFinalScore = averageFinalScore;
        }
    }

    public static class DepartmentAverage {
        private String department;
        private double aptitudeAvg;
        private double dsaAvg;
        private double mockAvg;

        public DepartmentAverage() {
        }

        public DepartmentAverage(String department, double aptitudeAvg, double dsaAvg, double mockAvg) {
            this.department = department;
            this.aptitudeAvg = aptitudeAvg;
            this.dsaAvg = dsaAvg;
            this.mockAvg = mockAvg;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public double getAptitudeAvg() {
            return aptitudeAvg;
        }

        public void setAptitudeAvg(double aptitudeAvg) {
            this.aptitudeAvg = aptitudeAvg;
        }

        public double getDsaAvg() {
            return dsaAvg;
        }

        public void setDsaAvg(double dsaAvg) {
            this.dsaAvg = dsaAvg;
        }

        public double getMockAvg() {
            return mockAvg;
        }

        public void setMockAvg(double mockAvg) {
            this.mockAvg = mockAvg;
        }
    }

    public static class DifficultyPerformance {
        private String difficulty;
        private double accuracy;

        public DifficultyPerformance() {
        }

        public DifficultyPerformance(String difficulty, double accuracy) {
            this.difficulty = difficulty;
            this.accuracy = accuracy;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }
    }

    public static class TopicPerformance {
        private String topic;
        private double accuracy;

        public TopicPerformance() {
        }

        public TopicPerformance(String topic, double accuracy) {
            this.topic = topic;
            this.accuracy = accuracy;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }
    }
}
