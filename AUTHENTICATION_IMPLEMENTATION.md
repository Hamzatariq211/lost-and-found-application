# Authentication Implementation Guide

## Overview

The Lost & Found application now has a complete authentication system that works with both **Firebase** and **MySQL** databases. Users can sign up and log in using email and password.

---

## Architecture

### Components

1. **API Service** (`ApiService.kt`)
   - Retrofit interface for API calls
   - Handles signup and login requests
   - Communicates with PHP backend

2. **Retrofit Client** (`RetrofitClient.kt`)
   - Singleton for API configuration
   - Base URL: `http://10.0.2.2/lost_and_found_api/` (emulator)
   - Includes logging interceptor for debugging

3. **Auth Repository** (`AuthRepository.kt`)
   - Manages authentication logic
   - Handles both Firebase and MySQL authentication
   - Stores tokens and user data locally

4. **Auth ViewModel** (`AuthViewModel.kt`)
   - MVVM pattern implementation
   - Manages UI state (Loading, Success, Error)
   - Observes authentication changes

5. **SharedPreferences Manager** (`SharedPreferencesManager.kt`)
   - Local token storage
   - User data persistence
   - Session management

---

## File Structure

```
app/src/main/java/com/hamzatariq/lost_and_found_application/
├── api/
│   ├── ApiService.kt          # Retrofit API interface
│   └── RetrofitClient.kt      # Retrofit configuration
├── repository/
│   └── AuthRepository.kt      # Authentication logic
├── viewmodel/
│   ├── AuthViewModel.kt       # ViewModel for auth
│   └── AuthViewModelFactory.kt # Factory for ViewModel
├── utils/
│   └── SharedPreferencesManager.kt # Local storage
├── SignupActivity.kt          # Updated signup screen
├── LoginActivity.kt           # Updated login screen
└── ... (other activities)
```

---

## How It Works

### Sign Up Flow

```
User enters data
    ↓
SignupActivity validates input
    ↓
AuthViewModel.signup() called
    ↓
AuthRepository.signup()
    ├─ Create user in Firebase
    ├─ Send request to MySQL API
    ├─ Save token locally
    └─ Save user data locally
    ↓
Success → Navigate to HomeActivity
Error → Show error message
```

### Login Flow

```
User enters credentials
    ↓
LoginActivity validates input
    ↓
AuthViewModel.login() called
    ↓
AuthRepository.login()
    ├─ Send request to MySQL API
    ├─ Try Firebase login (optional)
    ├─ Save token locally
    └─ Save user data locally
    ↓
Success → Navigate to HomeActivity
Error → Show error message
```

---

## Setup Instructions

### 1. Update Base URL

Edit `api/RetrofitClient.kt`:

```kotlin
// For Android Emulator:
private const val BASE_URL = "http://10.0.2.2/lost_and_found_api/"

// For Physical Device (replace with your PC's IP):
private const val BASE_URL = "http://192.168.1.100/lost_and_found_api/"
```

### 2. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing one
3. Add Android app to project
4. Download `google-services.json`
5. Place it in `app/` folder
6. Enable Email/Password authentication in Firebase Console

### 3. Build and Run

```bash
# Sync Gradle files
./gradlew sync

# Build the app
./gradlew build

# Run on emulator/device
./gradlew installDebug
```

---

## API Endpoints Used

### Sign Up
- **URL**: `/auth/signup.php`
- **Method**: POST
- **Body**:
```json
{
  "full_name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "mobile_number": "+1234567890",
  "password": "password123"
}
```

### Login
- **URL**: `/auth/login.php`
- **Method**: POST
- **Body**:
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

---

## Data Storage

### Local Storage (SharedPreferences)

Stored data:
- `auth_token` - API authentication token
- `user_id` - User ID from database
- `username` - Username
- `email` - Email address
- `full_name` - Full name
- `mobile_number` - Mobile number

### Firebase Storage

- User email and password
- Firebase UID
- User metadata

### MySQL Storage

- All user information
- Authentication tokens
- User sessions

---

## Error Handling

The app handles various error scenarios:

1. **Network Errors**
   - Connection timeout
   - No internet connection
   - Server unreachable

2. **Validation Errors**
   - Empty fields
   - Invalid email format
   - Password too short
   - Username already exists

3. **Authentication Errors**
   - Wrong password
   - User not found
   - Account inactive

4. **Firebase Errors**
   - Email already in use
   - Weak password
   - Invalid email

---

## Testing

### Test Credentials

After importing the SQL file, use these credentials:

**User 1:**
- Username: `johndoe`
- Email: `john@example.com`
- Password: `password`

**User 2:**
- Username: `janesmith`
- Email: `jane@example.com`
- Password: `password`

### Manual Testing Steps

1. **Test Sign Up**
   - Open app
   - Click "Sign Up"
   - Fill in all fields
   - Click "Sign Up" button
   - Should navigate to Home

2. **Test Login**
   - Open app
   - Enter username/email
   - Enter password
   - Click "Sign In"
   - Should navigate to Home

3. **Test Validation**
   - Try empty fields
   - Try invalid email
   - Try short password
   - Should show error messages

---

## Debugging

### Enable Logging

Retrofit logging is enabled by default. Check Logcat for:

```
D/OkHttp: --> POST /auth/login.php
D/OkHttp: {"username":"johndoe","password":"password123"}
D/OkHttp: <-- 200 OK
```

### Common Issues

**Issue**: "Not Found" error
- **Solution**: Check base URL in `RetrofitClient.kt`
- Verify API files are in correct XAMPP location

**Issue**: "Connection refused"
- **Solution**: Ensure XAMPP is running
- Check if using correct IP address for physical device

**Issue**: "Invalid response data"
- **Solution**: Check MySQL database is imported
- Verify API endpoint is working (test with Postman)

**Issue**: Firebase errors
- **Solution**: Download `google-services.json` from Firebase Console
- Place in `app/` folder
- Rebuild the app

---

## Security Considerations

1. **Token Storage**
   - Tokens stored in SharedPreferences (encrypted on Android 6+)
   - Consider using EncryptedSharedPreferences for sensitive data

2. **Password Handling**
   - Passwords hashed with bcrypt on server
   - Never stored in plain text locally

3. **HTTPS**
   - Use HTTPS in production
   - Update `usesCleartextTraffic` in manifest for production

4. **Token Expiry**
   - Tokens expire after 30 days
   - Implement token refresh mechanism

---

## Next Steps

1. ✅ Authentication implemented
2. ⏳ Implement offline storage (Room Database)
3. ⏳ Add post creation and management
4. ⏳ Implement image upload
5. ⏳ Add push notifications
6. ⏳ Implement search and filtering

---

## Dependencies Added

```kotlin
// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

// Firebase (already included)
implementation("com.google.firebase:firebase-auth:24.0.1")
```

---

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review API logs in Logcat
3. Test API endpoints with Postman
4. Verify database is properly imported
