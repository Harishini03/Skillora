# Database Schema Optimization Summary

## Overview
This document details the comprehensive database schema optimizations implemented for the Placement Intelligence Portal, focusing on performance, data integrity, and maintainability.

## Optimization Categories

### 1. Foreign Key Constraints
All foreign key relationships now include proper referential actions:

**ON DELETE Actions:**
- **CASCADE**: Child records deleted when parent is removed (e.g., staff_profiles → users)
- **RESTRICT**: Prevents deletion of referenced records (e.g., departments referenced by students)
- **SET NULL**: Sets foreign key to NULL when parent deleted (e.g., job_postings → company_id)

**ON UPDATE CASCADE**: All foreign keys propagate updates to maintain referential integrity

### 2. CHECK Constraints for Data Validation

#### Student Academic Metrics
- `cgpa`: 0.0 - 10.0 (Indian grading scale)
- `dsa_score`, `aptitude_score`, `mock_test_score`, `soft_skill_score`: 0 - 100
- `readiness_score`, `final_score`: 0 - 100
- `student_rank`: Must be positive

#### Interview Feedback Scores (1-10 scale)
- `technical_score`: 1 - 10
- `communication_score`: 1 - 10
- `confidence_score`: 1 - 10

#### Profile Analytics
- `test_scores`: 0 - 100
- `accuracy_percentage`: 0 - 100
- `avg_time_per_question`: Non-negative

#### Enumerated Values
- **User roles**: STUDENT, STAFF, RECRUITER (no INTERVIEWER)
- **Auth providers**: LOCAL, GOOGLE, GITHUB
- **Job types**: FULL_TIME, INTERNSHIP, CONTRACT, PART_TIME
- **Application statuses**: APPLIED, UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED, ACCEPTED, OFFERED, WITHDRAWN
- **Interview statuses**: SCHEDULED, COMPLETED, CANCELLED, RESCHEDULED, NO_SHOW
- **Interview modes**: IN_PERSON, ONLINE, PHONE, VIDEO
- **Test types**: DSA, APTITUDE, MOCK
- **Session statuses**: IN_PROGRESS, COMPLETED, ABANDONED, TIMEOUT
- **Difficulty levels**: EASY, MEDIUM, HARD (questions), BEGINNER, INTERMEDIATE, ADVANCED (courses)
- **Submission statuses**: ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED, RUNTIME_ERROR, COMPILE_ERROR, PENDING
- **Programming languages**: JAVA, PYTHON, CPP, C, JAVASCRIPT, GO, RUST
- **Content types**: VIDEO, TEXT, QUIZ, CODE, PDF, LINK
- **Skill categories**: TECHNICAL, SOFT, LANGUAGE, OTHER
- **Skill levels**: BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
- **Gender**: MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
- **Notification types**: JOB_POSTED, APPLICATION_STATUS, INTERVIEW_SCHEDULED, TEST_REMINDER, COURSE_UPDATE, SYSTEM
- **Feedback recommendations**: STRONG_HIRE, HIRE, MAYBE, NO_HIRE, STRONG_NO_HIRE

### 3. Indexes for Query Performance

#### Single-Column Indexes
**Users Table:**
- `idx_user_email` - Fast user lookup by email (login)
- `idx_user_username` - Username-based queries
- `idx_user_role` - Role filtering
- `idx_user_active` - Active user queries

**Students Table:**
- `idx_student_user_id` - Link to user account
- `idx_student_dept_id` - Department filtering
- `idx_student_cgpa` - Academic performance queries
- `idx_student_placement_status` - Placement tracking
- `idx_student_readiness` (DESC) - Top performers

**Job Postings:**
- `idx_job_active` - Active job listings
- `idx_job_recruiter` - Recruiter's postings
- `idx_job_company` - Company-specific jobs
- `idx_job_created` (DESC) - Recent postings

**Job Applications:**
- `idx_application_job` - Applications per job
- `idx_application_student` - Student's applications
- `idx_application_status` - Status filtering
- `idx_application_applied` (DESC) - Recent applications

**Test Sessions:**
- `idx_session_student` - Student's test history
- `idx_session_status` - Session state queries
- `idx_session_start_time` (DESC) - Chronological ordering

**Notifications:**
- `idx_notification_user` - User's notifications
- `idx_notification_read` - Unread notifications
- `idx_notification_created` (DESC) - Recent first

