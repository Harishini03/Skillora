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
 * Job Management Service implementing Requirement 3: Recruiter Profile and Job Management
 * 
 * Features:
 * - Job posting with eligibility criteria
 * - Application tracking and status management
 * - Eligibility checking logic
 * - Application workflow management
 */
@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobPostingRepository jobPostingRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    public JobService(JobPostingRepository jobPostingRepository,
                     JobApplicationRepository jobApplicationRepository,
                     StudentRepository studentRepository,
                     UserRepository userRepository,
                     DepartmentRepository departmentRepository,
                     CompanyRepository companyRepository,
                     NotificationService notificationService) {
        this.jobPostingRepository = jobPostingRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.companyRepository = companyRepository;
        this.notificationService = notificationService;
    }

    // ============= JOB POSTING OPERATIONS =============

    @Transactional
    public JobPosting createJobPosting(String title, String description, String location,
                                     String compensation, Double minCgpa, String requiredSkills,
                                     JobType jobType, Long recruiterId, Long departmentId, Long companyId) {
        logger.info("Creating job posting: {} by recruiter: {}", title, recruiterId);

        // Validate recruiter
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter not found"));
        
        if (recruiter.getRole() != Role.RECRUITER) {
            throw new BusinessLogicException("Only recruiters can create job postings");
        }

        // Validate optional references
        Department department = null;
        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        Company company = null;
        if (companyId != null) {
            company = companyRepository.findById(companyId)
                    .orElse(null); // Company might be optional
        }

        // Validate inputs
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessLogicException("Job title is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new BusinessLogicException("Job description is required");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new BusinessLogicException("Job location is required");
        }

        JobPosting jobPosting = new JobPosting();
        jobPosting.setTitle(title.trim());
        jobPosting.setDescription(description.trim());
        jobPosting.setLocation(location.trim());
        jobPosting.setCompensation(compensation != null ? compensation.trim() : null);
        jobPosting.setMinCgpa(minCgpa);
        jobPosting.setRequiredSkills(requiredSkills != null ? requiredSkills.trim() : null);
        jobPosting.setJobType(jobType);
        jobPosting.setRecruiter(recruiter);
        jobPosting.setDepartment(department);
        jobPosting.setCompany(company);
        jobPosting.setActive(true);

        JobPosting saved = jobPostingRepository.save(jobPosting);

        // Notify all active students about the new job
        notifyStudentsOfNewJob(saved);

        return saved;
    }

    // ============= trigger new-job notifications =============

    @Transactional
    protected void notifyStudentsOfNewJob(JobPosting jobPosting) {
        List<Student> students = studentRepository.findAll();
        for (Student s : students) {
            if (s.getUser() != null && Boolean.TRUE.equals(s.getUser().getActive())) {
                try {
                    notificationService.createStatusUpdateNotification(
                            s.getUser().getId(),
                            jobPosting.getTitle(),
                            "NEW_JOB_POSTED"
                    );
                } catch (Exception ex) {
                    logger.warn("Failed to notify user {} about new job: {}", s.getUser().getId(), ex.getMessage());
                }
            }
        }
    }

    @Transactional
    public JobPosting updateJobPosting(Long jobPostingId, String title, String description,
                                     String location, String compensation, Double minCgpa,
                                     String requiredSkills, JobType jobType, Long departmentId) {
        logger.info("Updating job posting: {}", jobPostingId);

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));

        if (title != null && !title.trim().isEmpty()) {
            jobPosting.setTitle(title.trim());
        }
        if (description != null && !description.trim().isEmpty()) {
            jobPosting.setDescription(description.trim());
        }
        if (location != null && !location.trim().isEmpty()) {
            jobPosting.setLocation(location.trim());
        }
        if (compensation != null) {
            jobPosting.setCompensation(compensation.trim());
        }
        if (minCgpa != null) {
            jobPosting.setMinCgpa(minCgpa);
        }
        if (requiredSkills != null) {
            jobPosting.setRequiredSkills(requiredSkills.trim());
        }
        if (jobType != null) {
            jobPosting.setJobType(jobType);
        }
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            jobPosting.setDepartment(department);
        }

        return jobPostingRepository.save(jobPosting);
    }

    @Transactional
    public void deactivateJobPosting(Long jobPostingId, Long recruiterId) {
        logger.info("Deactivating job posting: {} by recruiter: {}", jobPostingId, recruiterId);

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));

        // Verify ownership
        if (!jobPosting.getRecruiter().getId().equals(recruiterId)) {
            throw new BusinessLogicException("Only the recruiter who created the job can deactivate it");
        }

        jobPosting.setActive(false);
        jobPostingRepository.save(jobPosting);
    }

    @Transactional(readOnly = true)
    public List<JobPosting> getActiveJobPostings() {
        return jobPostingRepository.findByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingsByRecruiter(Long recruiterId) {
        return jobPostingRepository.findByRecruiter_IdOrderByCreatedAtDesc(recruiterId);
    }

    @Transactional(readOnly = true)
    public List<JobPosting> getEligibleJobsForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        return jobPostingRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .filter(job -> isStudentEligible(student, job))
                .toList();
    }

    // ============= APPLICATION OPERATIONS =============

    @Transactional
    public JobApplication applyForJob(Long jobPostingId, Long studentId) {
        logger.info("Student {} applying for job {}", studentId, jobPostingId);

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));

        if (!jobPosting.getActive()) {
            throw new BusinessLogicException("Job posting is no longer active");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Check eligibility
        if (!isStudentEligible(student, jobPosting)) {
            throw new BusinessLogicException("Student does not meet the eligibility criteria for this job");
        }

        // Check for duplicate application
        Optional<JobApplication> existingApplication = jobApplicationRepository
                .findByJobPosting_IdAndStudent_Id(jobPostingId, studentId);
        
        if (existingApplication.isPresent()) {
            throw new BusinessLogicException("Student has already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setJobPosting(jobPosting);
        application.setStudent(student);
        application.setStatus(ApplicationStatus.APPLIED);

        return jobApplicationRepository.save(application);
    }

    @Transactional
    public JobApplication updateApplicationStatus(Long applicationId, ApplicationStatus newStatus, 
                                                String recruiterNotes, Long recruiterId) {
        logger.info("Updating application {} status to {} by recruiter {}", applicationId, newStatus, recruiterId);

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));

        // Verify recruiter ownership
        if (!application.getJobPosting().getRecruiter().getId().equals(recruiterId)) {
            throw new BusinessLogicException("Only the recruiter who posted the job can update application status");
        }

        // Validate status transition
        if (!isValidStatusTransition(application.getStatus(), newStatus)) {
            throw new BusinessLogicException("Invalid status transition from " + 
                    application.getStatus() + " to " + newStatus);
        }

        application.setStatus(newStatus);
        application.setRecruiterNotes(recruiterNotes);
        application.setLastUpdatedAt(LocalDateTime.now());

        return jobApplicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getApplicationsByJob(Long jobPostingId, Long recruiterId) {
        // Verify recruiter ownership
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));

        if (!jobPosting.getRecruiter().getId().equals(recruiterId)) {
            throw new BusinessLogicException("Only the recruiter who posted the job can view applications");
        }

        return jobApplicationRepository.findByJobPosting_IdOrderByAppliedAtDesc(jobPostingId);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> getApplicationsByStudent(Long studentId) {
        return jobApplicationRepository.findByStudent_IdOrderByAppliedAtDesc(studentId);
    }

    // ============= ELIGIBILITY CHECKING LOGIC =============

    /**
     * Check if student meets job eligibility criteria.
     * Implements Requirement 3 correctness property: Eligibility Enforcement
     */
    public boolean isStudentEligible(Student student, JobPosting jobPosting) {
        // Check CGPA requirement
        if (jobPosting.getMinCgpa() != null) {
            if (student.getCgpa() == null || student.getCgpa() < jobPosting.getMinCgpa()) {
                logger.debug("Student {} CGPA {} below required {}", 
                        student.getId(), student.getCgpa(), jobPosting.getMinCgpa());
                return false;
            }
        }

        // Check department restriction
        if (jobPosting.getDepartment() != null) {
            if (!student.getDepartment().getId().equals(jobPosting.getDepartment().getId())) {
                logger.debug("Student {} department {} not matching required {}", 
                        student.getId(), student.getDepartment().getName(), 
                        jobPosting.getDepartment().getName());
                return false;
            }
        }

        // Additional eligibility checks can be added here (skills, etc.)
        
        return true;
    }

    /**
     * Validate application status transitions.
     * Implements Requirement 3 correctness property: Application Status Transitions
     */
    private boolean isValidStatusTransition(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        return switch (currentStatus) {
            case APPLIED -> newStatus == ApplicationStatus.SHORTLISTED || 
                          newStatus == ApplicationStatus.REJECTED ||
                          newStatus == ApplicationStatus.INTERVIEW_SCHEDULED;
            case SHORTLISTED -> newStatus == ApplicationStatus.OFFERED || 
                               newStatus == ApplicationStatus.REJECTED ||
                               newStatus == ApplicationStatus.INTERVIEW_SCHEDULED;
            case INTERVIEW_SCHEDULED -> newStatus == ApplicationStatus.OFFERED || 
                                       newStatus == ApplicationStatus.REJECTED ||
                                       newStatus == ApplicationStatus.SHORTLISTED;
            case OFFERED -> newStatus == ApplicationStatus.REJECTED ||
                          newStatus == ApplicationStatus.WITHDRAWN;
            case REJECTED, WITHDRAWN -> false; // Terminal states
        };
    }

    @Transactional(readOnly = true)
    public JobPosting getJobPosting(Long jobPostingId) {
        return jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));
    }

    @Transactional(readOnly = true)
    public JobApplication getJobApplication(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found"));
    }
}