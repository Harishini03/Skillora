package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.TestType;

import java.time.LocalDateTime;
import java.util.List;

public class TestSubmissionResponse {
    private Long attemptId;
    private TestType testType;
    private int score;
    private int totalQuestions;
    private double accuracyPercentage;
    private long timeTakenSeconds;
    private List<String> weakTopics;
    private LocalDateTime testDate;
    private List<QuestionReview> reviewItems;

    public TestSubmissionResponse() {
    }

    public TestSubmissionResponse(Long attemptId, TestType testType, int score, int totalQuestions,
                                  double accuracyPercentage, long timeTakenSeconds,
                                  List<String> weakTopics, LocalDateTime testDate) {
        this.attemptId = attemptId;
        this.testType = testType;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.accuracyPercentage = accuracyPercentage;
        this.timeTakenSeconds = timeTakenSeconds;
        this.weakTopics = weakTopics;
        this.testDate = testDate;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public TestType getTestType() {
        return testType;
    }

    public void setTestType(TestType testType) {
        this.testType = testType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public long getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(long timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public List<String> getWeakTopics() {
        return weakTopics;
    }

    public void setWeakTopics(List<String> weakTopics) {
        this.weakTopics = weakTopics;
    }

    public LocalDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }

    public List<QuestionReview> getReviewItems() {
        return reviewItems;
    }

    public void setReviewItems(List<QuestionReview> reviewItems) {
        this.reviewItems = reviewItems;
    }

    public static class QuestionReview {
        private Long questionId;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String correctOption;
        private String selectedOption;
        private String topic;
        private String difficultyLevel;

        public QuestionReview() {
        }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public String getOptionA() { return optionA; }
        public void setOptionA(String optionA) { this.optionA = optionA; }
        public String getOptionB() { return optionB; }
        public void setOptionB(String optionB) { this.optionB = optionB; }
        public String getOptionC() { return optionC; }
        public void setOptionC(String optionC) { this.optionC = optionC; }
        public String getOptionD() { return optionD; }
        public void setOptionD(String optionD) { this.optionD = optionD; }
        public String getCorrectOption() { return correctOption; }
        public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
        public String getSelectedOption() { return selectedOption; }
        public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    }
}
