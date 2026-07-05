# Skillora - Working Guide

## ✅ Current Status

**Both services are RUNNING:**
- ✅ Backend: http://localhost:8080 (Spring Boot)
- ✅ Frontend: http://localhost:5173 (React + Vite)

## 🔧 Fixed Issues

### 1. Dashboard Connection
- Fixed API endpoint configuration
- Added proper error handling
- Added loading states
- Improved error messages

### 2. What Should Work Now

**Login:**
```
Username: student1
Password: password
```

**After Login, you should see:**
1. Dashboard with loading animation
2. If backend is connected: Your readiness score, progress bars, weak areas
3. If backend is disconnected: Clear error message with instructions

## 🚀 Quick Test

### Step 1: Open Browser
Navigate to: **http://localhost:5173**

### Step 2: Login
- Username: `student1`
- Password: `password`

### Step 3: Check Dashboard
You should see:
- Readiness Score
- Department
- CGPA
- Progress bars for Aptitude, Coding, Soft Skills
- Weak Areas
- Recommendations

## 🔍 Troubleshooting

### If Dashboard Shows "Connection Error"

**Check 1: Is Backend Running?**
```powershell
# Open PowerShell and run:
curl http://localhost:8080/api/student/dashboard
```

If this fails, restart backend:
```powershell
cd "d:\Project\placement intelligence\Placement_intelligence"
.\gradlew.bat bootRun
```

**Check 2: Is Token Valid?**
Open Browser Console (F12) and check:
- localStorage.getItem("pi_token") should show a JWT token
- If missing, logout and login again

**Check 3: Browser Console Errors**
- Press F12
- Go to Console tab
- Look for red errors
- Check Network tab for failed requests

### If Login Fails

**Check Authentication:**
```powershell
curl -X POST http://localhost:8080/login `
  -H "Content-Type: application/json" `
  -d '{\"username\":\"student1\",\"password\":\"password\"}'
```

Should return:
```json
{
  "token": "eyJhbGc...",
  "role": "STUDENT",
  "name": "Student One"
}
```

## 📁 Key Files

### Frontend
- **API Config:** `frontend/src/lib/api.js`
- **Dashboard:** `frontend/src/pages/StudentPages.jsx`
- **Auth Context:** `frontend/src/context/AuthContext.jsx`
- **Vite Proxy:** `frontend/vite.config.js`

### Backend
- **Dashboard Controller:** `src/main/java/.../controller/StudentController.java`
- **Dashboard Service:** `src/main/java/.../service/StudentDashboardService.java`
- **Security Config:** `src/main/java/.../security/SecurityConfig.java`

## 🎯 Next Steps to Simplify

Since you mentioned the code is too complex and confusing, here's what I recommend:

### Option 1: Keep Core Features Only
Remove:
- Firebase integration
- CSV export
- Complex analytics
- Multiple fallback endpoints

Keep:
- Login/Logout
- Dashboard
- Profile
- Tests
- Basic progress tracking

### Option 2: Fresh Modern UI
I can create a completely new, simplified frontend with:
- Modern Tailwind UI components
- Simple, clean code
- Only essential features
- Better error handling
- Loading states everywhere

### Option 3: Debug Current Issues First
Let's first make sure current features work:
1. Fix dashboard loading
2. Test login flow
3. Verify each page
4. Remove broken features

**Which option would you prefer?**

## 🐛 Common Issues & Solutions

### Issue 1: "Failed to load dashboard"
**Solution:** Make sure backend is running on port 8080

### Issue 2: Blank page after login
**Solution:** Check browser console for errors, clear localStorage

### Issue 3: CORS errors
**Solution:** Backend CORS is configured for ports 5173 and 5174

### Issue 4: 401 Unauthorized
**Solution:** Token expired, logout and login again

### Issue 5: Proxy not working
**Solution:** Restart Vite dev server (stop and start frontend)

## 💡 Quick Commands

### Restart Everything
```powershell
# Terminal 1 - Backend
cd "d:\Project\placement intelligence\Placement_intelligence"
.\gradlew.bat clean bootRun

# Terminal 2 - Frontend  
cd "d:\Project\placement intelligence\Placement_intelligence\frontend"
npm run dev
```

### Clear Everything and Start Fresh
```powershell
# Stop both services (Ctrl+C in their terminals)

# Clear frontend cache
cd frontend
rm -rf node_modules/.vite
npm run dev

# Backend will auto-reload
```

### Check if Services are Running
```powershell
# Check backend
curl http://localhost:8080

# Check frontend
curl http://localhost:5173
```

## 📊 What's Currently Working

✅ **Working Features:**
- Authentication (Login/Logout)
- JWT Token Management
- Role-based access (STUDENT, STAFF, RECRUITER)
- Navigation sidebar
- Routing

❓ **Needs Testing:**
- Dashboard data loading
- Profile page
- Course pages
- Coding problems
- AI Mentor
- Tests

❌ **Known Issues:**
- Dashboard may show "Failed to load" on first try
- Some endpoints might return 404
- Complex error handling needs simplification

## 🔐 Default Test Accounts

```
Student Account:
- Username: student1
- Password: password
- Role: STUDENT

Staff Account:
- Username: staffadmin  
- Password: password
- Role: STAFF

Recruiter Account:
- Username: recruiter1
- Password: password
- Role: RECRUITER
```

## 🎨 UI Components Available

Your app uses Tailwind CSS with custom classes:
- `.portal-banner` - Hero sections with gradient
- `.dashboard-card` - White cards with shadow
- `.soft-pop` - Cards with hover effect

Example:
```jsx
<div className="dashboard-card p-6">
  <h3 className="text-lg font-bold text-slate-900">Title</h3>
  <p className="text-slate-600">Content</p>
</div>
```

---

## 🆘 Need Help?

If nothing works, we can:

1. **Simplify everything** - Remove 80% of code, keep only essentials
2. **Fresh start** - New clean project with modern UI
3. **Debug step by step** - Fix one feature at a time

**What would you like to do?**
