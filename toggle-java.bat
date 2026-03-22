@echo off
CLS
echo ===========================================
echo   JAVA VERSION SWITCHER
echo ===========================================
echo 1. Set to Java 1.8 (Java 8)
echo 2. Set to Java 21
echo 3. Exit
echo ===========================================

set /p choice="Select an option (1-3): "

if "%choice%"=="1" goto SET8
if "%choice%"=="2" goto SET21
if "%choice%"=="3" goto END

:SET8
set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_231"
goto APPLY

:SET21
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
goto APPLY

:APPLY
:: This adds the selected JAVA_HOME to the start of your PATH
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo.
echo SUCCESS: JAVA_HOME set to %JAVA_HOME%
echo Current Java Version:
java -version
echo.
pause
goto END

:END