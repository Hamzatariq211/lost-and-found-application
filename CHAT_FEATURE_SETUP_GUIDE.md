# CHAT FEATURE SETUP GUIDE
## Lost & Found Application

This guide will help you set up the complete chat feature for your Lost & Found application.

---

## Step 1: Setup XAMPP and Copy API Files

### 1.1 XAMPP Directory Structure
After installing XAMPP, copy your API files to:
```
C:\xampp\htdocs\lost_and_found_api\
```

Your folder structure should look like this:
```
C:\xampp\htdocs\lost_and_found_api\
├── config\
│   ├── database.php
│   └── cors.php
├── auth\
│   ├── login.php
│   ├── signup.php
│   └── login_mobile.php
├── posts\
│   ├── get_posts.php
│   ├── create_post_base64.php
│   ├── create_post.php
│   └── sync_posts.php
├── chat\
│   └── chat_api.php  (NEW - JUST CREATED)
├── user\
│   └── get_profile.php
└── index.php
```

### 1.2 Copy Files to XAMPP
Run this command in your project directory:
```cmd
xcopy /E /I /Y "E:\Mobile dev Projects\Lost_and_Found_Application\api\*" "C:\xampp\htdocs\lost_and_found_api\"
```

---

## Step 2: Setup Database

### 2.1 Start XAMPP Services
1. Open XAMPP Control Panel
2. Start **Apache** and **MySQL**

### 2.2 Create Database
1. Open browser and go to: `http://localhost/phpmyadmin`
2. Create a new database named: **lost_and_found_db**

### 2.3 Run SQL Scripts in Order

**Execute these SQL files in phpMyAdmin in this exact order:**

#### A. Main Database Schema
Run: `E:\Mobile dev Projects\Lost_and_Found_Application\database\lost_and_found.sql`

This creates:
- users table
- posts table (with Base64 image support)
- notifications table
- sessions table

#### B. Chat System Schema
Run: `E:\Mobile dev Projects\Lost_and_Found_Application\database\CHAT_SCHEMA.sql`

This creates:
- chat_rooms table
- chat_messages table

### 2.4 Verify Tables
Run this query to verify all tables are created:
```sql
SHOW TABLES FROM lost_and_found_db;
```

You should see:
- users
- posts
- notifications
- user_sessions
- search_history
- **chat_rooms** (new)
- **chat_messages** (new)

---

## Step 3: Verify Database Configuration

Make sure your `config/database.php` has these settings:
```php
<?php
class Database {
    private $host = "localhost";
    private $db_name = "lost_and_found_db";
    private $username = "root";
    private $password = "";
    public $conn;

    public function getConnection() {
        $this->conn = null;
        try {
            $this->conn = new PDO(
                "mysql:host=" . $this->host . ";dbname=" . $this->db_name,
                $this->username,
                $this->password
            );
            $this->conn->exec("set names utf8");
            $this->conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch(PDOException $exception) {
            echo "Connection error: " . $exception->getMessage();
        }
        return $this->conn;
    }
}
?>
```

---

## Step 4: Test API Endpoints

### 4.1 Test Chat API
Open browser and test:
```
http://localhost/lost_and_found_api/chat/chat_api.php?action=get_rooms&user_id=1
```

Expected response:
```json
{
  "success": true,
  "rooms": []
}
```

### 4.2 Test Posts API (Updated with user_id)
```
http://localhost/lost_and_found_api/posts/get_posts.php
```

Expected response should include `user_id` in the user object:
```json
{
  "success": true,
  "count": 0,
  "data": [
    {
      "post_id": 1,
      "item_name": "Test Item",
      "user": {
        "user_id": 1,
        "username": "testuser",
        "full_name": "Test User",
        "mobile_number": "+1234567890"
      }
    }
  ]
}
```

---

## Step 5: Build and Run the Android App

### 5.1 Check for Errors
Before building, verify no errors in the code:

Run in your project directory:
```cmd
gradlew clean
```

### 5.2 Build the Project
```cmd
gradlew assembleDebug
```

### 5.3 Run on Emulator/Device
- Make sure XAMPP Apache and MySQL are running
- Click "Run" in Android Studio
- The app should build successfully

---

## Step 6: Test the Chat Feature

### 6.1 Create Test Users
1. Sign up with 2 different accounts (User A and User B)

### 6.2 Create a Post
1. Login as User A
2. Click the **+** button
3. Create a "Found" or "Lost" item post with an image

### 6.3 Start a Chat
1. Logout and login as User B
2. On the home screen, click on User A's post
3. In the dialog, click **Contact** button
4. You'll be taken to the chat screen

