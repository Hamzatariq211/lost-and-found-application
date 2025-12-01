@echo off
echo ========================================
echo Fixing API Directory Structure
echo ========================================
echo.

REM Check if the nested api folder exists
if not exist "D:\xampp\htdocs\lost_and_found_api\api\" (
    echo ERROR: Directory D:\xampp\htdocs\lost_and_found_api\api\ not found
    echo Please check the path
    pause
    exit /b
)

echo Moving files from nested api folder to parent...
echo.

REM Move all files from api subfolder to parent
xcopy /E /I /Y "D:\xampp\htdocs\lost_and_found_api\api\*" "D:\xampp\htdocs\lost_and_found_api\"

echo.
echo Removing empty nested api folder...
rmdir /S /Q "D:\xampp\htdocs\lost_and_found_api\api"

echo.
echo Creating uploads directory if it doesn't exist...
if not exist "D:\xampp\htdocs\lost_and_found_api\uploads\" (
    mkdir "D:\xampp\htdocs\lost_and_found_api\uploads"
)

echo.
echo ========================================
echo Structure Fixed!
echo ========================================
echo.
echo Your API is now available at:
echo http://localhost/lost_and_found_api/
echo.
echo Test page:
echo http://localhost/lost_and_found_api/test_api.html
echo.
echo Documentation:
echo http://localhost/lost_and_found_api/index.php
echo.
pause
