package com.placement.placement_intelligence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "cgpa", nullable = false)
    private Double cgpa;

    @Column(name = "dsa_score")
    private Double dsaScore;

    @Column(name = "aptitude_score")
    private Double aptitudeScore;

    @Column(name = "mock_test_score")
    private Double mockTestScore;

    @Column(name = "soft_skill_score")
    private Double softSkillScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "readiness_score")
    private Double readinessScore;

    @Column(name = "student_rank")
    private Integer rank;

    @Enumerated(EnumType.STRING)
    @Column(name = "placement_status", length = 30)
    private PlacementStatus placementStatus = PlacementStatus.PENDING;

    @Column(name = "level", length = 30)
    private String level;

    @Column(name = "interests", length = 255)
    private String interests;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "achievements", length = 1200)
    private String achievements;

    @Column(name = "resume_path", length = 500)
    private String resumePath;

    public Student() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Double getDsaScore() {
        return dsaScore;
    }

    public void setDsaScore(Double dsaScore) {
        this.dsaScore = dsaScore;
    }

    public Double getAptitudeScore() {
        return aptitudeScore;
    }

    public void setAptitudeScore(Double aptitudeScore) {
        this.aptitudeScore = aptitudeScore;
    }

    public Double getMockTestScore() {
        return mockTestScore;
    }

    public void setMockTestScore(Double mockTestScore) {
        this.mockTestScore = mockTestScore;
    }

    public Double getSoftSkillScore() {
        return softSkillScore;
    }

    public void setSoftSkillScore(Double softSkillScore) {
        this.softSkillScore = softSkillScore;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public Double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(Double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public PlacementStatus getPlacementStatus() {
        return placementStatus;
    }

    public void setPlacementStatus(PlacementStatus placementStatus) {
        this.placementStatus = placementStatus;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getResumePath() {
        return resumePath;
    }

    public void setResumePath(String resumePath) {
        this.resumePath = resumePath;
    }
}