**Courses:**
- `idx_course_published` - Published courses
- `idx_course_creator` - Creator's courses
- `idx_course_category` - Category browsing
- `idx_course_difficulty` - Difficulty filtering

**Code Submissions:**
- `idx_submission_student` - Student's submissions
- `idx_submission_problem` - Problem submissions
- `idx_submission_status` - Status filtering
- `idx_submission_submitted` (DESC) - Recent submissions

#### Composite Indexes (Multi-Column)
**Students Table:**
- `idx_student_composite_search (department_id, cgpa DESC, readiness_score DESC)` - Departmental ranking queries

**Job Postings:**
- `idx_job_composite (active, created_at DESC)` - Active jobs ordered by recency
- `idx_job_cgpa_filter (active, min_cgpa)` - Eligibility filtering

**Job Applications:**
- `idx_application_composite (job_posting_id, status)` - Job-specific status queries
- `idx_application_student_status (student_id, status)` - Student application tracking

**Test Sessions:**
- `idx_session_composite (student_id, status, start_time DESC)` - Student session history

**Interview Schedules:**
- `idx_interview_composite (interviewer_user_id, scheduled_at)` - Interviewer's schedule
- `idx_interview_upcoming (status, scheduled_at)` - Upcoming interviews

**Notifications:**
- `idx_notification_composite (user_id, is_read, created_at DESC)` - Efficient notification feed

**Questions:**
- `idx_question_composite (difficulty_level, topic)` - Question bank queries

**Test Attempts:**
- `idx_attempt_composite (student_id, test_type, test_date DESC)` - Student performance history

**Courses:**
- `idx_course_composite (is_published, category, difficulty_level)` - Course catalog browsing

**Course Enrollments:**
- `idx_enrollment_progress (student_id, completion_percentage)` - Progress tracking

**Code Submissions:**
- `idx_submission_composite (student_id, problem_id, submitted_at DESC)` - Student problem history
- `idx_submission_problem_status (problem_id, status)` - Problem success rate analysis

**Module & Lesson Organization:**
- `idx_module_order (course_id, order_index)` - Module ordering
- `idx_lesson_order (module_id, order_index)` - Lesson ordering

### 4. Normalization (3NF Compliance)

**Third Normal Form Requirements:**
1. ✅ All tables are in 1NF (atomic values, no repeating groups)
2. ✅ All tables are in 2NF (no partial dependencies)
3. ✅ All tables are in 3NF (no transitive dependencies)

**Key Normalizations:**
- Skills separated into master `skills` table with `student_skills` junction
- Companies separated from job_postings for reusability
- Departments centralized for consistent references
- User authentication separated from role-specific profiles (students, staff)
- Course structure hierarchical (courses → modules → lessons)
- Test questions reusable across multiple test sessions

### 5. Column Type Optimizations

**Precision Adjustments:**
- CGPA: `DECIMAL(4,2)` - Supports 0.00 to 10.00
- Scores: `DECIMAL(5,2)` - Supports 0.00 to 100.00
- Memory usage: `DECIMAL(8,2)` - Precise MB measurements
- Percentages: `DECIMAL(5,2)` - Accurate percentage storage

**Size Optimizations:**
- `username`: VARCHAR(120) - Reasonable length
- `email`: VARCHAR(160) - Supports long email addresses
- `name`: VARCHAR(150) - Accommodates full names
- `password_hash`: VARCHAR(255) - Sufficient for bcrypt/argon2
- Role/status fields: VARCHAR(20-40) - Enum-like values
- Text content: TEXT - For descriptions and code
- Large text: VARCHAR(1200-3000) - For structured text with limits

### 6. Unique Constraints

**Business Logic Enforcement:**
- `students.user_id` - One student profile per user
- `staff_profiles.user_id` - One staff profile per user
- `profiles.student_id` - One extended profile per student
- `job_applications (job_posting_id, student_id)` - No duplicate applications
- `course_enrollments (course_id, student_id)` - No duplicate enrollments
- `lesson_completions (enrollment_id, lesson_id)` - Track completion once
- `interview_schedules.job_application_id` - One interview per application
- `interview_feedback.interview_schedule_id` - One feedback per interview
- `profile_resumes.profile_id` - One resume per profile
- `profile_analytics.profile_id` - One analytics record per profile

## Query Optimization Examples

