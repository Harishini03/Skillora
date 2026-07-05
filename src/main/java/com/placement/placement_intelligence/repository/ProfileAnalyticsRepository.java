package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.ProfileAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileAnalyticsRepository extends JpaRepository<ProfileAnalytics, Long> {
    Optional<ProfileAnalytics> findByProfile_Id(Long profileId);
}
