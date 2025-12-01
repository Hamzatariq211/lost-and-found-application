# Quick Start Guide - Authentication

## 🚀 Get Started in 5 Minutes

### Prerequisites
- ✅ XAMPP running (Apache + MySQL)
- ✅ Database imported
- ✅ API files in correct location
- ✅ Android Studio installed

---

## Step 1: Firebase Setup (2 minutes)

1. Go to https://console.firebase.google.com/
2. Create project or select existing
3. Add Android app
4. Download `google-services.json`
5. Place in `app/` folder
6. Enable Email/Password auth

---

## Step 2: Update Base URL (1 minute)

**File**: `app/src/main/java/com/hamzatariq/lost_and_found_application/api/RetrofitClient.kt`

```kotlin
// For Emulator (default):
private const val BASE_URL = "http://10.0.2.2/lost_and_found_api/"

// For Physical Device:
private const val BASE_URL = "http://YOUR_PC_IP/lost_and_found_api/"
```

Find your PC IP:
```bash
# Windows Command Prompt
ipconfig
# Look for IPv4 Address (e.g., 192.168.1.100)
```

---

## Step 3: Build & Run (2 minutes)

```bash
# In Android Studio
1. File → Sync Now
2. Build → Make Project
3. Run → Run 'app'
```

---

## 🧪 Test It

### Test Sign Up
1. Open app
2. Click "Sign Up"
3. Fill all fields:
   - Full Name: John Doe
   - Username: johndoe
   - Email: john@example.com
   - Mobile: +1234567890
   - Password: password123
4. Click "Sign Up"
5. Should navigate to Home

### Test Login
1. Open app
2. Enter username: `johndoe`
3. Enter password: `password123`
4. Click "Sign In"
5. Should navigate to Home

---

## 📊 What's Working

✅ Sign up with email and password
✅ Login with username/email
✅ Firebase authentication
✅ MySQL database storage
✅ Local token storage
✅ Input validation
✅ Error handling
✅ Loading states

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Not Found" error | Check base URL in RetrofitClient.kt |
| Connection refused | Ensure XAMPP is running |
| Firebase error | Download google-services.json from Firebase Console |
| Gradle sync failed | File → Invalidate Caches → Restart |
| API not responding | Test with Postman: http://localhost/lost_and_found_api/test_api.html |

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `ApiService.kt` | API endpoints |
| `RetrofitClient.kt` | API configuration |
| `AuthRepository.kt` | Auth logic |
| `AuthViewModel.kt` | ViewModel |
| `SignupActivity.kt` | Sign up screen |
| `LoginActivity.kt` | Login screen |
| `SharedPreferencesManager.kt` | Local storage |

---

## 🔑 Test Credentials

After database import:

```
Username: johndoe
Email: john@example.com
Password: password

OR

Username: janesmith
Email: jane@example.com
Password: password
```

---

## 📱 Architecture

```
SignupActivity/LoginActivity
        ↓
    AuthViewModel
        ↓
   AuthRepository
        ↓
    ┌───┴───┬────────┐
    ↓       ↓        ↓
Firebase MySQL SharedPrefs
```

---

## ✅ Verification

Check these to verify setup:

1. **Firebase**
   - [ ] Project created
   - [ ] Android app added
   - [ ] google-services.json in app/
   - [ ] Email/Password auth enabled

2. **XAMPP**
   - [ ] Apache running
   - [ ] MySQL running
   - [ ] Database imported
   - [ ] API accessible

3. **Android**
   - [ ] Gradle synced
   - [ ] Project builds
   - [ ] App runs on emulator/device

---

## 🎯 Next Steps

After authentication works:

1. Implement offline storage (Room Database)
2. Add post creation
3. Implement image upload
4. Add search functionality
5. Implement push notifications

---

## 📞 Need Help?

1. Check `AUTHENTICATION_SETUP_CHECKLIST.md`
2. Review `AUTHENTICATION_IMPLEMENTATION.md`
3. Check Logcat for errors
4. Test API with Postman

---

## 🎉 You're Ready!

Your authentication system is now fully functional with:
- ✅ Firebase integration
- ✅ MySQL backend
- ✅ Local token storage
- ✅ Input validation
- ✅ Error handling

**Happy coding!** 🚀
