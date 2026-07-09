# Requirements Document: Skillora - AI-Powered Placement Intelligence System

## Introduction

Skillora is a comprehensive placement readiness platform designed to prepare students for campus recruitment through AI-powered learning, coding practice, mock interviews, and performance analytics. The system serves three primary user roles: Students, Staff (Admin), and Recruiters.

## Glossary

- **Student**: End user who accesses learning materials, practices coding, takes tests, and applies for jobs
- **Staff**: Administrative user who manages student data, tracks placement statistics, and oversees the system
- **Recruiter**: External user who posts jobs, reviews applications, and schedules interviews
- **AI Mentor**: Adaptive learning system that generates personalized content based on student performance
- **PBT**: Property-Based Testing for ensuring correctness of implementations

## Requirements

### Requirement 1: Three-Role Authentication System

**User Story**: As a system user, I want to authenticate with role-based access control so that I can access features appropriate to my role.

#### Acceptance Criteria

1. WHERE the system is accessed, THE system SHALL support three distinct roles: STUDENT, STAFF, and RECRUITER
2. WHEN a user attempts to log in, THE system SHALL validate credentials against the user database
3. IF authentication is successful, THEN the system SHALL generate a JWT token valid for 24 hours
4. THE system SHALL support both email/password authentication and Google OAuth
5. WHEN a user registers, THE system SHALL require email verification before account activation
6. THE system SHALL enforce role-based access control on all API endpoints
7. IF an unauthorized access attempt occurs, THEN the system SHALL return HTTP 403 Forbidden

#### Correctness Properties

1. **Property: Authentication Token Validity**: For all valid login attempts, generated JWT tokens SHALL be parsable and contain correct user ID and role
2. **Property: Role Isolation**: For any endpoint marked with role restrictions, requests with mismatched roles SHALL be rejected
3. **Property: Session Expiry**: For all tokens older than 24 hours, authentication SHALL fail

### Requirement 2: Student Profile Management

**User Story**: As a student, I want to manage my comprehensive profile including education, skills, resume, and analytics so that recruiters can evaluate my candidacy.

#### Acceptance Criteria

