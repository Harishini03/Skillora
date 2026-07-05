-- ============= DEPARTMENT TABLE =============
-- Stores department information for academic organization
CREATE TABLE departments
(
    department_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(120) NOT NULL UNIQUE,
    INDEX idx_dept_name (department_name)
);

-- ============= USERS TABLE =============
-- Core authentication and user management
-- Supports three roles: STUDENT, STAFF, RECRUITER
CREATE TABLE users
(
    user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(120) NOT NULL UNIQUE,
    email          VARCHAR(160) NOT NULL UNIQUE,
    name           VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL,
    auth_provider  VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at  DATETIME,
    CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'STAFF', 'RECRUITER')),
    CONSTRAINT chk_auth_provider CHECK (auth_provider IN ('LOCAL', 'GOOGLE', 'GITHUB')),
    INDEX idx_user_email (email),
    INDEX idx_user_username (username),
    INDEX idx_user_role (role),
    INDEX idx_user_active (active)
);

-- ============= STUDENTS TABLE =============
-- Student profiles with academic metrics and placement readiness
-- CGPA validation: 0.0-10.0 scale
CREATE TABLE students
(
    student_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(150)  NOT NULL,
    department_id    BIGINT        NOT NULL,
    user_id          BIGINT        UNIQUE,
    cgpa             DECIMAL(4, 2) NOT NULL,
    dsa_score        DECIMAL(5, 2),
    aptitude_score   DECIMAL(5, 2),
    mock_test_score  DECIMAL(5, 2),
    soft_skill_score DECIMAL(5, 2),
    final_score      DECIMAL(6, 2),
    readiness_score  DECIMAL(5, 2),
    student_rank     INT,
    placement_status VARCHAR(30),
    level            VARCHAR(30),
    interests        VARCHAR(255),
    phone            VARCHAR(20),
    achievements     VARCHAR(1200),
    resume_path      VARCHAR(500),
    CONSTRAINT fk_students_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_students_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_student_cgpa CHECK (cgpa >= 0.0 AND cgpa <= 10.0),
    CONSTRAINT chk_student_dsa_score CHECK (dsa_score IS NULL OR (dsa_score >= 0 AND dsa_score <= 100)),
    CONSTRAINT chk_student_aptitude_score CHECK (aptitude_score IS NULL OR (aptitude_score >= 0 AND aptitude_score <= 100)),
    CONSTRAINT chk_student_mock_score CHECK (mock_test_score IS NULL OR (mock_test_score >= 0 AND mock_test_score <= 100)),
    CONSTRAINT chk_student_soft_skill CHECK (soft_skill_score IS NULL OR (soft_skill_score >= 0 AND soft_skill_score <= 100)),
    CONSTRAINT chk_student_final_score CHECK (final_score IS NULL OR (final_score >= 0 AND final_score <= 100)),
    CONSTRAINT chk_student_readiness CHECK (readiness_score IS NULL OR (readiness_score >= 0 AND readiness_score <= 100)),
    CONSTRAINT chk_student_rank CHECK (student_rank IS NULL OR student_rank > 0),
    CONSTRAINT chk_student_placement_status CHECK (placement_status IS NULL OR placement_status IN ('PLACED', 'NOT_PLACED', 'IN_PROCESS', 'NOT_INTERESTED')),
    INDEX idx_student_user_id (user_id),
    INDEX idx_student_dept_id (department_id),
    INDEX idx_student_cgpa (cgpa),
    INDEX idx_student_placement_status (placement_status),
    INDEX idx_student_readiness (readiness_score DESC),
    INDEX idx_student_composite_search (department_id, cgpa DESC, readiness_score DESC)
);

-- ============= STAFF PROFILES TABLE =============
-- Links staff users to departments
CREATE TABLE staff_profiles
(
    staff_profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT NOT NULL UNIQUE,
    department_id    BIGINT NOT NULL,
    CONSTRAINT fk_staff_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_staff_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_staff_user_id (user_id),
    INDEX idx_staff_dept_id (department_id)
);

-- ============= SKILLS TABLE =============
-- Master list of skills
CREATE TABLE skills
(
    skill_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(120) NOT NULL UNIQUE,
    INDEX idx_skill_name (skill_name)
);

