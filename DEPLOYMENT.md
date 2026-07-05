# Skillora — Complete Deployment Guide

## Quick Reference

| Environment | Frontend | Backend | Database |
|---|---|---|---|
| Local Dev | http://localhost:5173 | http://localhost:8080 | H2 (auto) |
| Docker | http://localhost:3000 | http://localhost:8080 | MySQL (container) |
| Production | https://skillora.com | https://skillora.com/api | MySQL (hosted) |

---

## Option A: Local Development (Quickest)

```bash
# Terminal 1 — Backend
.\gradlew bootRun --no-daemon

# Terminal 2 — Frontend
cd frontend
npm install
npm run dev
```

Open http://localhost:5173

**Demo accounts** (password: `Skillora@123`):
| Role | Username |
|---|---|
| Student | student.demo |
| Staff | staff.demo |
| Recruiter | recruiter.demo |

---

## Option B: Docker Compose (Recommended for staging)

### 1. Set environment variables
```bash
cp .env.production .env
```
Edit `.env` and set:
- `DB_PASSWORD` — strong MySQL password
- `DB_ROOT_PASSWORD` — MySQL root password  
- `JWT_SECRET` — run `openssl rand -hex 32`
- `GROQ_API_KEY` — your Groq API key from https://console.groq.com/keys
- `CORS_ALLOWED_ORIGINS` — your domain or `http://localhost:3000`

### 2. Build and start
```bash
docker-compose up --build -d
```

### 3. Check status
```bash
docker-compose ps
docker-compose logs backend
```

### 4. Access
- Frontend: http://localhost:3000
- API: http://localhost:8080

---

## Option C: Production Server (VPS/Cloud)

### Prerequisites
- Ubuntu 22.04 LTS server
- Java 17+ installed
- MySQL 8.0 installed
- Nginx installed
- Your domain pointing to server IP

### Step 1 — Server setup
```bash
sudo bash scripts/setup-server.sh
```

### Step 2 — Database
```bash
mysql -u root -p < scripts/setup-mysql.sql
# Enter your MySQL root password when prompted
```
Then edit the script first to set a strong password for `skillora_user`.

### Step 3 — Build artifacts
```bash
# Backend
.\gradlew bootJar --no-daemon -x test
# JAR: build/libs/Placement_Intelligence-0.0.1-SNAPSHOT.jar

# Frontend
cd frontend
npm ci
npm run build
# Output: frontend/dist/
```

### Step 4 — Deploy files
```bash
# Copy JAR to server
scp build/libs/*.jar user@your-server:/opt/skillora/backend/app.jar

# Copy frontend
scp -r frontend/dist/* user@your-server:/opt/skillora/frontend/dist/

# Copy environment file
scp .env user@your-server:/opt/skillora/.env
```

### Step 5 — SSL certificate
```bash
certbot --nginx -d skillora.com -d www.skillora.com
```

### Step 6 — Start backend service
```bash
sudo systemctl start skillora
sudo systemctl status skillora
```

### Step 7 — Verify
```bash
curl https://skillora.com/api/actuator/health
# Should return: {"status":"UP"}
```

---

## Environment Variables Reference

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | ✅ | 64-char hex string for JWT signing |
| `DB_PASSWORD` | ✅ | MySQL password for `skillora_user` |
| `DB_USERNAME` | ✅ | MySQL username (default: `skillora_user`) |
| `DB_URL` | ✅ | MySQL JDBC URL |
| `GROQ_API_KEY` | ✅ | Groq API key for AI features |
| `CORS_ALLOWED_ORIGINS` | ✅ | Comma-separated allowed origins |
| `GOOGLE_CLIENT_ID` | optional | For Google OAuth login |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | optional | For Firebase Auth |
| `FIREBASE_PROJECT_ID` | optional | `skillora-94d25` |

---

## Firebase Configuration (Already Configured)

Frontend Firebase config is already set in `frontend/.env.local`:
```
VITE_FIREBASE_API_KEY=AIzaSyAzGupH350FvSl7fb7tnzP843kLyLEZPCw
VITE_FIREBASE_AUTH_DOMAIN=skillora-94d25.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=skillora-94d25
```

For backend Firebase Auth (optional — enables `/api/auth/firebase-login`):
1. Go to Firebase Console → Project Settings → Service Accounts
2. Click "Generate new private key"
3. Set the JSON contents as `FIREBASE_SERVICE_ACCOUNT_JSON` env var

---

## Pre-Deploy Checklist

### Code
- [x] All 54 tests passing
- [x] Backend JAR built (112 MB)
- [x] Frontend built with code splitting
- [x] Production profile configured
- [x] Security headers enabled
- [x] Rate limiting active (20 req/min on auth, 200/min on API)
- [x] BCrypt password strength 12
- [x] H2 console disabled in prod
- [x] SQL errors hidden from responses
- [x] CORS properly configured

### Infrastructure (your responsibility)
- [ ] MySQL server running
- [ ] Database `skillora_db` created
- [ ] `skillora_user` created with correct password
- [ ] Nginx installed and configured
- [ ] SSL certificate from Let's Encrypt
- [ ] Domain DNS pointing to server
- [ ] Firewall: only ports 80, 443, 22 open

### Environment
- [ ] `JWT_SECRET` set to 64-char random hex
- [ ] `DB_PASSWORD` set to strong password
- [ ] `CORS_ALLOWED_ORIGINS` set to production domain
- [ ] `GROQ_API_KEY` set (get from https://console.groq.com/keys)
- [ ] Firebase service account JSON set (if using Firebase Auth)

---

## Monitoring & Maintenance

### Health check endpoint
```
GET /actuator/health
```

### View logs
```bash
# Systemd
sudo journalctl -u skillora -f

# File logs
tail -f /opt/skillora/logs/app.log
```

### Database backup
```bash
mysqldump -u skillora_user -p skillora_db > backup-$(date +%Y%m%d).sql
```

### Update deployment
```bash
# Rebuild and redeploy
./gradlew bootJar --no-daemon -x test
scp build/libs/*.jar user@server:/opt/skillora/backend/app.jar
ssh user@server "sudo systemctl restart skillora"
```

---

## Skillora Firebase Project Details
- **Project ID**: `skillora-94d25`
- **Auth Domain**: `skillora-94d25.firebaseapp.com`
- **Measurement ID**: `G-R1RNW325D3`
- **App ID**: `1:958098343020:web:557cc196c92d7ec4d7c449`

Enable in Firebase Console:
1. Authentication → Sign-in method → Enable **Email/Password** + **Google**
2. Authentication → Settings → Authorized domains → Add your production domain
