package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodingProblemRepository extends JpaRepository<CodingProblem, Long> {
    List<CodingProblem> findByDifficultyLevel(String difficultyLevel);
    List<CodingProblem> findByTopicTagsContaining(String tag);
}
