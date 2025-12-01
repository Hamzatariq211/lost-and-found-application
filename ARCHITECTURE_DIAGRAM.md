# Architecture Diagram - Lost & Found Authentication

## System Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         LOST & FOUND APPLICATION                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌────────────────────────────────────────────────────────────────┐    │
│  │                    PRESENTATION LAYER                          │    │
│  │                                                                │    │
│  │  ┌──────────────────┐         ┌──────────────────┐           │    │
│  │  │ SignupActivity   │         │ LoginActivity    │           │    │
│  │  │                  │         │                  │           │    │
│  │  │ • UI Layout      │         │ • UI Layout      │           │    │
│  │  │ • Input Fields   │         │ • Input Fields   │           │    │
│  │  │ • Validation     │         │ • Validation     │           │    │
│  │  │ • Error Display  │         │ • Error Display  │           │    │
│  │  └────────┬─────────┘         └────────┬─────────┘           │    │
│  │           │                            │                     │    │
│  └───────────┼────────────────────────────┼─────────────────────┘    │
│              │                            │                          │
│  ┌───────────▼────────────────────────────▼─────────────────────┐    │
│  │                    VIEWMODEL LAYER                           │    │
│  │                                                              │    │
│  │  ┌──────────────────────────────────────────────────────┐  │    │
│  │  │           AuthViewModel                             │  │    │
│  │  │                                                      │  │    │
│  │  │  • authState: LiveData<AuthState>                  │  │    │
│  │  │  • userData: LiveData<UserData>                    │  │    │
│  │  │  • signup(...)                                     │  │    │
│  │  │  • login(...)                                      │  │    │
│  │  │  • logout()                                        │  │    │
│  │  │  • isUserLoggedIn()                                │  │    │
│  │  │                                                      │  │    │
│  │  │  AuthState:                                        │  │    │
│  │  │  ├─ Idle                                           │  │    │
│  │  │  ├─ Loading                                        │  │    │
│  │  │  ├─ Success(message)                              │  │    │
│  │  │  └─ Error(message)                                │  │    │
│  │  └──────────────────────────────────────────────────────┘  │    │
│  │                           │                                 │    │
│  └───────────────────────────┼─────────────────────────────────┘    │
│                              │                                       │
│  ┌───────────────────────────▼─────────────────────────────────┐    │
│  │                    REPOSITORY LAYER                         │    │
│  │                                                             │    │
│  │  ┌────────────────────────────────────────────────────┐   │    │
│  │  │         AuthRepository                            │   │    │
│  │  │                                                    │   │    │
│  │  │  • signup(...)                                    │   │    │
│  │  │  • login(...)                                     │   │    │
│  │  │  • logout()                                       │   │    │
│  │  │  • isUserLoggedIn()                               │   │    │
│  │  │  • getCurrentUser()                               │   │    │
│  │  │                                                    │   │    │
│  │  │  AuthResult:                                      │   │    │
│  │  │  ├─ Success(userData)                             │   │    │
│  │  │  ├─ Error(message)                                │   │    │
│  │  │  └─ Loading                                       │   │    │
│  │  └────────────────────────────────────────────────────┘   │    │
│  │                           │                                │    │
│  └───────────────────────────┼────────────────────────────────┘    │
│                              │                                      │
│  ┌───────────────────────────▼────────────────────────────────┐    │
│  │                    DATA LAYER                              │    │
│  │                                                            │    │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │    │
│  │  │   Firebase   │  │    MySQL     │  │SharedPrefs   │   │    │
│  │  │     Auth     │  │     API      │  │   Manager    │   │    │
│  │  │              │  │              │  │              │   │    │
│  │  │ • Create     │  │ • Signup     │  │ • Save Token │   │    │
│  │  │   User       │  │ • Login      │  │ • Save User  │   │    │
│  │  │ • Sign In    │  │ • Get Profile│  │ • Get Token  │   │    │
│  │  │ • Sign Out   │  │              │  │ • Clear All  │   │    │
│  │  └──────────────┘  └──────────────┘  └──────────────┘   │    │
│  │                                                            │    │
│  └────────────────────────────────────────────────────────────┘    │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Diagram

### Sign Up Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                      SIGN UP FLOW                               │
└─────────────────────────────────────────────────────────────────┘

User Input
    │
    ▼
┌─────────────────────────────────────────┐
│  SignupActivity.performSignup()         │
│  • Get input values                     │
│  • Validate inputs                      │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  Validation Check                       │
│  ├─ Full name not empty                 │
│  ├─ Username not empty                  │
│  ├─ Email format valid                  │
│  ├─ Mobile not empty                    │
│  └─ Password >= 6 chars                 │
└─────────────────────────────────────────┘
    │
    ├─ Invalid ──────────────────┐
    │                            │
    │                            ▼
    │                    Show Error Message
    │
    └─ Valid
        │
        ▼
