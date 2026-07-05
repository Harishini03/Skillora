# Skillora - Quick Start Guide

## 🚀 Getting Started in 3 Steps

### Step 1: Access the Application
1. Open your browser
2. Navigate to: **http://localhost:5173**
3. Both backend and frontend are already running! ✅

### Step 2: Login
Use these test accounts:

**Student Account:**
```
Username: student1
Password: password
```

**Staff Account:**
```
Username: staffadmin
Password: password
```

**Recruiter Account:**
```
Username: recruiter1
Password: password
```

### Step 3: Explore Features!

---

## 🎓 Student Experience

### 1. Dashboard
- View your readiness score
- Track progress across Aptitude, Coding, and Soft Skills
- See weak areas and recommendations

### 2. Courses (NEW! 🎉)
**How to access:** Click "Courses" in the sidebar

**What you can do:**
- Browse all available courses
- Enroll in courses
- View your enrolled courses
- Track your progress

**Course Player:**
- Watch video lessons
- Read text content
- Complete quizzes
- Track lesson completion
- Navigate through modules

### 3. Coding Problems (NEW! 🎉)
**How to access:** Click "Coding Problems" in the sidebar

**What you can do:**
- Browse 100+ coding problems
- Filter by difficulty (Easy/Medium/Hard)
- Filter by category (Arrays, Strings, Trees, etc.)
- Search problems by name
- View problem statistics

**Code Editor:**
- Write code in Python, Java, JavaScript, or C++
- Run code against sample test cases
- Submit your solution
- View test results instantly
- Check submission history

### 4. AI Mentor (NEW! 🎉)
**How to access:** Click "AI Mentor" in the sidebar

**5 Learning Modes:**

**📚 Learn Mode**
- Deep dive into any topic
- Get concepts, formulas, examples
- Interview tips and FAQs
- Quick revision sheets

**✍️ Practice Mode**
- Generate fresh MCQs
- Get detailed explanations
- Never repeats questions
- Placement-focused content

**🎯 Adaptive Mode**
- Personalized based on your performance
- Input your scores and accuracy
- Get customized practice questions
- Focus on weak areas

**⚡ Revision Mode**
- Quick notes and shortcuts
- Formula sheets
- Common mistakes to avoid
- Fast review before exams

**🎓 Mock Test Mode**
- Full-length placement tests
- 40% Easy, 40% Medium, 20% Hard
- Simulates real placement tests

### 5. Aptitude Tests
- Take timed aptitude tests
- Multiple choice questions
- Auto-grading with explanations
- Identify weak topics

### 6. Soft Skills
- Communication practice
- Interview question practice
- Behavioral assessments

### 7. Mock Interviews
- Scheduled mock interviews
- Feedback from interviewers
- Performance tracking

### 8. Job Opportunities
- Browse job postings
- Apply for positions
- Track application status
- View interview schedules

### 9. Analytics
- Detailed performance graphs
- Test history
- Progress over time
- Comparison with peers

### 10. Profile
- Update personal information
- Upload resume
- Add skills
- Education history
- Contact details

---

## 👨‍🏫 Staff Experience

### 1. Staff Dashboard
- Overview of all students
- Department-wise analytics
- Performance metrics
- Quick actions

### 2. Talent Intelligence
- Identify top performers
- Department-wise leaderboards
- Export talent pools to CSV
- Advanced filtering

### 3. Student Monitoring
- Track individual student progress
- View test results
- Monitor course completion
- Check job application status

### 4. Course Management
**Create Courses:**
```
POST /api/courses
{
  "title": "Data Structures Masterclass",
  "description": "Complete guide to DSA",
  "level": "Intermediate",
  "duration": "8 weeks"
}
```

**Add Modules:**
```
POST /api/courses/{courseId}/modules
{
  "title": "Module 1: Arrays",
  "description": "Array basics",
  "orderIndex": 1
}
```

**Add Lessons:**
```
POST /api/courses/modules/{moduleId}/lessons
{
  "title": "Introduction to Arrays",
  "lessonType": "VIDEO",
  "videoUrl": "https://youtube.com/watch?v=...",
  "duration": 30,
  "orderIndex": 1
}
```

