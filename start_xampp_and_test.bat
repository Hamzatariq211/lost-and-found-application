@echo off
echo ========================================
echo XAMPP Apache Startup and Configuration
echo ========================================
echo.

echo Step 1: Checking if Apache is running...
netstat -ano | findstr ":80 " > nul
if %errorlevel% equ 0 (
    echo [OK] Apache is already running on port 80
) else (
    echo [!] Apache is NOT running on port 80
)
echo.

echo Step 2: Starting Apache via XAMPP...
echo Please make sure to:
echo   1. Open XAMPP Control Panel
echo   2. Click START next to Apache
echo   3. Wait for it to turn GREEN
echo.

echo Step 3: Verify Apache Configuration
echo Apache should listen on: 0.0.0.0:80 (all interfaces)
echo Your PC IP: 192.168.18.17
echo.

echo Step 4: Test from your phone browser:
echo http://192.168.18.17/lost_and_found_api/test_connection.php
echo.

echo Step 5: If phone can't connect, configure firewall:
echo   1. Windows Defender Firewall
echo   2. Allow an app through firewall
echo   3. Find "Apache HTTP Server"
echo   4. Check BOTH Private and Public boxes
echo   5. Click OK
echo.

echo ========================================
echo Press any key to open XAMPP Control Panel...
pause > nul

start "" "D:\xampp\xampp-control.exe"

echo.
echo After starting Apache, press any key to test connection...
pause > nul

echo Testing localhost connection...
curl http://localhost/lost_and_found_api/test_connection.php
echo.

echo Testing network connection (as phone would see it)...
curl http://192.168.18.17/lost_and_found_api/test_connection.php
echo.

echo ========================================
echo If both tests above show JSON, you're ready!
echo Now rebuild and run your app.
echo ========================================
pause