┌─────────────────────────────────────────┐
│  AuthViewModel.signup()                 │
│  • Set state to Loading                 │
│  • Call repository                      │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  AuthRepository.signup()                │
│  ├─ Firebase: createUserWithEmail()     │
│  ├─ MySQL: POST /auth/signup.php        │
│  ├─ Save token locally                  │
│  └─ Save user data locally              │
└─────────────────────────────────────────┘
    │
    ├─ Success ──────────────────┐
    │                            │
    │                            ▼
    │                    ┌──────────────────┐
    │                    │ Update LiveData  │
    │                    │ • Set Success    │
    │                    │ • Set UserData   │
    │                    └──────────────────┘
    │                            │
    │                            ▼
    │                    ┌──────────────────┐
    │                    │ UI Observes      │
    │                    │ • Show Toast     │
    │                    │ • Navigate Home  │
    │                    └──────────────────┘
    │
    └─ Error ───────────────────┐
                                │
                                ▼
                        ┌──────────────────┐
                        │ Update LiveData  │
                        │ • Set Error      │
                        │ • Set Message    │
                        └──────────────────┘
                                │
                                ▼
                        ┌──────────────────┐
                        │ UI Observes      │
                        │ • Show Error     │
                        │ • Enable Button  │
                        └──────────────────┘
```

### Login Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                      LOGIN FLOW                                 │
└─────────────────────────────────────────────────────────────────┘

User Input
    │
    ▼
┌─────────────────────────────────────────┐
│  LoginActivity.performLogin()           │
│  • Get input values                     │
│  • Validate inputs                      │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  Validation Check                       │
│  ├─ Username not empty                  │
│  └─ Password not empty                  │
└─────────────────────────────────────────┘
    │
    ├─ Invalid ──────────────────┐
    │                            │
    │                            ▼
    │                    Show Error Message
    │
    └─ Valid
        │
        ▼
┌─────────────────────────────────────────┐
│  AuthViewModel.login()                  │
│  • Set state to Loading                 │
│  • Call repository                      │
└─────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────┐
│  AuthRepository.login()                 │
│  ├─ MySQL: POST /auth/login.php         │
│  ├─ Firebase: signInWithEmail() (opt)   │
│  ├─ Save token locally                  │
│  └─ Save user data locally              │
└─────────────────────────────────────────┘
    │
    ├─ Success ──────────────────┐
    │                            │
    │                            ▼
    │                    ┌──────────────────┐
    │                    │ Update LiveData  │
    │                    │ • Set Success    │
    │                    │ • Set UserData   │
    │                    └──────────────────┘
    │                            │
    │                            ▼
    │                    ┌──────────────────┐
    │                    │ UI Observes      │
    │                    │ • Show Toast     │
    │                    │ • Navigate Home  │
    │                    └──────────────────┘
    │
    └─ Error ───────────────────┐
                                │
                                ▼
                        ┌──────────────────┐
                        │ Update LiveData  │
                        │ • Set Error      │
                        │ • Set Message    │
                        └──────────────────┘
                                │
                                ▼
                        ┌──────────────────┐
                        │ UI Observes      │
                        │ • Show Error     │
                        │ • Enable Button  │
                        └──────────────────┘
```

---

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      CLASS RELATIONSHIPS                        │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────┐
│    SignupActivity        │
├──────────────────────────┤
│ - authViewModel          │
│ - fullNameInput          │
│ - usernameInput          │
│ - emailInput             │
│ - mobileInput            │
│ - passwordInput          │
├──────────────────────────┤
│ + onCreate()             │
│ + performSignup()        │
└──────────────────────────┘
           │
           │ uses
           ▼
┌──────────────────────────┐
│    AuthViewModel         │
├──────────────────────────┤
│ - authRepository         │
│ - authState: LiveData    │
│ - userData: LiveData     │
├──────────────────────────┤
│ + signup()               │
│ + login()                │
│ + logout()               │
│ + isUserLoggedIn()       │
└──────────────────────────┘
           │
           │ uses
           ▼
┌──────────────────────────┐
│   AuthRepository         │
├──────────────────────────┤
│ - apiService             │
│ - firebaseAuth           │
│ - prefsManager           │
├──────────────────────────┤
│ + signup()               │
│ + login()                │
│ + logout()               │
│ + isUserLoggedIn()       │
└──────────────────────────┘
           │
           ├─────────────────────────────┐
           │                             │
           ▼                             ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│    ApiService            │  │ SharedPreferencesManager │
