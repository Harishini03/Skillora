package com.placement.placement_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "eligibility_criteria")
public class EligibilityCriteria {

    @Id
    @Column(name = "company_id")
    private Long companyId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "min_cgpa", nullable = false)
    private Double minCgpa;

    @Column(name = "min_dsa", nullable = false)
    private Double minDsa;

    @Column(name = "min_aptitude", nullable = false)
    private Double minAptitude;

    @Column(name = "weight_cgpa", nullable = false)
    private Double weightCgpa;

    @Column(name = "weight_dsa", nullable = false)
    private Double weightDsa;

    @Column(name = "weight_aptitude", nullable = false)
    private Double weightAptitude;

    @Column(name = "weight_mock", nullable = false)
    private Double weightMock;

    @Column(name = "weight_skill", nullable = false)
    private Double weightSkill;

    public EligibilityCriteria() {
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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
