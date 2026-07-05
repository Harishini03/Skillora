package com.placement.placement_intelligence.dto;

import java.util.List;

public class TestQuestionResponse {
    private String testType;
    private int totalQuestions;
    private int durationMinutes;
    private Long sessionId;
    private List<QuestionPayload> questions;

    public TestQuestionResponse() {
    }

    public TestQuestionResponse(String testType, int totalQuestions, int durationMinutes, Long sessionId, List<QuestionPayload> questions) {
        this.testType = testType;
        this.totalQuestions = totalQuestions;
        this.durationMinutes = durationMinutes;
        this.sessionId = sessionId;
        this.questions = questions;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public List<QuestionPayload> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionPayload> questions) {
        this.questions = questions;
    }

    public static class QuestionPayload {
        private Long id;
        private String questionText;
        private String optionA;
        private String optionB;
        private String optionC;
        private String optionD;
        private String difficultyLevel;
        private String topic;

        public QuestionPayload() {
        }

        public QuestionPayload(Long id, String questionText, String optionA, String optionB,
                               String optionC, String optionD, String difficultyLevel, String topic) {
            this.id = id;
            this.questionText = questionText;
            this.optionA = optionA;
            this.optionB = optionB;
            this.optionC = optionC;
            this.optionD = optionD;
            this.difficultyLevel = difficultyLevel;
            this.topic = topic;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getOptionA() {
            return optionA;
        }

        public void setOptionA(String optionA) {
            this.optionA = optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public void setOptionB(String optionB) {
            this.optionB = optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public void setOptionC(String optionC) {
            this.optionC = optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public void setOptionD(String optionD) {
            this.optionD = optionD;
        }

        public String getDifficultyLevel() {
            return difficultyLevel;
        }

        public void setDifficultyLevel(String difficultyLevel) {
            this.difficultyLevel = difficultyLevel;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
}
