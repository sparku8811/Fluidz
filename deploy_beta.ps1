# Fluidz Unified Beta Deployment Script (Android + Windows)
Write-Host "🚀 Starting Fluidz Multiplatform Build (v1.1.2)..." -ForegroundColor Cyan

# 1. Set Java Home (Using full JDK 17 for Desktop packaging)
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$signtool = "C:\Program Files (x86)\Windows Kits\10\App Certification Kit\signtool.exe"
$pfxPath = "C:\Users\range\AndroidStudioProjects\Fluidz\FluidzCert.pfx"

# 2. Build Android APK
Write-Host "🤖 Building Android APK..." -ForegroundColor Yellow
.\gradlew assembleBeta
if ($LASTEXITCODE -ne 0) { Write-Host "❌ Android Build Failed!" -ForegroundColor Red; exit 1 }

# 3. Build Windows MSI
Write-Host "💻 Building Windows MSI..." -ForegroundColor Yellow
.\gradlew packageMsi
if ($LASTEXITCODE -ne 0) { Write-Host "❌ Windows Build Failed!" -ForegroundColor Red; exit 1 }

# 4. Sign the Windows MSI
Write-Host "✍️ Signing Windows Installer..." -ForegroundColor Yellow
if (Test-Path $signtool) {
    & $signtool sign /f $pfxPath /p "Fluidz2026" /fd SHA256 /v "C:\Users\range\AndroidStudioProjects\Fluidz\composeApp\build\compose\binaries\main\msi\Fluidz-1.1.2.msi"
    Write-Host "✅ MSI Signed successfully." -ForegroundColor Green
} else {
    Write-Host "⚠️ Signtool not found. MSI will be unsigned." -ForegroundColor Magenta
}

# 5. Prepare Assets for GitHub
Write-Host "🗜️ Zipping Android APK for GitHub..." -ForegroundColor Yellow
$apkPath = "composeApp/build/outputs/apk/beta/composeApp-beta.apk"
$zipPath = "composeApp/build/outputs/apk/beta/Fluidz-Android-Beta.zip"
Compress-Archive -Path $apkPath -DestinationPath $zipPath -Force

# 6. Update Web Links (GitHub Pages)
Write-Host "🌐 Updating permanent web links..." -ForegroundColor Yellow
cp "C:\Users\range\AndroidStudioProjects\Fluidz\composeApp\build\compose\binaries\main\msi\Fluidz-1.1.2.msi" "C:\Users\range\AndroidStudioProjects\Fluidz\docs\Fluidz-1.1.2.msi"
cp $zipPath "C:\Users\range\AndroidStudioProjects\Fluidz\docs\Fluidz-Android-Beta.zip"

# 7. Push Code & Website
Write-Host "📤 Pushing update to GitHub..." -ForegroundColor Yellow
git add .
git commit -m "Beta Multiplatform Update (Android + Windows) - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
git push origin main

# 8. Success Summary
Write-Host "`n✅ SUCCESS! BOTH PLATFORMS READY." -ForegroundColor Green
Write-Host "--------------------------------------------------" -ForegroundColor Gray
Write-Host "1. Android Assets: $zipPath" -ForegroundColor White
Write-Host "2. Windows Assets: composeApp/build/compose/binaries/main/msi/Fluidz-1.1.2.msi" -ForegroundColor White
Write-Host "3. Store Link: https://sparku8811.github.io/Fluidz/Fluidz-1.1.2.msi" -ForegroundColor White
Write-Host "--------------------------------------------------" -ForegroundColor Gray

Start-Process "https://github.com/sparku8811/Fluidz/releases/new"
Write-Host "📋 Opening GitHub. Drag BOTH the ZIP and the MSI into the release." -ForegroundColor Cyan
