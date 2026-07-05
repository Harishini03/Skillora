package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByActiveTrueOrderByCreatedAtDesc();

    List<JobPosting> findByRecruiter_IdOrderByCreatedAtDesc(Long recruiterId);
}
