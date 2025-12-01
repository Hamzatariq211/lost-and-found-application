<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get post ID from URL
$post_id = isset($_GET['post_id']) ? intval($_GET['post_id']) : 0;

if ($post_id <= 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Invalid post ID"
    ]);
    exit();
}

// Get post details
$query = "SELECT p.*, u.username, u.full_name, u.email, u.mobile_number 
          FROM posts p 
          INNER JOIN users u ON p.user_id = u.user_id 
          WHERE p.post_id = :post_id AND p.status = 'active'";

$stmt = $db->prepare($query);
$stmt->bindParam(":post_id", $post_id);
$stmt->execute();

if ($stmt->rowCount() > 0) {
    $row = $stmt->fetch(PDO::FETCH_ASSOC);
    
    http_response_code(200);
    echo json_encode([
        "success" => true,
        "data" => [
            "post_id" => $row['post_id'],
            "item_name" => $row['item_name'],
            "item_description" => $row['item_description'],
            "location" => $row['location'],
            "item_type" => $row['item_type'],
            "image_url" => $row['item_image'] ? "http://localhost/lost_and_found_api/uploads/" . $row['item_image'] : null,
            "status" => $row['status'],
            "created_at" => $row['created_at'],
            "updated_at" => $row['updated_at'],
            "user" => [
                "username" => $row['username'],
                "full_name" => $row['full_name'],
                "email" => $row['email'],
                "mobile_number" => $row['mobile_number']
            ]
        ]
    ]);
} else {
    http_response_code(404);
    echo json_encode([
        "success" => false,
        "message" => "Post not found"
    ]);
}
?>
