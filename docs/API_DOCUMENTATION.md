# Skillora API Documentation

## Base URL
- **Development:** `http://localhost:8080`
- **Production:** `https://api.skillora.com`

## Authentication
All API endpoints (except `/api/auth/**`) require a JWT Bearer token in the Authorization header:
```
Authorization: Bearer <token>
```

Tokens are valid for **24 hours**. Obtain a token via the login or signup endpoints.

---

## Auth Endpoints

### POST /api/auth/signup
Register a new user account.

- **Required role:** None (public)
- **Request body:**
```json
{
  "email": "student@example.com",
  "password": "SecurePassword@123",
  "fullName": "Jane Doe",
  "role": "STUDENT"
}
```
- **Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "student@example.com",
  "fullName": "Jane Doe",
  "role": "STUDENT"
}
```

---

### POST /api/auth/login
Authenticate with email and password.

- **Required role:** None (public)
- **Request body:**
```json
{
  "email": "student@example.com",
  "password": "SecurePassword@123"
}
```
- **Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "student@example.com",
  "fullName": "Jane Doe",
  "role": "STUDENT"
}
```

---

### POST /api/auth/google-login
Authenticate using a Google OAuth ID token.

- **Required role:** None (public)
- **Request body:**
```json
{
  "idToken": "google-id-token-here"
}
```
- **Response:** Same as `/api/auth/login`

---

### GET /api/auth/google-client-id
Retrieve the Google OAuth client ID for frontend initialization.

- **Required role:** None (public)
- **Response:**
```json
{
  "clientId": "your-google-client-id.apps.googleusercontent.com"
}
```

---

## Student Endpoints

All student endpoints require `STUDENT` role unless otherwise noted.

### GET /api/student/dashboard
Returns the student's readiness dashboard including scores, progress, and weak areas.

- **Required role:** STUDENT
- **Response:**
```json
{
  "studentId": 1,
  "name": "Jane Doe",
  "readinessScore": 72.5,
  "aptitudeProgress": 68.0,
  "codingProgress": 80.0,
  "softSkillsProgress": 65.0,
  "weakAreas": ["Logical Reasoning", "Graphs"],
  "recentActivity": []
}
```

---

### GET /api/student/tests?section={type}
Fetch a test session with AI-generated questions.

- **Required role:** STUDENT
- **Query params:**
  - `section` (required): `aptitude`, `coding`, `softskills`, or `mock`
  - `topic` (optional): specific topic to focus on
- **Response:**
```json
{
  "testType": "APTITUDE",
  "totalQuestions": 20,
  "durationMinutes": 20,
  "sessionId": 42,
  "questions": [
    {
      "id": 101,
      "questionText": "If A is twice as old as B...",
      "optionA": "10",
      "optionB": "12",
      "optionC": "14",
      "optionD": "16",
      "difficultyLevel": "MEDIUM",
      "topic": "Age Problems"
    }
  ]
}
```

---

### POST /api/student/submit-test
Submit completed test answers for scoring.

- **Required role:** STUDENT
- **Request body:**
```json
{
  "testType": "APTITUDE",
  "sessionId": 42,
  "timeTakenSeconds": 720,
  "answers": {
    "101": "B",
    "102": "C"
  }
}
```
- **Response:**
```json
{
  "attemptId": 15,
  "testType": "APTITUDE",
  "score": 16,
  "totalQuestions": 20,
  "accuracy": 80.0,
  "timeTakenSeconds": 720,
  "weakAreas": ["Age Problems"],
  "testDate": "2026-07-03T10:30:00",
  "reviewItems": [
    {
      "questionId": 101,
      "questionText": "...",
      "correctOption": "B",
      "selectedOption": "B",
      "topic": "Age Problems"
    }
  ]
}
```

---

### GET /api/student/test-history
Retrieve the student's past test attempts.

- **Required role:** STUDENT
- **Response:**
```json
[
  {
    "id": 15,
    "testType": "APTITUDE",
    "score": 16,
    "totalQuestions": 20,
    "testDate": "2026-07-03T10:30:00"
  }
]
```

---

### GET /api/student/progress
Returns timeline-based progress data and section-wise performance.

