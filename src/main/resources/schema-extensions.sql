-- ====================================================================
-- SKILLORA - Course Management & Coding Platform Schema Extensions
-- Production-Ready Database Schema
-- ====================================================================

-- Course Management Tables
CREATE TABLE IF NOT EXISTS courses
(
    course_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200)  NOT NULL,
    description         TEXT          NOT NULL,
    category            VARCHAR(60)   NOT NULL,
    difficulty_level    VARCHAR(20)   NOT NULL,
    created_by_user_id  BIGINT        NOT NULL,
    is_published        BOOLEAN       NOT NULL DEFAULT FALSE,
    estimated_hours     INT,
    thumbnail_url       VARCHAR(500),
    created_at          DATETIME      NOT NULL,
    updated_at          DATETIME      NOT NULL,
    CONSTRAINT fk_course_creator
        FOREIGN KEY (created_by_user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS course_modules
(
    module_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id     BIGINT       NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    order_index   INT          NOT NULL,
    CONSTRAINT fk_module_course
        FOREIGN KEY (course_id) REFERENCES courses (course_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_lessons
(
    lesson_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_id        BIGINT       NOT NULL,
    title            VARCHAR(200) NOT NULL,
    content_type     VARCHAR(20)  NOT NULL, -- VIDEO, TEXT, QUIZ, CODE, EXERCISE
    content_data     TEXT         NOT NULL, -- JSON structure based on type
    order_index      INT          NOT NULL,
    duration_minutes INT,
    is_mandatory     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_lesson_module
        FOREIGN KEY (module_id) REFERENCES course_modules (module_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_enrollments
(
    enrollment_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id             BIGINT        NOT NULL,
    student_id            BIGINT        NOT NULL,
    enrolled_at           DATETIME      NOT NULL,
    last_accessed_at      DATETIME,
    completion_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0,
    certificate_issued    BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_enrollment UNIQUE (course_id, student_id),
    CONSTRAINT fk_enrollment_course
        FOREIGN KEY (course_id) REFERENCES courses (course_id),
    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
);

CREATE TABLE IF NOT EXISTS lesson_completions
(
    completion_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT   NOT NULL,
    lesson_id     BIGINT   NOT NULL,
    completed_at  DATETIME NOT NULL,
    time_spent_minutes INT,
    CONSTRAINT uq_lesson_completion UNIQUE (enrollment_id, lesson_id),
    CONSTRAINT fk_completion_enrollment
        FOREIGN KEY (enrollment_id) REFERENCES course_enrollments (enrollment_id) ON DELETE CASCADE,
    CONSTRAINT fk_completion_lesson
        FOREIGN KEY (lesson_id) REFERENCES course_lessons (lesson_id) ON DELETE CASCADE
);

-- Coding Platform Tables
CREATE TABLE IF NOT EXISTS coding_problems
(
    problem_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200) NOT NULL,
    description         TEXT         NOT NULL,
    difficulty_level    VARCHAR(20)  NOT NULL, -- EASY, MEDIUM, HARD
    topic_tags          VARCHAR(300),
    time_limit_seconds  INT          NOT NULL DEFAULT 5,
    memory_limit_mb     INT          NOT NULL DEFAULT 256,
    created_by_user_id  BIGINT       NOT NULL,
    acceptance_rate     DECIMAL(5, 2),
    total_submissions   INT          NOT NULL DEFAULT 0,
    successful_submissions INT       NOT NULL DEFAULT 0,
    created_at          DATETIME     NOT NULL,
    CONSTRAINT fk_problem_creator
        FOREIGN KEY (created_by_user_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS problem_test_cases
(
    test_case_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id      BIGINT  NOT NULL,
    input_data      TEXT    NOT NULL,
    expected_output TEXT    NOT NULL,
    is_sample       BOOLEAN NOT NULL DEFAULT FALSE,
    is_hidden       BOOLEAN NOT NULL DEFAULT FALSE,
    order_index     INT     NOT NULL,
    explanation     TEXT,
    CONSTRAINT fk_testcase_problem
        FOREIGN KEY (problem_id) REFERENCES coding_problems (problem_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS code_submissions
(
    submission_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id          BIGINT       NOT NULL,
    student_id          BIGINT       NOT NULL,
    language            VARCHAR(20)  NOT NULL, -- JAVA, PYTHON, JAVASCRIPT
    code_content        TEXT         NOT NULL,
    status              VARCHAR(30)  NOT NULL, -- ACCEPTED, WRONG_ANSWER, TIME_LIMIT, RUNTIME_ERROR, COMPILE_ERROR
    execution_time_ms   INT,
    memory_used_kb      INT,
    test_cases_passed   INT          NOT NULL DEFAULT 0,
    test_cases_total    INT          NOT NULL,
    error_message       TEXT,
    submitted_at        DATETIME     NOT NULL,
    CONSTRAINT fk_submission_problem
        FOREIGN KEY (problem_id) REFERENCES coding_problems (problem_id),
    CONSTRAINT fk_submission_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
);

CREATE TABLE IF NOT EXISTS submission_test_results
(
    result_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id      BIGINT  NOT NULL,
    test_case_id       BIGINT  NOT NULL,
    passed             BOOLEAN NOT NULL,
    actual_output      TEXT,
    error_message      TEXT,
    execution_time_ms  INT,
    CONSTRAINT fk_result_submission
        FOREIGN KEY (submission_id) REFERENCES code_submissions (submission_id) ON DELETE CASCADE,
    CONSTRAINT fk_result_testcase
        FOREIGN KEY (test_case_id) REFERENCES problem_test_cases (test_case_id) ON DELETE CASCADE
);

-- AI Learning History (Track AI interactions for personalized learning)
CREATE TABLE IF NOT EXISTS ai_learning_sessions
(
    session_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id           BIGINT       NOT NULL,
    mode                 VARCHAR(20)  NOT NULL, -- LEARN, PRACTICE, ADAPTIVE, REVISION, MOCK_TEST
    topic                VARCHAR(100) NOT NULL,
    subtopic             VARCHAR(100),
    questions_generated  INT,
    questions_answered   INT,
    correct_answers      INT,
    accuracy             DECIMAL(5, 2),
    time_spent_minutes   INT,
    created_at           DATETIME     NOT NULL,
    CONSTRAINT fk_ai_session_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
);

CREATE TABLE IF NOT EXISTS ai_generated_questions
(
    generated_question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id            BIGINT   NOT NULL,
    question_text         TEXT     NOT NULL,
    student_answer        VARCHAR(1),
    correct_answer        VARCHAR(1),
    is_correct            BOOLEAN,
    time_taken_seconds    INT,
    CONSTRAINT fk_generated_question_session
        FOREIGN KEY (session_id) REFERENCES ai_learning_sessions (session_id) ON DELETE CASCADE
);

-- Performance tracking for adaptive learning
CREATE TABLE IF NOT EXISTS student_topic_performance
(
    performance_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id           BIGINT        NOT NULL,
    topic                VARCHAR(100)  NOT NULL,
    subtopic             VARCHAR(100),
    attempts             INT           NOT NULL DEFAULT 0,
    correct_count        INT           NOT NULL DEFAULT 0,
    accuracy             DECIMAL(5, 2) NOT NULL DEFAULT 0,
    avg_time_seconds     DECIMAL(8, 2),
    difficulty_level     VARCHAR(20),
    last_practiced_at    DATETIME,
    mastery_level        VARCHAR(20), -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    CONSTRAINT uq_student_topic UNIQUE (student_id, topic, subtopic),
    CONSTRAINT fk_topic_performance_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
);

-- Study Plan Management
CREATE TABLE IF NOT EXISTS study_plans
(
    plan_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id           BIGINT       NOT NULL,
    title                VARCHAR(200) NOT NULL,
    description          TEXT,
    start_date           DATE         NOT NULL,
    target_date          DATE         NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, PAUSED
    created_at           DATETIME     NOT NULL,
    CONSTRAINT fk_study_plan_student
        FOREIGN KEY (student_id) REFERENCES students (student_id)
);

CREATE TABLE IF NOT EXISTS study_plan_tasks
(
    task_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id         BIGINT       NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    task_type       VARCHAR(30)  NOT NULL, -- COURSE, PRACTICE, TEST, CODING, REVISION
    reference_id    BIGINT, -- course_id, problem_id, etc.
    scheduled_date  DATE         NOT NULL,
    completed_date  DATE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    order_index     INT          NOT NULL,
    CONSTRAINT fk_plan_task
        FOREIGN KEY (plan_id) REFERENCES study_plans (plan_id) ON DELETE CASCADE
);

-- Indexes for Performance Optimization
CREATE INDEX idx_course_published ON courses (is_published);
CREATE INDEX idx_enrollment_student ON course_enrollments (student_id);
CREATE INDEX idx_enrollment_course ON course_enrollments (course_id);
CREATE INDEX idx_lesson_module ON course_lessons (module_id);
CREATE INDEX idx_completion_enrollment ON lesson_completions (enrollment_id);
CREATE INDEX idx_submission_student ON code_submissions (student_id, submitted_at DESC);
CREATE INDEX idx_submission_problem ON code_submissions (problem_id, status);
CREATE INDEX idx_testcase_problem ON problem_test_cases (problem_id, is_sample);
CREATE INDEX idx_ai_session_student ON ai_learning_sessions (student_id, created_at DESC);
CREATE INDEX idx_topic_performance_student ON student_topic_performance (student_id, accuracy);
CREATE INDEX idx_study_plan_student ON study_plans (student_id, status);

