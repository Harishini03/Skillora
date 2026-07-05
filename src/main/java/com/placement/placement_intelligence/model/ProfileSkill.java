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
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_skills")
public class ProfileSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_skill_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "skill_name", nullable = false, length = 120)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_category", nullable = false, length = 30)
    private ProfileSkillCategory skillCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 20)
    private ProfileSkillLevel skillLevel;

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

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public ProfileSkillCategory getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(ProfileSkillCategory skillCategory) {
        this.skillCategory = skillCategory;
    }

    public ProfileSkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(ProfileSkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }
}