- **Required role:** STUDENT
- **Response:**
```json
{
  "timeline": [
    { "date": "2026-07-01", "readinessScore": 65.0 },
    { "date": "2026-07-03", "readinessScore": 72.5 }
  ],
  "sectionPerformance": {
    "aptitude": 68.0,
    "coding": 80.0,
    "softSkills": 65.0
  },
  "suggestions": ["Improve in Arrays", "Practice Logical Reasoning"]
}
```

---

### GET /api/student/profile
Returns the student's full profile.

- **Required role:** STUDENT
- **Response:**
```json
{
  "studentId": 1,
  "name": "Jane Doe",
  "email": "jane@example.com",
  "department": "Computer Science",
  "cgpa": 8.5,
  "phone": "+91-9876543210",
  "interests": "AI, Web Development",
  "level": "Intermediate",
  "achievements": "Won hackathon 2025",
  "resumeUploaded": true,
  "resumeFileName": "jane_resume.pdf",
  "profileStrength": 88.89,
  "skills": ["Java", "React", "SQL"],
  "history": []
}
```

---

### PUT /api/student/profile
Update the student's profile information.

- **Required role:** STUDENT
- **Request body:**
```json
{
  "name": "Jane Doe",
  "cgpa": 8.7,
  "phone": "+91-9876543210",
  "interests": "AI, Machine Learning",
  "level": "Advanced",
  "achievements": "Won hackathon 2025, Dean's list 2026"
}
```
- **Response:**
```json
{ "message": "Profile updated" }
```

---

### POST /api/student/profile/resume
Upload a resume file (PDF, max 5MB).

- **Required role:** STUDENT
- **Content-Type:** `multipart/form-data`
- **Form field:** `file` — the PDF resume
- **Response:**
```json
{
  "message": "Resume uploaded successfully",
  "resumeFileName": "jane_resume.pdf"
}
```

---

### POST /api/student/code/execute
Execute code in a sandboxed environment (5s timeout, 256MB memory).

- **Required role:** STUDENT
- **Request body:**
```json
{
  "language": "PYTHON",
  "code": "print('Hello World')",
  "stdin": ""
}
```
- **Response:**
```json
{
  "stdout": "Hello World\n",
  "stderr": "",
  "exitCode": 0,
  "executionTimeMs": 120,
  "verdict": "ACCEPTED"
}
```
Supported languages: `JAVA`, `PYTHON`, `JAVASCRIPT`, `CPP`

---

## AI Mentor Endpoints

### POST /api/ai-mentor/generate
Generate AI-powered learning content in the requested mode.

- **Required role:** STUDENT
- **Request body:**
```json
{
  "mode": "LEARN",
  "topic": "Binary Search",
  "numberOfQuestions": 5,
  "difficulty": "MEDIUM",
  "studentLevel": "Intermediate",
  "weakTopics": ["Graphs", "DP"],
  "accuracy": 72.0
}
```
- **Available modes:** `LEARN`, `PRACTICE`, `ADAPTIVE`, `REVISION`, `MOCK_TEST`
- **Response:**
```json
{
  "mode": "LEARN",
  "topic": "Binary Search",
  "content": "## Binary Search\n\nBinary search is...",
  "aiGenerated": true
}
```

---

### GET /api/ai-mentor/modes
List all available AI mentor modes with metadata.

- **Required role:** STUDENT
- **Response:**
```json
{
  "modes": [
    {
      "mode": "LEARN",
      "name": "Learn Mode",
      "description": "Get comprehensive explanations with examples and concepts",
      "icon": "📚",
      "estimatedTime": "10-15 minutes"
    },
    {
      "mode": "PRACTICE",
      "name": "Practice Mode",
      "description": "Solve practice questions with immediate feedback",
      "icon": "💪",
      "estimatedTime": "15-30 minutes"
    }
  ],
  "totalModes": 5,
  "defaultMode": "LEARN"
}
```

---

### GET /api/ai-mentor/student-context
Retrieve the current student's context used to personalize AI content.

