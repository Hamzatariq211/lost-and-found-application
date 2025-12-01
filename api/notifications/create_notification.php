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

// Get posted data
$data = json_decode(file_get_contents("php://input"));

if (
    !empty($data->user_id) &&
    !empty($data->post_id) &&
    !empty($data->title) &&
    !empty($data->message)
) {
    $notification_type = $data->notification_type ?? 'match';
    
    $query = "INSERT INTO notifications (user_id, post_id, notification_type, title, message) 
              VALUES (:user_id, :post_id, :notification_type, :title, :message)";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(":user_id", $data->user_id);
    $stmt->bindParam(":post_id", $data->post_id);
    $stmt->bindParam(":notification_type", $notification_type);
    $stmt->bindParam(":title", $data->title);
    $stmt->bindParam(":message", $data->message);
    
    if ($stmt->execute()) {
        http_response_code(201);
        echo json_encode([
            "success" => true,
            "message" => "Notification created successfully",
            "notification_id" => $db->lastInsertId()
        ]);
    } else {
        http_response_code(500);
        echo json_encode([
            "success" => false,
            "message" => "Failed to create notification"
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields"
    ]);
}
?>
