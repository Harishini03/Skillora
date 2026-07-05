package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    Optional<StaffProfile> findByUser_Id(Long userId);
}