- **Required role:** STUDENT
- **Response:**
```json
{
  "studentLevel": "Intermediate",
  "weakAreas": ["Logical Reasoning", "Graphs"],
  "accuracy": 72.5,
  "readinessScore": 72.5,
  "aptitudeProgress": 68.0,
  "codingProgress": 80.0,
  "softSkillsProgress": 65.0
}
```

---

## Job Endpoints

### GET /api/jobs
List all active job postings.

- **Required role:** Any authenticated user
- **Response:**
```json
[
  {
    "id": 1,
    "title": "Software Engineer",
    "description": "Join our backend team...",
    "location": "Bangalore",
    "compensation": "8-12 LPA",
    "minCgpa": 7.5,
    "requiredSkills": "Java, Spring Boot",
    "jobType": "FULL_TIME",
    "active": true,
    "postedAt": "2026-07-01T09:00:00"
  }
]
```

---

### POST /api/jobs
Create a new job posting.

- **Required role:** RECRUITER
- **Request body:**
```json
{
  "title": "Software Engineer",
  "description": "Join our backend team...",
  "location": "Bangalore",
  "compensation": "8-12 LPA",
  "minCgpa": 7.5,
  "requiredSkills": "Java, Spring Boot",
  "jobType": "FULL_TIME",
  "departmentId": 1,
  "companyId": 2
}
```
- **Response:** The created `JobPosting` object.

---

### GET /api/jobs/eligible
Returns jobs the current student is eligible for based on CGPA and department.

- **Required role:** STUDENT
- **Response:** Array of `JobPosting` objects matching the student's eligibility criteria.

---

### POST /api/jobs/{jobId}/apply
Apply for a specific job posting.

- **Required role:** STUDENT
- **Path param:** `jobId` — the ID of the job to apply for
- **Response:** The created `JobApplication` object with status `APPLIED`.

---

### GET /api/applications/my
List the current student's job applications.

- **Required role:** STUDENT
- **Response:**
```json
[
  {
    "id": 10,
    "jobId": 1,
    "jobTitle": "Software Engineer",
    "status": "UNDER_REVIEW",
    "appliedAt": "2026-07-02T14:00:00",
    "recruiterNotes": null
  }
]
```

---

## Interview Endpoints

### POST /api/interviews
Schedule an interview for a shortlisted application.

- **Required role:** STAFF / RECRUITER
- **Request body:**
```json
{
  "jobApplicationId": 10,
  "scheduledAt": "2026-07-15T10:00:00",
  "durationMinutes": 45,
  "mode": "ONLINE",
  "meetingLink": "https://meet.google.com/abc-defg-hij"
}
```
- **Response:** The created `InterviewSchedule` object.

---

### GET /api/interviews/my
Get interviews associated with the current user (as interviewer or candidate).

- **Required role:** STAFF / STUDENT
- **Response:** Array of `InterviewSchedule` objects.

---

### GET /api/interviews/upcoming
Get all upcoming (future-dated, SCHEDULED) interviews for the current user.

- **Required role:** STAFF / STUDENT
- **Response:** Array of `InterviewSchedule` objects sorted by scheduled time.

---

### POST /api/interviews/{id}/feedback
Submit feedback and rating after an interview is completed.

- **Required role:** STAFF
- **Path param:** `id` — interview schedule ID
- **Request body:**
```json
{
  "rating": 8,
  "comments": "Strong problem-solving skills, good communication.",
  "recommended": true
}
```
- **Response:** The created `InterviewFeedback` object.

---

## Notification Endpoints

### GET /api/notifications/
List all notifications for the current user, ordered newest first.

- **Required role:** Any authenticated user
- **Response:**
```json
[
  {
    "id": 5,
    "type": "JOB_APPLICATION",
    "message": "Your application for Software Engineer has been shortlisted.",
    "read": false,
    "createdAt": "2026-07-03T09:15:00"
  }
]
```

---

### GET /api/notifications/unread-count
Returns the count of unread notifications.

- **Required role:** Any authenticated user
- **Response:**
```json
{ "count": 3 }
```

---

### PATCH /api/notifications/{id}/read
Mark a single notification as read.

- **Required role:** Any authenticated user
- **Path param:** `id` — notification ID
- **Response:**
```json
{ "message": "Notification marked as read" }
```

