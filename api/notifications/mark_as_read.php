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

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if (!empty($data->notification_id)) {
    $query = "UPDATE notifications 
              SET is_read = TRUE 
              WHERE notification_id = :notification_id AND user_id = :user_id";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(":notification_id", $data->notification_id);
    $stmt->bindParam(":user_id", $user_id);
    
    if ($stmt->execute()) {
        http_response_code(200);
        echo json_encode([
            "success" => true,
            "message" => "Notification marked as read"
        ]);
    } else {
        http_response_code(500);
        echo json_encode([
            "success" => false,
            "message" => "Failed to update notification"
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Notification ID required"
    ]);
}
?>
