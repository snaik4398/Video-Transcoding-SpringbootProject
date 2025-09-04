@echo off
echo Building Video Transcoding Standalone Application...
echo.

REM Check if Maven is available
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and add it to your PATH
    pause
    exit /b 1
)

REM Clean and build the project
echo Cleaning and building project...
mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Build successful! JAR file created in target/ directory
echo.

REM Check if JAR file exists
if not exist "target\video-transcoding-standalone-1.0.0.jar" (
    echo ERROR: JAR file not found!
    pause
    exit /b 1
)

echo Available commands:
echo.
echo 1. Basic transcoding:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4
echo.
echo 2. With custom settings:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4 --video-codec libx265 --resolution 1280x720
echo.
echo 3. GPU acceleration:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4 --processing-mode GPU --video-bitrate 5M
echo.
echo 4. High quality preset:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4 --quality slow --video-bitrate 8M
echo.

REM Ask user if they want to run a demo
set /p choice="Do you want to run a demo transcoding? (y/n): "
if /i "%choice%"=="y" (
    echo.
    echo Running demo with help...
    java -jar target\video-transcoding-standalone-1.0.0.jar
)

echo.
echo Application ready! Use the commands above to transcode videos.
pause
