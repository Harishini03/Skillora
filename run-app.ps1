# Skillora startup script — sets Groq API key and launches backend
Write-Host "Starting Skillora Backend with Groq AI..." -ForegroundColor Cyan

# Load from .env if present
if (Test-Path ".env") {
    Get-Content .env | Foreach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line.Split('=', 2)
            if ($parts.Length -eq 2) {
                $name = $parts[0].Trim()
                $value = $parts[1].Trim()
                [Environment]::SetEnvironmentVariable($name, $value, "Process")
            }
        }
    }
}

if (-not $env:GROQ_API_KEY) {
    Write-Host "Warning: GROQ_API_KEY is not set. Falling back to offline mode." -ForegroundColor Yellow
} else {
    Write-Host "GROQ_API_KEY is loaded from .env successfully!" -ForegroundColor Green
}

# Kill any existing process on port 8080
$proc = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess
if ($proc) {
    Stop-Process -Id $proc -Force -ErrorAction SilentlyContinue
    Write-Host "Stopped previous backend on port 8080." -ForegroundColor Yellow
    Start-Sleep -Seconds 2
}

./gradlew bootRun
