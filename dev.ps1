# Habitik Development Helper Script
# Usage: .\dev.ps1 [-LogOnly] [-Watch] [-Wireless IP]

param(
    [switch]$LogOnly,
    [switch]$Watch,
    [string]$Wireless
)

$PACKAGE_NAME = "com.habitik"
$ACTIVITY_NAME = "$PACKAGE_NAME/.MainActivity"
$SRC_PATH = "./app/src"

function Show-Header {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "   Habitik Dev Server (CLI Edition)   " -ForegroundColor Cyan
    Write-Host "========================================`n" -ForegroundColor Cyan
}

function Run-Build {
    Write-Host "[1/3] Building and Installing app..." -ForegroundColor Yellow
    cmd /c "gradlew.bat installDebug"
}

function Launch-App {
    Write-Host "[2/3] Launching $ACTIVITY_NAME..." -ForegroundColor Yellow
    $devices = adb devices | Select-String "device$"
    if (-not $devices) {
        Write-Host "[!] No devices found. Is your phone connected?" -ForegroundColor Red
        return 1
    }
    adb shell am start -n $ACTIVITY_NAME
    return 0
}

function Stream-Logs {
    Write-Host "[3/3] Starting Logcat stream (Package: $PACKAGE_NAME)..." -ForegroundColor Yellow
    Write-Host "--- Press Ctrl+C to stop ---`n" -ForegroundColor Gray
    # Use a broader filter to ensure we see logs
    adb logcat *:V | Select-String $PACKAGE_NAME
}

function Get-LastWriteTime {
    return (Get-ChildItem -Path $SRC_PATH -Recurse | Measure-Object -Property LastWriteTime -Maximum).Maximum
}

# --- Main Logic ---

Clear-Host
Show-Header

if ($Wireless) {
    Write-Host "Connecting to $Wireless..." -ForegroundColor Cyan
    adb connect "$Wireless:5555"
}

if ($LogOnly) {
    Stream-Logs
    exit
}

do {
    Run-Build
    if ($LASTEXITCODE -eq 0) {
        $launchResult = Launch-App
        if ($launchResult -eq 0 -and -not $Watch) {
            Stream-Logs
        }
    } else {
        Write-Host "`n[!] Build Failed. Check errors above." -ForegroundColor Red
    }

    if ($Watch) {
        Write-Host "`n[WATCH] Waiting for changes in $SRC_PATH..." -ForegroundColor Green
        $lastChange = Get-LastWriteTime
        while ($true) {
            Start-Sleep -Seconds 2
            $currentChange = Get-LastWriteTime
            if ($currentChange -gt $lastChange) {
                Write-Host "`n[!] Change detected at $(Get-Date -Format "HH:mm:ss")" -ForegroundColor Cyan
                break
            }
        }
    }
} while ($Watch)