---

### POST /api/notifications/mark-all-read
Mark all of the current user's notifications as read.

- **Required role:** Any authenticated user
- **Response:**
```json
{ "message": "All notifications marked as read" }
```

---

## Staff Endpoints

All staff endpoints require `STAFF` role.

### GET /api/staff/department-stats
Returns overall platform statistics broken down by department.

- **Required role:** STAFF
- **Response:**
```json
{
  "totalStudents": 450,
  "activeUsers": 120,
  "loggedInToday": 35,
  "departmentPerformance": [
    { "department": "Computer Science", "averageScore": 74.5 },
    { "department": "Electronics", "averageScore": 68.2 }
  ]
}
```

---

### GET /api/staff/top-students
Returns the top 3 students per department ranked by readiness score.

- **Required role:** STAFF
- **Response:**
```json
{
  "topByDepartment": {
    "Computer Science": [
      { "studentId": 1, "name": "Jane Doe", "readinessScore": 92.0, "finalScore": 88.5 }
    ],
    "Electronics": []
  }
}
```

---

### GET /api/staff/student-details?studentId={id}
Get detailed performance breakdown for a specific student.

- **Required role:** STAFF
- **Query param:** `studentId` — the target student's ID
- **Response:**
```json
{
  "studentId": 1,
  "name": "Jane Doe",
  "department": "Computer Science",
  "readiness": 92.0,
  "aptitude": 85.0,
  "coding": 90.0,
  "softSkills": 78.0,
  "weakAreas": ["Probability"],
  "activity": []
}
```

---

## Analytics Endpoints

### GET /api/analytics/overview
Full analytics overview including placement summary, department averages, and topic performance.

- **Required role:** STAFF / RECRUITER
- **Response:**
```json
{
  "totalStudents": 450,
  "placedCount": 180,
  "placementRate": 40.0,
  "avgReadinessScore": 68.4,
  "departmentAverageScores": [
    { "department": "Computer Science", "avgAptitude": 72.0, "avgCoding": 78.0, "avgSoftSkills": 65.0 }
  ],
  "weakTopics": [
    { "topic": "Probability", "accuracy": 42.0 }
  ]
}
```

---

### GET /api/analytics/student/{studentId}
Detailed analytics for a single student.

- **Required role:** STUDENT (own data), STAFF, RECRUITER
- **Path param:** `studentId`
- **Response:**
```json
{
  "studentId": 1,
  "name": "Jane Doe",
  "department": "Computer Science",
  "aptitudeScore": 85.0,
  "dsaScore": 90.0,
  "softSkillScore": 78.0,
  "mockTestScore": 82.0,
  "finalScore": 88.5,
  "readinessScore": 92.0,
  "placementStatus": "NOT_PLACED",
  "cgpa": 8.7,
  "rank": 3
}
```

---

### GET /api/analytics/department-summary
Department-wise average scores across all sections.

- **Required role:** STAFF / RECRUITER
- **Response:** Array of `DepartmentAverage` objects (same shape as `departmentAverageScores` in overview).

---

### GET /api/analytics/topic-performance
Topic-level accuracy data to identify platform-wide weak areas.

- **Required role:** STAFF / RECRUITER
- **Response:**
```json
[
  { "topic": "Probability", "accuracy": 42.0 },
  { "topic": "Graphs", "accuracy": 51.3 }
]
```

---

## Error Responses

All error responses follow this structure:

```json
{
  "error": "UNAUTHORIZED",
  "message": "JWT token is expired or invalid",
  "timestamp": "2026-07-03T10:30:00"
}
```

| HTTP Status | Meaning |
|-------------|---------|
| 200 | Success |
| 201 | Resource created |
| 400 | Bad request / validation error |
| 401 | Missing or invalid JWT token |
| 403 | Insufficient role permissions |
| 404 | Resource not found |
| 500 | Internal server error |

---

## Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Student | `student@skillora.com` | `Skillora@123` |
| Staff | `staff@skillora.com` | `Skillora@123` |
| Recruiter | `recruiter@skillora.com` | `Skillora@123` |
