package com.placement.placement_intelligence.dto;

import java.util.List;

public class StudentDetailsResponse {
    private Long studentId;
    private String name;
    private String department;
    private Double readiness;
    private Double aptitude;
    private Double coding;
    private Double softSkills;
    private List<String> weakAreas;
    private List<TestHistoryItem> activity;

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

    public Double getReadiness() {
        return readiness;
    }

    public void setReadiness(Double readiness) {
        this.readiness = readiness;
    }

    public Double getAptitude() {
        return aptitude;
    }

    public void setAptitude(Double aptitude) {
        this.aptitude = aptitude;
    }

    public Double getCoding() {
        return coding;
    }

    public void setCoding(Double coding) {
        this.coding = coding;
    }

    public Double getSoftSkills() {
        return softSkills;
    }

    public void setSoftSkills(Double softSkills) {
        this.softSkills = softSkills;
    }

    public List<String> getWeakAreas() {
        return weakAreas;
    }

    public void setWeakAreas(List<String> weakAreas) {
        this.weakAreas = weakAreas;
    }

    public List<TestHistoryItem> getActivity() {
        return activity;
    }

    public void setActivity(List<TestHistoryItem> activity) {
        this.activity = activity;
    }
}
