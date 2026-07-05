package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {
    List<CourseLesson> findByModule_IdOrderByOrderIndexAsc(Long moduleId);
}