1. WHEN a student registers, THE system SHALL automatically create a profile entity
2. THE student profile SHALL include: personal details, education history, skills with proficiency levels, resume upload, and analytics data
3. WHEN a student adds education details, THE system SHALL validate required fields (institution, degree, field, start date, end date, CGPA)
4. THE system SHALL support uploading resume files in PDF format up to 5MB
5. WHEN a student adds skills, THE system SHALL categorize them (TECHNICAL, SOFT, DOMAIN) and assign proficiency levels (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
6. THE system SHALL track profile completion percentage
7. IF profile completion is below 70%, THEN the student SHALL see a completion prompt on dashboard

#### Correctness Properties

1. **Property: Profile Data Integrity**: For all student profiles, mandatory fields (name, email, department) SHALL never be null
2. **Property: CGPA Validity**: For all education records, CGPA values SHALL be between 0.0 and 10.0
3. **Property: Resume File Size**: For all resume uploads, file size SHALL not exceed 5MB

### Requirement 3: Recruiter Profile and Job Management

**User Story**: As a recruiter, I want to post job openings, set eligibility criteria, and manage applications so that I can hire qualified candidates.

#### Acceptance Criteria

1. WHEN a recruiter creates a job posting, THE system SHALL require: title, description, company, job type, location, salary range, required skills, and eligibility criteria
2. THE system SHALL support defining eligibility criteria based on: minimum CGPA, allowed departments, allowed degree types, and required skills
3. WHEN a student views job postings, THE system SHALL display only jobs matching their eligibility
4. THE system SHALL allow recruiters to view all applications for their job postings
5. WHEN a recruiter reviews an application, THE system SHALL support status updates: APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED, OFFERED, ACCEPTED
6. THE system SHALL send notifications to students when their application status changes

#### Correctness Properties

1. **Property: Eligibility Enforcement**: For all job applications, the student SHALL meet the minimum CGPA requirement and department restrictions
2. **Property: Application Status Transitions**: For all applications, status changes SHALL follow valid state transitions (APPLIED → UNDER_REVIEW → SHORTLISTED/REJECTED)
3. **Property: Duplicate Application Prevention**: For any student-job pair, only one active application SHALL exist

### Requirement 4: Course Management and Learning Platform

**User Story**: As a student, I want to enroll in structured courses with video lessons and track my progress so that I can systematically learn placement preparation topics.

#### Acceptance Criteria

1. THE system SHALL support creating courses with hierarchical structure: Course → Modules → Lessons
2. WHEN a course is created, THE system SHALL require: title, description, instructor name, thumbnail URL, difficulty level, and duration
3. EACH lesson SHALL include: title, video URL, content text, order sequence, and duration
4. WHEN a student enrolls in a course, THE system SHALL track enrollment date and completion status
5. WHEN a student completes a lesson, THE system SHALL record completion timestamp
6. THE system SHALL calculate course progress as percentage of lessons completed
7. THE student dashboard SHALL display enrolled courses with progress indicators

#### Correctness Properties

1. **Property: Lesson Order Integrity**: For all modules, lesson order values SHALL be unique and sequential
2. **Property: Progress Calculation**: For all enrollments, progress percentage SHALL equal (completed lessons / total lessons) × 100
3. **Property: Completion Timestamp Validity**: For all lesson completions, completion timestamp SHALL be after enrollment timestamp

### Requirement 5: Coding Platform with Multi-Language Support

**User Story**: As a student, I want to solve coding problems in multiple languages with automated test case validation so that I can practice algorithmic problem-solving.

#### Acceptance Criteria

1. THE system SHALL support coding problems with: title, description, difficulty (EASY, MEDIUM, HARD), category, constraints, and test cases
2. THE system SHALL support code execution in: Java, Python, JavaScript, and C++
3. WHEN a student submits code, THE system SHALL execute it in a sandboxed environment with timeout (5 seconds) and memory limits (256MB)
4. THE system SHALL validate submissions against all test cases (visible and hidden)
5. IF all test cases pass, THEN the submission SHALL be marked as ACCEPTED
6. THE system SHALL display execution results: verdict, execution time, memory used, and failed test case details
7. THE system SHALL maintain submission history with code, language, timestamp, and verdict

#### Correctness Properties

1. **Property: Sandbox Isolation**: For all code executions, processes SHALL terminate within 5 seconds
2. **Property: Test Case Coverage**: For all coding problems, at least one test case SHALL be marked as hidden
3. **Property: Verdict Consistency**: For identical code submissions, verdict SHALL be deterministic

### Requirement 6: AI Learning System with Five Modes

**User Story**: As a student, I want an AI mentor that adapts to my performance and provides personalized learning content so that I can improve efficiently in weak areas.

#### Acceptance Criteria

1. THE AI system SHALL support five modes: LEARN, PRACTICE, ADAPTIVE, REVISION, MOCK_TEST
2. WHEN mode is LEARN, THE AI SHALL generate concept explanations with examples
3. WHEN mode is PRACTICE, THE AI SHALL generate practice questions with immediate feedback
4. WHEN mode is ADAPTIVE, THE AI SHALL adjust question difficulty based on student's recent performance
5. WHEN mode is REVISION, THE AI SHALL generate concise summaries of previously learned topics
6. WHEN mode is MOCK_TEST, THE AI SHALL generate timed assessments with scoring
7. THE AI SHALL generate FRESH content for every request (no question repetition)
8. THE system SHALL track AI interaction history with timestamps and performance metrics
9. THE AI SHALL use student's weak areas (from analytics) to personalize content generation

#### Correctness Properties

1. **Property: Content Freshness**: For any two consecutive AI requests with identical parameters, generated content SHALL differ
2. **Property: Difficulty Adaptation**: For students with success rate > 80%, next adaptive question difficulty SHALL increase
3. **Property: Response Time**: For all AI requests, response SHALL be delivered within 10 seconds

### Requirement 7: Aptitude Test System with Question Generation

**User Story**: As a student, I want to take timed aptitude tests with AI-generated questions so that I can prepare for written assessments.

#### Acceptance Criteria

1. THE system SHALL support creating test sessions with: test type (APTITUDE, CODING, MOCK), duration, number of questions
2. WHEN a test starts, THE system SHALL generate questions dynamically using AI
3. THE system SHALL support question types: multiple choice, numerical answer, and subjective
4. WHEN a test is in progress, THE system SHALL display remaining time countdown
5. THE system SHALL auto-submit tests when time expires
6. WHEN a test is submitted, THE system SHALL calculate score, accuracy, and time taken
7. THE system SHALL provide detailed solutions for incorrect answers
8. THE student dashboard SHALL display test history with scores and performance trends

#### Correctness Properties

1. **Property: Test Timing**: For all test sessions, submission timestamp SHALL not exceed (start time + duration)
2. **Property: Score Calculation**: For all tests, score SHALL equal (correct answers / total questions) × 100
3. **Property: Question Uniqueness**: For any test session, no question SHALL appear more than once

### Requirement 8: Interview Scheduling System

**User Story**: As a recruiter, I want to schedule interviews with shortlisted candidates and provide feedback so that I can streamline the hiring process.

#### Acceptance Criteria

1. WHEN a recruiter schedules an interview, THE system SHALL require: student, job posting, date/time, mode (ONLINE, OFFLINE), location/meeting link
2. THE system SHALL send notifications to students when interviews are scheduled
3. THE system SHALL support interview status: SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
4. WHEN an interview is completed, THE recruiter SHALL provide feedback with rating (1-10) and comments
5. THE system SHALL display upcoming interviews on student and recruiter dashboards
6. THE system SHALL prevent scheduling conflicts (same student, overlapping time)

#### Correctness Properties

1. **Property: Future Scheduling**: For all new interview schedules, scheduled time SHALL be in the future
2. **Property: Conflict Prevention**: For any student, no two SCHEDULED interviews SHALL have overlapping time slots
3. **Property: Feedback Completeness**: For all COMPLETED interviews, feedback rating SHALL be between 1 and 10

### Requirement 9: Staff Dashboard with Analytics

**User Story**: As staff, I want to view comprehensive placement analytics and student performance data so that I can monitor and improve placement outcomes.

#### Acceptance Criteria

1. THE staff dashboard SHALL display: total students, placed students, placement percentage, average package, company-wise placements
2. THE system SHALL provide department-wise analytics: student count, placement rate, average package per department
3. THE system SHALL display top-performing students with their scores and skills
4. THE staff dashboard SHALL show recent activities: new registrations, job applications, interview schedules
5. THE system SHALL generate downloadable reports in CSV format
6. THE system SHALL display eligibility distribution (students eligible vs not eligible for jobs)

#### Correctness Properties

1. **Property: Placement Percentage**: For all analytics, placement percentage SHALL equal (placed students / total students) × 100
2. **Property: Package Calculation**: For all salary ranges, average package SHALL be within min and max salary bounds
3. **Property: Department Count Consistency**: For all departments, sum of students SHALL equal total student count

### Requirement 10: Notification System

**User Story**: As a user, I want to receive timely notifications about important events so that I stay informed about updates.

#### Acceptance Criteria

1. THE system SHALL generate notifications for: new job postings, application status updates, interview schedules, course enrollments, test submissions
2. EACH notification SHALL include: title, message, timestamp, read status, and target user
3. THE system SHALL display unread notification count on user dashboard
4. WHEN a user views notifications, THE system SHALL mark them as read
5. THE system SHALL retain notifications for 30 days
6. THE system SHALL support filtering notifications by type and read status

#### Correctness Properties

1. **Property: Notification Uniqueness**: For any event, only one notification per user SHALL be created
2. **Property: Timestamp Ordering**: For all notifications, display order SHALL be descending by timestamp
3. **Property: Retention Policy**: For all notifications older than 30 days, system SHALL archive or delete them

### Requirement 11: Performance Analytics Engine

**User Story**: As a student, I want to view detailed analytics of my performance across tests, coding, and courses so that I can identify improvement areas.

#### Acceptance Criteria

1. THE system SHALL track student analytics: test scores, coding submission count, course completion rate, skill proficiency levels
2. THE analytics dashboard SHALL display performance trends with charts (line graphs, bar charts, pie charts)
3. THE system SHALL identify weak areas based on: test topics with low scores, coding problems with multiple failed attempts
4. THE system SHALL calculate overall readiness score (0-100) based on: profile completion, test performance, coding success rate, course progress
5. THE analytics SHALL show company-wise preparation status
6. THE system SHALL provide personalized recommendations based on analytics data

#### Correctness Properties

1. **Property: Readiness Score Bounds**: For all students, readiness score SHALL be between 0 and 100
2. **Property: Trend Data Consistency**: For all performance trends, data points SHALL match actual submission/completion records
3. **Property: Weak Area Detection**: For topics with accuracy < 50%, they SHALL appear in weak areas list

### Requirement 12: Security and Data Protection

**User Story**: As a system administrator, I want robust security measures to protect user data and prevent unauthorized access.

#### Acceptance Criteria

1. THE system SHALL encrypt all passwords using BCrypt with salt rounds >= 10
2. THE system SHALL store JWT secret in environment variables (not hardcoded)
3. THE system SHALL implement CORS with whitelist of allowed origins
4. THE system SHALL validate and sanitize all user inputs to prevent SQL injection and XSS attacks
5. THE system SHALL implement rate limiting on authentication endpoints (5 attempts per 15 minutes)
6. THE system SHALL use HTTPS for all production API communications
7. THE system SHALL log all authentication attempts with IP addresses

#### Correctness Properties

1. **Property: Password Encryption**: For all user records, password field SHALL never contain plaintext
2. **Property: JWT Secret Security**: For all deployments, JWT secret SHALL be loaded from environment variables
3. **Property: Rate Limit Enforcement**: For authentication endpoints, more than 5 failed attempts within 15 minutes SHALL result in temporary lockout

### Requirement 13: RESTful API Design

**User Story**: As a frontend developer, I want well-structured REST APIs with consistent response formats so that I can build a reliable user interface.

#### Acceptance Criteria

1. THE API SHALL follow REST conventions: GET (retrieve), POST (create), PUT (update), DELETE (remove)
2. THE API SHALL use appropriate HTTP status codes: 200 (success), 201 (created), 400 (bad request), 401 (unauthorized), 403 (forbidden), 404 (not found), 500 (server error)
3. ALL error responses SHALL include: error code, message, and timestamp
4. THE API SHALL support pagination for list endpoints with: page number, page size, total count
5. THE API SHALL validate request payloads and return detailed validation errors
6. THE API SHALL use consistent endpoint naming: `/api/{role}/{resource}`
7. THE API SHALL implement global exception handling

#### Correctness Properties

1. **Property: Error Response Structure**: For all error responses, JSON SHALL contain "error", "message", and "timestamp" fields
2. **Property: Pagination Consistency**: For paginated endpoints, total items SHALL equal sum of items across all pages
3. **Property: Status Code Accuracy**: For resource creation, successful responses SHALL return HTTP 201 with resource location

### Requirement 14: Responsive Modern UI

**User Story**: As a user, I want a modern, responsive interface that works seamlessly across devices so that I can access the platform from anywhere.

#### Acceptance Criteria

1. THE UI SHALL be responsive and functional on: desktop (1920px+), tablet (768px-1920px), mobile (320px-768px)
2. THE UI SHALL use modern design principles: glassmorphism effects, smooth transitions, micro-interactions
3. THE UI SHALL implement proper loading states for asynchronous operations
4. THE UI SHALL display user-friendly error messages
5. THE UI SHALL use accessible color contrast ratios (WCAG 2.1 AA compliance)
6. THE UI SHALL implement keyboard navigation support
7. THE UI SHALL use semantic HTML for better accessibility

#### Correctness Properties

1. **Property: Responsive Breakpoints**: For all viewport widths, UI components SHALL not overflow or break layout
2. **Property: Loading State Coverage**: For all API calls, loading indicators SHALL be visible during requests
3. **Property: Color Contrast**: For all text-background pairs, contrast ratio SHALL be >= 4.5:1

### Requirement 15: SEO Optimization

**User Story**: As a marketing team member, I want the platform to be SEO-optimized so that it ranks well in search engines and attracts organic traffic.

#### Acceptance Criteria

1. ALL pages SHALL include proper meta tags: title, description, keywords, og:tags
2. THE landing page SHALL use semantic HTML5 tags: header, nav, main, section, article, footer
3. THE system SHALL implement proper heading hierarchy (h1 > h2 > h3)
4. THE landing page SHALL include descriptive alt text for all images
5. THE system SHALL generate a sitemap.xml file
6. THE page titles SHALL follow pattern: "{Page Name} | Skillora - AI-Powered Placement Readiness"
7. THE content SHALL be keyword-optimized for: "placement preparation", "coding practice", "AI learning", "campus recruitment"

#### Correctness Properties

1. **Property: Unique Page Titles**: For all pages, title tags SHALL be unique and descriptive
2. **Property: Heading Hierarchy**: For all pages, there SHALL be exactly one h1 tag
3. **Property: Alt Text Coverage**: For all images, alt attributes SHALL be non-empty and descriptive

---

## Non-Functional Requirements

### Performance
- API response time: < 500ms for 95th percentile
- Page load time: < 3 seconds on 3G connection
- Code execution: < 5 seconds with timeout enforcement
- AI content generation: < 10 seconds

### Scalability
- Support 10,000 concurrent users
- Handle 100,000 test submissions per day
- Support 50,000 code execution requests per day

### Reliability
- System uptime: 99.9%
- Database backup: Daily automated backups
- Error recovery: Graceful degradation on service failures

### Maintainability
- Code coverage: > 80%
- Documentation: Comprehensive API and code documentation
- Code quality: Follow Clean Code, SOLID principles
- Logging: Structured logging with appropriate log levels

---

**Document Version**: 1.0  
**Last Updated**: 2026-07-03
