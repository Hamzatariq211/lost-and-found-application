<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Check if user_id is provided as GET parameter
if (isset($_GET['user_id'])) {
    $user_id = $_GET['user_id'];
} else {
    // Fall back to token-based authentication
    $headers = getallheaders();
    $token = isset($headers['Authorization']) ? str_replace('Bearer ', '', $headers['Authorization']) : null;

    if (!$token) {
        http_response_code(401);
        echo json_encode([
            "success" => false,
            "message" => "User ID or authorization token required"
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
}

// Get user profile
$query = "SELECT user_id, full_name, username, email, mobile_number, created_at 
          FROM users 
          WHERE user_id = :user_id";

$stmt = $db->prepare($query);
$stmt->bindParam(":user_id", $user_id);
$stmt->execute();

if ($stmt->rowCount() > 0) {
    $row = $stmt->fetch(PDO::FETCH_ASSOC);
    
    // Get user's post count
    $post_query = "SELECT COUNT(*) as post_count FROM posts WHERE user_id = :user_id AND status = 'active'";
    $post_stmt = $db->prepare($post_query);
    $post_stmt->bindParam(":user_id", $user_id);
    $post_stmt->execute();
    $post_data = $post_stmt->fetch(PDO::FETCH_ASSOC);
    
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "data" => [
            "user_id" => $row['user_id'],
            "full_name" => $row['full_name'],
            "username" => $row['username'],
            "email" => $row['email'],
            "mobile_number" => $row['mobile_number'],
            "created_at" => $row['created_at'],
            "post_count" => $post_data['post_count']
        ]
    ]);
} else {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "User not found"
    ]);
}
?>
