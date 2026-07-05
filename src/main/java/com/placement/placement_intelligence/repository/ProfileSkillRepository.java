package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.ProfileSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileSkillRepository extends JpaRepository<ProfileSkill, Long> {
    List<ProfileSkill> findByProfile_IdOrderBySkillNameAsc(Long profileId);

    void deleteByProfile_Id(Long profileId);
}
