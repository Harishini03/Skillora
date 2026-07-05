package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.service.CurrentUserService;
import com.placement.placement_intelligence.service.InterviewService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interview Management Controller implementing Requirement 8: Interview Scheduling System
 * 
 * Endpoints:
 * - POST /api/interviews - Schedule interview (STAFF only)
 * - PUT /api/interviews/{id} - Update interview (STAFF only)  
 * - DELETE /api/interviews/{id} - Cancel interview (STAFF/RECRUITER)
 * - POST /api/interviews/{id}/feedback - Provide feedback (STAFF only)
 * - GET /api/interviews/my - Get my interviews (STAFF/STUDENT)
 * - GET /api/interviews/upcoming - Get upcoming interviews
 * - GET /api/interviews/{id} - Get interview details
 * - GET /api/interviews/{id}/feedback - Get interview feedback
 */
@RestController
@RequestMapping("/api/interviews")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174"})
public class InterviewController {

    private final InterviewService interviewService;
    private final CurrentUserService currentUserService;

    public InterviewController(InterviewService interviewService, CurrentUserService currentUserService) {
        this.interviewService = interviewService;
        this.currentUserService = currentUserService;
    }

    // ============= INTERVIEW SCHEDULING ENDPOINTS =============

    @PostMapping
    public ResponseEntity<InterviewSchedule> scheduleInterview(@RequestBody ScheduleInterviewRequest request) {
        Long interviewerId = currentUserService.getCurrentUserId();
        
        InterviewSchedule interview = interviewService.scheduleInterview(
                request.getJobApplicationId(),
                interviewerId,
                request.getScheduledAt(),
                request.getDurationMinutes(),
                request.getMode(),
                request.getMeetingLink()
        );
        
        return ResponseEntity.ok(interview);
    }

    @PutMapping("/{interviewId}")
    public ResponseEntity<InterviewSchedule> updateInterview(@PathVariable Long interviewId,
                                                           @RequestBody UpdateInterviewRequest request) {
        InterviewSchedule interview = interviewService.updateInterview(
                interviewId,
                request.getScheduledAt(),
                request.getDurationMinutes(),
                request.getMode(),
                request.getMeetingLink()
        );
        
        return ResponseEntity.ok(interview);
    }

    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Map<String, String>> cancelInterview(@PathVariable Long interviewId) {
        Long requesterId = currentUserService.getCurrentUserId();
        interviewService.cancelInterview(interviewId, requesterId);
        
        return ResponseEntity.ok(Map.of("message", "Interview cancelled successfully"));
    }

    // ============= FEEDBACK ENDPOINTS =============

    @PostMapping("/{interviewId}/feedback")
    public ResponseEntity<InterviewFeedback> provideFeedback(@PathVariable Long interviewId,
                                                           @RequestBody ProvideFeedbackRequest request) {
        Long interviewerId = currentUserService.getCurrentUserId();
        
        InterviewFeedback feedback = interviewService.provideFeedback(
                interviewId,
                request.getRating(),
                request.getComments(),
                request.isRecommended(),
                interviewerId
        );
        
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/{interviewId}/feedback")
    public ResponseEntity<InterviewFeedback> getInterviewFeedback(@PathVariable Long interviewId) {
        Optional<InterviewFeedback> feedback = interviewService.getInterviewFeedback(interviewId);
        
        if (feedback.isPresent()) {
            return ResponseEntity.ok(feedback.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ============= QUERY ENDPOINTS =============

    @GetMapping("/my")
    public ResponseEntity<List<InterviewSchedule>> getMyInterviews() {
        Long userId = currentUserService.getCurrentUserId();
        
        // For STAFF - get interviews they're conducting
        // For STUDENT - get interviews they're attending
        List<InterviewSchedule> interviews;
        
        try {
            // Try as interviewer first (STAFF)
            interviews = interviewService.getInterviewsByInterviewer(userId);
        } catch (Exception e) {
            // If that fails, try as student
            Long studentId = currentUserService.getCurrentStudentId();
            interviews = interviewService.getInterviewsByStudent(studentId);
        }
        
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<InterviewSchedule>> getUpcomingInterviews() {
        Long userId = currentUserService.getCurrentUserId();
        List<InterviewSchedule> interviews = interviewService.getUpcomingInterviews(userId);
        
        return ResponseEntity.ok(interviews);
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewSchedule> getInterview(@PathVariable Long interviewId) {
        InterviewSchedule interview = interviewService.getInterview(interviewId);
        return ResponseEntity.ok(interview);
    }

    // ============= UTILITY ENDPOINTS =============

    @GetMapping("/check-conflicts")
    public ResponseEntity<Map<String, Object>> checkSchedulingConflicts(
            @RequestParam Long interviewerId,
            @RequestParam Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledAt,
            @RequestParam Integer durationMinutes) {
        
        boolean hasConflict = interviewService.hasSchedulingConflict(interviewerId, studentId, scheduledAt, durationMinutes);
        
        Map<String, Object> response = Map.of(
                "hasConflict", hasConflict,
                "scheduledAt", scheduledAt,
                "durationMinutes", durationMinutes,
                "message", hasConflict ? "Scheduling conflict detected" : "Time slot is available"
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/modes")
    public ResponseEntity<Map<String, Object>> getInterviewModes() {
        Map<String, Object> modes = Map.of(
                "modes", List.of(
                        Map.of("value", "ONLINE", "label", "Online Interview", "description", "Conducted via video call"),
                        Map.of("value", "OFFLINE", "label", "In-Person Interview", "description", "Conducted at office location")
                ),
                "defaultMode", "ONLINE"
        );
        
        return ResponseEntity.ok(modes);
    }

    @GetMapping("/statuses")
    public ResponseEntity<Map<String, Object>> getInterviewStatuses() {
        Map<String, Object> statuses = Map.of(
                "statuses", List.of(
                        Map.of("value", "SCHEDULED", "label", "Scheduled", "description", "Interview is scheduled"),
                        Map.of("value", "COMPLETED", "label", "Completed", "description", "Interview has been completed"),
                        Map.of("value", "CANCELLED", "label", "Cancelled", "description", "Interview has been cancelled")
                )
        );
        
        return ResponseEntity.ok(statuses);
    }

    // ============= REQUEST DTOs =============

    public static class ScheduleInterviewRequest {
        private Long jobApplicationId;
        private LocalDateTime scheduledAt;
        private Integer durationMinutes = 45;
        private InterviewMode mode = InterviewMode.ONLINE;
        private String meetingLink;

        // Getters and setters
        public Long getJobApplicationId() { return jobApplicationId; }
        public void setJobApplicationId(Long jobApplicationId) { this.jobApplicationId = jobApplicationId; }

        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public InterviewMode getMode() { return mode; }
        public void setMode(InterviewMode mode) { this.mode = mode; }

        public String getMeetingLink() { return meetingLink; }
        public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    }

    public static class UpdateInterviewRequest {
        private LocalDateTime scheduledAt;
        private Integer durationMinutes;
        private InterviewMode mode;
        private String meetingLink;

        // Getters and setters
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

        public Integer getDurationMinutes() { return durationMinutes; }
        public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

        public InterviewMode getMode() { return mode; }
        public void setMode(InterviewMode mode) { this.mode = mode; }

        public String getMeetingLink() { return meetingLink; }
        public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    }

    public static class ProvideFeedbackRequest {
        private Integer rating;
        private String comments;
        private boolean recommended;

        // Getters and setters
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }

        public boolean isRecommended() { return recommended; }
        public void setRecommended(boolean recommended) { this.recommended = recommended; }
    }
}