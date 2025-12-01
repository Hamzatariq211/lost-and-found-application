# Authentication Setup Checklist

## ✅ Backend Setup (Already Done)

- [x] SQL database created with users table
- [x] PHP API endpoints for signup and login
- [x] Token-based authentication system
- [x] MySQL integration

## ✅ Android Dependencies (Already Done)

- [x] Retrofit 2.9.0 added
- [x] OkHttp logging interceptor added
- [x] Coroutines added
- [x] Lifecycle components added
- [x] Firebase Auth added
- [x] Permissions added to manifest

## ✅ Code Implementation (Already Done)

- [x] ApiService.kt - Retrofit interface
- [x] RetrofitClient.kt - Retrofit configuration
- [x] AuthRepository.kt - Authentication logic
- [x] AuthViewModel.kt - ViewModel
- [x] AuthViewModelFactory.kt - Factory
- [x] SharedPreferencesManager.kt - Local storage
- [x] SignupActivity.kt - Updated with full implementation
- [x] LoginActivity.kt - Updated with full implementation

## 📋 Manual Setup Steps

### Step 1: Firebase Setup
- [ ] Go to https://console.firebase.google.com/
- [ ] Create new project or select existing
- [ ] Add Android app
- [ ] Download `google-services.json`
- [ ] Place in `app/` folder (same level as build.gradle.kts)
- [ ] Enable Email/Password authentication

### Step 2: Update Base URL
- [ ] Open `app/src/main/java/com/hamzatariq/lost_and_found_application/api/RetrofitClient.kt`
- [ ] For emulator: Keep `http://10.0.2.2/lost_and_found_api/`
- [ ] For physical device: Change to your PC's IP (e.g., `http://192.168.1.100/lost_and_found_api/`)

### Step 3: Verify XAMPP Setup
- [ ] XAMPP is running (Apache + MySQL)
- [ ] Database imported: `database/lost_and_found.sql`
- [ ] API files in: `D:\xampp\htdocs\lost_and_found_api\`
- [ ] Test API: `http://localhost/lost_and_found_api/test_api.html`

### Step 4: Build and Run
- [ ] Open Android Studio
- [ ] Sync Gradle files (File → Sync Now)
- [ ] Build project (Build → Make Project)
- [ ] Run on emulator or device (Run → Run 'app')

### Step 5: Test Authentication
- [ ] Open app
- [ ] Test Sign Up with new account
- [ ] Verify user created in MySQL
- [ ] Test Login with created account
- [ ] Verify token saved locally
- [ ] Check Logcat for any errors

## 🔍 Verification Steps

### Check Firebase Configuration
```bash
# Verify google-services.json exists
ls -la app/google-services.json
```

### Check API Connectivity
1. Open browser: `http://localhost/lost_and_found_api/`
2. Should see API documentation
3. Database status should show "Connected"

### Check Android Logs
1. Open Android Studio
2. View → Tool Windows → Logcat
3. Filter by "AuthRepository" or "OkHttp"
4. Should see API requests and responses

### Test with Postman
1. Import `Lost_and_Found_API.postman_collection.json`
2. Test signup endpoint
3. Test login endpoint
4. Verify responses

## 🐛 Troubleshooting

### Issue: "google-services.json not found"
- [ ] Download from Firebase Console
- [ ] Place in `app/` folder
- [ ] Rebuild project

### Issue: "Connection refused"
- [ ] Check XAMPP is running
- [ ] Verify base URL is correct
- [ ] For physical device, use correct IP address

### Issue: "Invalid response data"
- [ ] Check MySQL database is imported
- [ ] Test API with Postman
- [ ] Check PHP error logs

### Issue: "Firebase authentication failed"
- [ ] Verify email/password authentication enabled in Firebase
- [ ] Check email format is valid
- [ ] Ensure password is at least 6 characters

### Issue: "Gradle sync failed"
- [ ] File → Invalidate Caches → Invalidate and Restart
- [ ] Delete `.gradle` folder
- [ ] Sync again

## 📱 Testing Credentials

After database import, use these to test:

**Account 1:**
- Username: `johndoe`
- Email: `john@example.com`
- Password: `password`

**Account 2:**
- Username: `janesmith`
- Email: `jane@example.com`
- Password: `password`

**Create New Account:**
- Use Sign Up screen
- Fill all fields
- Password must be 6+ characters

## 📊 Expected Behavior

### Successful Sign Up
1. User enters all fields
2. Clicks "Sign Up"
3. Button shows "Creating Account..."
4. After 2-3 seconds, navigates to Home
5. Toast shows "Signup successful!"

### Successful Login
1. User enters username/email and password
2. Clicks "Sign In"
3. Button shows "Signing In..."
4. After 2-3 seconds, navigates to Home
5. Toast shows "Login successful!"

### Error Handling
1. Empty fields show error messages
2. Invalid email shows error
3. Short password shows error
4. Wrong credentials show error toast
5. Network errors show error toast

## 🎯 Next Steps After Setup

1. Test authentication thoroughly
2. Implement offline storage (Room Database)
3. Add post creation functionality
4. Implement image upload
5. Add push notifications
6. Implement search and filtering

## 📞 Support Resources

- **Firebase Docs**: https://firebase.google.com/docs/auth
- **Retrofit Docs**: https://square.github.io/retrofit/
- **Android Docs**: https://developer.android.com/
- **Kotlin Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html

---

**Status**: ✅ Ready for Testing

Once you complete all steps above, the authentication system will be fully functional!