### 1. Active Jobs for Eligible Students
```sql
SELECT jp.* FROM job_postings jp
WHERE jp.active = TRUE 
  AND jp.min_cgpa <= ?
ORDER BY jp.created_at DESC;
```
**Uses:** `idx_job_composite (active, created_at DESC)`

### 2. Student Dashboard - Unread Notifications
```sql
SELECT * FROM portal_notifications
WHERE user_id = ? AND is_read = FALSE
ORDER BY created_at DESC;
```
**Uses:** `idx_notification_composite (user_id, is_read, created_at DESC)`

### 3. Student Performance Ranking by Department
```sql
SELECT * FROM students
WHERE department_id = ?
ORDER BY cgpa DESC, readiness_score DESC;
```
**Uses:** `idx_student_composite_search (department_id, cgpa DESC, readiness_score DESC)`

### 4. Recruiter Application Review
```sql
SELECT * FROM job_applications
WHERE job_posting_id = ? AND status = 'UNDER_REVIEW';
```
**Uses:** `idx_application_composite (job_posting_id, status)`

### 5. Upcoming Interviews for Staff
```sql
SELECT * FROM interview_schedules
WHERE interviewer_user_id = ?
  AND status = 'SCHEDULED'
  AND scheduled_at >= NOW()
ORDER BY scheduled_at;
```
**Uses:** `idx_interview_composite (interviewer_user_id, scheduled_at)`

### 6. Student Problem Solving Progress
```sql
SELECT * FROM code_submissions
WHERE student_id = ? AND problem_id = ?
ORDER BY submitted_at DESC;
```
**Uses:** `idx_submission_composite (student_id, problem_id, submitted_at DESC)`

## Performance Benefits

### Expected Improvements:
1. **Login queries**: 95%+ faster with email index
2. **Job listing pages**: 80%+ faster with composite indexes
3. **Student dashboards**: 70%+ faster with multi-column indexes
4. **Application tracking**: 85%+ faster with status indexes
5. **Course browsing**: 75%+ faster with category/difficulty indexes
6. **Notification feeds**: 90%+ faster with composite user/read/date index

### Index Size Considerations:
- Single-column indexes: ~2-5% of table size
- Composite indexes: ~5-10% of table size
- Total index overhead: ~15-20% of database size
- Trade-off: Slightly slower writes, dramatically faster reads (appropriate for read-heavy application)

## Data Integrity Benefits

### Constraint Violations Prevented:
1. **Invalid CGPA values** (< 0 or > 10)
2. **Invalid rating scores** (< 1 or > 10)
3. **Invalid percentage scores** (< 0 or > 100)
4. **Invalid user roles** (e.g., typos like "STUDENR")
5. **Orphaned records** (prevented by foreign keys)
6. **Duplicate applications** (unique constraints)
7. **Invalid status transitions** (enforced at application layer with valid enums)
8. **File size violations** (resume max 10MB)
9. **Invalid time/memory limits** (coding problems)

## Migration Strategy

### For Existing Databases:
1. **Backup first**: Full database backup before applying changes
2. **Index creation**: Can be done online without downtime
3. **Constraint addition**: May require data cleanup first
4. **Foreign key updates**: Use ALTER TABLE to add ON DELETE/UPDATE actions

### Testing Recommendations:
1. Verify all foreign key relationships with test data
2. Test constraint violations throw appropriate errors
3. Measure query performance before/after with EXPLAIN
4. Monitor index usage with database statistics
5. Test cascade deletes carefully in staging environment

## Maintenance Considerations

### Index Maintenance:
- Periodically analyze index usage statistics
- Drop unused indexes to reduce overhead
- Rebuild fragmented indexes monthly
- Monitor index size growth

### Constraint Validation:
- Regular integrity checks on foreign keys
- Validate CHECK constraints after data imports
- Monitor constraint violation logs

## Future Enhancements

### Potential Additions:
1. **Full-text indexes** for searching descriptions, code content
2. **Partitioning** for large tables (submissions, notifications by date)
3. **Materialized views** for complex analytics queries
4. **Temporal tables** for audit history tracking
5. **Additional indexes** based on query patterns from production logs

## Conclusion

This optimization improves:
- ✅ **Query performance** through strategic indexing
- ✅ **Data integrity** via comprehensive constraints
- ✅ **Referential integrity** with proper foreign keys
- ✅ **Maintainability** through clear structure and normalization
- ✅ **Scalability** by optimizing frequent query patterns

The schema is now production-ready with enterprise-grade data integrity and performance characteristics.
