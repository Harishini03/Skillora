# Skillora: Advanced Placement Intelligence Platform

Skillora is a comprehensive, AI-driven placement preparation platform built for engineering students (B.Tech). Designed specifically for campus placements at top Indian IT firms (TCS, Infosys, Wipro, Amazon, Microsoft), Skillora provides adaptive learning paths, real-time mock interviews, and a fully featured code execution environment.

## 🚀 Key Features

*   **AI Mentor (Skillora AI)**: Context-aware, dynamic preparation module for Aptitude, SQL, and Data Structures. It generates mock placement tests tailored to specific companies and adapts difficulty based on performance.
*   **LeetCode-Style Code Editor**: Fully featured IDE with synchronized line numbering, bracket autoclosing, smart indentations, and a collapsible test case console.
*   **Role-Based Dashboards**: 
    *   **Student**: Track progress, solve DSA problems, take mock tests.
    *   **Staff/Admin**: Oversee placement readiness, track student metrics, and manage content.
    *   **Recruiter**: View candidate profiles, verify skills, and scout top talent.
*   **Secure Authentication**: Role-based JWT authentication with Spring Security and Firebase.

## 🛠️ Technology Stack

*   **Frontend**: React 19 (Vite), Tailwind CSS, Vanilla CSS (Modern aesthetic with JetBrains Mono typography).
*   **Backend**: Spring Boot 3.5.6 (Java 17), Spring Security, Spring Data JPA.
*   **Database**: PostgreSQL.
*   **AI Integration**: Groq API (Llama 3.3 70B) with dynamic offline fallbacks.
*   **Code Execution**: Integrated backend compiler engine supporting Python, Java, JavaScript, and C++.

## 💻 Getting Started

### Prerequisites
*   Node.js & npm
*   Java 17 (JDK)
*   PostgreSQL database

### Running Locally

1. **Backend**:
   Configure your `application.properties` (or `.env`) with your Database and Groq API keys.
   ```bash
   ./gradlew bootRun
   ```

2. **Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

3. Open `http://localhost:5173` in your browser.

## 📄 License
This project is licensed under the MIT License.
