package com.placement.placement_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_educations")
public class ProfileEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "education_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "institution_name", nullable = false, length = 180)
    private String institutionName;

    @Column(name = "degree", nullable = false, length = 180)
    private String degree;

    @Column(name = "year_of_passing", nullable = false)
    private Integer year;

    @Column(name = "cgpa_or_percentage", nullable = false, length = 40)
    private String cgpaOrPercentage;

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

    public String getInstitutionName() {
        return institutionName;
    }

    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getCgpaOrPercentage() {
        return cgpaOrPercentage;
    }

    public void setCgpaOrPercentage(String cgpaOrPercentage) {
        this.cgpaOrPercentage = cgpaOrPercentage;
    }
}
