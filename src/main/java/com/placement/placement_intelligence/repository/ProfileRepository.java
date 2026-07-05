package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByStudent_Id(Long studentId);
}
