package com.placement.placement_intelligence.dto;

import java.util.List;

public class StudentDashboardResponse {
    private Long studentId;
    private String name;
    private String department;
    private Double cgpa;
    private Double readinessScore;
    private Double aptitudeProgress;
    private Double codingProgress;
    private Double softSkillsProgress;
    private List<String> weakAreas;
    private List<String> recommendations;

    public StudentDashboardResponse() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(Double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public Double getAptitudeProgress() {
        return aptitudeProgress;
    }

    public void setAptitudeProgress(Double aptitudeProgress) {
        this.aptitudeProgress = aptitudeProgress;
    }

    public Double getCodingProgress() {
        return codingProgress;
    }

    public void setCodingProgress(Double codingProgress) {
        this.codingProgress = codingProgress;
    }

    public Double getSoftSkillsProgress() {
        return softSkillsProgress;
    }

    public void setSoftSkillsProgress(Double softSkillsProgress) {
        this.softSkillsProgress = softSkillsProgress;
    }

    public List<String> getWeakAreas() {
        return weakAreas;
    }

    public void setWeakAreas(List<String> weakAreas) {
        this.weakAreas = weakAreas;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
