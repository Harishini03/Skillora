package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.SubmissionTestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionTestResultRepository extends JpaRepository<SubmissionTestResult, Long> {
    List<SubmissionTestResult> findBySubmission_Id(Long submissionId);
}
