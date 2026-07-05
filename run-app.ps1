param(
    [switch]$MySql
)

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendPath = Join-Path $projectRoot "frontend"
$backendPort = 8080
$frontendPort = 5173

function Get-ListeningProcess {
    param(
        [int]$Port
    )

    $conn = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $conn) {
        return $null
    }

    return Get-Process -Id $conn.OwningProcess -ErrorAction SilentlyContinue
}

if ($MySql) {
    $backendCommand = "Set-Location -LiteralPath '$projectRoot'; & .\gradlew.bat bootRun --args='--spring.profiles.active=mysql'"
} else {
    $backendCommand = "Set-Location -LiteralPath '$projectRoot'; & .\gradlew.bat bootRun"
}

$frontendCommand = "Set-Location -LiteralPath '$frontendPath'; npm.cmd run dev"

if (Get-ListeningProcess -Port $backendPort) {
    $backendProcess = Get-ListeningProcess -Port $backendPort
    Write-Host "Backend not started: port $backendPort is already in use by $($backendProcess.ProcessName) (PID $($backendProcess.Id))."
} else {
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $backendCommand
    Write-Host "Backend launch command started on port $backendPort."
}

Start-Sleep -Seconds 2
if (Get-ListeningProcess -Port $frontendPort) {
    $frontendProcess = Get-ListeningProcess -Port $frontendPort
    Write-Host "Frontend not started: port $frontendPort is already in use by $($frontendProcess.ProcessName) (PID $($frontendProcess.Id))."
} else {
    Start-Process -FilePath "powershell.exe" -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $frontendCommand
    Write-Host "Frontend launch command started on port $frontendPort."
}

Write-Host "Open http://localhost:5173 after servers are ready."
