package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.CodeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findByStudent_IdOrderBySubmittedAtDesc(Long studentId);
    List<CodeSubmission> findByProblem_IdAndStudent_IdOrderBySubmittedAtDesc(Long problemId, Long studentId);
    List<CodeSubmission> findByStudent_IdAndStatus(Long studentId, String status);
}
