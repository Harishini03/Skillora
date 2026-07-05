package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.InterviewFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {
    Optional<InterviewFeedback> findBySchedule_Id(Long scheduleId);
}
