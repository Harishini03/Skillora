# Skillora — Railway Deployment Checklist

## What you need to do (3 steps only)

---

## Step 1 — Create the Railway project

1. Go to https://railway.app → **New Project**
2. Choose **"Deploy from GitHub repo"**
3. Select **Harishini03/Skillora** → click **Deploy Now**
4. Railway detects the `Dockerfile` automatically — no config needed

---

## Step 2 — Add a MySQL database

In your Railway project dashboard:
1. Click **"+ New"** → **"Database"** → **"Add MySQL"**
2. Railway spins up a MySQL 8 instance and links it to your project

---

## Step 3 — Set environment variables

In your backend service → **Variables** tab → click **"Raw Editor"** and paste:

```
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
JWT_SECRET=5b4f232ec51721fa9dfe606c53171c346b956f3e1e0465e36189f49451bee2f9
GROQ_API_KEY=PASTE_YOUR_NEW_GROQ_KEY_HERE
CORS_ALLOWED_ORIGINS=https://YOUR-APP.up.railway.app
```

> **Important:**
> - Replace `PASTE_YOUR_NEW_GROQ_KEY_HERE` with a fresh key from https://console.groq.com/keys
> - After Railway gives you the app URL (ends in `.up.railway.app`), update `CORS_ALLOWED_ORIGINS`

---

## What happens automatically on first boot

- Hibernate creates all database tables (`ddl-auto=update`)
- `ProductionDataSeeder` seeds: departments, skills, companies, eligibility criteria, 30 questions
- `DemoAccountSeeder` creates demo accounts:
  - `student.demo` / `Skillora@123`
  - `staff.demo` / `Skillora@123`
  - `recruiter.demo` / `Skillora@123`

---

## After deployment

Your app runs at: `https://YOUR-APP.up.railway.app`

Health check: `https://YOUR-APP.up.railway.app/actuator/health`

---

## Notes

- First build takes ~8–12 min (Docker multi-stage: Node 20 + JDK 17)
- Subsequent deploys auto-trigger on every `git push origin main`
- Free Railway plan: $5 credit/month — enough for light usage
- Firebase Auth is pre-configured (project: `skillora-94d25`)
