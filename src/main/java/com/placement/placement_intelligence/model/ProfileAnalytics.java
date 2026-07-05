package com.placement.placement_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_analytics")
public class ProfileAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_analytics_id")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(name = "test_scores", nullable = false)
    private Double testScores = 0.0;

    @Column(name = "avg_time_per_question", nullable = false)
    private Double averageTimePerQuestion = 0.0;

    @Column(name = "accuracy_percentage", nullable = false)
    private Double accuracyPercentage = 0.0;

    @Column(name = "strengths", length = 1000)
    private String strengths;

    @Column(name = "weaknesses", length = 1000)
    private String weaknesses;

    @Column(name = "insight_summary", length = 1200)
    private String insightSummary;

    @Column(name = "recommended_learning_strategy", length = 1200)
    private String recommendedLearningStrategy;

    @Column(name = "weak_areas", length = 1200)
    private String weakAreas;

    @Column(name = "suggested_topics", length = 1200)
    private String suggestedTopics;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Double getTestScores() {
        return testScores;
    }

    public void setTestScores(Double testScores) {
        this.testScores = testScores;
    }

    public Double getAverageTimePerQuestion() {
        return averageTimePerQuestion;
    }

    public void setAverageTimePerQuestion(Double averageTimePerQuestion) {
        this.averageTimePerQuestion = averageTimePerQuestion;
    }

    public Double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(Double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getInsightSummary() {
        return insightSummary;
    }

    public void setInsightSummary(String insightSummary) {
        this.insightSummary = insightSummary;
    }

    public String getRecommendedLearningStrategy() {
        return recommendedLearningStrategy;
    }

    public void setRecommendedLearningStrategy(String recommendedLearningStrategy) {
        this.recommendedLearningStrategy = recommendedLearningStrategy;
    }

    public String getWeakAreas() {
        return weakAreas;
    }

    public void setWeakAreas(String weakAreas) {
        this.weakAreas = weakAreas;
    }

    public String getSuggestedTopics() {
        return suggestedTopics;
    }

    public void setSuggestedTopics(String suggestedTopics) {
        this.suggestedTopics = suggestedTopics;
    }
}
