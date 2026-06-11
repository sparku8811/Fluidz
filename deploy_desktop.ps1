# Fluidz Desktop Deployment Script
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"

Write-Host "🚀 Starting Fluidz Desktop Build..." -ForegroundColor Cyan

# Attempt MSIX
Write-Host "📦 Attempting MSIX build..." -ForegroundColor Yellow
.\gradlew packageMsix
if ($?) {
    Write-Host "✅ MSIX Successful!" -ForegroundColor Green
} else {
    # Attempt MSI
    Write-Host "⚠️ MSIX Failed. Attempting MSI build..." -ForegroundColor Yellow
    .\gradlew packageMsi
    if ($?) {
        Write-Host "✅ MSI Successful!" -ForegroundColor Green
    } else {
        # Attempt EXE
        Write-Host "⚠️ MSI Failed. Attempting EXE fallback..." -ForegroundColor Yellow
        .\gradlew packageExe
        if ($?) {
            Write-Host "✅ EXE Successful!" -ForegroundColor Green
        } else {
            Write-Host "❌ All builds failed." -ForegroundColor Red
        }
    }
}

$target = "composeApp/build/compose/binaries/main"
if (Test-Path $target) {
    explorer $target
}
