package com.placement.placement_intelligence.dto;

import com.placement.placement_intelligence.model.ApplicationStatus;
import com.placement.placement_intelligence.model.InterviewMode;
import com.placement.placement_intelligence.model.JobType;

import java.time.LocalDateTime;
import java.util.List;

public final class PortalDtos {

    private PortalDtos() {
    }

    public record StudentHomeResponse(
            String studentName,
            Double readinessScore,
            int openJobs,
            int applicationsSubmitted,
            int interviewsScheduled,
            List<ApplicationSummary> recentApplications
    ) {
    }

    public record ApplicationSummary(
            Long applicationId,
            Long jobId,
            String jobTitle,
            String companyName,
            ApplicationStatus status,
            LocalDateTime appliedAt,
            LocalDateTime interviewAt
    ) {
    }

    public record JobPostingResponse(
            Long jobId,
            String title,
            String description,
            String location,
            String compensation,
            Double minCgpa,
            String requiredSkills,
            JobType jobType,
            String department,
            String recruiterName,
            boolean active,
            LocalDateTime createdAt
    ) {
    }

    public record CreateJobPostingRequest(
            String title,
            String description,
            String location,
            String compensation,
            Double minCgpa,
            String requiredSkills,
            JobType jobType,
            Long departmentId,
            Long companyId
    ) {
    }

    public record UpdateApplicationStatusRequest(
            ApplicationStatus status,
            String recruiterNotes
    ) {
    }

    public record ScheduleInterviewRequest(
            Long jobApplicationId,
            Long interviewerUserId,
            LocalDateTime scheduledAt,
            Integer durationMinutes,
            InterviewMode mode,
            String meetingLink
    ) {
    }

    public record InterviewQueueItem(
            Long interviewScheduleId,
            Long jobApplicationId,
            Long studentId,
            String studentName,
            String jobTitle,
            String companyName,
            LocalDateTime scheduledAt,
            Integer durationMinutes,
            InterviewMode mode,
            String meetingLink,
            ApplicationStatus applicationStatus
    ) {
    }

    public record SubmitInterviewFeedbackRequest(
            Integer technicalScore,
            Integer communicationScore,
            Integer confidenceScore,
            String recommendation,
            String comments
    ) {
    }

    public record RecruiterDashboardResponse(
            long activeJobs,
            long appliedCount,
            long shortlistedCount,
            long interviewScheduledCount,
            long offeredCount,
            List<JobPostingResponse> jobs
    ) {
    }

    public record NotificationItem(
            Long notificationId,
            String type,
            String message,
            boolean read,
            LocalDateTime createdAt
    ) {
    }

    public record UserOption(
            Long userId,
            String name,
            String email,
            String role
    ) {
    }
}
