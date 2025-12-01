@echo off
echo ========================================
echo Restarting XAMPP Apache to Open Port 80
echo ========================================
echo.

echo Stopping Apache...
taskkill /F /IM httpd.exe 2>nul
timeout /t 2 /nobreak >nul

echo Starting Apache...
start "" "D:\xampp\apache\bin\httpd.exe"
timeout /t 3 /nobreak >nul

echo.
echo Checking if port 80 is now open...
netstat -ano | findstr ":80 "

echo.
echo Testing connection...
curl http://localhost/lost_and_found_api/test_connection.php

echo.
echo Testing network connection (as your phone sees it)...
curl http://192.168.18.17/lost_and_found_api/test_connection.php

echo.
echo ========================================
echo If you see JSON above, port 80 is working!
echo Now test on your phone browser:
echo http://192.168.18.17/lost_and_found_api/test_connection.php
echo ========================================
pause

