<?php
// Firebase Cloud Messaging - Send Push Notification
// This script sends notifications when:
// 1. A new chat message is received
// 2. A matching found item is posted for a lost item

require_once '../config/database.php';

class FCMNotificationService {
    private $serverKey;
    private $fcmEndpoint = 'https://fcm.googleapis.com/fcm/send';

    public function __construct() {
        // TODO: Replace with your Firebase Server Key from Firebase Console
        // Go to: Firebase Console > Project Settings > Cloud Messaging > Server Key
        $this->serverKey = 'YOUR_FIREBASE_SERVER_KEY_HERE';
    }

    /**
     * Send notification to a single user
     */
    public function sendToUser($userId, $title, $body, $data = []) {
        $database = new Database();
        $conn = $database->getConnection();

        // Get user's FCM token
        $sql = "SELECT fcm_token FROM users WHERE user_id = :user_id AND fcm_token IS NOT NULL";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':user_id', $userId, PDO::PARAM_INT);
        $stmt->execute();

        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if (!$user || empty($user['fcm_token'])) {
            return ['success' => false, 'message' => 'User has no FCM token'];
        }

        return $this->sendNotification($user['fcm_token'], $title, $body, $data);
    }

    /**
     * Send notification to multiple users
     */
    public function sendToMultipleUsers($userIds, $title, $body, $data = []) {
        $database = new Database();
        $conn = $database->getConnection();

        $placeholders = str_repeat('?,', count($userIds) - 1) . '?';
        $sql = "SELECT fcm_token FROM users WHERE user_id IN ($placeholders) AND fcm_token IS NOT NULL";
        $stmt = $conn->prepare($sql);
        $stmt->execute($userIds);

        $tokens = $stmt->fetchAll(PDO::FETCH_COLUMN);

        if (empty($tokens)) {
            return ['success' => false, 'message' => 'No users have FCM tokens'];
        }

        return $this->sendNotificationToTokens($tokens, $title, $body, $data);
    }

    /**
     * Send notification to specific FCM token(s)
     */
    private function sendNotification($token, $title, $body, $data = []) {
        return $this->sendNotificationToTokens([$token], $title, $body, $data);
    }

    private function sendNotificationToTokens($tokens, $title, $body, $data = []) {
        $notification = [
            'title' => $title,
            'body' => $body,
            'sound' => 'default',
            'badge' => '1'
        ];

        $payload = [
            'registration_ids' => $tokens,
            'notification' => $notification,
            'data' => $data,
            'priority' => 'high'
        ];

        $headers = [
            'Authorization: key=' . $this->serverKey,
            'Content-Type: application/json'
        ];

        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL, $this->fcmEndpoint);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));

        $result = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        $response = json_decode($result, true);

        return [
            'success' => $httpCode == 200,
            'response' => $response,
            'http_code' => $httpCode
        ];
    }

    /**
     * Send notification for new chat message
     */
    public function sendChatNotification($receiverId, $senderName, $messageText, $chatId) {
        $title = "New message from $senderName";
        $body = $messageText;

        $data = [
            'type' => 'chat_message',
            'chat_id' => (string)$chatId,
            'sender_name' => $senderName,
            'click_action' => 'OPEN_CHAT'
        ];

        return $this->sendToUser($receiverId, $title, $body, $data);
    }

    /**
     * Send notification for matching found item
     */
    public function sendMatchNotification($userId, $itemName, $location, $lostItemId, $postId) {
        $title = "🎉 Possible match found!";
        $body = "Someone found a $itemName in $location. Check if it's yours!";

        $data = [
            'type' => 'item_match',
            'lost_item_id' => (string)$lostItemId,
            'post_id' => (string)$postId,
            'click_action' => 'OPEN_MATCHES'
        ];

        return $this->sendToUser($userId, $title, $body, $data);
    }
}

// Example usage (for testing):
if (isset($_GET['test'])) {
    $fcm = new FCMNotificationService();

    // Test sending notification to user ID 1
    $result = $fcm->sendToUser(1, "Test Notification", "This is a test message from Lost & Found", [
        'type' => 'test',
        'timestamp' => time()
    ]);

    header('Content-Type: application/json');
    echo json_encode($result, JSON_PRETTY_PRINT);
}
?>

