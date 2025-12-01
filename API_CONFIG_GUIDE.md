# API Configuration Guide

## Problem Solved
Your app now automatically adapts to different network environments (emulators vs physical devices) using a centralized configuration system.

## How It Works

The app uses `ApiConfig.kt` which automatically detects whether you're running on:
- **Android Emulator** → Uses `10.0.2.2`
- **Physical Device** → Uses your local network IP

## Setup Instructions

### For Emulator Testing (Default)
No changes needed! The app automatically detects the emulator and uses `10.0.2.2`.

### For Physical Device Testing
1. Open `ApiConfig.kt` 
2. Find the line: `private const val LOCAL_NETWORK_IP = "192.168.1.100"`
3. Replace with your computer's actual IP address:
   - **Windows**: Run `ipconfig` in CMD, look for IPv4 Address
   - **Mac/Linux**: Run `ifconfig`, look for inet address
4. Example: Change to `"192.168.0.105"` or whatever your PC's IP is

### Manual Override (Optional)
If you need to force a specific environment, add this in your Application class or SplashActivity:

```kotlin
// Force emulator mode
ApiConfig.setEnvironment(ApiConfig.Environment.EMULATOR)

// Force physical device mode
ApiConfig.setEnvironment(ApiConfig.Environment.PHYSICAL_DEVICE)

// Use custom IP
ApiConfig.setCustomIP("192.168.1.50")
```

## What Changed

### Files Modified:
1. ✅ `ApiConfig.kt` - New centralized configuration file
2. ✅ `RetrofitClient.kt` - Now uses ApiConfig
3. ✅ `SplashActivity.kt` - Updated to use ApiConfig
4. ✅ `FCMService.kt` - Updated to use ApiConfig
5. ✅ `MyPostsActivity.kt` - Updated to use ApiConfig
6. ✅ `HomeActivity.kt` - Updated to use ApiConfig
7. ✅ `ChatScreenActivity.kt` - Updated to use ApiConfig
8. ✅ `ChatsActivity.kt` - Updated to use ApiConfig
9. ✅ `AddPostActivity.kt` - Updated to use ApiConfig

### Old Way:
```kotlin
val url = URL("http://10.0.2.2/lost_and_found_api/posts/get_posts.php")
```

### New Way:
```kotlin
val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_MY_POSTS))
```

## Testing Different Devices

1. **Emulator 1** → Automatically uses `10.0.2.2`
2. **Emulator 2** → Automatically uses `10.0.2.2`
3. **Physical Phone** → Automatically uses your local network IP

No more manual URL changes! 🎉

## Troubleshooting

**App can't connect on physical device?**
- Make sure your phone and PC are on the same WiFi network
- Update `LOCAL_NETWORK_IP` in `ApiConfig.kt` with your PC's correct IP
- Make sure XAMPP is allowing connections from your network (not just localhost)

**Want to see which IP is being used?**
Check the logs - ApiConfig logs show which base URL is being used.

## Environment Detection

The app automatically detects emulators by checking:
- Build fingerprint (e.g., "google/sdk_gphone_")
- Build model (e.g., "Emulator", "Android SDK built for x86")
- Build manufacturer (e.g., "Genymotion")

Physical devices will fail these checks and use the local network IP instead.

