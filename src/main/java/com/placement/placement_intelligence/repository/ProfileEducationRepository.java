package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.ProfileEducation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileEducationRepository extends JpaRepository<ProfileEducation, Long> {
    List<ProfileEducation> findByProfile_IdOrderByYearDesc(Long profileId);

    Optional<ProfileEducation> findByIdAndProfile_Id(Long id, Long profileId);
}
