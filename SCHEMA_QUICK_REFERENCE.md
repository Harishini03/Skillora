# Database Schema Quick Reference

## Key Constraints Summary

### CGPA & Score Validations
```sql
-- Student CGPA: 0.0 to 10.0
cgpa >= 0.0 AND cgpa <= 10.0

-- Percentage scores: 0 to 100
dsa_score, aptitude_score, mock_test_score, soft_skill_score, readiness_score, final_score
BETWEEN 0 AND 100

-- Interview ratings: 1 to 10
technical_score, communication_score, confidence_score
BETWEEN 1 AND 10

-- Resume file size: Max 10MB
size_bytes > 0 AND size_bytes <= 10485760
```

### User Roles (No INTERVIEWER role)
```sql
CHECK (role IN ('STUDENT', 'STAFF', 'RECRUITER'))
```

### Job Application Workflow
```sql
status IN (
  'APPLIED',
  'UNDER_REVIEW',
  'SHORTLISTED',
  'INTERVIEW_SCHEDULED',
  'REJECTED',
  'ACCEPTED',
  'OFFERED',
  'WITHDRAWN'
)
```

### Interview Status Flow
```sql
status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED', 'NO_SHOW')
mode IN ('IN_PERSON', 'ONLINE', 'PHONE', 'VIDEO')
```

### Test Types & Session Status
```sql
test_type IN ('DSA', 'APTITUDE', 'MOCK')
status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED', 'TIMEOUT')
```

### Coding Submission Status
```sql
status IN (
  'ACCEPTED',
  'WRONG_ANSWER',
  'TIME_LIMIT_EXCEEDED',
  'MEMORY_LIMIT_EXCEEDED',
  'RUNTIME_ERROR',
  'COMPILE_ERROR',
  'PENDING'
)
language IN ('JAVA', 'PYTHON', 'CPP', 'C', 'JAVASCRIPT', 'GO', 'RUST')
```

## Critical Indexes for Performance

### Most Frequently Used Indexes

#### User Authentication & Authorization
- `idx_user_email` - Login queries
- `idx_user_username` - Username lookups
- `idx_user_role` - Role-based access

#### Student Queries
- `idx_student_user_id` - User to student mapping
- `idx_student_composite_search (department_id, cgpa DESC, readiness_score DESC)` - Rankings

#### Job & Application Tracking
- `idx_job_composite (active, created_at DESC)` - Active job listings
- `idx_application_composite (job_posting_id, status)` - Application management
- `idx_application_student_status (student_id, status)` - Student application tracking

#### Notifications
- `idx_notification_composite (user_id, is_read, created_at DESC)` - Notification feed

#### Courses & Learning
- `idx_course_composite (is_published, category, difficulty_level)` - Course catalog
- `idx_enrollment_progress (student_id, completion_percentage)` - Progress tracking

#### Coding Platform
- `idx_submission_composite (student_id, problem_id, submitted_at DESC)` - Submission history
- `idx_submission_problem_status (problem_id, status)` - Problem statistics

## Foreign Key Cascade Rules

### CASCADE on DELETE (Child deleted with parent)
- `student_skills` → `students`
- `student_skills` → `skills`
- `staff_profiles` → `users`
- `job_applications` → `job_postings`
- `job_applications` → `students`
- `interview_schedules` → `job_applications`
- `interview_feedback` → `interview_schedules`
- `portal_notifications` → `users`
- `profiles` → `students`
- All profile-related tables → `profiles`
- `course_modules` → `courses`
- `course_lessons` → `course_modules`
- `course_enrollments` → `courses`, `students`
- `lesson_completions` → `course_enrollments`, `course_lessons`
- `code_submissions` → `students`
- All test-related tables cascade appropriately

### RESTRICT on DELETE (Prevents deletion if referenced)
- `students` → `departments`
- `staff_profiles` → `departments`
- `job_postings` → `users` (recruiter)
- `interview_schedules` → `users` (interviewer)
- `courses` → `users` (creator)
- `coding_problems` → `users` (creator)

### SET NULL on DELETE (Allows deletion, nullifies reference)
- `students` → `users`
- `job_postings` → `departments`
- `job_postings` → `companies`
- `test_sessions` → optional references

## Unique Constraints (Business Rules)

```sql
-- One profile per entity
students.user_id
staff_profiles.user_id
profiles.student_id
profile_resumes.profile_id
profile_analytics.profile_id

-- No duplicates in relationships
(job_posting_id, student_id) in job_applications
(course_id, student_id) in course_enrollments
(enrollment_id, lesson_id) in lesson_completions

-- One-to-one relationships
interview_schedules.job_application_id
interview_feedback.interview_schedule_id
```

## Common Query Patterns

### 1. Find Eligible Students for Job
```sql
SELECT s.* FROM students s
INNER JOIN job_postings jp ON jp.job_posting_id = ?
WHERE s.cgpa >= jp.min_cgpa
  AND s.department_id = jp.department_id
  AND s.placement_status = 'NOT_PLACED'
ORDER BY s.readiness_score DESC;
```

### 2. Student's Application Status
```sql
SELECT ja.*, jp.title, jp.company_id 
FROM job_applications ja
INNER JOIN job_postings jp ON ja.job_posting_id = jp.job_posting_id
WHERE ja.student_id = ?
ORDER BY ja.applied_at DESC;
```