-- ============= STUDENT SKILLS TABLE =============
-- Many-to-many relationship between students and skills with proficiency scores
CREATE TABLE student_skills
(
    student_id  BIGINT NOT NULL,
    skill_id    BIGINT NOT NULL,
    skill_score DECIMAL(5, 2),
    PRIMARY KEY (student_id, skill_id),
    CONSTRAINT fk_student_skills_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_student_skills_skill
        FOREIGN KEY (skill_id) REFERENCES skills (skill_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_student_skill_score CHECK (skill_score IS NULL OR (skill_score >= 0 AND skill_score <= 100)),
    INDEX idx_student_skills_student (student_id),
    INDEX idx_student_skills_skill (skill_id)
);

-- ============= COMPANIES TABLE =============
-- Master list of companies for job postings and eligibility
CREATE TABLE companies
(
    company_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL UNIQUE,
    INDEX idx_company_name (company_name)
);

-- ============= ELIGIBILITY CRITERIA TABLE =============
-- Company-specific eligibility requirements and scoring weights
CREATE TABLE eligibility_criteria
(
    company_id      BIGINT PRIMARY KEY,
    min_cgpa        DECIMAL(4, 2) NOT NULL,
    min_dsa         DECIMAL(5, 2) NOT NULL,
    min_aptitude    DECIMAL(5, 2) NOT NULL,
    weight_cgpa     DECIMAL(5, 2) NOT NULL,
    weight_dsa      DECIMAL(5, 2) NOT NULL,
    weight_aptitude DECIMAL(5, 2) NOT NULL,
    weight_mock     DECIMAL(5, 2) NOT NULL,
    weight_skill    DECIMAL(5, 2) NOT NULL,
    CONSTRAINT fk_eligibility_company
        FOREIGN KEY (company_id) REFERENCES companies (company_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_eligibility_min_cgpa CHECK (min_cgpa >= 0.0 AND min_cgpa <= 10.0),
    CONSTRAINT chk_eligibility_min_dsa CHECK (min_dsa >= 0 AND min_dsa <= 100),
    CONSTRAINT chk_eligibility_min_aptitude CHECK (min_aptitude >= 0 AND min_aptitude <= 100),
    CONSTRAINT chk_eligibility_weights CHECK (
        weight_cgpa >= 0 AND weight_dsa >= 0 AND weight_aptitude >= 0 AND 
        weight_mock >= 0 AND weight_skill >= 0
    )
);

-- ============= QUESTIONS TABLE =============
-- Test questions for aptitude and assessment tests
CREATE TABLE questions
(
    question_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_text    TEXT         NOT NULL,
    option_a         VARCHAR(255) NOT NULL,
    option_b         VARCHAR(255) NOT NULL,
    option_c         VARCHAR(255) NOT NULL,
    option_d         VARCHAR(255) NOT NULL,
    correct_option   CHAR(1)      NOT NULL,
    difficulty_level VARCHAR(30)  NOT NULL,
    topic            VARCHAR(60)  NOT NULL,
    company_id       BIGINT,
    CONSTRAINT fk_questions_company
        FOREIGN KEY (company_id) REFERENCES companies (company_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_question_correct_option CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    CONSTRAINT chk_question_difficulty CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD')),
    INDEX idx_question_difficulty (difficulty_level),
    INDEX idx_question_topic (topic),
    INDEX idx_question_company (company_id),
    INDEX idx_question_composite (difficulty_level, topic)
);

-- ============= TEST SESSIONS TABLE =============
-- Active test sessions with time limits and status tracking
CREATE TABLE test_sessions
(
    session_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id       BIGINT      NOT NULL,
    test_type        VARCHAR(20) NOT NULL,
    start_time       DATETIME    NOT NULL,
    duration_minutes INT         NOT NULL,
    total_questions  INT         NOT NULL,
    status           VARCHAR(20) NOT NULL,
    CONSTRAINT fk_session_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_session_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_session_questions CHECK (total_questions > 0),
    CONSTRAINT chk_session_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED', 'TIMEOUT')),
    CONSTRAINT chk_session_test_type CHECK (test_type IN ('DSA', 'APTITUDE', 'MOCK')),
    INDEX idx_session_student (student_id),
    INDEX idx_session_status (status),
    INDEX idx_session_start_time (start_time DESC),
    INDEX idx_session_composite (student_id, status, start_time DESC)
);

-- ============= STUDENT TEST ATTEMPTS TABLE =============
-- Completed test attempts with scores and timestamps
CREATE TABLE student_test_attempts
(
    attempt_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT      NOT NULL,
    session_id      BIGINT,
    test_type       VARCHAR(20) NOT NULL,
    score           INT         NOT NULL,
    total_questions INT         NOT NULL,
    test_date       DATETIME    NOT NULL,
    CONSTRAINT fk_attempt_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_attempt_session
        FOREIGN KEY (session_id) REFERENCES test_sessions (session_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_attempt_score CHECK (score >= 0),
    CONSTRAINT chk_attempt_questions CHECK (total_questions > 0),
    CONSTRAINT chk_attempt_score_valid CHECK (score <= total_questions),
    CONSTRAINT chk_attempt_test_type CHECK (test_type IN ('DSA', 'APTITUDE', 'MOCK')),
    INDEX idx_attempt_student (student_id),
    INDEX idx_attempt_session (session_id),
    INDEX idx_attempt_test_date (test_date DESC),
    INDEX idx_attempt_composite (student_id, test_type, test_date DESC)
);

-- ============= STUDENT ANSWERS TABLE =============
-- Individual question responses within test attempts
CREATE TABLE student_answers
(
    answer_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id      BIGINT  NOT NULL,
    question_id     BIGINT  NOT NULL,
    selected_option CHAR(1),
    is_correct      BOOLEAN NOT NULL,
    CONSTRAINT fk_answers_attempt
        FOREIGN KEY (attempt_id) REFERENCES student_test_attempts (attempt_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_answers_question
        FOREIGN KEY (question_id) REFERENCES questions (question_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_answer_selected_option CHECK (selected_option IS NULL OR selected_option IN ('A', 'B', 'C', 'D')),
    INDEX idx_answer_attempt (attempt_id),
    INDEX idx_answer_question (question_id),
    INDEX idx_answer_composite (attempt_id, question_id)
);

-- ============= JOB POSTINGS TABLE =============
-- Job opportunities posted by recruiters
CREATE TABLE job_postings
(
    job_posting_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(160)  NOT NULL,
    description        VARCHAR(3000) NOT NULL,
    location           VARCHAR(120)  NOT NULL,
    compensation       VARCHAR(120),
    min_cgpa           DECIMAL(4, 2),
    required_skills    VARCHAR(600),
    job_type           VARCHAR(20)   NOT NULL,
    active             BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         DATETIME      NOT NULL,
    recruiter_user_id  BIGINT        NOT NULL,
    department_id      BIGINT,
    company_id         BIGINT,
    CONSTRAINT fk_job_posting_recruiter
        FOREIGN KEY (recruiter_user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_job_posting_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_job_posting_company
        FOREIGN KEY (company_id) REFERENCES companies (company_id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_job_min_cgpa CHECK (min_cgpa IS NULL OR (min_cgpa >= 0.0 AND min_cgpa <= 10.0)),
    CONSTRAINT chk_job_type CHECK (job_type IN ('FULL_TIME', 'INTERNSHIP', 'CONTRACT', 'PART_TIME')),
    INDEX idx_job_active (active),
    INDEX idx_job_recruiter (recruiter_user_id),
    INDEX idx_job_company (company_id),
    INDEX idx_job_department (department_id),
    INDEX idx_job_created (created_at DESC),
    INDEX idx_job_composite (active, created_at DESC),
    INDEX idx_job_cgpa_filter (active, min_cgpa)
);

-- ============= JOB APPLICATIONS TABLE =============
-- Student applications to job postings with status workflow
CREATE TABLE job_applications
(
    job_application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_posting_id     BIGINT       NOT NULL,
    student_id         BIGINT       NOT NULL,
    status             VARCHAR(30)  NOT NULL,
    applied_at         DATETIME     NOT NULL,
    last_updated_at    DATETIME     NOT NULL,
    recruiter_notes    VARCHAR(1200),
    CONSTRAINT uq_job_application UNIQUE (job_posting_id, student_id),
    CONSTRAINT fk_application_job
        FOREIGN KEY (job_posting_id) REFERENCES job_postings (job_posting_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_application_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_application_status CHECK (status IN ('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'REJECTED', 'ACCEPTED', 'OFFERED', 'WITHDRAWN')),
    INDEX idx_application_job (job_posting_id),
    INDEX idx_application_student (student_id),
    INDEX idx_application_status (status),
    INDEX idx_application_applied (applied_at DESC),
    INDEX idx_application_composite (job_posting_id, status),
    INDEX idx_application_student_status (student_id, status)
);

-- ============= INTERVIEW SCHEDULES TABLE =============
-- Interview scheduling for job applications
CREATE TABLE interview_schedules
(
    interview_schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_application_id    BIGINT      NOT NULL UNIQUE,
    interviewer_user_id   BIGINT      NOT NULL,
    scheduled_at          DATETIME    NOT NULL,
    duration_minutes      INT         NOT NULL,
    mode                  VARCHAR(20) NOT NULL,
    meeting_link          VARCHAR(300),
    status                VARCHAR(20) NOT NULL,
    created_at            DATETIME    NOT NULL,
    CONSTRAINT fk_interview_application
        FOREIGN KEY (job_application_id) REFERENCES job_applications (job_application_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_interview_user
        FOREIGN KEY (interviewer_user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_interview_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_interview_mode CHECK (mode IN ('IN_PERSON', 'ONLINE', 'PHONE', 'VIDEO')),
    CONSTRAINT chk_interview_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED', 'NO_SHOW')),
    INDEX idx_interview_application (job_application_id),
    INDEX idx_interview_interviewer (interviewer_user_id),
    INDEX idx_interview_scheduled (scheduled_at),
    INDEX idx_interview_status (status),
    INDEX idx_interview_composite (interviewer_user_id, scheduled_at),
    INDEX idx_interview_upcoming (status, scheduled_at)
);

-- ============= INTERVIEW FEEDBACK TABLE =============
-- Feedback and ratings from interviews (scores 1-10)
CREATE TABLE interview_feedback
(
    interview_feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_schedule_id BIGINT        NOT NULL UNIQUE,
    technical_score       INT           NOT NULL,
    communication_score   INT           NOT NULL,
    confidence_score      INT           NOT NULL,
    recommendation        VARCHAR(40)   NOT NULL,
    comments              VARCHAR(1500),
    submitted_at          DATETIME      NOT NULL,
    CONSTRAINT fk_feedback_schedule
        FOREIGN KEY (interview_schedule_id) REFERENCES interview_schedules (interview_schedule_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_feedback_technical CHECK (technical_score >= 1 AND technical_score <= 10),
    CONSTRAINT chk_feedback_communication CHECK (communication_score >= 1 AND communication_score <= 10),
    CONSTRAINT chk_feedback_confidence CHECK (confidence_score >= 1 AND confidence_score <= 10),
    CONSTRAINT chk_feedback_recommendation CHECK (recommendation IN ('STRONG_HIRE', 'HIRE', 'MAYBE', 'NO_HIRE', 'STRONG_NO_HIRE')),
    INDEX idx_feedback_schedule (interview_schedule_id),
    INDEX idx_feedback_recommendation (recommendation)
);

-- ============= PORTAL NOTIFICATIONS TABLE =============
-- System notifications for users
CREATE TABLE portal_notifications
(
    portal_notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                BIGINT        NOT NULL,
    notification_type      VARCHAR(40)   NOT NULL,
    message                VARCHAR(1200) NOT NULL,
    is_read                BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at             DATETIME      NOT NULL,
    CONSTRAINT fk_notification_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_notification_type CHECK (notification_type IN ('JOB_POSTED', 'APPLICATION_STATUS', 'INTERVIEW_SCHEDULED', 'TEST_REMINDER', 'COURSE_UPDATE', 'SYSTEM')),
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_created (created_at DESC),
    INDEX idx_notification_composite (user_id, is_read, created_at DESC)
);

-- ============= PROFILES TABLE =============
-- Extended student profile information
CREATE TABLE profiles
(
    profile_id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id                    BIGINT       NOT NULL UNIQUE,
    first_name                    VARCHAR(100),
    last_name                     VARCHAR(100),
    personal_email                VARCHAR(160),
    college_email                 VARCHAR(160),
    mobile_number                 VARCHAR(20),
    alternate_mobile_number       VARCHAR(20),
    whatsapp_number               VARCHAR(20),
    visible_to_hr                 BOOLEAN      NOT NULL DEFAULT TRUE,
    address_line1                 VARCHAR(255),
    address_line2                 VARCHAR(255),
    city                          VARCHAR(120),
    state                         VARCHAR(120),
    pincode                       VARCHAR(12),
    date_of_birth                 DATE,
    gender                        VARCHAR(20),
    father_name                   VARCHAR(150),
    father_contact_number         VARCHAR(20),
    mother_name                   VARCHAR(150),
    mother_contact_number         VARCHAR(20),
    about_me                      VARCHAR(2000),
    profile_image_path            VARCHAR(500),
    profile_image_visible_to_hr   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                    DATETIME     NOT NULL,
    updated_at                    DATETIME     NOT NULL,
    CONSTRAINT fk_profile_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_profile_gender CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY')),
    INDEX idx_profile_student (student_id),
    INDEX idx_profile_visible (visible_to_hr),
    INDEX idx_profile_email (personal_email)
);

-- ============= PROFILE EDUCATIONS TABLE =============
-- Educational qualifications for student profiles
CREATE TABLE profile_educations
(
    education_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id            BIGINT       NOT NULL,
    institution_name      VARCHAR(180) NOT NULL,
    degree                VARCHAR(180) NOT NULL,
    year_of_passing       INT          NOT NULL,
    cgpa_or_percentage    VARCHAR(40)  NOT NULL,
    CONSTRAINT fk_education_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (profile_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_education_year CHECK (year_of_passing >= 1950 AND year_of_passing <= 2100),
    INDEX idx_education_profile (profile_id)
);

-- ============= PROFILE SKILLS TABLE =============
-- Skills listed in student profiles
CREATE TABLE profile_skills
(
    profile_skill_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id       BIGINT       NOT NULL,
    skill_name       VARCHAR(120) NOT NULL,
    skill_category   VARCHAR(30)  NOT NULL,
    skill_level      VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_profile_skill_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (profile_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_profile_skill_category CHECK (skill_category IN ('TECHNICAL', 'SOFT', 'LANGUAGE', 'OTHER')),
    CONSTRAINT chk_profile_skill_level CHECK (skill_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
    INDEX idx_profile_skill_profile (profile_id),
    INDEX idx_profile_skill_category (skill_category)
);

-- ============= PROFILE RESUMES TABLE =============
-- Resume file uploads for student profiles
CREATE TABLE profile_resumes
(
    resume_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id     BIGINT       NOT NULL UNIQUE,
    file_path      VARCHAR(500) NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    content_type   VARCHAR(120) NOT NULL,
    size_bytes     BIGINT       NOT NULL,
    uploaded_at    DATETIME     NOT NULL,
    CONSTRAINT fk_profile_resume_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (profile_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_resume_size CHECK (size_bytes > 0 AND size_bytes <= 10485760), -- Max 10MB
    INDEX idx_resume_profile (profile_id)
);

-- ============= PROFILE ANALYTICS TABLE =============
-- Performance analytics for student profiles
CREATE TABLE profile_analytics
(
    profile_analytics_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id                    BIGINT        NOT NULL UNIQUE,
    test_scores                   DECIMAL(6, 2) NOT NULL DEFAULT 0,
    avg_time_per_question         DECIMAL(8, 2) NOT NULL DEFAULT 0,
    accuracy_percentage           DECIMAL(6, 2) NOT NULL DEFAULT 0,
    strengths                     VARCHAR(1000),
    weaknesses                    VARCHAR(1000),
    insight_summary               VARCHAR(1200),
    recommended_learning_strategy VARCHAR(1200),
    weak_areas                    VARCHAR(1200),
    suggested_topics              VARCHAR(1200),
    updated_at                    DATETIME      NOT NULL,
    CONSTRAINT fk_profile_analytics_profile
        FOREIGN KEY (profile_id) REFERENCES profiles (profile_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_analytics_test_scores CHECK (test_scores >= 0 AND test_scores <= 100),
    CONSTRAINT chk_analytics_avg_time CHECK (avg_time_per_question >= 0),
    CONSTRAINT chk_analytics_accuracy CHECK (accuracy_percentage >= 0 AND accuracy_percentage <= 100),
    INDEX idx_analytics_profile (profile_id)
);

-- ============= BACKWARD COMPATIBILITY MIGRATIONS =============
-- These ALTER statements handle schema evolution and are ignored if columns already exist
-- (spring.sql.init.continue-on-error=true in application.properties)

ALTER TABLE students CHANGE COLUMN rank student_rank INT;
ALTER TABLE users ADD COLUMN email VARCHAR(160) UNIQUE;
ALTER TABLE users ADD COLUMN name VARCHAR(150);
ALTER TABLE users ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN last_login_at DATETIME;
ALTER TABLE students ADD COLUMN soft_skill_score DECIMAL(5, 2);
ALTER TABLE students ADD COLUMN readiness_score DECIMAL(5, 2);
ALTER TABLE students ADD COLUMN interests VARCHAR(255);
ALTER TABLE students ADD COLUMN phone VARCHAR(20);
ALTER TABLE students ADD COLUMN achievements VARCHAR(1200);
ALTER TABLE students ADD COLUMN resume_path VARCHAR(500);
ALTER TABLE profiles ADD COLUMN profile_image_visible_to_hr BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users MODIFY COLUMN email VARCHAR(160) NOT NULL;
ALTER TABLE users MODIFY COLUMN name VARCHAR(150) NOT NULL;
ALTER TABLE users MODIFY COLUMN role VARCHAR(20) NOT NULL;


-- ============= COURSE MANAGEMENT TABLES =============

-- Courses table with categorization and difficulty levels
CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(60) NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_course_creator 
        FOREIGN KEY (created_by_user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_course_difficulty CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    INDEX idx_course_published (is_published),
    INDEX idx_course_creator (created_by_user_id),
    INDEX idx_course_category (category),
    INDEX idx_course_difficulty (difficulty_level),
    INDEX idx_course_composite (is_published, category, difficulty_level)
);

-- Course modules for organizing course content
CREATE TABLE IF NOT EXISTS course_modules (
    module_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    order_index INT NOT NULL,
    CONSTRAINT fk_module_course 
        FOREIGN KEY (course_id) REFERENCES courses(course_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_module_order CHECK (order_index >= 0),
    INDEX idx_module_course (course_id),
    INDEX idx_module_order (course_id, order_index)
);

-- Individual lessons within modules
CREATE TABLE IF NOT EXISTS course_lessons (
    lesson_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content_type VARCHAR(20) NOT NULL,
    content_data TEXT NOT NULL,
    order_index INT NOT NULL,
    duration_minutes INT,
    CONSTRAINT fk_lesson_module 
        FOREIGN KEY (module_id) REFERENCES course_modules(module_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_lesson_order CHECK (order_index >= 0),
    CONSTRAINT chk_lesson_duration CHECK (duration_minutes IS NULL OR duration_minutes > 0),
    CONSTRAINT chk_lesson_content_type CHECK (content_type IN ('VIDEO', 'TEXT', 'QUIZ', 'CODE', 'PDF', 'LINK')),
    INDEX idx_lesson_module (module_id),
    INDEX idx_lesson_order (module_id, order_index)
);

-- Student course enrollments
CREATE TABLE IF NOT EXISTS course_enrollments (
    enrollment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    enrolled_at DATETIME NOT NULL,
    last_accessed_at DATETIME,
    completion_percentage DECIMAL(5,2) NOT NULL DEFAULT 0,
    CONSTRAINT uq_enrollment UNIQUE (course_id, student_id),
    CONSTRAINT fk_enrollment_course 
        FOREIGN KEY (course_id) REFERENCES courses(course_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_enrollment_student 
        FOREIGN KEY (student_id) REFERENCES students(student_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_enrollment_completion CHECK (completion_percentage >= 0 AND completion_percentage <= 100),
    INDEX idx_enrollment_student (student_id),
    INDEX idx_enrollment_course (course_id),
    INDEX idx_enrollment_progress (student_id, completion_percentage),
    INDEX idx_enrollment_access (last_accessed_at DESC)
);

-- Track lesson completion status
CREATE TABLE IF NOT EXISTS lesson_completions (
    completion_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    completed_at DATETIME NOT NULL,
    CONSTRAINT uq_lesson_completion UNIQUE (enrollment_id, lesson_id),
    CONSTRAINT fk_completion_enrollment 
        FOREIGN KEY (enrollment_id) REFERENCES course_enrollments(enrollment_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_completion_lesson 
        FOREIGN KEY (lesson_id) REFERENCES course_lessons(lesson_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_completion_enrollment (enrollment_id),
    INDEX idx_completion_lesson (lesson_id),
    INDEX idx_completion_date (completed_at DESC)
);

-- ============= CODING PLATFORM TABLES =============

-- Coding problems with difficulty levels and topic tags
CREATE TABLE IF NOT EXISTS coding_problems (
    problem_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL,
    topic_tags VARCHAR(300),
    time_limit_seconds INT NOT NULL DEFAULT 5,
    memory_limit_mb INT NOT NULL DEFAULT 256,
    created_by_user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_problem_creator 
        FOREIGN KEY (created_by_user_id) REFERENCES users(user_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_problem_difficulty CHECK (difficulty_level IN ('EASY', 'MEDIUM', 'HARD')),
    CONSTRAINT chk_problem_time_limit CHECK (time_limit_seconds > 0 AND time_limit_seconds <= 60),
    CONSTRAINT chk_problem_memory_limit CHECK (memory_limit_mb > 0 AND memory_limit_mb <= 1024),
    INDEX idx_problem_difficulty (difficulty_level),
    INDEX idx_problem_creator (created_by_user_id),
    INDEX idx_problem_created (created_at DESC),
    INDEX idx_problem_composite (difficulty_level, created_at DESC)
);

-- Test cases for validating code submissions
CREATE TABLE IF NOT EXISTS problem_test_cases (
    test_case_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    input_data TEXT NOT NULL,
    expected_output TEXT NOT NULL,
    is_sample BOOLEAN NOT NULL DEFAULT FALSE,
    order_index INT NOT NULL,
    CONSTRAINT fk_testcase_problem 
        FOREIGN KEY (problem_id) REFERENCES coding_problems(problem_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_testcase_order CHECK (order_index >= 0),
    INDEX idx_testcase_problem (problem_id),
    INDEX idx_testcase_sample (problem_id, is_sample),
    INDEX idx_testcase_order (problem_id, order_index)
);

-- Student code submissions with execution metrics
CREATE TABLE IF NOT EXISTS code_submissions (
    submission_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    language VARCHAR(20) NOT NULL,
    code_content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_time_ms INT,
    memory_used_mb DECIMAL(8,2),
    test_cases_passed INT NOT NULL,
    test_cases_total INT NOT NULL,
    submitted_at DATETIME NOT NULL,
    CONSTRAINT fk_submission_problem 
        FOREIGN KEY (problem_id) REFERENCES coding_problems(problem_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_submission_student 
        FOREIGN KEY (student_id) REFERENCES students(student_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_submission_language CHECK (language IN ('JAVA', 'PYTHON', 'CPP', 'C', 'JAVASCRIPT', 'GO', 'RUST')),
    CONSTRAINT chk_submission_status CHECK (status IN ('ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT_EXCEEDED', 'MEMORY_LIMIT_EXCEEDED', 'RUNTIME_ERROR', 'COMPILE_ERROR', 'PENDING')),
    CONSTRAINT chk_submission_passed CHECK (test_cases_passed >= 0 AND test_cases_passed <= test_cases_total),
    CONSTRAINT chk_submission_execution_time CHECK (execution_time_ms IS NULL OR execution_time_ms >= 0),
    CONSTRAINT chk_submission_memory CHECK (memory_used_mb IS NULL OR memory_used_mb >= 0),
    INDEX idx_submission_student (student_id),
    INDEX idx_submission_problem (problem_id),
    INDEX idx_submission_status (status),
    INDEX idx_submission_submitted (submitted_at DESC),
    INDEX idx_submission_composite (student_id, problem_id, submitted_at DESC),
    INDEX idx_submission_problem_status (problem_id, status)
);

-- Detailed test case results for each submission
CREATE TABLE IF NOT EXISTS submission_test_results (
    result_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    test_case_id BIGINT NOT NULL,
    passed BOOLEAN NOT NULL,
    actual_output TEXT,
    error_message TEXT,
    execution_time_ms INT,
    CONSTRAINT fk_result_submission 
        FOREIGN KEY (submission_id) REFERENCES code_submissions(submission_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_result_testcase 
        FOREIGN KEY (test_case_id) REFERENCES problem_test_cases(test_case_id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_result_execution_time CHECK (execution_time_ms IS NULL OR execution_time_ms >= 0),
    INDEX idx_result_submission (submission_id),
    INDEX idx_result_testcase (test_case_id),
    INDEX idx_result_passed (passed)
);
