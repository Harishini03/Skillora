package com.placement.placement_intelligence.controller;

import com.placement.placement_intelligence.model.*;
import com.placement.placement_intelligence.service.CurrentUserService;
import com.placement.placement_intelligence.service.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Job Management Controller implementing Requirement 3: Recruiter Profile and Job Management
 * 
 * Endpoints:
 * - POST /api/jobs - Create job posting (RECRUITER only)
 * - GET /api/jobs - Get all active jobs
 * - GET /api/jobs/eligible - Get eligible jobs for current student
 * - POST /api/jobs/{jobId}/apply - Apply for job (STUDENT only)
 * - PUT /api/jobs/{jobId} - Update job posting (RECRUITER only)
 * - DELETE /api/jobs/{jobId} - Deactivate job posting (RECRUITER only)
 * - GET /api/jobs/{jobId}/applications - Get applications for job (RECRUITER only)
 * - PUT /api/applications/{applicationId} - Update application status (RECRUITER only)
 * - GET /api/applications/my - Get my applications (STUDENT only)
 */
@RestController
@RequestMapping("/api")
public class JobController {

    private final JobService jobService;
    private final CurrentUserService currentUserService;

    public JobController(JobService jobService, CurrentUserService currentUserService) {
        this.jobService = jobService;
        this.currentUserService = currentUserService;
    }

    // ============= JOB POSTING ENDPOINTS =============

    @PostMapping("/jobs")
    public ResponseEntity<JobPosting> createJobPosting(@RequestBody CreateJobPostingRequest request) {
        Long recruiterId = currentUserService.getCurrentUserId();
        
        JobPosting jobPosting = jobService.createJobPosting(
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getCompensation(),
                request.getMinCgpa(),
                request.getRequiredSkills(),
                request.getJobType(),
                recruiterId,
                request.getDepartmentId(),
                request.getCompanyId()
        );
        
        return ResponseEntity.ok(jobPosting);
    }

    @PutMapping("/jobs/{jobId}")
    public ResponseEntity<JobPosting> updateJobPosting(@PathVariable Long jobId, 
                                                      @RequestBody UpdateJobPostingRequest request) {
        JobPosting jobPosting = jobService.updateJobPosting(
                jobId,
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getCompensation(),
                request.getMinCgpa(),
                request.getRequiredSkills(),
                request.getJobType(),
                request.getDepartmentId()
        );
        
        return ResponseEntity.ok(jobPosting);
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, String>> deactivateJobPosting(@PathVariable Long jobId) {
        Long recruiterId = currentUserService.getCurrentUserId();
        jobService.deactivateJobPosting(jobId, recruiterId);
        
        return ResponseEntity.ok(Map.of("message", "Job posting deactivated successfully"));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobPosting>> getActiveJobs() {
        List<JobPosting> jobs = jobService.getActiveJobPostings();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/eligible")
    public ResponseEntity<List<JobPosting>> getEligibleJobs() {
        Long studentId = currentUserService.currentStudentId();
        List<JobPosting> eligibleJobs = jobService.getEligibleJobsForStudent(studentId);
        return ResponseEntity.ok(eligibleJobs);
    }

    @GetMapping("/jobs/my")
    public ResponseEntity<List<JobPosting>> getMyJobPostings() {
        Long recruiterId = currentUserService.getCurrentUserId();
        List<JobPosting> jobs = jobService.getJobPostingsByRecruiter(recruiterId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobPosting> getJobPosting(@PathVariable Long jobId) {
        JobPosting jobPosting = jobService.getJobPosting(jobId);
        return ResponseEntity.ok(jobPosting);
    }

    // ============= APPLICATION ENDPOINTS =============

    @PostMapping("/jobs/{jobId}/apply")
    public ResponseEntity<JobApplication> applyForJob(@PathVariable Long jobId) {
        Long studentId = currentUserService.currentStudentId();
        JobApplication application = jobService.applyForJob(jobId, studentId);
        
        return ResponseEntity.ok(application);
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<List<JobApplication>> getJobApplications(@PathVariable Long jobId) {
        Long recruiterId = currentUserService.getCurrentUserId();
        List<JobApplication> applications = jobService.getApplicationsByJob(jobId, recruiterId);
        
        return ResponseEntity.ok(applications);
    }

    @PutMapping("/applications/{applicationId}")
    public ResponseEntity<JobApplication> updateApplicationStatus(@PathVariable Long applicationId,
                                                                @RequestBody UpdateApplicationStatusRequest request) {
        Long recruiterId = currentUserService.getCurrentUserId();
        JobApplication application = jobService.updateApplicationStatus(
                applicationId,
                request.getStatus(),
                request.getRecruiterNotes(),
                recruiterId
        );
        
        return ResponseEntity.ok(application);
    }

    @GetMapping("/applications/my")
    public ResponseEntity<List<JobApplication>> getMyApplications() {
        Long studentId = currentUserService.currentStudentId();
        List<JobApplication> applications = jobService.getApplicationsByStudent(studentId);
        
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<JobApplication> getJobApplication(@PathVariable Long applicationId) {
        JobApplication application = jobService.getJobApplication(applicationId);
        return ResponseEntity.ok(application);
    }

    // ============= REQUEST DTOs =============

    public static class CreateJobPostingRequest {
        private String title;
        private String description;
        private String location;
        private String compensation;
        private Double minCgpa;
        private String requiredSkills;
        private JobType jobType;
        private Long departmentId;
        private Long companyId;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getCompensation() { return compensation; }
        public void setCompensation(String compensation) { this.compensation = compensation; }

        public Double getMinCgpa() { return minCgpa; }
        public void setMinCgpa(Double minCgpa) { this.minCgpa = minCgpa; }

        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

        public JobType getJobType() { return jobType; }
        public void setJobType(JobType jobType) { this.jobType = jobType; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
    }

    public static class UpdateJobPostingRequest {
        private String title;
        private String description;
        private String location;
        private String compensation;
        private Double minCgpa;
        private String requiredSkills;
        private JobType jobType;
        private Long departmentId;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getCompensation() { return compensation; }
        public void setCompensation(String compensation) { this.compensation = compensation; }

        public Double getMinCgpa() { return minCgpa; }
        public void setMinCgpa(Double minCgpa) { this.minCgpa = minCgpa; }

        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }

        public JobType getJobType() { return jobType; }
        public void setJobType(JobType jobType) { this.jobType = jobType; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    }

    public static class UpdateApplicationStatusRequest {
        private ApplicationStatus status;
        private String recruiterNotes;

        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }

        public String getRecruiterNotes() { return recruiterNotes; }
        public void setRecruiterNotes(String recruiterNotes) { this.recruiterNotes = recruiterNotes; }
    }
}