### 5. Coding Problem Management
**Create Problems:**
```
POST /api/coding/problems
{
  "title": "Two Sum",
  "description": "Find two numbers that add up to target",
  "difficulty": "EASY",
  "category": "ARRAYS",
  "inputFormat": "First line: n, target...",
  "outputFormat": "Two space-separated indices"
}
```

**Add Test Cases:**
```
POST /api/coding/problems/{problemId}/testcases
{
  "input": "4\n2 7 11 15\n9",
  "expectedOutput": "0 1",
  "isHidden": false
}
```

---

## 👔 Recruiter Experience

### 1. Hiring Dashboard
- Post new job openings
- View active jobs
- Pipeline analytics
- Offer conversion rate

### 2. Post Jobs
**Fill the form:**
- Job title
- Location
- Compensation
- Min CGPA
- Required skills
- Job type (Full Time/Internship/Contract)
- Description

### 3. Top Talent Pools
- View top students by department
- Review candidate profiles
- Check readiness scores
- Add to shortlist
- Calculate fit scores

### 4. Candidate Monitoring
- View all applications
- Filter by status
- Update application status
- Add recruiter notes
- Schedule interviews
- Assign interviewers

---

## 🔥 New Features Walkthrough

### Feature 1: Course System

**Step 1:** Login as Student
**Step 2:** Click "Courses" in sidebar
**Step 3:** Browse available courses
**Step 4:** Click "Enroll Now" on any course
**Step 5:** Click on the course card to open
**Step 6:** Navigate through modules and lessons
**Step 7:** Watch videos, read content
**Step 8:** Click "Mark as Complete" after each lesson
**Step 9:** Track your progress bar

**Benefits:**
- Structured learning path
- Video tutorials
- Progress tracking
- Module-based organization
- Self-paced learning

---

### Feature 2: Coding Platform

**Step 1:** Login as Student
**Step 2:** Click "Coding Problems" in sidebar
**Step 3:** Use filters to find problems (Difficulty, Category, Search)
**Step 4:** Click "Solve" on any problem
**Step 5:** Read problem description carefully
**Step 6:** Select your programming language
**Step 7:** Write your code in the editor
**Step 8:** Click "Run Code" to test
**Step 9:** Check test results
**Step 10:** Click "Submit" when ready
**Step 11:** View your submission history

**Benefits:**
- LeetCode-style interface
- Multi-language support
- Instant feedback
- Test case validation
- Submission tracking
- Acceptance rate stats

---

### Feature 3: AI Mentor

**Step 1:** Login as Student
**Step 2:** Click "AI Mentor" in sidebar
**Step 3:** Select a learning mode (5 options)
**Step 4:** Enter topic (e.g., "Arrays")
**Step 5:** Enter subtopic (optional, e.g., "Sliding Window")
**Step 6:** Configure settings (number of questions, difficulty, etc.)
**Step 7:** Click "Generate"
**Step 8:** Wait for AI to create content (~5-10 seconds)
**Step 9:** Read and study the generated content
**Step 10:** Copy to clipboard if needed
**Step 11:** Recent topics saved automatically

**Pro Tips:**
- Use LEARN mode first for new topics
- Practice with ADAPTIVE mode to improve weak areas
- Take MOCK_TEST before real placements
- Use REVISION mode the night before exams
- AI never repeats questions!

---

## 📊 Analytics & Tracking

### Student Analytics
- **Readiness Score:** Overall placement preparedness (0-100%)
- **Module Progress:** Aptitude, Coding, Soft Skills (0-100%)
- **Weak Areas:** Topics needing more practice
- **Test History:** All past test results
- **Course Progress:** Completion percentage per course
- **Problem Solving:** Acceptance rate, problems solved

### Staff Analytics
- **Department Performance:** Average scores by department
- **Top Performers:** Leaderboard by readiness score
- **Student Progress:** Individual tracking
- **Course Enrollment:** Students per course
- **Test Participation:** Completion rates

