package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByIsPublishedTrue();
    List<Course> findByCreatedBy_Id(Long userId);
    List<Course> findByCategory(String category);
    List<Course> findByDifficultyLevel(String difficultyLevel);
}
