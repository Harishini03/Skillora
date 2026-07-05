# Test dashboard endpoint
Write-Host "Testing Student Dashboard..." -ForegroundColor Cyan

# Login
$loginBody = '{"username":"student.demo","password":"Skillora@123"}'
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $token = $loginResponse.token
    Write-Host "Login successful!" -ForegroundColor Green
} catch {
    Write-Host "Login failed: $($_.Exception)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails)" -ForegroundColor Red
    exit 1
}

# Get dashboard
try {
    $headers = @{"Authorization" = "Bearer $token"}
    $dashboard = Invoke-RestMethod -Uri "http://localhost:8080/api/student/dashboard" -Method GET -Headers $headers
    
    Write-Host "`nDashboard Data:" -ForegroundColor Yellow
    Write-Host "Name: $($dashboard.name)"
    Write-Host "Department: $($dashboard.department)"
    Write-Host "CGPA: $($dashboard.cgpa)"
    Write-Host "Readiness Score: $($dashboard.readinessScore)%"
    Write-Host "Aptitude: $($dashboard.aptitudeProgress)%"
    Write-Host "Coding: $($dashboard.codingProgress)%"
    Write-Host "Soft Skills: $($dashboard.softSkillsProgress)%"
    
    Write-Host "`nWeak Areas:" -ForegroundColor Yellow
    $dashboard.weakAreas | ForEach-Object { Write-Host "  - $_" }
    
    Write-Host "`nRecommendations:" -ForegroundColor Yellow
    $dashboard.recommendations | ForEach-Object { Write-Host "  - $_" }
    
    Write-Host "`nTest passed!" -ForegroundColor Green
} catch {
    Write-Host "Dashboard fetch failed" -ForegroundColor Red
    exit 1
}