### 3. Unread Notifications
```sql
SELECT * FROM portal_notifications
WHERE user_id = ? AND is_read = FALSE
ORDER BY created_at DESC
LIMIT 10;
```

### 4. Student Course Progress
```sql
SELECT c.*, ce.completion_percentage, ce.last_accessed_at
FROM course_enrollments ce
INNER JOIN courses c ON ce.course_id = c.course_id
WHERE ce.student_id = ?
ORDER BY ce.last_accessed_at DESC;
```

### 5. Coding Problem Success Rate
```sql
SELECT 
  COUNT(*) as total_submissions,
  SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) as accepted,
  ROUND(SUM(CASE WHEN status = 'ACCEPTED' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as success_rate
FROM code_submissions
WHERE problem_id = ?;
```

### 6. Interview Schedule for Interviewer
```sql
SELECT 
  isch.*,
  ja.student_id,
  s.name as student_name,
  jp.title as job_title
FROM interview_schedules isch
INNER JOIN job_applications ja ON isch.job_application_id = ja.job_application_id
INNER JOIN students s ON ja.student_id = s.student_id
INNER JOIN job_postings jp ON ja.job_posting_id = jp.job_posting_id
WHERE isch.interviewer_user_id = ?
  AND isch.status = 'SCHEDULED'
  AND isch.scheduled_at >= NOW()
ORDER BY isch.scheduled_at;
```

## Index Maintenance Commands

### Check Index Usage (MySQL/H2)
```sql
-- View all indexes on a table
SHOW INDEX FROM students;

-- Analyze table for optimization
ANALYZE TABLE students;
```

### Performance Analysis
```sql
-- Explain query execution plan
EXPLAIN SELECT * FROM students 
WHERE department_id = 1 
ORDER BY cgpa DESC, readiness_score DESC;
```

## Data Validation Examples

### Valid Inserts
```sql
-- Student with valid CGPA
INSERT INTO students (name, department_id, cgpa) 
VALUES ('John Doe', 1, 8.75); -- ✅ Valid

-- Interview feedback with valid scores
INSERT INTO interview_feedback (interview_schedule_id, technical_score, communication_score, confidence_score, recommendation)
VALUES (1, 8, 9, 7, 'HIRE'); -- ✅ Valid

-- Job application with valid status
INSERT INTO job_applications (job_posting_id, student_id, status, applied_at, last_updated_at)
VALUES (1, 1, 'APPLIED', NOW(), NOW()); -- ✅ Valid
```

### Invalid Inserts (Will Fail)
```sql
-- CGPA out of range
INSERT INTO students (name, department_id, cgpa) 
VALUES ('Jane Doe', 1, 11.5); -- ❌ Fails: cgpa > 10

-- Invalid interview score
INSERT INTO interview_feedback (interview_schedule_id, technical_score, communication_score, confidence_score, recommendation)
VALUES (1, 15, 9, 7, 'HIRE'); -- ❌ Fails: technical_score > 10

-- Invalid job application status
INSERT INTO job_applications (job_posting_id, student_id, status, applied_at, last_updated_at)
VALUES (1, 1, 'PENDING', NOW(), NOW()); -- ❌ Fails: invalid status

-- Invalid user role
INSERT INTO users (username, email, name, password_hash, role)
VALUES ('jdoe', 'jdoe@example.com', 'John Doe', 'hash', 'INTERVIEWER'); -- ❌ Fails: invalid role
```

## Backup & Recovery

### Regular Backup Schedule
```bash
# Daily backup
mysqldump -u root -p placement_intelligence > backup_$(date +%Y%m%d).sql

# Backup with compression
mysqldump -u root -p placement_intelligence | gzip > backup_$(date +%Y%m%d).sql.gz
```

### Restore from Backup
```bash
mysql -u root -p placement_intelligence < backup_20240101.sql
```

## Performance Monitoring Queries

### Find Slow Queries
```sql
-- Enable slow query log in MySQL
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2; -- Log queries taking > 2 seconds
```

### Index Size Analysis
```sql
SELECT 
  table_name,
  index_name,
  ROUND(stat_value * @@innodb_page_size / 1024 / 1024, 2) as size_mb
FROM mysql.innodb_index_stats
WHERE database_name = 'placement_intelligence'
  AND stat_name = 'size'
ORDER BY size_mb DESC;
```

## Common Pitfalls to Avoid

1. **Don't use SELECT *** on large tables - specify needed columns
2. **Always use indexes** in WHERE, JOIN, and ORDER BY clauses
3. **Avoid LIKE '%term%'** - it can't use indexes (prefer 'term%')
4. **Use LIMIT** for pagination to avoid loading entire result sets
5. **Batch inserts** instead of individual INSERT statements
6. **Use transactions** for multiple related operations
7. **Validate data** at application layer before INSERT/UPDATE
8. **Monitor index usage** and drop unused indexes

## Schema Version
- **Version**: 2.0
- **Last Updated**: 2024
- **Optimization Level**: Production-Ready
- **Normalization**: 3NF Compliant
- **Indexing**: Comprehensive
- **Constraints**: Full Validation
