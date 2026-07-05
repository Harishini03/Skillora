package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.TestType;

import java.time.LocalDateTime;

public class TestHistoryItem {
    private Long attemptId;
    private TestType testType;
    private Integer score;
    private Integer totalQuestions;
    private LocalDateTime testDate;

    public TestHistoryItem() {
    }

    public TestHistoryItem(Long attemptId, TestType testType, Integer score, Integer totalQuestions, LocalDateTime testDate) {
        this.attemptId = attemptId;
        this.testType = testType;
        this.score = score;
        this.totalQuestions = totalQuestions;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public LocalDateTime getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDateTime testDate) {
        this.testDate = testDate;
    }
}
