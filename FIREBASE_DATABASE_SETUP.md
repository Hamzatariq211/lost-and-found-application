# Firebase Database Setup Guide
## Mirroring MySQL Database to Firebase

This guide will help you set up Firebase Firestore to mirror your MySQL database structure.

## Project Information
- Project ID: `lost-and-found-applicati-d438c`
- Storage Bucket: `lost-and-found-applicati-d438c.firebasestorage.app`

---

## 1. Firebase Console Setup Steps

### Step 1: Enable Firestore Database
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: **lost-and-found-applicati-d438c**
3. Click on **Firestore Database** in the left menu
4. Click **Create database**
5. Select **Start in production mode** (we'll add custom rules)
6. Choose your preferred location (e.g., `us-central`)

### Step 2: Enable Firebase Storage
1. In Firebase Console, click **Storage** in the left menu
2. Click **Get Started**
3. Use production mode
4. Choose the same location as Firestore

### Step 3: Enable Firebase Authentication
1. Click **Authentication** in the left menu
2. Click **Get Started**
3. Enable **Phone** authentication
4. Enable **Email/Password** authentication

---

## 2. Firestore Database Structure

### Collections and Documents Structure:

```
lost_and_found_db (Root)
├── users/
│   └── {userId}/
│       ├── user_id: number
│       ├── full_name: string
│       ├── username: string
│       ├── email: string
│       ├── mobile_number: string
│       ├── fcm_token: string
│       ├── password: string (hashed)
│       ├── created_at: timestamp
│       ├── updated_at: timestamp
│       ├── is_active: boolean
│       └── post_count: number
│
├── posts/
│   └── {postId}/
│       ├── post_id: number
│       ├── user_id: number
│       ├── item_name: string
│       ├── item_description: string
│       ├── location: string
│       ├── item_type: string (lost/found)
│       ├── item_image: string (base64 or storage URL)
│       ├── status: string (active/resolved/deleted)
│       ├── created_at: timestamp
│       ├── updated_at: timestamp
│       └── sync_status: boolean
│
├── notifications/
│   └── {notificationId}/
│       ├── notification_id: number
│       ├── user_id: number
│       ├── post_id: number
│       ├── notification_type: string (match/message/system)
│       ├── title: string
│       ├── message: string
│       ├── is_read: boolean
│       └── created_at: timestamp
│
├── user_sessions/
│   └── {sessionId}/
│       ├── session_id: number
│       ├── user_id: number
│       ├── token: string
│       ├── device_info: string
│       ├── created_at: timestamp
│       └── expires_at: timestamp
│
├── search_history/
│   └── {searchId}/
│       ├── search_id: number
│       ├── user_id: number
│       ├── search_query: string
│       ├── search_type: string
│       └── created_at: timestamp
│
├── chat_rooms/
│   └── {roomId}/
│       ├── room_id: number
│       ├── post_id: number
│       ├── sender_id: number
│       ├── receiver_id: number
│       ├── created_at: timestamp
│       ├── updated_at: timestamp
│       ├── is_active: boolean
│       └── last_message: string
│
└── chat_messages/
    └── {messageId}/
        ├── message_id: number
        ├── room_id: number
        ├── sender_id: number
        ├── message_text: string
        ├── message_type: string (text/image)
        ├── is_read: boolean
        └── created_at: timestamp
```

---

## 3. Firestore Security Rules

Copy these rules to Firebase Console → Firestore Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Users collection
    match /users/{userId} {
      // Anyone can read user profiles
      allow read: if true;
      // Only the user can create/update their own profile
      allow create: if isAuthenticated();
      allow update: if isOwner(userId) || isAuthenticated();
      allow delete: if isOwner(userId);
    }
    
    // Posts collection
    match /posts/{postId} {
      // Anyone can read posts
      allow read: if true;
      // Authenticated users can create posts
      allow create: if isAuthenticated();
      // Only post owner can update/delete
      allow update, delete: if isAuthenticated() && 
                               resource.data.user_id == request.auth.uid;
    }
    
    // Notifications collection
    match /notifications/{notificationId} {
      // Users can read their own notifications
      allow read: if isAuthenticated() && 
                     resource.data.user_id == request.auth.uid;
      // System can create notifications
      allow create: if isAuthenticated();
      // Users can update their own notifications (mark as read)
      allow update: if isAuthenticated() && 
                       resource.data.user_id == request.auth.uid;
      allow delete: if isAuthenticated() && 
                       resource.data.user_id == request.auth.uid;
    }
    
    // User sessions
    match /user_sessions/{sessionId} {
      allow read, write: if isAuthenticated();
    }
    
    // Search history
    match /search_history/{searchId} {
      allow read, write: if isAuthenticated() && 
                            resource.data.user_id == request.auth.uid;
    }
    
    // Chat rooms
    match /chat_rooms/{roomId} {
      allow read: if isAuthenticated() && 
                     (resource.data.sender_id == request.auth.uid || 
                      resource.data.receiver_id == request.auth.uid);
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && 
                       (resource.data.sender_id == request.auth.uid || 
                        resource.data.receiver_id == request.auth.uid);
    }
    
    // Chat messages
    match /chat_messages/{messageId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update: if isAuthenticated() && 
                       resource.data.sender_id == request.auth.uid;
    }
  }
}
```

---

## 4. Firebase Storage Rules

Copy these rules to Firebase Console → Storage → Rules:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // Post images
    match /post_images/{userId}/{imageId} {
      // Anyone can read
      allow read: if true;
      // Only authenticated users can upload
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Profile pictures
    match /profile_pictures/{userId}/{imageId} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Chat images
    match /chat_images/{roomId}/{imageId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

---

## 5. Firestore Indexes

Create these composite indexes in Firebase Console → Firestore → Indexes:

### Collection: posts
- Fields: `user_id` (Ascending), `created_at` (Descending)
- Fields: `item_type` (Ascending), `status` (Ascending), `created_at` (Descending)
- Fields: `status` (Ascending), `created_at` (Descending)

### Collection: notifications
- Fields: `user_id` (Ascending), `is_read` (Ascending), `created_at` (Descending)

### Collection: chat_messages
- Fields: `room_id` (Ascending), `created_at` (Ascending)

### Collection: chat_rooms
- Fields: `sender_id` (Ascending), `updated_at` (Descending)
- Fields: `receiver_id` (Ascending), `updated_at` (Descending)

---

## 6. Next Steps

1. **Apply Firestore Rules**: Copy the security rules to Firebase Console
2. **Create Indexes**: Set up the composite indexes as specified
3. **Enable Authentication**: Set up Phone and Email authentication
4. **Test Connection**: Use the Firebase helper class in the app
5. **Data Migration**: Use the migration script to sync MySQL data to Firestore

---

## 7. Important Notes

- **User IDs**: Firebase uses string UIDs, while MySQL uses integer user_id. Keep both for compatibility.
- **Phone Auth**: Ensure phone numbers are in E.164 format (+1234567890)
- **Storage**: Large images should use Firebase Storage instead of base64 in Firestore
- **Costs**: Monitor Firebase usage - Firestore has free tier limits
- **Sync**: Keep MySQL as primary database, use Firebase for real-time features

---

## 8. Testing

Test endpoints:
- Authentication: Test phone and email login
- Firestore: Test reading/writing posts
- Storage: Test image uploads
- Real-time: Test chat messages sync


