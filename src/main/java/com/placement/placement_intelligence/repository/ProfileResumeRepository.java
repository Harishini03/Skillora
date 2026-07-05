package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.ProfileResume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileResumeRepository extends JpaRepository<ProfileResume, Long> {
    Optional<ProfileResume> findByProfile_Id(Long profileId);
}
