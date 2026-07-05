package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.ApplicationStatus;
import com.placement.placement_intelligence.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Optional<JobApplication> findByJobPosting_IdAndStudent_Id(Long jobPostingId, Long studentId);

    List<JobApplication> findByStudent_IdOrderByAppliedAtDesc(Long studentId);

    List<JobApplication> findByJobPosting_IdOrderByAppliedAtDesc(Long jobPostingId);

    List<JobApplication> findByJobPosting_IdAndStatusOrderByAppliedAtDesc(Long jobPostingId, ApplicationStatus status);

    long countByJobPosting_Recruiter_IdAndStatusIn(Long recruiterId, List<ApplicationStatus> statuses);

    long countByJobPosting_Recruiter_IdAndStatus(Long recruiterId, ApplicationStatus status);
}
