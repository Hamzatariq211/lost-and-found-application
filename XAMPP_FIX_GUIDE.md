# URGENT: API Not Working - Solution Guide

## ❌ Problem Identified
XAMPP is not running or the API is not properly set up!

## ✅ Step-by-Step Solution

### Step 1: Start XAMPP
1. Open **XAMPP Control Panel**
2. Click **Start** next to **Apache**
3. Click **Start** next to **MySQL**
4. Both should show green "Running" status

### Step 2: Verify API Files Are in Correct Location
Your API files should be at:
```
C:\xampp\htdocs\lost_and_found_api\
```

**Current location of your files:**
```
E:\Mobile dev Projects\Lost_and_Found_Application\api\
```

**YOU NEED TO COPY THEM!**

### Step 3: Copy API Files to XAMPP

**Option A - Using the batch file:**
1. Run the existing batch file: `copy_to_xampp.bat`

**Option B - Manual copy:**
1. Copy everything from: `E:\Mobile dev Projects\Lost_and_Found_Application\api\`
2. Paste to: `C:\xampp\htdocs\lost_and_found_api\`

### Step 4: Test the API in Browser

Open your browser and go to:
```
http://localhost/lost_and_found_api/test_connection.php
```

**Expected result:**
```json
{
  "success": true,
  "message": "API is working!",
  "server_ip": "127.0.0.1",
  "client_ip": "127.0.0.1",
  "timestamp": "2025-12-02 10:30:45"
}
```

If you see this, the API is working! ✅

### Step 5: Configure Firewall (For Physical Devices)

If testing on a physical phone:

1. Open **Windows Defender Firewall**
2. Click **Advanced Settings**
3. Click **Inbound Rules** → **New Rule**
4. Select **Port** → Next
5. Enter port **80** (for HTTP) → Next
6. Select **Allow the connection** → Next
7. Check all profiles → Next
8. Name it "XAMPP Apache" → Finish

### Step 6: Test on Your Phone

1. Make sure phone is on same WiFi as PC
2. Phone should be on network: `192.168.18.x`
3. Open phone browser and go to:
   ```
   http://192.168.18.17/lost_and_found_api/test_connection.php
   ```
4. You should see the JSON response

### Step 7: Run the App

Now your app should work on:
- ✅ **Emulator**: Uses `10.0.2.2`
- ✅ **Physical Device**: Uses `192.168.18.17`

## Quick Test Checklist

- [ ] XAMPP Apache is running (green)
- [ ] XAMPP MySQL is running (green)
- [ ] API files copied to `C:\xampp\htdocs\lost_and_found_api\`
- [ ] Browser test works: `http://localhost/lost_and_found_api/test_connection.php`
- [ ] Phone on same WiFi as PC
- [ ] Phone can access: `http://192.168.18.17/lost_and_found_api/test_connection.php`
- [ ] Windows Firewall allows port 80
- [ ] Rebuild and run the app

## Common Issues

**Issue 1: "Connection refused"**
→ XAMPP Apache not running

**Issue 2: "404 Not Found"**
→ API files not in `C:\xampp\htdocs\lost_and_found_api\`

**Issue 3: "Timeout" on phone**
→ Firewall blocking, or phone on different WiFi

**Issue 4: Different PC IP address**
→ Run `ipconfig` again and update `ApiConfig.kt` with new IP

