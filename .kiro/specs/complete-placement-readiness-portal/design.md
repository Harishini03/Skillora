# Technical Design Document: Skillora - AI-Powered Placement Intelligence System

## 1. System Architecture

### 1.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend Layer                            │
│  React 19 + Vite + Tailwind CSS + React Router + Axios          │
└────────────────────┬────────────────────────────────────────────┘
                     │ HTTPS/REST
┌────────────────────┴────────────────────────────────────────────┐
│                     API Gateway Layer                            │
│              Spring Security + JWT Filter                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────────┐
│                   Application Layer                              │
│  ┌─────────────┬──────────────┬─────────────┬────────────┐     │
│  │ Controllers │   Services   │    DTOs     │  Mappers   │     │
│  └─────────────┴──────────────┴─────────────┴────────────┘     │
└────────────────────┬────────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────────┐
│                  Domain Layer                                    │
│  ┌──────────────────────────────────────────────────┐          │
│  │  Entities + Repositories + Business Logic         │          │
│  └──────────────────────────────────────────────────┘          │
└────────────────────┬────────────────────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────────────────────┐
│              Data Persistence Layer                              │
│        H2 (Development) / MySQL (Production)                    │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                   External Services                              │
│  ┌────────────────┬─────────────────┬──────────────────┐       │
│  │  Groq AI API   │  Google OAuth   │  Code Sandbox    │       │
│  └────────────────┴─────────────────┴──────────────────┘       │
└──────────────────────────────────────────────────────────────────┘
```

### 1.2 Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.5.6
- Spring Security 6
- Spring Data JPA
- JWT (io.jsonwebtoken:jjwt)
- Lombok
- H2 Database (Dev) / MySQL (Prod)

**Frontend:**
- React 19.2.4
- Vite 8.0.1
- Tailwind CSS 4.2.2
- React Router 7.13.1
- Axios 1.13.6
- Recharts 3.8.0 (Analytics)
- Firebase 12.15.0 (Optional features)

**External APIs:**
- Groq AI API (llama-3.3-70b-versatile)
- Google OAuth 2.0

## 2. Database Schema

### 2.1 Core Entity Relationships

```
User (1) ──────────────── (*) Student
User (1) ──────────────── (*) StaffProfile
User (1) ──────────────── (*) Profile (Recruiter)
Student (1) ────────────── (1) Profile
Student (*) ────────────── (*) Course [CourseEnrollment]
Student (*) ────────────── (*) CodingProblem [CodeSubmission]
```

Profile (*) ─────────────── (*) JobPosting [JobApplication]
JobPosting (1) ──────────── (*) InterviewSchedule
Course (1) ──────────────── (*) CourseModule
CourseModule (1) ─────────── (*) CourseLesson
Student (1) ──────────────── (*) TestSession
TestSession (1) ─────────────(*) StudentAnswer
```

### 2.2 Entity Details

#### User Entity
```java
@Entity
@Table(name = "users")
class User {
    Long id;
    String email; // UNIQUE, NOT NULL
    String password; // BCrypt encrypted
    String fullName;
    Role role; // STUDENT, STAFF, RECRUITER
    AuthProvider authProvider; // LOCAL, GOOGLE
    Boolean enabled;
    LocalDateTime createdAt;
    LocalDateTime lastLogin;
}
```

#### Student Entity
```java
@Entity
@Table(name = "students")
class Student {
    Long id;
    @OneToOne User user;
    String rollNumber; // UNIQUE
    String department;
    String batch;
    Double cgpa;
    PlacementStatus placementStatus; // NOT_PLACED, PLACED, NOT_INTERESTED
    @OneToOne Profile profile;
    @OneToMany Set<CourseEnrollment> enrollments;
    @OneToMany Set<CodeSubmission> submissions;
    @OneToMany Set<TestSession> testSessions;
}
```

