# Skillora - AI-Powered Placement Intelligence System

> Transform campus recruitment with intelligent placement readiness tracking, AI-powered learning, and comprehensive career preparation tools.

## 🚀 Overview

Skillora is a production-ready placement intelligence platform that connects students, institutions, and recruiters through one unified system. Built with modern technologies, it provides comprehensive tools for placement preparation including AI mentoring, coding practice, aptitude tests, and placement tracking.

## ✨ Key Features

### For Students
- 🎓 **AI-Powered Learning**: Adaptive learning system with 5 modes (Learn, Practice, Adaptive, Revision, Mock Test)
- 💻 **Multi-Language Coding Platform**: Practice coding in Java, Python, JavaScript, and C++ with sandboxed execution
- 📝 **Aptitude Test System**: AI-generated questions with detailed analytics
- 📊 **Readiness Dashboard**: Track progress across aptitude, coding, and soft skills
- 🎯 **Job Application Portal**: Browse jobs, check eligibility, and apply seamlessly
- 📈 **Performance Analytics**: Detailed insights into strengths and weak areas

### For Recruiters
- 📢 **Job Posting Management**: Create and manage job postings with eligibility criteria
- 🔍 **Candidate Discovery**: Search and filter qualified candidates
- 📅 **Interview Scheduling**: Schedule and manage interviews with conflict prevention
- 💬 **Feedback System**: Provide structured feedback and ratings

### For Staff/Admin
- 📊 **Placement Analytics**: Comprehensive dashboard with placement statistics
- 🎓 **Student Management**: Track student progress and readiness scores
- 📈 **Department-wise Reports**: Analyze performance by department
- 📥 **Data Export**: Download reports in CSV format

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 17
- **Security**: Spring Security 6 + JWT
- **Database**: H2 (Development) / MySQL (Production)
- **ORM**: Spring Data JPA
- **AI Integration**: Groq API (llama-3.3-70b-versatile)

### Frontend
- **Library**: React 19.2.4
- **Build Tool**: Vite 8.0.1
- **Styling**: Tailwind CSS 4.2.2
- **Routing**: React Router 7.13.1
- **HTTP Client**: Axios 1.13.6
- **Charts**: Recharts 3.8.0

## 📋 Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- Git

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Placement_intelligence
```

### 2. Backend Setup
```bash
# Build the project
./gradlew build

# Run the backend server
./gradlew bootRun
```

Backend will start on `http://localhost:8080`

### 3. Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start on `http://localhost:5173`

### 4. Access the Application
Open your browser and navigate to `http://localhost:5173`

## 🔐 Demo Accounts

Test the application with these pre-configured demo accounts:

| Role | Email | Password |
|------|-------|----------|
| Student | `student@skillora.com` | `Skillora@123` |
| Staff | `staff@skillora.com` | `Skillora@123` |
| Recruiter | `recruiter@skillora.com` | `Skillora@123` |

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the project root:

```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/skillora
DB_USERNAME=root
DB_PASSWORD=yourpassword

# JWT Configuration
JWT_SECRET=your-super-secret-key-min-256-bits
JWT_EXPIRATION_MS=86400000

# Groq AI API
GROQ_API_KEY=your-groq-api-key
GROQ_MODEL=llama-3.3-70b-versatile

# Google OAuth (Optional)
GOOGLE_CLIENT_ID=your-google-client-id

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

### Database Setup

**For Development (H2 - Default)**:
No setup required. H2 database will be created automatically in `./data/` directory.

**For Production (MySQL)**:
```sql
CREATE DATABASE skillora CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then update `application.properties` or set environment variables.

## 📁 Project Structure

```
Placement_intelligence/
├── src/main/java/com/placement/placement_intelligence/
│   ├── config/          # Security, CORS, and app configuration
│   ├── controller/      # REST API controllers
│   ├── dto/             # Data Transfer Objects
│   ├── model/           # JPA Entities
│   ├── repository/      # Spring Data repositories
│   ├── security/        # JWT and authentication
│   ├── service/         # Business logic services
│   └── PlacementIntelligenceApplication.java
├── src/main/resources/
│   ├── application.properties    # Main configuration
│   └── schema.sql               # Database schema
├── frontend/
│   ├── src/
│   │   ├── components/   # Reusable React components
│   │   ├── context/      # React Context (Auth, etc.)
│   │   ├── lib/          # Utilities (API client, etc.)
│   │   └── pages/        # Page components
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── .kiro/
│   └── specs/            # Feature specifications
├── build.gradle
└── README.md
```

## 🧪 Testing

### Backend Tests
```bash
./gradlew test
```

### Frontend Tests
```bash
cd frontend
npm run test
```

## 🏗️ Building for Production

### Backend
```bash
./gradlew clean build
java -jar build/libs/Placement_Intelligence-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm run build
```

Build output will be in `frontend/dist/`

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [API Documentation](docs/API_DOCUMENTATION.md) | Complete REST API reference with request/response examples |
| [Deployment Guide](docs/DEPLOYMENT_GUIDE.md) | Local setup, Docker deployment, and production checklist |

## 🐳 Docker Quick Start

Make sure Docker and Docker Compose are installed, then:

```bash
# 1. Copy and configure environment variables
cp .env.example .env
# Edit .env and set DB_PASSWORD, DB_ROOT_PASSWORD, JWT_SECRET, GROQ_API_KEY

# 2. Build and start all services (backend + MySQL + frontend)
docker-compose up --build -d

# 3. Open the app
#    Frontend: http://localhost:3000
#    API:      http://localhost:8080
```

To stop:
```bash
docker-compose down
```

See [docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md) for full production deployment instructions.

## 🔒 Security Features

- ✅ JWT-based authentication with 24-hour expiry
- ✅ BCrypt password hashing (strength 12)
- ✅ Role-based access control (STUDENT, STAFF, RECRUITER)
- ✅ CORS whitelist configuration
- ✅ Security headers (CSP, HSTS, X-Frame-Options)
- ✅ Input validation and sanitization
- ✅ SQL injection prevention via JPA
- ✅ Rate limiting on auth endpoints (planned)

## 🎨 UI/UX Features

- ✨ Modern glassmorphism design
- 🎭 Smooth animations and transitions
- 📱 Fully responsive (mobile, tablet, desktop)
- 🌐 SEO optimized with proper meta tags
- ♿ Accessibility compliant (WCAG 2.1)
- 🎨 Custom design system with CSS variables
- 🔤 Modern typography (Inter, Poppins, Space Grotesk)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is proprietary software. All rights reserved.

## 👥 Team

- **Project**: Skillora - AI-Powered Placement Intelligence
- **Architecture**: Spring Boot + React + Modern UI/UX
- **Status**: Production-Ready

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Contact the development team

---

Built with ❤️ using Spring Boot, React, and modern web technologies.
