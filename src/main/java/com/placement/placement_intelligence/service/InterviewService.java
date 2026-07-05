package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.exception.BusinessLogicException;
import com.placement.placement_intelligence.exception.ResourceNotFoundException;
import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interview Scheduling Service implementing Requirement 8: Interview Scheduling System
 * 
 * Features:
 * - Interview scheduling with conflict detection
 * - Meeting link management for online interviews
 * - Interview feedback system
 * - Status management (SCHEDULED, COMPLETED, CANCELLED)
 */
@Service
public class InterviewService {

    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewScheduleRepository interviewScheduleRepository;
    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;

    public InterviewService(InterviewScheduleRepository interviewScheduleRepository,
                           InterviewFeedbackRepository interviewFeedbackRepository,
                           JobApplicationRepository jobApplicationRepository,
                           UserRepository userRepository) {
        this.interviewScheduleRepository = interviewScheduleRepository;
        this.interviewFeedbackRepository = interviewFeedbackRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
    }

    // ============= INTERVIEW SCHEDULING =============

    @Transactional
    public InterviewSchedule scheduleInterview(Long jobApplicationId, Long interviewerId,
                                             LocalDateTime scheduledAt, Integer durationMinutes,
                                             InterviewMode mode, String meetingLink) {
        logger.info("Scheduling interview for application: {} with interviewer: {}", jobApplicationId, interviewerId);

        // Validate inputs
        validateSchedulingInputs(scheduledAt, durationMinutes, mode, meetingLink);

        // Get job application
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));

        // Verify application is shortlisted
        if (jobApplication.getStatus() != ApplicationStatus.SHORTLISTED) {
            throw new BusinessLogicException("Can only schedule interviews for shortlisted candidates");
        }

        // Get interviewer (must be STAFF role as per 3-role system)
        User interviewer = userRepository.findById(interviewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Interviewer not found"));

        if (interviewer.getRole() != Role.STAFF) {
            throw new BusinessLogicException("Only staff members can conduct interviews");
        }

        // Check for existing interview
        if (interviewScheduleRepository.findByJobApplication_Id(jobApplicationId).isPresent()) {
            throw new BusinessLogicException("Interview already scheduled for this application");
        }

        // Check for scheduling conflicts
        if (hasSchedulingConflict(interviewerId, jobApplication.getStudent().getId(), scheduledAt, durationMinutes)) {
            throw new BusinessLogicException("Scheduling conflict detected. Please choose a different time.");
        }

        // Create interview schedule
        InterviewSchedule interview = new InterviewSchedule();
        interview.setJobApplication(jobApplication);
        interview.setInterviewer(interviewer);
        interview.setScheduledAt(scheduledAt);
        interview.setDurationMinutes(durationMinutes);
        interview.setMode(mode);
        interview.setMeetingLink(meetingLink);
        interview.setStatus(InterviewStatus.SCHEDULED);

        InterviewSchedule savedInterview = interviewScheduleRepository.save(interview);

        // Update job application status
        jobApplication.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        jobApplicationRepository.save(jobApplication);

        logger.info("Interview scheduled successfully: {}", savedInterview.getId());
        return savedInterview;
    }

    @Transactional
    public InterviewSchedule updateInterview(Long interviewId, LocalDateTime scheduledAt,
                                           Integer durationMinutes, InterviewMode mode, String meetingLink) {
        logger.info("Updating interview: {}", interviewId);

        InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        // Can only update scheduled interviews
        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new BusinessLogicException("Can only update scheduled interviews");
        }

        // Validate new time if provided
        if (scheduledAt != null) {
            validateFutureDateTime(scheduledAt);
            
            // Check for conflicts with new time
            if (hasSchedulingConflict(interview.getInterviewer().getId(),
                    interview.getJobApplication().getStudent().getId(), scheduledAt, 
                    durationMinutes != null ? durationMinutes : interview.getDurationMinutes())) {
                throw new BusinessLogicException("Scheduling conflict detected with new time");
            }
            
            interview.setScheduledAt(scheduledAt);
        }

        if (durationMinutes != null) {
            validateDuration(durationMinutes);
            interview.setDurationMinutes(durationMinutes);
        }

        if (mode != null) {
            interview.setMode(mode);
            
            // Clear meeting link if switching to onsite
            if (mode == InterviewMode.ONSITE) {
                interview.setMeetingLink(null);
            }
        }

        if (meetingLink != null && interview.getMode() == InterviewMode.ONLINE) {
            interview.setMeetingLink(meetingLink.trim());
        }

        return interviewScheduleRepository.save(interview);
    }

    @Transactional
    public void cancelInterview(Long interviewId, Long requesterId) {
        logger.info("Cancelling interview: {} by user: {}", interviewId, requesterId);

        InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        // Verify permission (interviewer, recruiter, or staff)
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean canCancel = interview.getInterviewer().getId().equals(requesterId) ||
                           interview.getJobApplication().getJobPosting().getRecruiter().getId().equals(requesterId) ||
                           requester.getRole() == Role.STAFF;

        if (!canCancel) {
            throw new BusinessLogicException("Not authorized to cancel this interview");
        }

        // Can only cancel scheduled interviews
        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new BusinessLogicException("Can only cancel scheduled interviews");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interviewScheduleRepository.save(interview);

        // Update job application status back to shortlisted
        JobApplication jobApplication = interview.getJobApplication();
        jobApplication.setStatus(ApplicationStatus.SHORTLISTED);
        jobApplicationRepository.save(jobApplication);

        logger.info("Interview cancelled successfully: {}", interviewId);
    }

    // ============= INTERVIEW FEEDBACK =============

    @Transactional
    public InterviewFeedback provideFeedback(Long interviewId, Integer rating, String comments,
                                           boolean recommended, Long interviewerId) {
        logger.info("Providing feedback for interview: {} by interviewer: {}", interviewId, interviewerId);

        InterviewSchedule interview = interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        // Verify interviewer
        if (!interview.getInterviewer().getId().equals(interviewerId)) {
            throw new BusinessLogicException("Only the assigned interviewer can provide feedback");
        }

        // Validate rating
        if (rating < 1 || rating > 10) {
            throw new BusinessLogicException("Rating must be between 1 and 10");
        }

        // Check if feedback already exists
        Optional<InterviewFeedback> existingFeedback = interviewFeedbackRepository
                .findBySchedule_Id(interviewId);
        
        InterviewFeedback feedback;
        if (existingFeedback.isPresent()) {
            // Update existing feedback
            feedback = existingFeedback.get();
            feedback.setTechnicalScore(rating);
            feedback.setComments(comments != null ? comments.trim() : null);
            feedback.setRecommendation(recommended ? "RECOMMENDED" : "NOT_RECOMMENDED");
            feedback.setSubmittedAt(LocalDateTime.now());
        } else {
            // Create new feedback
            feedback = new InterviewFeedback();
            feedback.setSchedule(interview);
            feedback.setTechnicalScore(rating);
            feedback.setComments(comments != null ? comments.trim() : null);
            feedback.setRecommendation(recommended ? "RECOMMENDED" : "NOT_RECOMMENDED");
        }

        InterviewFeedback savedFeedback = interviewFeedbackRepository.save(feedback);

        // Update interview status to completed
        if (interview.getStatus() == InterviewStatus.SCHEDULED) {
            interview.setStatus(InterviewStatus.COMPLETED);
            interviewScheduleRepository.save(interview);
        }

        logger.info("Interview feedback provided successfully: {}", savedFeedback.getId());
        return savedFeedback;
    }

    // ============= QUERY OPERATIONS =============

    @Transactional(readOnly = true)
    public List<InterviewSchedule> getInterviewsByInterviewer(Long interviewerId) {
        return interviewScheduleRepository.findByInterviewer_IdOrderByScheduledAtDesc(interviewerId);
    }

    @Transactional(readOnly = true)
    public List<InterviewSchedule> getInterviewsByStudent(Long studentId) {
        return interviewScheduleRepository.findByJobApplication_Student_IdOrderByScheduledAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<InterviewSchedule> getUpcomingInterviews(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        
        // Get interviews where user is either interviewer or student
        List<InterviewSchedule> asInterviewer = interviewScheduleRepository
                .findByInterviewer_IdAndScheduledAtAfterAndStatusOrderByScheduledAtAsc(userId, now, InterviewStatus.SCHEDULED);
        
        // This would need a custom query to also get interviews as student
        return asInterviewer;
    }

    @Transactional(readOnly = true)
    public InterviewSchedule getInterview(Long interviewId) {
        return interviewScheduleRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }

    @Transactional(readOnly = true)
    public Optional<InterviewFeedback> getInterviewFeedback(Long interviewId) {
        return interviewFeedbackRepository.findBySchedule_Id(interviewId);
    }

    // ============= CONFLICT DETECTION =============

    /**
     * Check for scheduling conflicts.
     * Implements Requirement 8 correctness property: Conflict Prevention
     */
    public boolean hasSchedulingConflict(Long interviewerId, Long studentId, 
                                        LocalDateTime scheduledAt, Integer durationMinutes) {
        LocalDateTime endTime = scheduledAt.plusMinutes(durationMinutes);
        
        // Check interviewer conflicts
        List<InterviewSchedule> interviewerSchedules = interviewScheduleRepository
                .findByInterviewer_IdAndScheduledAtBetweenAndStatus(
                        interviewerId,
                        scheduledAt.minusMinutes(60), // Buffer
                        endTime.plusMinutes(60),      // Buffer 
                        InterviewStatus.SCHEDULED
                );
        
        for (InterviewSchedule existing : interviewerSchedules) {
            LocalDateTime existingEnd = existing.getScheduledAt().plusMinutes(existing.getDurationMinutes());
            
            // Check for overlap
            if (scheduledAt.isBefore(existingEnd) && endTime.isAfter(existing.getScheduledAt())) {
                logger.debug("Interviewer conflict detected: {} overlaps with existing interview {}", 
                        scheduledAt, existing.getScheduledAt());
                return true;
            }
        }
        
        // Check student conflicts
        List<InterviewSchedule> studentSchedules = interviewScheduleRepository
                .findByJobApplication_Student_IdAndScheduledAtBetweenAndStatus(
                        studentId,
                        scheduledAt.minusMinutes(30), // Smaller buffer for students
                        endTime.plusMinutes(30),
                        InterviewStatus.SCHEDULED
                );
        
        for (InterviewSchedule existing : studentSchedules) {
            LocalDateTime existingEnd = existing.getScheduledAt().plusMinutes(existing.getDurationMinutes());
            
            // Check for overlap
            if (scheduledAt.isBefore(existingEnd) && endTime.isAfter(existing.getScheduledAt())) {
                logger.debug("Student conflict detected: {} overlaps with existing interview {}", 
                        scheduledAt, existing.getScheduledAt());
                return true;
            }
        }
        
        return false;
    }

    // ============= VALIDATION METHODS =============

    private void validateSchedulingInputs(LocalDateTime scheduledAt, Integer durationMinutes,
                                        InterviewMode mode, String meetingLink) {
        validateFutureDateTime(scheduledAt);
        validateDuration(durationMinutes);
        validateMeetingLink(mode, meetingLink);
    }

    private void validateFutureDateTime(LocalDateTime scheduledAt) {
        if (scheduledAt.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BusinessLogicException("Interview must be scheduled at least 1 hour in the future");
        }
    }

    private void validateDuration(Integer durationMinutes) {
        if (durationMinutes < 15 || durationMinutes > 240) {
            throw new BusinessLogicException("Interview duration must be between 15 and 240 minutes");
        }
    }

    private void validateMeetingLink(InterviewMode mode, String meetingLink) {
        if (mode == InterviewMode.ONLINE) {
            if (meetingLink == null || meetingLink.trim().isEmpty()) {
                throw new BusinessLogicException("Meeting link is required for online interviews");
            }
            
            String trimmedLink = meetingLink.trim();
            if (!trimmedLink.startsWith("http://") && !trimmedLink.startsWith("https://")) {
                throw new BusinessLogicException("Meeting link must be a valid URL");
            }
        }
    }
}