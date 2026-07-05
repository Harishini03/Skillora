package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourse_IdAndStudent_Id(Long courseId, Long studentId);
    List<CourseEnrollment> findByStudent_Id(Long studentId);
    List<CourseEnrollment> findByCourse_Id(Long courseId);
    boolean existsByCourse_IdAndStudent_Id(Long courseId, Long studentId);
}
