<?php
/**
 * Export all MySQL data for Firebase migration
 * This creates JSON files that can be imported to Firebase
 */

header('Content-Type: application/json');
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

$exportData = [
    'users' => [],
    'posts' => [],
    'notifications' => [],
    'chat_rooms' => [],
    'chat_messages' => [],
    'user_sessions' => [],
    'search_history' => []
];

try {
    // Export Users
    $userQuery = "SELECT user_id, full_name, username, email, mobile_number, fcm_token,
                  is_active, created_at, updated_at
                  FROM users WHERE is_active = 1";
    $userStmt = $db->prepare($userQuery);
    $userStmt->execute();

    while ($row = $userStmt->fetch(PDO::FETCH_ASSOC)) {
        $userId = $row['user_id'];
        $exportData['users'][$userId] = [
            'user_id' => (int)$row['user_id'],
            'full_name' => $row['full_name'],
            'username' => $row['username'],
            'email' => $row['email'],
            'mobile_number' => $row['mobile_number'] ?? '',
            'fcm_token' => $row['fcm_token'] ?? '',
            'is_active' => (bool)$row['is_active'],
            'created_at' => strtotime($row['created_at']) * 1000,
            'updated_at' => strtotime($row['updated_at']) * 1000
        ];
    }

    // Export Posts
    $postQuery = "SELECT post_id, user_id, item_name, item_description, location,
                  item_type, item_image, status, created_at, updated_at
                  FROM posts WHERE status != 'deleted'";
    $postStmt = $db->prepare($postQuery);
    $postStmt->execute();

    while ($row = $postStmt->fetch(PDO::FETCH_ASSOC)) {
        $postId = $row['post_id'];
        $exportData['posts'][$postId] = [
            'post_id' => (int)$row['post_id'],
            'user_id' => (string)$row['user_id'],
            'item_name' => $row['item_name'],
            'item_description' => $row['item_description'],
            'location' => $row['location'],
            'item_type' => $row['item_type'],
            'item_image' => $row['item_image'] ?? '',
            'status' => $row['status'],
            'created_at' => strtotime($row['created_at']) * 1000,
            'updated_at' => strtotime($row['updated_at']) * 1000,
            'sync_status' => true
        ];
    }

    // Export Notifications
    $notifQuery = "SELECT notification_id, user_id, post_id, notification_type,
                   title, message, is_read, created_at
                   FROM notifications
                   ORDER BY created_at DESC
                   LIMIT 1000";
    $notifStmt = $db->prepare($notifQuery);
    $notifStmt->execute();

    while ($row = $notifStmt->fetch(PDO::FETCH_ASSOC)) {
        $notifId = $row['notification_id'];
        $exportData['notifications'][$notifId] = [
            'notification_id' => (int)$row['notification_id'],
            'user_id' => (string)$row['user_id'],
            'post_id' => (int)$row['post_id'],
            'notification_type' => $row['notification_type'],
            'title' => $row['title'],
            'message' => $row['message'],
            'is_read' => (bool)$row['is_read'],
            'created_at' => strtotime($row['created_at']) * 1000
        ];
    }

    // Export Chat Rooms
    $roomQuery = "SELECT room_id, post_id, sender_id, receiver_id,
                  is_active, created_at, updated_at
                  FROM chat_rooms WHERE is_active = 1";
    $roomStmt = $db->prepare($roomQuery);
    $roomStmt->execute();

    while ($row = $roomStmt->fetch(PDO::FETCH_ASSOC)) {
        $roomId = $row['room_id'];
        $exportData['chat_rooms'][$roomId] = [
            'room_id' => (int)$row['room_id'],
            'post_id' => (int)$row['post_id'],
            'sender_id' => (string)$row['sender_id'],
            'receiver_id' => (string)$row['receiver_id'],
            'is_active' => (bool)$row['is_active'],
            'created_at' => strtotime($row['created_at']) * 1000,
            'updated_at' => strtotime($row['updated_at']) * 1000
        ];
    }

    // Export Chat Messages
    $messageQuery = "SELECT message_id, room_id, sender_id, message_text,
                     message_type, is_read, created_at
                     FROM chat_messages
                     ORDER BY created_at DESC
                     LIMIT 5000";
    $messageStmt = $db->prepare($messageQuery);
    $messageStmt->execute();

    while ($row = $messageStmt->fetch(PDO::FETCH_ASSOC)) {
        $messageId = $row['message_id'];
        $exportData['chat_messages'][$messageId] = [
            'message_id' => (int)$row['message_id'],
            'room_id' => (string)$row['room_id'],
            'sender_id' => (string)$row['sender_id'],
            'message_text' => $row['message_text'],
            'message_type' => $row['message_type'],
            'is_read' => (bool)$row['is_read'],
            'created_at' => strtotime($row['created_at']) * 1000
        ];
    }

    // Get statistics
    $stats = [
        'total_users' => count($exportData['users']),
        'total_posts' => count($exportData['posts']),
        'total_notifications' => count($exportData['notifications']),
        'total_chat_rooms' => count($exportData['chat_rooms']),
        'total_chat_messages' => count($exportData['chat_messages']),
        'export_date' => date('Y-m-d H:i:s')
    ];

    // Save to file
    $filename = '../exports/firebase_export_' . date('Y-m-d_His') . '.json';
    $exportDir = '../exports';

    if (!file_exists($exportDir)) {
        mkdir($exportDir, 0777, true);
    }

    file_put_contents($filename, json_encode($exportData, JSON_PRETTY_PRINT));

    echo json_encode([
        'success' => true,
        'message' => 'Data exported successfully',
        'stats' => $stats,
        'filename' => $filename,
        'data' => $exportData
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Export failed: ' . $e->getMessage()
    ]);
}
?>

