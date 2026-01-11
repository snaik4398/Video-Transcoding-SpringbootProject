# PowerShell build script for Video Transcoding Service
# This script sets JAVA_HOME to Java 21 and builds all modules

Write-Host "Building Video Transcoding Service..." -ForegroundColor Cyan

# Set JAVA_HOME to Java 21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Using Java: $env:JAVA_HOME" -ForegroundColor Green
java -version
Write-Host ""

# Build Common Module
Write-Host "Building Common Module..." -ForegroundColor Yellow
Set-Location common
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Common Module build failed!" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build Auth Service
Write-Host "Building Auth Service..." -ForegroundColor Yellow
Set-Location auth-service
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Auth Service build failed!" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build Transcoding Service
Write-Host "Building Transcoding Service..." -ForegroundColor Yellow
Set-Location transcoding-service
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Transcoding Service build failed!" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build File Service
Write-Host "Building File Service..." -ForegroundColor Yellow
Set-Location file-service
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "File Service build failed!" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# Build Notification Service
Write-Host "Building Notification Service..." -ForegroundColor Yellow
Set-Location notification-service
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Notification Service build failed!" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

Write-Host ""
Write-Host "All services built successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "To start the services, run:" -ForegroundColor Cyan
Write-Host "  docker-compose up -d" -ForegroundColor White

