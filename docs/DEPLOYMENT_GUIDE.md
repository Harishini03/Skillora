# Skillora Deployment Guide

## Prerequisites

| Tool | Minimum Version | Purpose |
|------|----------------|---------|
| Java (JDK) | 17 | Build and run the Spring Boot backend |
| Node.js | 20 | Build and serve the React frontend |
| npm | 9+ | Frontend dependency management |
| Docker | 24+ | Containerised deployment (optional) |
| Docker Compose | 2.20+ | Multi-container orchestration (optional) |
| Git | 2.40+ | Clone the repository |

---

## Local Development Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Placement_intelligence
```

### 2. Configure Environment Variables
Copy the example environment file and fill in your values:
```bash
cp .env.example .env
```

Edit `.env` and set at minimum:
- `JWT_SECRET` — a random 256-bit string (e.g. generate with `openssl rand -hex 32`)
- `GROQ_API_KEY` — your key from [console.groq.com](https://console.groq.com)

> **Note:** For local development the backend defaults to an H2 file-based database stored in `./data/`. No database installation is required.

### 3. Start the Backend
```bash
# Windows
.\gradlew.bat bootRun

# macOS / Linux
./gradlew bootRun
```

The backend starts on **http://localhost:8080**.

To use MySQL locally instead of H2:
```bash
# Windows
.\gradlew.bat bootRun --args="--spring.profiles.active=mysql"
```

### 4. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```

The frontend dev server starts on **http://localhost:5173** with hot reload enabled.

### 5. Verify the Setup
Open **http://localhost:5173** in your browser. Log in with a demo account:

| Role | Email | Password |
|------|-------|----------|
| Student | `student@skillora.com` | `Skillora@123` |
| Staff | `staff@skillora.com` | `Skillora@123` |
| Recruiter | `recruiter@skillora.com` | `Skillora@123` |

---

## Docker Deployment

### 1. Create the Environment File
```bash
cp .env.example .env
```

Edit `.env` and set all required variables (see [Environment Variables Reference](#environment-variables-reference) below).

### 2. Build and Start All Services
```bash
docker-compose up --build -d
```

This starts three containers:
- `backend` — Spring Boot API on port **8080**
- `db` — MySQL 8.0 on an internal network (not exposed externally)
- `frontend` — React static build served on port **3000**

### 3. Check Service Health
```bash
docker-compose ps
docker-compose logs backend
```

Wait for the backend health check to pass (up to 90 seconds on first run while MySQL initialises).

### 4. Access the Application
- Frontend: **http://localhost:3000**
- API: **http://localhost:8080**

### 5. Stop the Application
```bash
docker-compose down
```

To also remove the database volume (all data):
```bash
docker-compose down -v
```

### Rebuilding After Code Changes
```bash
docker-compose up --build -d backend
```

---

## Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_PASSWORD` | Yes (Docker) | — | MySQL `skillora` user password |
| `DB_ROOT_PASSWORD` | Yes (Docker) | — | MySQL root password |
| `JWT_SECRET` | Yes | dev fallback | 256-bit secret for signing JWT tokens. **Change in production.** |
| `GROQ_API_KEY` | Yes | — | API key for Groq AI (llama-3.3-70b) |
| `GOOGLE_CLIENT_ID` | No | — | Google OAuth 2.0 client ID (enables Google login) |
| `DB_URL` | No | H2 file DB | Override datasource URL for local MySQL |
| `DB_USERNAME` | No | `sa` | Database username |
| `CORS_ALLOWED_ORIGINS` | No | localhost variants | Comma-separated list of allowed CORS origins |
| `JWT_EXPIRATION_MS` | No | `86400000` | JWT token lifetime in milliseconds (default 24 h) |
| `GROQ_MODEL` | No | `llama-3.3-70b-versatile` | Groq model name |

---

## Production Checklist

### Security
- [ ] Replace `JWT_SECRET` with a cryptographically random 256-bit value
- [ ] Set strong, unique passwords for `DB_PASSWORD` and `DB_ROOT_PASSWORD`
- [ ] Disable H2 console (`spring.h2.console.enabled=false` — already default in prod profile)
- [ ] Ensure the backend is behind a reverse proxy (Nginx/Caddy) with TLS
- [ ] Configure CORS (`CORS_ALLOWED_ORIGINS`) to list only your frontend domain
- [ ] Store all secrets in a secrets manager or CI/CD environment — never commit `.env` to git

### Database
- [ ] Run `schema.sql` against your production MySQL instance before first boot
- [ ] Enable automated daily backups on the MySQL volume / managed DB
- [ ] Use a managed database service (RDS, PlanetScale, etc.) for high availability

### Performance
- [ ] Use `eclipse-temurin:17-jre-alpine` runtime image (already in `Dockerfile`)
- [ ] Set JVM heap limits appropriate to your container: add `-Xmx512m` (or higher) to `ENTRYPOINT`
- [ ] Enable MySQL query cache and connection pooling (HikariCP is configured by default)

### Frontend
- [ ] Set `VITE_API_URL` to your production API domain before building
- [ ] Serve the built `dist/` from a CDN or Nginx for best performance
- [ ] Enable gzip/brotli compression on your reverse proxy

### Monitoring
- [ ] Configure structured JSON logging (already enabled via `logback-spring.xml`)
- [ ] Route logs to a log aggregation service (Loki, CloudWatch, etc.)
- [ ] Set up uptime monitoring on the `/api/auth/google-client-id` health endpoint
- [ ] Configure alerts for error rate spikes and high response times

### CI/CD
- [ ] Run `./gradlew test` in your pipeline before building the Docker image
- [ ] Tag Docker images with the git commit SHA for traceability
- [ ] Deploy to a staging environment and run smoke tests before promoting to production

---

## Building for Production Without Docker

### Backend JAR
```bash
./gradlew clean bootJar
java -jar -Dspring.profiles.active=prod build/libs/Placement_Intelligence-0.0.1-SNAPSHOT.jar
```

### Frontend Static Files
```bash
cd frontend
npm ci
VITE_API_URL=https://api.yourproductiondomain.com npm run build
# Output is in frontend/dist/ — serve with Nginx or any static host
```

### Nginx Configuration Example
```nginx
server {
    listen 80;
    server_name yourproductiondomain.com;
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name yourproductiondomain.com;

    ssl_certificate /etc/letsencrypt/live/yourproductiondomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourproductiondomain.com/privkey.pem;

    # Serve React frontend
    root /var/www/skillora/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Proxy API requests to Spring Boot
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
