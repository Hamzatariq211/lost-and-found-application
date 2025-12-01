@echo off
echo ========================================
echo Lost and Found API - XAMPP Deployment
echo ========================================
echo.

REM Check if XAMPP exists
if not exist "D:\xampp\htdocs\" (
    echo ERROR: XAMPP not found at D:\xampp\
    echo Please install XAMPP or update the path in this script
    pause
    exit /b
)

echo Creating API directory in XAMPP...
if not exist "D:\xampp\htdocs\lost_and_found_api\" (
    mkdir "D:\xampp\htdocs\lost_and_found_api"
)

echo.
echo Copying API files to XAMPP...
xcopy /E /I /Y "api\*" "D:\xampp\htdocs\lost_and_found_api\"

echo.
echo Creating uploads directory...
if not exist "D:\xampp\htdocs\lost_and_found_api\uploads\" (
    mkdir "D:\xampp\htdocs\lost_and_found_api\uploads"
)

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo.
echo Your API is now available at:
echo http://localhost/lost_and_found_api/
echo.
echo Test page:
echo http://localhost/lost_and_found_api/test_api.html
echo.
echo Next steps:
echo 1. Make sure Apache is running in XAMPP
echo 2. Import database/lost_and_found.sql in phpMyAdmin
echo 3. Open the test page URL above
echo.
pause
