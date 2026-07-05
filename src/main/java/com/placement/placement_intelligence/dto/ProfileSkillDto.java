package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.ProfileSkillCategory;
import com.placement.placement_intelligence.model.ProfileSkillLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProfileSkillDto {

    @NotBlank(message = "Skill name is required")
    @Size(max = 120)
    private String skillName;

    @NotNull(message = "Skill category is required")
    private ProfileSkillCategory skillCategory;

    @NotNull(message = "Skill level is required")
    private ProfileSkillLevel skillLevel;

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
