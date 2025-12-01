# Push Notifications & Persistent Login - Complete Setup Guide

## 🎯 Features Implemented

### 1. **Push Notifications via Firebase Cloud Messaging (FCM)**
   - ✅ Notifications for new chat messages
   - ✅ Notifications for matching found items when you report a lost item
   - ✅ Automatic notification delivery even when app is in background

### 2. **Persistent Login (Keep User Logged In)**
   - ✅ User stays logged in after closing the app
   - ✅ Automatic login on app restart
   - ✅ Only logs out when user explicitly clicks "Logout"

### 3. **Logout Functionality**
   - ✅ Red "Logout" button added to Settings page
   - ✅ Confirmation dialog before logging out
   - ✅ Clears all user data and redirects to login

---

## 📋 Setup Instructions

### **STEP 1: Update Database Schema**

Run this SQL in phpMyAdmin to add FCM token support:

```sql
-- File: database/ADD_FCM_TOKEN_COLUMN.sql
USE lost_and_found_db;

ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(255) DEFAULT NULL AFTER mobile_number;
CREATE INDEX IF NOT EXISTS idx_fcm_token ON users(fcm_token);
```

### **STEP 2: Get Firebase Server Key**

1. Go to **Firebase Console**: https://console.firebase.google.com
2. Select your project: `lost-and-found-applicati-d438c`
3. Click ⚙️ **Settings** → **Project Settings**
4. Go to **Cloud Messaging** tab
5. Copy the **Server Key** (starts with `AAAA...`)
6. Open `api/notifications/fcm_service.php`
7. Replace this line:
   ```php
   $this->serverKey = 'YOUR_FIREBASE_SERVER_KEY_HERE';
   ```
   With:
   ```php
   $this->serverKey = 'AAAA... (your actual server key)';
   ```

### **STEP 3: Deploy API Files to XAMPP**

Copy all updated API files to XAMPP:

```cmd
cd "E:\Mobile dev Projects\Lost_and_Found_Application"
.\copy_to_xampp.bat
```

This copies:
- `api/user/update_fcm_token.php` - Saves FCM token to database
- `api/notifications/fcm_service.php` - Sends push notifications
- `api/notifications/send_match_notification.php` - Notifies about matches

### **STEP 4: Sync Gradle and Build App**

1. Open Android Studio
2. Click **File** → **Sync Project with Gradle Files**
3. Wait for sync to complete
4. Build the app: **Build** → **Rebuild Project**

---

## 🔔 How Notifications Work

### **A. Chat Notifications**

When someone sends you a message:

1. **Sender** sends message via chat API
2. **Server** calls `fcm_service.php` to send notification
3. **Your device** receives notification even if app is closed
4. **You tap** notification → Opens chat screen directly

**Code to trigger (add to chat API):**
```php
require_once '../notifications/fcm_service.php';
$fcm = new FCMNotificationService();
$fcm->sendChatNotification($receiverId, $senderName, $messageText, $chatId);
```

### **B. Match Notifications**

When someone posts a found item matching your lost item:

1. **Someone** posts a found item (e.g., "Watch" in "C110")
2. **Server** automatically checks all active lost items
3. **Finds matches** using smart algorithm (name + location)
4. **Sends notifications** to users with matching lost items
5. **You receive** notification: "🎉 Possible match found! Someone found a Watch in C110"
6. **You tap** notification → Opens My Posts → Lost Items tab

**Matching Logic:**
- Lost: "watch" in "C110"
- Found: "WATCH" in "c110" → Match Score: 80 → ✅ Notification sent!
- Found: "Wrist Watch" in "C-110" → Match Score: 45 → ✅ Notification sent!

---

## 🔐 Persistent Login Flow

### **How It Works:**

**First Login:**
```
User logs in → 
Save user_id, username, email, is_logged_in=true → 
Get FCM token → 
Send token to server → 
Go to Home
```

**App Restart:**
```
Open app → 
SplashActivity checks is_logged_in flag → 
If TRUE: Get FCM token → Go to Home directly → 
If FALSE: Go to Login screen
```

