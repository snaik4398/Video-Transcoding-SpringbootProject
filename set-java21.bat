@echo off
REM Script to set JAVA_HOME to Java 21 for this project
REM Run this before building: set-java21.bat

set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME set to: %JAVA_HOME%
echo.
java -version
echo.
echo You can now run: mvn clean install
pause

