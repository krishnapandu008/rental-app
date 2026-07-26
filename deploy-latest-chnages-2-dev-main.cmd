@echo off
setlocal enabledelayedexpansion

echo ============================================
echo  Looking for Git Bash...
echo ============================================

:: Try common Git Bash locations
set "GIT_BASH="
if exist "C:\Program Files\Git\git-bash.exe" set "GIT_BASH=C:\Program Files\Git\git-bash.exe"
if exist "C:\Program Files\Git\bin\bash.exe" set "GIT_BASH=C:\Program Files\Git\bin\bash.exe"
if exist "%USERPROFILE%\AppData\Local\Programs\Git\git-bash.exe" set "GIT_BASH=%USERPROFILE%\AppData\Local\Programs\Git\git-bash.exe"
if exist "%USERPROFILE%\AppData\Local\Programs\Git\bin\bash.exe" set "GIT_BASH=%USERPROFILE%\AppData\Local\Programs\Git\bin\bash.exe"

if "%GIT_BASH%"=="" (
    echo [ERROR] Git Bash not found!
    echo Please install Git for Windows from https://git-scm.com/download/win
    echo.
    pause
    exit /b 1
)

echo [OK] Found Git Bash at: %GIT_BASH%
echo.

:: Run the script
"%GIT_BASH%" --login -i "%~dp0deploy-latest-chnages-2-dev-main.sh"

:: Keep window open if script exited with error or normally
echo.
echo ============================================
echo Script finished. Press any key to exit...
pause >nul