### Recruiter Analytics
- **Application Pipeline:** Applied → Shortlisted → Interview → Offered
- **Conversion Rate:** Offer/Total applications
- **Time to Hire:** Average days from application to offer
- **Candidate Quality:** Readiness score distribution

---

## 🎨 UI/UX Features

### Design Elements
- **Gradient Banners:** Teal-to-cyan gradients for headers
- **Dashboard Cards:** Clean white cards with shadows
- **Progress Bars:** Animated progress indicators
- **Color Coding:**
  - Easy: Green
  - Medium: Amber
  - Hard: Red
  - Success: Emerald
  - Error: Rose
  - Info: Cyan

### Responsive Design
- Mobile-first approach
- Tablet optimization
- Desktop layouts
- Adaptive navigation

### Accessibility
- Keyboard navigation
- Screen reader support
- High contrast mode
- Clear focus indicators

---

## 🔧 Technical Details

### API Base URL
```
http://localhost:8080/api
```

### Frontend Port
```
http://localhost:5173
```

### Database
```
H2 Console: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/placement_intelligence
Username: sa
Password: (leave empty)
```

---

## 💡 Usage Tips

### For Students
1. Complete your profile first (upload resume, add skills)
2. Take the dashboard seriously - it guides your learning
3. Practice coding daily - consistency beats intensity
4. Use AI Mentor for topics you find difficult
5. Take mock tests regularly
6. Track your weak areas and focus on them
7. Apply for jobs early and often

### For Staff
1. Create courses with clear learning objectives
2. Add video content for better engagement
3. Create coding problems with varying difficulty
4. Monitor student progress weekly
5. Identify struggling students early
6. Export talent pools before placements
7. Use analytics to improve curriculum

### For Recruiters
1. Post detailed job descriptions
2. Set realistic CGPA requirements
3. Review candidate profiles thoroughly
4. Schedule interviews promptly
5. Provide feedback quickly
6. Track pipeline metrics
7. Maintain communication with candidates

---

## 🐛 Troubleshooting

### Frontend Not Loading?
```bash
cd frontend
npm install
npm run dev
```

### Backend Not Running?
```bash
.\gradlew.bat clean bootRun
```

### CORS Errors?
Check `application.properties`:
```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:5174
```

### Database Issues?
Delete `data/` folder and restart backend (will recreate database)

### AI Not Working?
Check Groq API key in `application.properties`:
```properties
app.groq.api-key=your-key-here
```

---

## 🎯 Success Metrics

### Student Success
- ✅ Complete profile
- ✅ Enroll in 3+ courses
- ✅ Solve 50+ coding problems
- ✅ Achieve 70%+ readiness score
- ✅ Take weekly mock tests
- ✅ Apply to 10+ jobs

### Platform Success
- ✅ 80% student engagement rate
- ✅ 90% course completion rate
- ✅ 70% problem solving rate
- ✅ 60% placement rate
- ✅ 85% recruiter satisfaction

---

## 🚀 Next Steps

1. **Explore Everything:** Login with each role and explore all features
2. **Test Core Features:** Create a course, solve a coding problem, use AI Mentor
3. **Customize:** Update branding, colors, content as needed
4. **Add Content:** Populate courses and coding problems
5. **Go Live:** Deploy to production server
6. **Monitor:** Track usage and improve based on feedback

---

## 📚 Additional Resources

### API Documentation
- All endpoints follow REST conventions
- Authentication: JWT Bearer token
- Content-Type: application/json
- CORS: Configured for local development

### Code Organization
- **Backend:** Standard Spring Boot structure
- **Frontend:** Feature-based component organization
- **Database:** JPA entities with proper relationships
- **Security:** JWT + Spring Security

### Development Workflow
1. Backend changes: Restart Spring Boot
2. Frontend changes: Hot reload (automatic)
3. Database changes: Update schema.sql and restart
4. API changes: Update both backend and frontend

---

**🎉 You're all set! Start exploring Skillora and transform placement preparation! 🎉**

---

**Questions? Issues? Suggestions?**
Check PROJECT_STATUS.md for detailed technical documentation.
