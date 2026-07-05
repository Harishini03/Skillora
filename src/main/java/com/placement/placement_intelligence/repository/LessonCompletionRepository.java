package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.LessonCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {
    Optional<LessonCompletion> findByEnrollment_IdAndLesson_Id(Long enrollmentId, Long lessonId);
    List<LessonCompletion> findByEnrollment_Id(Long enrollmentId);
    boolean existsByEnrollment_IdAndLesson_Id(Long enrollmentId, Long lessonId);
}
