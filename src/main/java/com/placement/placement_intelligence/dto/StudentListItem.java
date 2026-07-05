package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.PlacementStatus;

public class StudentListItem {
    private Long studentId;
    private String name;
    private String department;
    private Double cgpa;
    private Double aptitudeScore;
    private Double dsaScore;
    private Double mockTestScore;
    private Double finalScore;
    private PlacementStatus placementStatus;
    private boolean placementReady;

    public StudentListItem() {
    }

    public StudentListItem(Long studentId, String name, String department, Double cgpa,
                           Double aptitudeScore, Double dsaScore, Double mockTestScore,
                           Double finalScore, PlacementStatus placementStatus, boolean placementReady) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.cgpa = cgpa;
        this.aptitudeScore = aptitudeScore;
        this.dsaScore = dsaScore;
        this.mockTestScore = mockTestScore;
        this.finalScore = finalScore;
        this.placementStatus = placementStatus;
        this.placementReady = placementReady;
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

    public Double getAptitudeScore() {
        return aptitudeScore;
    }

    public void setAptitudeScore(Double aptitudeScore) {
        this.aptitudeScore = aptitudeScore;
    }

    public Double getDsaScore() {
        return dsaScore;
    }

    public void setDsaScore(Double dsaScore) {
        this.dsaScore = dsaScore;
    }

    public Double getMockTestScore() {
        return mockTestScore;
    }

    public void setMockTestScore(Double mockTestScore) {
        this.mockTestScore = mockTestScore;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public PlacementStatus getPlacementStatus() {
        return placementStatus;
    }

    public void setPlacementStatus(PlacementStatus placementStatus) {
        this.placementStatus = placementStatus;
    }

    public boolean isPlacementReady() {
        return placementReady;
    }

    public void setPlacementReady(boolean placementReady) {
        this.placementReady = placementReady;
    }
}