#### Profile Entity (Recruiter & Student Extended Info)
```java
@Entity
@Table(name = "profiles")
class Profile {
    Long id;
    @OneToOne Student student; // Nullable for recruiter profiles
    String phone;
    about: { content: form.aboutMe, characterCount: form.aboutMe.length },
          skills: form.skills,
        };
      } else {
        const payload = {
          firstName: form.firstName,
          lastName: form.lastName,
          personalEmail: form.personalEmail,
          collegeEmail: form.collegeEmail,
          mobileNumber: form.mobileNumber,
          alternateMobileNumber: form.alternateMobileNumber,
          whatsappNumber: form.whatsappNumber,
          visibleToHr: form.visibleToHr,
          addressLine1: form.addressLine1,
          addressLine2: form.addressLine2,
          city: form.city,
          state: form.state,
          pincode: form.pincode,
          dateOfBirth: form.dateOfBirth,
          gender: form.gender,
          fatherName: form.fatherName,
          fatherContactNumber: form.fatherContactNumber,
          motherName: form.motherName,
          motherContactNumber: form.motherContactNumber,
          aboutMe: form.aboutMe,
          profileImageVisibleToHr: form.profileImageVisibleToHr,
        };
        const res = await api.put("/api/student/user-profile", payload);
        data = res.data;
      }
      setProfile(data);
      localStorage.setItem("pi_profile_draft", JSON.stringify(data));
      setMessage("Profile saved successfully!");
      if (isFirebaseConfigured()) {
        try {
          await saveFirebaseRecord("profile_updates", {
            timestamp: new Date().toISOString(),
            studentId: data.studentId,
            action: "profile_update",
          });
        } catch (fbError) {
          console.warn("Firebase logging failed:", fbError);
        }
      }
    } catch (e) {
      setError(e.response?.data?.message || "Failed to save profile.");
    } finally {
      setSaving(false);
    }
  };

  const addEducation = async () => {
    if (!educationDraft.institutionName || !educationDraft.degree) {
      setError("Institution and degree are required.");
      return;
    }


## 3. API Architecture

### 3.1 REST Endpoints Structure

**Authentication Endpoints:**
- POST `/api/auth/login` - User login with credentials
- POST `/api/auth/signup` - New user registration
- POST `/api/auth/google` - Google OAuth authentication
- POST `/api/auth/refresh` - Refresh JWT token
- POST `/api/auth/logout` - Logout user session

**Student Endpoints:**
- GET `/api/student/dashboard` - Student dashboard data
- GET `/api/student/profile` - Student profile details
- PUT `/api/student/profile` - Update student profile
- GET `/api/student/tests` - Fetch test session
- POST `/api/student/submit-test` - Submit test answers
- GET `/api/student/analytics` - Performance analytics

**Course Endpoints:**
- GET `/api/courses` - List all courses
- GET `/api/courses/{id}` - Course details
- POST `/api/courses/{id}/enroll` - Enroll in course
- GET `/api/courses/{id}/progress` - Course progress
- POST `/api/lessons/{id}/complete` - Mark lesson complete

**Coding Platform Endpoints:**
- GET `/api/coding/problems` - List coding problems
- GET `/api/coding/problems/{id}` - Problem details
- POST `/api/code/execute` - Execute code with test cases
- POST `/api/code/submit` - Submit solution
- GET `/api/code/submissions` - User submission history

**AI Mentor Endpoints:**
- POST `/api/ai/chat` - AI conversation endpoint
- POST `/api/ai/generate-question` - Generate practice question
- POST `/api/ai/adaptive-test` - Create adaptive test
- GET `/api/ai/learning-path` - Personalized learning recommendations

**Job & Recruitment Endpoints:**
- GET `/api/jobs` - List available jobs
- POST `/api/jobs/{id}/apply` - Apply to job
- GET `/api/applications` - User applications
- GET `/api/interviews` - Interview schedule
- POST `/api/interviews/{id}/feedback` - Submit feedback

**Staff/Admin Endpoints:**
- GET `/api/staff/dashboard` - Staff analytics dashboard
- GET `/api/staff/students` - Student list with filters
- GET `/api/staff/analytics` - Placement statistics
- POST `/api/staff/reports/export` - Download reports

**Recruiter Endpoints:**
- POST `/api/recruiter/jobs` - Create job posting
- GET `/api/recruiter/applications` - View applications
- PUT `/api/recruiter/applications/{id}` - Update application status
- POST `/api/recruiter/interviews` - Schedule interview

### 3.2 Security Architecture

**Authentication Flow:**
```
1. User submits credentials
2. AuthService validates credentials
3. JwtService generates token (24h expiry)
4. Token returned to client
5. Client includes token in Authorization header
6. JwtAuthenticationFilter validates token on each request
7. SecurityContext populated with UserPrincipal
```

**Security Configuration:**
- BCrypt password hashing (strength 12)
- JWT with HS512 algorithm
- CORS whitelisting
- Rate limiting on auth endpoints
- Input validation on all endpoints
- SQL injection prevention via JPA
- XSS prevention via output encoding

## 4. Component Design

### 4.1 Backend Service Layer

**AuthService:**
- `login(credentials)`: Authenticate user and generate JWT
- `signup(userDetails)`: Create new user account
- `googleLogin(tokenId)`: Authenticate via Google OAuth
- `refreshToken(token)`: Generate new JWT from refresh token

**StudentDashboardService:**
- `buildDashboard(studentId)`: Aggregate dashboard data
- `calculateReadinessScore(studentId)`: Compute overall readiness
- `identifyWeakAreas(studentId)`: Analyze performance gaps
- `generateRecommendations(studentId)`: Personalized suggestions

**CourseService:**
- `getAllCourses()`: Fetch course catalog
- `getCourseDetails(courseId)`: Detailed course info
- `enrollStudent(studentId, courseId)`: Create enrollment
- `trackProgress(studentId, courseId)`: Calculate completion %

**CodingPlatformService:**
- `getProblems(filters)`: Fetch problems with filters
- `executeCode(request)`: Run code in sandbox
- `validateSolution(problemId, code)`: Check against test cases
- `recordSubmission(studentId, submission)`: Save attempt

**SkilloraAiMentorService:**
- `generateContent(mode, topic, difficulty)`: AI content generation
- `adaptiveDifficultyAdjustment(studentId)`: Dynamic difficulty
- `conversationalMentor(message, context)`: Chat interface
- `generateMockTest(studentId, duration)`: Create timed test

**TestService:**
- `createTestSession(type, config)`: Initialize test
- `generateQuestions(topic, count)`: AI question generation
- `evaluateSubmission(answers, testId)`: Score calculation
- `identifyWeakTopics(answers)`: Topic analysis

**JobApplicationService:**
- `applyToJob(studentId, jobId)`: Create application
- `checkEligibility(studentId, jobId)`: Validate criteria
- `updateApplicationStatus(applicationId, status)`: Status change
- `notifyStudent(studentId, message)`: Send notification

### 4.2 Frontend Component Architecture

**Pages:**
- `LandingPage`: Marketing landing page
- `LoginPage` / `SignupPage`: Authentication
- `StudentDashboardPage`: Student control center
- `CoursesPage` / `CourseDetailPage`: Learning platform
- `CodingProblemsPage` / `ProblemSolvePage`: Coding practice
- `AiMentorPage`: AI learning interface
- `ProfilePage`: User profile management
- `StaffDashboardPage`: Admin analytics
- `RecruiterDashboardPage`: Recruiter workspace

**Shared Components:**
- `AppShell`: Navigation layout
- `StatCard`: Dashboard metric display
- `ProgressBar`: Progress visualization
- `TestTimer`: Countdown timer component
- `CodeEditor`: Syntax-highlighted editor
- `ChartWidget`: Analytics charts

## 5. Data Flow

### 5.1 Authentication Flow
```
Frontend → POST /api/auth/login → AuthController 
→ AuthService.login() → UserRepository.findByEmail()
→ BCrypt.matches() → JwtService.generateToken()
→ AuthResponse(token, user) → Frontend stores token
→ All subsequent requests include: Authorization: Bearer {token}
```

### 5.2 AI Content Generation Flow
```
Frontend → POST /api/ai/generate-question
→ SkilloraAiController → SkilloraAiMentorService
→ Groq API (llama-3.3-70b) → Parse response
→ QuestionRepository.save() → Return generated content
```

### 5.3 Code Execution Flow
```
Frontend → POST /api/code/execute(code, language, stdin)
→ CodingPlatformController → CodeExecutionService
→ Create isolated process → Set timeout(5s) & memory(256MB)
→ Execute code → Capture stdout/stderr
→ Kill process → Return execution result
```

## 6. Database Schema (Complete)

### Core Tables

**users**
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  full_name VARCHAR(255),
  role ENUM('STUDENT', 'STAFF', 'RECRUITER') NOT NULL,
  auth_provider ENUM('LOCAL', 'GOOGLE') DEFAULT 'LOCAL',
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_login TIMESTAMP
);
```

