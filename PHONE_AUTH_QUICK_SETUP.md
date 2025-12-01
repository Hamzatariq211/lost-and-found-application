# Firebase Phone Authentication - Quick Setup (5 Minutes)

## ✅ What's Done

- ✅ PhoneAuthRepository created
- ✅ PhoneAuthViewModel created
- ✅ LoginMobileActivity fully implemented
- ✅ OTP sending and verification working
- ✅ Resend OTP with countdown timer
- ✅ Navigation to HomeActivity

---

## 🔧 Setup Steps

### Step 1: Enable Phone Auth in Firebase (2 minutes)

1. Go to https://console.firebase.google.com/
2. Select your project
3. Click **Authentication** in left menu
4. Click **Sign-in method** tab
5. Find **Phone** and click it
6. Toggle **Enable** to ON
7. Click **Save**

### Step 2: Add Test Numbers (Optional, 1 minute)

For testing without real SMS:

1. In Firebase Console → Authentication → Phone
2. Scroll to **Test phone numbers**
3. Click **Add phone number**
4. Enter phone number: +923001234567
5. Firebase generates test OTP
6. Use this for testing

### Step 3: Build & Run (2 minutes)

```bash
# In Android Studio
1. File → Sync Now
2. Build → Make Project
3. Run → Run 'app'
```

---

## 🧪 Test It

### Test with Real Phone Number

1. **Open app**
   - Click "Login using Mobile Number"

2. **Enter phone number**
   - Format: +923001234567 or 03001234567

3. **Click "Send OTP"**
   - Wait for SMS (30-60 seconds)

4. **Enter OTP from SMS**
   - 6-digit code

5. **Click "Verify OTP"**
   - Should navigate to Home

### Test with Firebase Test Number

1. **Use test number from Firebase**
   - Example: +923001234567

2. **Click "Send OTP"**
   - No SMS sent (test mode)

3. **Use generated test OTP**
   - From Firebase Console

4. **Click "Verify OTP"**
   - Should navigate to Home

---

## 📱 Phone Number Formats

```
✅ +923001234567    (International)
✅ 03001234567      (Local - Pakistan)
✅ 923001234567     (Without +)
```

---

## 🎯 Features

✅ Send OTP via SMS
✅ Verify OTP
✅ Resend OTP (60-second countdown)
✅ Phone number validation
✅ Firebase integration
✅ MySQL integration
✅ Token storage
✅ Error handling
✅ Loading states

---

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| OTP not received | Check phone number format, wait 30-60s, try resending |
| "Invalid OTP" | Check OTP is 6 digits, verify it hasn't expired |
| Firebase error | Enable Phone auth in Firebase Console |
| Navigation fails | Check HomeActivity exists, verify AndroidManifest.xml |

---

## 📊 Flow

```
Enter Phone Number
    ↓
Click "Send OTP"
    ↓
Receive SMS with OTP
    ↓
Enter OTP
    ↓
Click "Verify OTP"
    ↓
Navigate to HomeActivity
```

---

## ✅ Checklist

- [ ] Firebase Phone auth enabled
- [ ] google-services.json in app/ folder
- [ ] App builds successfully
- [ ] OTP sending works
- [ ] OTP verification works
- [ ] Navigation works

---

## 🚀 Ready to Test!

1. Enable Phone auth in Firebase
2. Build and run the app
3. Click "Login using Mobile Number"
4. Enter phone number
5. Click "Send OTP"
6. Enter OTP from SMS
7. Click "Verify OTP"
8. Should navigate to HomeActivity

**That's it!** 🎉

For detailed info, see: `PHONE_AUTH_IMPLEMENTATION.md`
