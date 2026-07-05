# Firebase Authentication Setup

## Overview
Skillora supports Firebase Authentication as an optional login method alongside the built-in email/password system. When configured, users see "Sign in with Firebase Email" and "Sign in with Google (Firebase)" buttons on the login page.

## Step 1 — Create a Firebase Project
1. Go to https://console.firebase.google.com
2. Click "Add project" → give it a name (e.g. "skillora-auth")
3. Disable Google Analytics if not needed → Create project

## Step 2 — Enable Authentication Providers
1. In Firebase Console → Authentication → Sign-in method
2. Enable **Email/Password**
3. Enable **Google**

## Step 3 — Get Frontend Config
1. Firebase Console → Project Settings → Your apps → Add app → Web
2. Register app (name: "Skillora Web")
3. Copy the firebaseConfig object values
4. Create `frontend/.env.local`:
```
VITE_FIREBASE_API_KEY=AIza...
VITE_FIREBASE_AUTH_DOMAIN=your-project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your-project-id
VITE_FIREBASE_STORAGE_BUCKET=your-project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=123456789
VITE_FIREBASE_APP_ID=1:123456789:web:abc123
```

## Step 4 — Get Backend Service Account
1. Firebase Console → Project Settings → Service accounts
2. Click "Generate new private key" → download the JSON file
3. Either:
   - Set env var: `FIREBASE_SERVICE_ACCOUNT_JSON=<paste entire JSON on one line>`
   - Or set path: `FIREBASE_SERVICE_ACCOUNT_PATH=/path/to/serviceAccountKey.json`

## Step 5 — Restart Services
- Restart the Spring Boot backend
- Restart the frontend dev server (`npm run dev`)

Firebase login buttons will appear automatically once the frontend env vars are set.

## Notes
- Firebase Auth is **optional** — the existing email/password system continues to work without any Firebase config
- Existing users can log in with Firebase if their email matches what's already in the database
- New users signing up via Firebase must provide their role (STUDENT/STAFF/RECRUITER) in the signup form before clicking the Firebase button
- The backend `/api/auth/firebase-login` endpoint is under `/api/auth/**` which is already publicly permitted — no security config changes needed