**students**
```sql
CREATE TABLE students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNIQUE,
  roll_number VARCHAR(50) UNIQUE,
  department VARCHAR(100),
  batch VARCHAR(20),
  cgpa DECIMAL(3,2),
  placement_status ENUM('NOT_PLACED', 'PLACED', 'NOT_INTERESTED'),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**profiles**
```sql
CREATE TABLE profiles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT,
  phone VARCHAR(20),
  alternate_phone VARCHAR(20),
  address_line1 VARCHAR(255),
  city VARCHAR(100),
  state VARCHAR(100),
  pincode VARCHAR(10),
  date_of_birth DATE,
  gender ENUM('MALE', 'FEMALE', 'OTHER'),
  about_me TEXT,
  FOREIGN KEY (student_id) REFERENCES students(id)
);
```

**courses**
```sql
CREATE TABLE courses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  instructor_name VARCHAR(255),
  thumbnail_url VARCHAR(500),
  difficulty ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED'),
  duration_hours INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**course_modules**
```sql
CREATE TABLE course_modules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  course_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  order_index INT NOT NULL,
  FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

**course_lessons**
```sql
CREATE TABLE course_lessons (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  module_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  video_url VARCHAR(500),
  content TEXT,
  duration_minutes INT,
  order_index INT NOT NULL,
  FOREIGN KEY (module_id) REFERENCES course_modules(id)
);
```

**course_enrollments**
```sql
CREATE TABLE course_enrollments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  progress_percentage INT DEFAULT 0,
  completed BOOLEAN DEFAULT FALSE,
  UNIQUE KEY (student_id, course_id),
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (course_id) REFERENCES courses(id)
);
```

**coding_problems**
```sql
CREATE TABLE coding_problems (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  difficulty ENUM('EASY', 'MEDIUM', 'HARD'),
  category VARCHAR(100),
  constraints TEXT,
  sample_input TEXT,
  sample_output TEXT,
  time_limit_seconds INT DEFAULT 5,
  memory_limit_mb INT DEFAULT 256
);
```

**code_submissions**
```sql
CREATE TABLE code_submissions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  problem_id BIGINT NOT NULL,
  language ENUM('JAVA', 'PYTHON', 'JAVASCRIPT', 'CPP'),
  code TEXT NOT NULL,
  verdict ENUM('ACCEPTED', 'WRONG_ANSWER', 'TIME_LIMIT', 'RUNTIME_ERROR', 'COMPILE_ERROR'),
  execution_time_ms INT,
  memory_used_kb INT,
  submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (problem_id) REFERENCES coding_problems(id)
);
```

**job_postings**
```sql
CREATE TABLE job_postings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  company_id BIGINT,
  description TEXT,
  job_type ENUM('FULL_TIME', 'INTERN', 'CONTRACT'),
  location VARCHAR(255),
  min_salary DECIMAL(10,2),
  max_salary DECIMAL(10,2),
  required_skills TEXT,
  posted_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  deadline TIMESTAMP,
  FOREIGN KEY (company_id) REFERENCES companies(id)
);
```

**job_applications**
```sql
CREATE TABLE job_applications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  job_id BIGINT NOT NULL,
  profile_id BIGINT,
  status ENUM('APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'REJECTED', 'OFFERED', 'ACCEPTED'),
  applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY (student_id, job_id),
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (job_id) REFERENCES job_postings(id),
  FOREIGN KEY (profile_id) REFERENCES profiles(id)
);
```

**interview_schedules**
```sql
CREATE TABLE interview_schedules (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  application_id BIGINT NOT NULL,
  scheduled_time TIMESTAMP NOT NULL,
  mode ENUM('ONLINE', 'OFFLINE'),
  location VARCHAR(255),
  meeting_link VARCHAR(500),
  status ENUM('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'),
  feedback TEXT,
  rating INT,
  FOREIGN KEY (application_id) REFERENCES job_applications(id)
);
```

**test_sessions**
```sql
CREATE TABLE test_sessions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  test_type ENUM('APTITUDE', 'CODING', 'MOCK'),
  duration_minutes INT NOT NULL,
  started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  submitted_at TIMESTAMP,
  score INT,
  total_questions INT,
  FOREIGN KEY (student_id) REFERENCES students(id)
);
```

**notifications**
```sql
CREATE TABLE notifications (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  message TEXT,
  type VARCHAR(50),
  read_status BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## 7. Implementation Details

### 7.1 AI Integration

**Groq API Configuration:**
- Model: `llama-3.3-70b-versatile`
- Temperature: 0.7 (for variety)
- Max tokens: 1000
- System prompt: Role-specific instructions

**Content Generation Strategies:**
- **LEARN Mode**: Generate concept explanations with examples
- **PRACTICE Mode**: Create graded questions with solutions
- **ADAPTIVE Mode**: Adjust based on recent performance (success rate)
- **REVISION Mode**: Summarize key points from past topics
- **MOCK_TEST Mode**: Timed full-length tests

**Freshness Guarantee:**
- Include timestamp in prompt
- Use random seed variation
- Track generated question IDs to prevent repetition

### 7.2 Code Execution Sandbox

**Security Measures:**
- Process isolation (separate container/process per execution)
- Timeout enforcement (5 seconds hard limit)
- Memory limit (256MB max)
- No network access
- No file system write access
- Kill process tree on completion

**Language Support:**
- **Python**: `python3 -c "code"` with stdin redirect
- **Java**: Compile + execute Main class
- **JavaScript**: `node -e "code"` with stdin
- **C++**: `g++ -o prog && ./prog` with timeout

### 7.3 Performance Optimization

**Backend:**
- Database connection pooling (HikariCP)
- Query optimization with indexes
- Lazy loading for JPA entities
- Caching for frequently accessed data (courses, problems)
- Async processing for heavy operations (AI generation, code execution)

**Frontend:**
- Code splitting with React.lazy()
- Memoization with useMemo() and useCallback()
- Virtual scrolling for large lists
- Image lazy loading
- Bundle size optimization with Vite tree-shaking

## 8. Deployment Architecture

### 8.1 Development Environment
- H2 in-memory database
- Embedded Tomcat server (port 8080)
- Vite dev server (port 5173)
- Hot reload enabled

### 8.2 Production Environment
- MySQL 8.0 database
- Spring Boot WAR deployed on Tomcat 10
- Nginx reverse proxy
- SSL/TLS encryption
- Docker containerization
- Environment-based configuration

### 8.3 CI/CD Pipeline
```
1. Code push to Git
2. Run tests (JUnit, Jest)
3. Build backend JAR
4. Build frontend static files
5. Create Docker image
6. Push to registry
7. Deploy to staging
8. Run smoke tests
9. Deploy to production
```

---

**Document Version**: 1.0  
**Last Updated**: 2026-07-03
