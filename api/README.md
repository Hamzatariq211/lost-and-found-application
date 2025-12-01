# Lost and Found Application - Backend API

## Setup Instructions for XAMPP

### 1. Database Setup

1. Start XAMPP Control Panel
2. Start Apache and MySQL services
3. Open phpMyAdmin (http://localhost/phpmyadmin)
4. Create a new database or import the SQL file:
   - Click on "Import" tab
   - Choose the file: `database/lost_and_found.sql`
   - Click "Go" to execute

### 2. API Installation

1. Copy the `api` folder to your XAMPP htdocs directory:
   ```
   C:\xampp\htdocs\lost_and_found_api\
   ```

2. Create an `uploads` folder inside the api directory:
   ```
   C:\xampp\htdocs\lost_and_found_api\uploads\
   ```

3. Set folder permissions (if needed) to allow file uploads

### 3. Configuration

Edit `api/config/database.php` if your MySQL credentials are different:
```php
private $host = "localhost";
private $db_name = "lost_and_found_db";
private $username = "root";
private $password = "";
```

### 4. Test the API

Open your browser and test:
```
http://localhost/lost_and_found_api/posts/get_posts.php
```

---

## API Endpoints

### Base URL
```
http://localhost/lost_and_found_api/
```

### Authentication Endpoints

#### 1. Sign Up
- **URL**: `/auth/signup.php`
- **Method**: `POST`
- **Body** (JSON):
```json
{
  "full_name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "mobile_number": "+1234567890",
  "password": "password123"
}
```
- **Response**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user_id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "full_name": "John Doe",
    "token": "abc123..."
  }
}
```

#### 2. Login (Email/Username)
- **URL**: `/auth/login.php`
- **Method**: `POST`
- **Body** (JSON):
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

#### 3. Login (Mobile Number)
- **URL**: `/auth/login_mobile.php`
- **Method**: `POST`
- **Body** (JSON):
```json
{
  "mobile_number": "+1234567890",
  "password": "password123"
}
```

---

### Post Endpoints

#### 4. Create Post (with Image Upload)
- **URL**: `/posts/create_post.php`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Body** (multipart/form-data):
  - `item_name`: "Water Bottle"
  - `item_description`: "Blue water bottle"
  - `location`: "C-110"
  - `item_type`: "found" or "lost"
  - `item_image`: (file upload)

#### 5. Get All Posts
- **URL**: `/posts/get_posts.php`
- **Method**: `GET`
- **Query Parameters**:
  - `item_type`: "lost" or "found" (optional)
  - `search`: search term (optional)
  - `limit`: number of results (default: 50)
  - `offset`: pagination offset (default: 0)
- **Example**:
```
/posts/get_posts.php?item_type=found&search=bottle&limit=10
```

#### 6. Get Post by ID
- **URL**: `/posts/get_post_by_id.php`
- **Method**: `GET`
- **Query Parameters**:
  - `post_id`: ID of the post
- **Example**:
```
/posts/get_post_by_id.php?post_id=1
```

#### 7. Sync Offline Posts
- **URL**: `/posts/sync_posts.php`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Body** (JSON):
```json
{
  "posts": [
    {
      "local_id": "local_1",
      "item_name": "Notebook",
      "item_description": "Blue notebook",
      "location": "D-312",
      "item_type": "found"
    }
  ]
}
```

---

### Notification Endpoints

#### 8. Get Notifications
- **URL**: `/notifications/get_notifications.php`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`

#### 9. Create Notification
- **URL**: `/notifications/create_notification.php`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Body** (JSON):
```json
{
  "user_id": 1,
  "post_id": 5,
  "notification_type": "match",
  "title": "Item Match Found!",
  "message": "A water bottle matching your search was found"
}
```

#### 10. Mark Notification as Read
- **URL**: `/notifications/mark_as_read.php`
- **Method**: `POST`
- **Headers**: 
  - `Authorization: Bearer {token}`
- **Body** (JSON):
```json
{
  "notification_id": 1
}
```

---

### User Endpoints

#### 11. Get User Profile
- **URL**: `/user/get_profile.php`
- **Method**: `GET`
- **Headers**: 
  - `Authorization: Bearer {token}`

---

## Android Integration

### Add Dependencies to build.gradle.kts

```kotlin
dependencies {
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp for logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Room Database for offline storage
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
```

### Add Permissions to AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

---

## Project Rubrics Coverage

✅ **Store data locally (10 points)** - Use Room Database in Android
✅ **Data sync (15 points)** - `/posts/sync_posts.php` endpoint
✅ **Store data on cloud (10 points)** - MySQL database via XAMPP
✅ **GET/POST images (10 points)** - Image upload in `/posts/create_post.php`
✅ **Lists and search boxes (10 points)** - Search parameter in `/posts/get_posts.php`
✅ **Signup and Login with Authentication (10 points)** - Token-based auth
✅ **Push Notifications (10 points)** - Notification endpoints + FCM integration needed

---

## Testing with Postman

1. Import the API endpoints into Postman
2. Test signup and login first to get authentication token
3. Use the token in Authorization header for protected endpoints
4. Test image upload using form-data in Postman

---

## Notes

- Default password for sample users: `password`
- Images are stored in `/uploads/` folder
- Tokens expire after 30 days
- Update image URLs in responses to match your server address
