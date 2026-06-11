# Fluidz Beta Deployment Script

Write-Host "🚀 Starting Fluidz Beta Build..." -ForegroundColor Cyan

# 1. Set Java Home
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

# 2. Build the APK
Write-Host "📦 Building APK..." -ForegroundColor Yellow
./gradlew assembleBeta

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build Failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

# 3. Zip the APK
Write-Host "🗜️ Packaging for GitHub..." -ForegroundColor Yellow
$apkPath = "composeApp/build/outputs/apk/beta/composeApp-beta.apk"
$zipPath = "composeApp/build/outputs/apk/beta/Fluidz-Beta.zip"
Compress-Archive -Path $apkPath -DestinationPath $zipPath -Force

# 4. Commit and Push
Write-Host "📤 Pushing code to GitHub..." -ForegroundColor Yellow
git add .
git commit -m "Beta update build $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
git push origin main

# 5. Open GitHub Release Page
Write-Host "✅ Done! Opening GitHub to create your release..." -ForegroundColor Green
Write-Host "👉 Remember to drag and drop: $zipPath" -ForegroundColor Cyan
Start-Process "https://github.com/sparku8811/Fluidz/releases/new"

# Copy path to clipboard for easy pasting into GitHub
$zipPathFull = Get-Item $zipPath | Select-Object -ExpandProperty FullName
$zipPathFull | Set-Clipboard
Write-Host "📋 Zip path copied to clipboard! Just paste it in the file dialog." -ForegroundColor Magenta