### 6.4 Send Messages
1. Type a message and click send
2. Messages should appear in real-time
3. Logout and login as User A
4. Click the **Chat Icon** (email icon) in the home screen
5. You should see the conversation with User B
6. Click on it to open and reply

---

## Features Overview

### Home Screen Updates
- **Chat Icon**: New email icon next to profile icon
- Click it to view all your conversations

### Contact Button Behavior
- When you click "Contact" on a post:
  - Creates a chat room (or opens existing one)
  - Navigates directly to chat screen
  - Prevents users from chatting with their own posts

### Chats Screen (ChatsActivity)
- Shows all conversations
- Displays last message
- Shows unread message count
- Shows item thumbnail and name
- Shows other user's name
- Auto-refreshes when opened

### Chat Screen (ChatScreenActivity)
- Real-time messaging
- Auto-refreshes every 3 seconds
- Sent messages on the right (purple)
- Received messages on the left (white)
- Shows sender name and timestamp
- Marks messages as read automatically

---

## Database Schema Details

### chat_rooms Table
```sql
- room_id (Primary Key)
- post_id (Foreign Key to posts)
- sender_id (Foreign Key to users)
- receiver_id (Foreign Key to users)
- created_at
- updated_at
- is_active
- Unique: (post_id, sender_id, receiver_id)
```

### chat_messages Table
```sql
- message_id (Primary Key)
- room_id (Foreign Key to chat_rooms)
- sender_id (Foreign Key to users)
- message_text
- message_type (text/image)
- is_read (Boolean)
- created_at
```

---

## API Endpoints

### Create Chat Room
```
POST: /chat/chat_api.php?action=create_room
Parameters: post_id, sender_id, receiver_id
```

### Get User's Chat Rooms
```
GET: /chat/chat_api.php?action=get_rooms&user_id={id}
```

### Send Message
```
POST: /chat/chat_api.php?action=send_message
Parameters: room_id, sender_id, message_text
```

### Get Messages in Room
```
GET: /chat/chat_api.php?action=get_messages&room_id={id}
```

### Mark Messages as Read
```
POST: /chat/chat_api.php?action=mark_read
Parameters: room_id, user_id
```

### Get Unread Count
```
GET: /chat/chat_api.php?action=get_unread_count&user_id={id}
```

---

## Troubleshooting

### Error: "Connection refused"
- Make sure XAMPP Apache is running
- Verify the API URL is: `http://10.0.2.2/lost_and_found_api/...`
- For physical device, use your computer's IP address instead

### Error: "Table doesn't exist"
- Run the SQL scripts again in correct order
- Verify database name is `lost_and_found_db`

### Error: "Unresolved reference"
- Clean and rebuild project: `gradlew clean build`
- Sync Gradle files

### Images not showing in chat
- Check that Base64 images are properly stored in database
- Verify `item_image` column in posts table is `MEDIUMTEXT`

### Messages not appearing
- Check that chat_api.php has correct database credentials
- Verify CORS is properly configured
- Check Android Logcat for error messages

---

## Next Steps (Optional Enhancements)

1. **Push Notifications**: Notify users when they receive a new message
2. **Image Sharing**: Allow sending images in chat
3. **Typing Indicators**: Show when the other person is typing
4. **Read Receipts**: Show double checkmarks when message is read
5. **Delete Conversations**: Allow users to delete chat rooms
6. **Block Users**: Allow users to block others
7. **Report Feature**: Report inappropriate messages

---

## Files Created

### Kotlin Files:
- `models/ChatRoom.kt`
- `models/Message.kt`
- `adapters/ChatRoomAdapter.kt`
- `adapters/MessageAdapter.kt`
- `ChatsActivity.kt`
- `ChatScreenActivity.kt`

### XML Layout Files:
- `layout/activity_chats.xml`
- `layout/activity_chat_screen.xml`
- `layout/item_chat_room.xml`
- `layout/item_message_sent.xml`
- `layout/item_message_received.xml`

### API Files:
- `api/chat/chat_api.php`

### Database Files:
- `database/CHAT_SCHEMA.sql`

### Updated Files:
- `app/src/main/res/layout/activity_home.xml` (added chat icon)
- `app/src/main/java/.../HomeActivity.kt` (added chat functionality)
- `app/src/main/java/.../api/ApiService.kt` (added user_id to PostUser)
- `api/posts/get_posts.php` (added user_id to response)
- `app/src/main/AndroidManifest.xml` (registered chat activities)

---

**You're all set! The complete chat feature is now implemented.**

