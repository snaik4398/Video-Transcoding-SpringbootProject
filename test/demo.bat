@echo off
echo ========================================
echo Video Transcoding Demo
echo ========================================
echo.

REM Check if JAR exists
if not exist "target\video-transcoding-standalone-1.0.0.jar" (
    echo Building application first...
    call build-and-run.bat
    if %errorlevel% neq 0 (
        echo Build failed! Cannot run demo.
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo Available Demo Commands
echo ========================================
echo.

echo 1. Show help and system information:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar
echo.

echo 2. Basic transcoding (if you have a video file):
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output.mp4
echo.

echo 3. High quality H.265 transcoding:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output_h265.mp4 --video-codec libx265 --quality slow
echo.

echo 4. GPU accelerated transcoding:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output_gpu.mp4 --processing-mode GPU --video-bitrate 5M
echo.

echo 5. WebM format for web streaming:
echo    java -jar target\video-transcoding-standalone-1.0.0.jar input.mp4 output_webm.webm --video-codec libvpx-vp9 --format webm
echo.

echo ========================================
echo Running System Information Demo
echo ========================================
echo.

echo This will show your system capabilities and FFmpeg availability:
java -jar target\video-transcoding-standalone-1.0.0.jar

echo.
echo ========================================
echo Demo Complete!
echo ========================================
echo.
echo To transcode actual videos, use one of the commands above.
echo Make sure you have FFmpeg installed and a video file ready.
echo.
pause
