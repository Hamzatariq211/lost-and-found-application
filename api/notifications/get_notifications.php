<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get authorization token
$headers = getallheaders();
$token = isset($headers['Authorization']) ? str_replace('Bearer ', '', $headers['Authorization']) : null;

if (!$token) {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Authorization token required"
    ]);
    exit();
}

// Verify token
$token_query = "SELECT user_id FROM user_sessions WHERE token = :token AND expires_at > NOW()";
$token_stmt = $db->prepare($token_query);
$token_stmt->bindParam(":token", $token);
$token_stmt->execute();

if ($token_stmt->rowCount() == 0) {
    http_response_code(401);
    echo json_encode([
        "success" => false,
        "message" => "Invalid or expired token"
    ]);
    exit();
}

$user_data = $token_stmt->fetch(PDO::FETCH_ASSOC);
$user_id = $user_data['user_id'];

// Get notifications
$query = "SELECT n.*, p.item_name, p.item_type 
          FROM notifications n 
          INNER JOIN posts p ON n.post_id = p.post_id 
          WHERE n.user_id = :user_id 
          ORDER BY n.created_at DESC 
          LIMIT 50";

$stmt = $db->prepare($query);
$stmt->bindParam(":user_id", $user_id);
$stmt->execute();

$notifications = [];
while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $notifications[] = [
        "notification_id" => $row['notification_id'],
        "post_id" => $row['post_id'],
        "notification_type" => $row['notification_type'],
        "title" => $row['title'],
        "message" => $row['message'],
        "is_read" => (bool)$row['is_read'],
        "created_at" => $row['created_at'],
        "post" => [
            "item_name" => $row['item_name'],
            "item_type" => $row['item_type']
        ]
    ];
}

http_response_code(200);
echo json_encode([
    "success" => true,
    "count" => count($notifications),
    "data" => $notifications
]);
?>
