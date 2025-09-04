@echo off
echo Building Video Transcoding Service...

echo.
echo Building Common Module...
cd common
call mvn clean install -DskipTests
cd ..

echo.
echo Building Auth Service...
cd auth-service
call mvn clean install -DskipTests
cd ..

echo.
echo Building Transcoding Service...
cd transcoding-service
call mvn clean install -DskipTests
cd ..

echo.
echo Building File Service...
cd file-service
call mvn clean install -DskipTests
cd ..

echo.
echo Building Notification Service...
cd notification-service
call mvn clean install -DskipTests
cd ..

echo.
echo All services built successfully!
echo.
echo To start the services, run:
echo docker-compose up -d
echo.
pause
