package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.dto.PortalDtos;
import com.placement.placement_intelligence.model.ApplicationStatus;
import com.placement.placement_intelligence.service.PortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/student/home")
    public ResponseEntity<PortalDtos.StudentHomeResponse> studentHome() {
        return ResponseEntity.ok(portalService.studentHome());
    }

    @GetMapping("/student/jobs")
    public ResponseEntity<List<PortalDtos.JobPostingResponse>> discoverJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(portalService.discoverJobs(search, departmentId));
    }

    @PostMapping("/student/jobs/{jobId}/apply")
    public ResponseEntity<PortalDtos.ApplicationSummary> applyToJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(portalService.applyToJob(jobId));
    }

    @GetMapping("/student/applications")
    public ResponseEntity<List<PortalDtos.ApplicationSummary>> studentApplications() {
        return ResponseEntity.ok(portalService.studentApplications());
    }

    @GetMapping("/recruiter/dashboard")
    public ResponseEntity<PortalDtos.RecruiterDashboardResponse> recruiterDashboard() {
        return ResponseEntity.ok(portalService.recruiterDashboard());
    }

    @PostMapping("/recruiter/jobs")
    public ResponseEntity<PortalDtos.JobPostingResponse> createJob(
            @RequestBody PortalDtos.CreateJobPostingRequest request) {
        return ResponseEntity.ok(portalService.createJob(request));
    }

    @GetMapping("/recruiter/applications")
    public ResponseEntity<List<PortalDtos.ApplicationSummary>> recruiterApplications(
            @RequestParam Long jobId,
            @RequestParam(required = false) ApplicationStatus status) {
        return ResponseEntity.ok(portalService.recruiterApplications(jobId, status));
    }

    @PatchMapping("/recruiter/applications/{applicationId}/status")
    public ResponseEntity<PortalDtos.ApplicationSummary> updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody PortalDtos.UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(portalService.updateApplicationStatus(applicationId, request));
    }

    @PostMapping("/recruiter/interviews")
    public ResponseEntity<PortalDtos.InterviewQueueItem> scheduleInterview(
            @RequestBody PortalDtos.ScheduleInterviewRequest request) {
        return ResponseEntity.ok(portalService.scheduleInterview(request));
    }

    @GetMapping("/recruiter/interviewers")
    public ResponseEntity<List<PortalDtos.UserOption>> availableInterviewers() {
        return ResponseEntity.ok(portalService.availableInterviewers());
    }

    @GetMapping("/staff/interviews/queue")
    public ResponseEntity<List<PortalDtos.InterviewQueueItem>> interviewerQueue() {
        return ResponseEntity.ok(portalService.interviewerQueue());
    }

    @PostMapping("/staff/interviews/{scheduleId}/feedback")
    public ResponseEntity<PortalDtos.InterviewQueueItem> submitFeedback(
            @PathVariable Long scheduleId,
            @RequestBody PortalDtos.SubmitInterviewFeedbackRequest request) {
        return ResponseEntity.ok(portalService.submitInterviewFeedback(scheduleId, request));
    }

    @GetMapping("/common/notifications")
    public ResponseEntity<List<PortalDtos.NotificationItem>> notifications() {
        return ResponseEntity.ok(portalService.notifications());
    }

    @PatchMapping("/common/notifications/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long notificationId) {
        portalService.markNotificationRead(notificationId);
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }
}
