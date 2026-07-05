package com.placement.placement_intelligence.service;

import com.placement.placement_intelligence.dto.PortalDtos;
import com.placement.placement_intelligence.model.ApplicationStatus;
import com.placement.placement_intelligence.model.Company;
import com.placement.placement_intelligence.model.InterviewFeedback;
import com.placement.placement_intelligence.model.InterviewMode;
import com.placement.placement_intelligence.model.InterviewSchedule;
import com.placement.placement_intelligence.model.InterviewStatus;
import com.placement.placement_intelligence.model.JobApplication;
import com.placement.placement_intelligence.model.JobPosting;
import com.placement.placement_intelligence.model.PortalNotification;
import com.placement.placement_intelligence.model.Role;
import com.placement.placement_intelligence.model.Student;
import com.placement.placement_intelligence.model.User;
import com.placement.placement_intelligence.repository.CompanyRepository;
import com.placement.placement_intelligence.repository.DepartmentRepository;
import com.placement.placement_intelligence.repository.InterviewFeedbackRepository;
import com.placement.placement_intelligence.repository.InterviewScheduleRepository;
import com.placement.placement_intelligence.repository.JobApplicationRepository;
import com.placement.placement_intelligence.repository.JobPostingRepository;
import com.placement.placement_intelligence.repository.PortalNotificationRepository;
import com.placement.placement_intelligence.repository.StudentRepository;
import com.placement.placement_intelligence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PortalService {

    private final CurrentUserService currentUserService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewScheduleRepository interviewScheduleRepository;
    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final PortalNotificationRepository portalNotificationRepository;

    public PortalService(CurrentUserService currentUserService,
                         StudentRepository studentRepository,
                         UserRepository userRepository,
                         DepartmentRepository departmentRepository,
                         CompanyRepository companyRepository,
                         JobPostingRepository jobPostingRepository,
                         JobApplicationRepository jobApplicationRepository,
                         InterviewScheduleRepository interviewScheduleRepository,
                         InterviewFeedbackRepository interviewFeedbackRepository,
                         PortalNotificationRepository portalNotificationRepository) {
        this.currentUserService = currentUserService;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewScheduleRepository = interviewScheduleRepository;
        this.interviewFeedbackRepository = interviewFeedbackRepository;
        this.portalNotificationRepository = portalNotificationRepository;
    }

    @Transactional(readOnly = true)
    public PortalDtos.StudentHomeResponse studentHome() {
        Student student = requireCurrentStudent();
        List<JobPosting> jobs = jobPostingRepository.findByActiveTrueOrderByCreatedAtDesc();
        int openJobs = (int) jobs.stream()
                .filter(job -> job.getMinCgpa() == null || student.getCgpa() >= job.getMinCgpa())
                .count();

        List<JobApplication> applications = jobApplicationRepository.findByStudent_IdOrderByAppliedAtDesc(student.getId());
        int interviewsScheduled = (int) applications.stream()
                .filter(application -> application.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED)
                .count();

        List<PortalDtos.ApplicationSummary> recent = applications.stream()
                .limit(5)
                .map(this::toApplicationSummary)
                .toList();

        return new PortalDtos.StudentHomeResponse(
                student.getName(),
                safe(student.getReadinessScore()),
                openJobs,
                applications.size(),
                interviewsScheduled,
                recent
        );
    }

    @Transactional(readOnly = true)
    public List<PortalDtos.JobPostingResponse> discoverJobs(String search, Long departmentId) {
        Student student = requireCurrentStudent();
        String keyword = search == null ? "" : search.trim().toLowerCase(Locale.ENGLISH);
        return jobPostingRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .filter(job -> job.getMinCgpa() == null || student.getCgpa() >= job.getMinCgpa())
                .filter(job -> departmentId == null || (job.getDepartment() != null && departmentId.equals(job.getDepartment().getId())))
                .filter(job -> keyword.isBlank()
                        || job.getTitle().toLowerCase(Locale.ENGLISH).contains(keyword)
                        || job.getDescription().toLowerCase(Locale.ENGLISH).contains(keyword))
                .map(this::toJobPostingResponse)
                .toList();
    }

    @Transactional
    public PortalDtos.ApplicationSummary applyToJob(Long jobId) {
        Student student = requireCurrentStudent();
        JobPosting posting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (!Boolean.TRUE.equals(posting.getActive())) {
            throw new IllegalArgumentException("Job is no longer active");
        }
        if (posting.getMinCgpa() != null && student.getCgpa() < posting.getMinCgpa()) {
            throw new IllegalArgumentException("Student not eligible for this job");
        }
        jobApplicationRepository.findByJobPosting_IdAndStudent_Id(jobId, student.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Already applied to this job");
                });

        JobApplication application = new JobApplication();
        application.setJobPosting(posting);
        application.setStudent(student);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setLastUpdatedAt(LocalDateTime.now());
        application = jobApplicationRepository.save(application);

        notifyUser(posting.getRecruiter(), "APPLICATION",
                student.getName() + " applied for " + posting.getTitle());
        return toApplicationSummary(application);
    }

    @Transactional(readOnly = true)
    public List<PortalDtos.ApplicationSummary> studentApplications() {
        Student student = requireCurrentStudent();
        return jobApplicationRepository.findByStudent_IdOrderByAppliedAtDesc(student.getId()).stream()
                .map(this::toApplicationSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortalDtos.RecruiterDashboardResponse recruiterDashboard() {
        User recruiter = requireCurrentRecruiter();
        List<JobPosting> jobs = jobPostingRepository.findByRecruiter_IdOrderByCreatedAtDesc(recruiter.getId());
        long applied = jobApplicationRepository.countByJobPosting_Recruiter_IdAndStatusIn(recruiter.getId(),
                List.of(ApplicationStatus.APPLIED));
        long shortlisted = jobApplicationRepository.countByJobPosting_Recruiter_IdAndStatusIn(recruiter.getId(),
                List.of(ApplicationStatus.SHORTLISTED));
        long scheduled = jobApplicationRepository.countByJobPosting_Recruiter_IdAndStatusIn(recruiter.getId(),
                List.of(ApplicationStatus.INTERVIEW_SCHEDULED));
        long offered = jobApplicationRepository.countByJobPosting_Recruiter_IdAndStatus(recruiter.getId(),
                ApplicationStatus.OFFERED);
        return new PortalDtos.RecruiterDashboardResponse(
                jobs.stream().filter(JobPosting::getActive).count(),
                applied,
                shortlisted,
                scheduled,
                offered,
                jobs.stream().map(this::toJobPostingResponse).toList()
        );
    }

    @Transactional
    public PortalDtos.JobPostingResponse createJob(PortalDtos.CreateJobPostingRequest request) {
        User recruiter = requireCurrentRecruiter();
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Job title is required");
        }
        if (request.description() == null || request.description().isBlank()) {
            throw new IllegalArgumentException("Job description is required");
        }
        JobPosting job = new JobPosting();
        job.setTitle(request.title().trim());
        job.setDescription(request.description().trim());
        job.setLocation(request.location() == null || request.location().isBlank() ? "Remote" : request.location().trim());
        job.setCompensation(request.compensation());
        job.setMinCgpa(request.minCgpa());
        job.setRequiredSkills(request.requiredSkills());
        job.setJobType(request.jobType() == null ? com.placement.placement_intelligence.model.JobType.FULL_TIME : request.jobType());
        job.setRecruiter(recruiter);
        job.setActive(true);
        job.setCreatedAt(LocalDateTime.now());
        if (request.departmentId() != null) {
            job.setDepartment(departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Department not found")));
        }
        if (request.companyId() != null) {
            Company company = companyRepository.findById(request.companyId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found"));
            job.setCompany(company);
        }
        return toJobPostingResponse(jobPostingRepository.save(job));
    }

    @Transactional(readOnly = true)
    public List<PortalDtos.ApplicationSummary> recruiterApplications(Long jobId, ApplicationStatus status) {
        User recruiter = requireCurrentRecruiter();
        if (jobId == null) {
            throw new IllegalArgumentException("jobId is required");
        }
        JobPosting job = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new IllegalArgumentException("Unauthorized job access");
        }
        List<JobApplication> applications = status == null
                ? jobApplicationRepository.findByJobPosting_IdOrderByAppliedAtDesc(jobId)
                : jobApplicationRepository.findByJobPosting_IdAndStatusOrderByAppliedAtDesc(jobId, status);
        return applications.stream().map(this::toApplicationSummary).toList();
    }

    @Transactional
    public PortalDtos.ApplicationSummary updateApplicationStatus(Long applicationId, PortalDtos.UpdateApplicationStatusRequest request) {
        User recruiter = requireCurrentRecruiter();
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (!application.getJobPosting().getRecruiter().getId().equals(recruiter.getId())) {
            throw new IllegalArgumentException("Unauthorized application access");
        }
        if (request.status() == null) {
            throw new IllegalArgumentException("Application status is required");
        }
        application.setStatus(request.status());
        application.setRecruiterNotes(request.recruiterNotes());
        application.setLastUpdatedAt(LocalDateTime.now());
        JobApplication saved = jobApplicationRepository.save(application);
        notifyUser(application.getStudent().getUser(), "APPLICATION_STATUS",
                "Your application for " + application.getJobPosting().getTitle() + " is now " + request.status().name());
        return toApplicationSummary(saved);
    }

    @Transactional
    public PortalDtos.InterviewQueueItem scheduleInterview(PortalDtos.ScheduleInterviewRequest request) {
        User recruiter = requireCurrentRecruiter();
        if (request.jobApplicationId() == null) {
            throw new IllegalArgumentException("jobApplicationId is required");
        }
        JobApplication application = jobApplicationRepository.findById(request.jobApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (!application.getJobPosting().getRecruiter().getId().equals(recruiter.getId())) {
            throw new IllegalArgumentException("Unauthorized application access");
        }
        interviewScheduleRepository.findByJobApplication_Id(application.getId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Interview already scheduled for this application");
        });
        User interviewer = userRepository.findById(request.interviewerUserId() == null ? -1L : request.interviewerUserId())
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found"));
        if (interviewer.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Selected user must be a STAFF member");
        }
        InterviewSchedule schedule = new InterviewSchedule();
        schedule.setJobApplication(application);
        schedule.setInterviewer(interviewer);
        schedule.setScheduledAt(request.scheduledAt() == null ? LocalDateTime.now().plusDays(1) : request.scheduledAt());
        schedule.setDurationMinutes(request.durationMinutes() == null ? 45 : Math.max(15, request.durationMinutes()));
        schedule.setMode(request.mode() == null ? InterviewMode.ONLINE : request.mode());
        schedule.setMeetingLink(request.meetingLink());
        schedule.setStatus(InterviewStatus.SCHEDULED);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule = interviewScheduleRepository.save(schedule);

        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        application.setLastUpdatedAt(LocalDateTime.now());
        jobApplicationRepository.save(application);

        String dateTimeText = schedule.getScheduledAt().toString();
        notifyUser(application.getStudent().getUser(), "INTERVIEW",
                "Interview scheduled for " + application.getJobPosting().getTitle() + " at " + dateTimeText);
        notifyUser(interviewer, "INTERVIEW",
                "New interview assigned for " + application.getStudent().getName() + " at " + dateTimeText);
        return toInterviewQueueItem(schedule);
    }

    @Transactional(readOnly = true)
    public List<PortalDtos.InterviewQueueItem> interviewerQueue() {
        User interviewer = requireCurrentInterviewer();
        return interviewScheduleRepository.findByInterviewer_IdAndStatusOrderByScheduledAtAsc(interviewer.getId(), InterviewStatus.SCHEDULED)
                .stream()
                .sorted(Comparator.comparing(InterviewSchedule::getScheduledAt))
                .map(this::toInterviewQueueItem)
                .toList();
    }

    @Transactional
    public PortalDtos.InterviewQueueItem submitInterviewFeedback(Long scheduleId, PortalDtos.SubmitInterviewFeedbackRequest request) {
        User current = requireCurrentInterviewer();
        InterviewSchedule schedule = interviewScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Interview schedule not found"));
        if (current.getRole() != Role.STAFF && !schedule.getInterviewer().getId().equals(current.getId())) {
            throw new IllegalArgumentException("Cannot submit feedback for this interview");
        }
        interviewFeedbackRepository.findBySchedule_Id(scheduleId).ifPresent(existing -> {
            throw new IllegalArgumentException("Feedback already submitted");
        });

        validateScore(request.technicalScore(), "technicalScore");
        validateScore(request.communicationScore(), "communicationScore");
        validateScore(request.confidenceScore(), "confidenceScore");

        InterviewFeedback feedback = new InterviewFeedback();
        feedback.setSchedule(schedule);
        feedback.setTechnicalScore(request.technicalScore());
        feedback.setCommunicationScore(request.communicationScore());
        feedback.setConfidenceScore(request.confidenceScore());
        feedback.setRecommendation(request.recommendation() == null || request.recommendation().isBlank() ? "Hold" : request.recommendation().trim());
        feedback.setComments(request.comments());
        feedback.setSubmittedAt(LocalDateTime.now());
        interviewFeedbackRepository.save(feedback);

        JobApplication application = schedule.getJobApplication();
        schedule.setStatus(InterviewStatus.COMPLETED);
        interviewScheduleRepository.save(schedule);

        String recommendation = feedback.getRecommendation().toLowerCase(Locale.ENGLISH);
        if (recommendation.contains("strong hire") || recommendation.equals("hire")) {
            application.setStatus(ApplicationStatus.OFFERED);
        } else if (recommendation.contains("reject")) {
            application.setStatus(ApplicationStatus.REJECTED);
        } else {
            application.setStatus(ApplicationStatus.SHORTLISTED);
        }
        application.setLastUpdatedAt(LocalDateTime.now());
        jobApplicationRepository.save(application);

        notifyUser(application.getStudent().getUser(), "INTERVIEW_FEEDBACK",
                "Interview feedback submitted. Application status: " + application.getStatus().name());
        notifyUser(application.getJobPosting().getRecruiter(), "INTERVIEW_FEEDBACK",
                "Feedback submitted for " + application.getStudent().getName() + " (" + application.getJobPosting().getTitle() + ")");

        return toInterviewQueueItem(schedule);
    }

    @Transactional(readOnly = true)
    public List<PortalDtos.NotificationItem> notifications() {
        User user = currentUserService.currentUser();
        return portalNotificationRepository.findTop20ByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(item -> new PortalDtos.NotificationItem(
                        item.getId(),
                        item.getNotificationType(),
                        item.getMessage(),
                        Boolean.TRUE.equals(item.getRead()),
                        item.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortalDtos.UserOption> availableInterviewers() {
        requireCurrentRecruiter();
        return userRepository.findByRoleOrderByNameAsc(Role.STAFF).stream()
                .map(user -> new PortalDtos.UserOption(user.getId(), user.getName(), user.getEmail(), user.getRole().name()))
                .toList();
    }

    @Transactional
    public void markNotificationRead(Long notificationId) {
        User user = currentUserService.currentUser();
        PortalNotification notification = portalNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized notification access");
        }
        notification.setRead(true);
        portalNotificationRepository.save(notification);
    }

    private PortalDtos.ApplicationSummary toApplicationSummary(JobApplication application) {
        LocalDateTime interviewAt = interviewScheduleRepository.findByJobApplication_Id(application.getId())
                .map(InterviewSchedule::getScheduledAt)
                .orElse(null);
        return new PortalDtos.ApplicationSummary(
                application.getId(),
                application.getJobPosting().getId(),
                application.getJobPosting().getTitle(),
                resolveCompanyName(application.getJobPosting()),
                application.getStatus(),
                application.getAppliedAt(),
                interviewAt
        );
    }

    private PortalDtos.JobPostingResponse toJobPostingResponse(JobPosting job) {
        return new PortalDtos.JobPostingResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getCompensation(),
                job.getMinCgpa(),
                job.getRequiredSkills(),
                job.getJobType(),
                job.getDepartment() == null ? null : job.getDepartment().getName(),
                job.getRecruiter().getName(),
                Boolean.TRUE.equals(job.getActive()),
                job.getCreatedAt()
        );
    }

    private PortalDtos.InterviewQueueItem toInterviewQueueItem(InterviewSchedule schedule) {
        JobApplication application = schedule.getJobApplication();
        return new PortalDtos.InterviewQueueItem(
                schedule.getId(),
                application.getId(),
                application.getStudent().getId(),
                application.getStudent().getName(),
                application.getJobPosting().getTitle(),
                resolveCompanyName(application.getJobPosting()),
                schedule.getScheduledAt(),
                schedule.getDurationMinutes(),
                schedule.getMode(),
                schedule.getMeetingLink(),
                application.getStatus()
        );
    }

    private Student requireCurrentStudent() {
        Long studentId = currentUserService.currentStudentId();
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student profile not found"));
    }

    private User requireCurrentRecruiter() {
        User user = currentUserService.currentUser();
        if (!(user.getRole() == Role.RECRUITER || user.getRole() == Role.STAFF)) {
            throw new IllegalArgumentException("Recruiter access required");
        }
        return user;
    }

    private User requireCurrentInterviewer() {
        User user = currentUserService.currentUser();
        if (user.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Staff access required for interview management");
        }
        return user;
    }

    private void notifyUser(User user, String type, String message) {
        if (user == null) {
            return;
        }
        PortalNotification notification = new PortalNotification();
        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        portalNotificationRepository.save(notification);
    }

    private void validateScore(Integer score, String fieldName) {
        if (score == null || score < 1 || score > 10) {
            throw new IllegalArgumentException(fieldName + " must be between 1 and 10");
        }
    }

    private String resolveCompanyName(JobPosting posting) {
        if (posting.getCompany() != null) {
            return posting.getCompany().getName();
        }
        return "Skillora Partner";
    }

    private double safe(Double value) {
        return value == null ? 0.0 : value;
    }
}