├──────────────────────────┤  ├──────────────────────────┤
│ + signup()               │  │ + saveAuthToken()        │
│ + login()                │  │ + getAuthToken()         │
│ + getProfile()           │  │ + saveUserData()         │
└──────────────────────────┘  │ + clearAll()             │
           │                  └──────────────────────────┘
           │
           ▼
┌──────────────────────────┐
│   RetrofitClient         │
├──────────────────────────┤
│ - BASE_URL               │
│ - okHttpClient           │
│ - retrofit               │
│ - apiService             │
└──────────────────────────┘
           │
           ▼
┌──────────────────────────┐
│   PHP Backend API        │
├──────────────────────────┤
│ /auth/signup.php         │
│ /auth/login.php          │
│ /user/get_profile.php    │
└──────────────────────────┘
           │
           ▼
┌──────────────────────────┐
│   MySQL Database         │
├──────────────────────────┤
│ users table              │
│ user_sessions table      │
│ posts table              │
│ notifications table      │
└──────────────────────────┘
```

---

## State Management

```
┌─────────────────────────────────────────────────────────────────┐
│                    AUTH STATE MACHINE                           │
└─────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │    Idle      │
                    └──────────────┘
                           │
                ┌──────────┴──────────┐
                │                     │
                ▼                     ▼
        ┌──────────────┐      ┌──────────────┐
        │   Signup     │      │    Login     │
        │   Clicked    │      │   Clicked    │
        └──────────────┘      └──────────────┘
                │                     │
                └──────────┬──────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   Loading    │
                    └──────────────┘
                           │
                ┌──────────┴──────────┐
                │                     │
                ▼                     ▼
        ┌──────────────┐      ┌──────────────┐
        │   Success    │      │    Error     │
        │              │      │              │
        │ • Save Token │      │ • Show Toast │
        │ • Save User  │      │ • Enable UI  │
        │ • Navigate   │      │              │
        └──────────────┘      └──────────────┘
                │                     │
                │                     │
                └──────────┬──────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │    Idle      │
                    └──────────────┘
```

---

## Component Interaction

```
┌─────────────────────────────────────────────────────────────────┐
│                  COMPONENT INTERACTION                          │
└─────────────────────────────────────────────────────────────────┘

User
  │
  ├─ Enters Data
  │
  ▼
SignupActivity / LoginActivity
  │
  ├─ Validates Input
  ├─ Calls ViewModel
  │
  ▼
AuthViewModel
  │
  ├─ Manages State
  ├─ Calls Repository
  │
  ▼
AuthRepository
  │
  ├─ Coordinates Data Sources
  │
  ├─────────────────────────────────┐
  │                                 │
  ▼                                 ▼
Firebase Auth                    MySQL API
  │                                 │
  ├─ Create User                    ├─ POST /signup
  ├─ Sign In                        ├─ POST /login
  │                                 │
  ▼                                 ▼
Firebase DB                     MySQL DB
                                    │
                                    ├─ Hash Password
                                    ├─ Create Token
                                    ├─ Store User
                                    │
                                    ▼
                            Return Token & User Data
                                    │
                                    ▼
                            SharedPreferences
                                    │
                                    ├─ Save Token
                                    ├─ Save User Data
                                    │
                                    ▼
                            AuthViewModel
                                    │
                                    ├─ Update LiveData
                                    │
                                    ▼
                            SignupActivity / LoginActivity
                                    │
                                    ├─ Observe Changes
                                    ├─ Update UI
                                    ├─ Show Toast
                                    ├─ Navigate
                                    │
                                    ▼
                                  User
```

---

## Technology Stack

```
┌─────────────────────────────────────────────────────────────────┐
│                    TECHNOLOGY STACK                             │
└─────────────────────────────────────────────────────────────────┘

Frontend (Android)
├─ Kotlin 2.0.21
├─ Android 14+ (Min SDK 34)
├─ Material Design 3
├─ Jetpack Components
│  ├─ ViewModel
│  ├─ LiveData
│  ├─ Lifecycle
│  └─ Room
├─ Retrofit 2.9.0
├─ OkHttp 4.11.0
├─ Coroutines 1.7.3
├─ Firebase Auth 24.0.1
└─ Glide 4.16.0

Backend (Server)
├─ PHP 8.2.12
├─ MySQL 8.0
├─ Apache 2.4.58
├─ XAMPP
└─ PDO (Database Abstraction)

Development Tools
├─ Android Studio
├─ Gradle 8.9.1
├─ Git
├─ Postman
└─ Firebase Console
```

---

This architecture provides a scalable, maintainable, and secure authentication system for the Lost & Found application.
