package com.placement.placement_intelligence.repository;

import com.placement.placement_intelligence.model.InterviewSchedule;
import com.placement.placement_intelligence.model.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    Optional<InterviewSchedule> findByJobApplication_Id(Long jobApplicationId);

    List<InterviewSchedule> findByInterviewer_IdAndStatusOrderByScheduledAtAsc(Long interviewerId, InterviewStatus status);

    List<InterviewSchedule> findByJobApplication_JobPosting_Recruiter_IdOrderByScheduledAtDesc(Long recruiterId);
    
    // Missing methods used by InterviewService
    List<InterviewSchedule> findByInterviewer_IdOrderByScheduledAtDesc(Long interviewerId);
    
    List<InterviewSchedule> findByJobApplication_Student_IdOrderByScheduledAtDesc(Long studentId);
    
    List<InterviewSchedule> findByInterviewer_IdAndScheduledAtAfterAndStatusOrderByScheduledAtAsc(
        Long interviewerId, LocalDateTime scheduledAt, InterviewStatus status);
    
    List<InterviewSchedule> findByInterviewer_IdAndScheduledAtBetweenAndStatus(
        Long interviewerId, LocalDateTime start, LocalDateTime end, InterviewStatus status);
    
    List<InterviewSchedule> findByJobApplication_Student_IdAndScheduledAtBetweenAndStatus(
        Long studentId, LocalDateTime start, LocalDateTime end, InterviewStatus status);
}
