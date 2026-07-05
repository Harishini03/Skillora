package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.ProblemTestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProblemTestCaseRepository extends JpaRepository<ProblemTestCase, Long> {
    List<ProblemTestCase> findByProblem_IdOrderByOrderIndexAsc(Long problemId);
    List<ProblemTestCase> findByProblem_IdAndIsSampleTrue(Long problemId);
}
