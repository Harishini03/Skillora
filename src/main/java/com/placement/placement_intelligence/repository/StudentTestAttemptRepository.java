package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.StudentTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentTestAttemptRepository extends JpaRepository<StudentTestAttempt, Long> {
    List<StudentTestAttempt> findByStudent_IdOrderByTestDateDesc(Long studentId);
}