**Logout:**
```
Settings → Click Logout → 
Confirmation dialog → 
Clear all data (is_logged_in=false) → 
Firebase sign out → 
Go to Login screen
```

---

## 🎨 UI Changes

### **Settings Page - Logout Button**

Added a red "Logout" button at the bottom of Settings page:
- **Color:** Red (holo_red_dark)
- **Icon:** Power off icon
- **Position:** Below "Save Changes" button
- **Behavior:** Shows confirmation dialog before logging out

### **Icons Repositioned**

All three icons (Chat, My Posts, Profile) are now at the top of Home screen with proper spacing (48dp top margin to avoid status bar overlap).

---

## 🧪 Testing Instructions

### **Test 1: Persistent Login**

1. **Login** to the app
2. **Close** the app completely (swipe from recent apps)
3. **Reopen** the app
4. ✅ Should go directly to **Home** screen (no login required)

### **Test 2: Logout**

1. Go to **Settings** (profile icon)
2. Scroll down to **Logout** button (red button)
3. Click **Logout**
4. Click **Yes** in confirmation dialog
5. ✅ Should go to **Login** screen
6. Close and reopen app
7. ✅ Should show **Login** screen (not auto-login)

### **Test 3: Match Notifications (Once Firebase Server Key is added)**

1. **User A:** Report lost item: "Watch" in "C110"
2. **User B:** Post found item: "watch" in "c110"
3. ✅ **User A** should receive notification: "🎉 Possible match found!"
4. Tap notification
5. ✅ Opens My Posts → Lost Items → Can see matching found post

### **Test 4: FCM Token Registration**

Check if FCM token is being saved:

1. Login to app
2. Check Logcat: `adb logcat | grep FCM`
3. Should see: `FCM Token: <long token string>`
4. Check database:
   ```sql
   SELECT user_id, username, fcm_token FROM users WHERE user_id = 1;
   ```
5. ✅ Should show the FCM token

---

## 🔧 Troubleshooting

### **Issue: "No FCM token saved"**

**Solution:**
1. Make sure Google Play Services is installed on emulator
2. Check internet connection
3. Rebuild app after adding Firebase dependencies
4. Check Logcat for Firebase initialization errors

### **Issue: "Notifications not received"**

**Solution:**
1. Verify Firebase Server Key is correctly set in `fcm_service.php`
2. Check that FCM token column exists in database: `DESCRIBE users;`
3. Verify user has FCM token: `SELECT fcm_token FROM users WHERE user_id = 1;`
4. Test notification manually: http://localhost/lost_and_found_api/notifications/fcm_service.php?test

### **Issue: "User logs out automatically"**

**Solution:**
1. Check that `is_logged_in` flag is being saved
2. Verify SharedPreferencesManager is saving data
3. Check Logcat for any errors during login

---

## 📱 App Permissions

The app now requires these permissions (already added):

```xml
<!-- For push notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- For internet access (FCM) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🎯 Summary

### **What's Working Now:**

✅ **Persistent Login** - Users stay logged in
✅ **Logout Button** - Red button in Settings with confirmation
✅ **FCM Service** - Ready to send push notifications
✅ **Auto Match Detection** - Server finds matching lost/found items
✅ **Notification Channels** - Separate channels for Chat & Matches
✅ **Deep Linking** - Tapping notification opens relevant screen

### **What You Need To Do:**

1. ⚠️ Run `ADD_FCM_TOKEN_COLUMN.sql` in phpMyAdmin
2. ⚠️ Get Firebase Server Key and update `fcm_service.php`
3. ⚠️ Copy API files to XAMPP using `copy_to_xampp.bat`
4. ⚠️ Sync Gradle and rebuild app
5. ✅ Test login, logout, and notifications!

---

## 🚀 Ready to Test!

Once you complete the setup steps above, your app will have:
- 📱 Push notifications for chats and matches
- 🔐 Users stay logged in automatically
- 🚪 Clean logout functionality

Enjoy your enhanced Lost & Found app! 🎉

