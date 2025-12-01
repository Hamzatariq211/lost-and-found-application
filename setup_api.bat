@echo off
echo ========================================
echo Lost and Found API - XAMPP Setup Script
echo ========================================
echo.

REM Check if XAMPP exists
if not exist "C:\xampp\htdocs\" (
    echo ERROR: XAMPP htdocs folder not found!
    echo Please install XAMPP first and make sure it's installed at C:\xampp\
    pause
    exit /b
)

echo Creating API directory...
if not exist "C:\xampp\htdocs\lost_and_found_api\" (
    mkdir "C:\xampp\htdocs\lost_and_found_api\"
)

echo.
echo Copying API files to XAMPP...
xcopy /E /I /Y "api\*" "C:\xampp\htdocs\lost_and_found_api\"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS! API files copied successfully!
    echo ========================================
    echo.
    echo Your API is now available at:
    echo http://localhost/lost_and_found_api/
    echo.
    echo NEXT STEPS:
    echo 1. Start Apache and MySQL in XAMPP Control Panel
    echo 2. Go to http://localhost/phpmyadmin
    echo 3. Create database: lost_and_found_db
    echo 4. Import: database\COMPLETE_SETUP.sql
    echo 5. Test API: http://localhost/lost_and_found_api/posts/get_posts.php
    echo.
) else (
    echo.
    echo ERROR: Failed to copy files!
    echo Please make sure you're running this script from the project root directory.
    echo.
)

pause

