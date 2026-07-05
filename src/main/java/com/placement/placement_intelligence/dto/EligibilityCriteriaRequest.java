package com.placement.placement_intelligence.dto;

public class EligibilityCriteriaRequest {
    private Long companyId;
    private Double minCgpa;
    private Double minDsa;
    private Double minAptitude;
    private Double weightCgpa;
    private Double weightDsa;
    private Double weightAptitude;
    private Double weightMock;
    private Double weightSkill;

    public EligibilityCriteriaRequest() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(Double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public Double getMinDsa() {
        return minDsa;
    }

    public void setMinDsa(Double minDsa) {
        this.minDsa = minDsa;
    }

    public Double getMinAptitude() {
        return minAptitude;
    }

    public void setMinAptitude(Double minAptitude) {
        this.minAptitude = minAptitude;
    }

    public Double getWeightCgpa() {
        return weightCgpa;
    }

    public void setWeightCgpa(Double weightCgpa) {
        this.weightCgpa = weightCgpa;
    }

    public Double getWeightDsa() {
        return weightDsa;
    }

    public void setWeightDsa(Double weightDsa) {
        this.weightDsa = weightDsa;
    }

    public Double getWeightAptitude() {
        return weightAptitude;
    }

    public void setWeightAptitude(Double weightAptitude) {
        this.weightAptitude = weightAptitude;
    }

    public Double getWeightMock() {
        return weightMock;
    }

    public void setWeightMock(Double weightMock) {
        this.weightMock = weightMock;
    }

    public Double getWeightSkill() {
        return weightSkill;
    }

    public void setWeightSkill(Double weightSkill) {
        this.weightSkill = weightSkill;
    }
}
