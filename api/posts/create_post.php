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

// Handle multipart form data (for image upload)
$item_name = $_POST['item_name'] ?? null;
$item_description = $_POST['item_description'] ?? null;
$location = $_POST['location'] ?? null;
$item_type = $_POST['item_type'] ?? null;

// Validate required fields
if (!$item_name || !$item_description || !$location || !$item_type) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "All fields are required"
    ]);
    exit();
}

// Handle image upload
$image_name = null;
if (isset($_FILES['item_image']) && $_FILES['item_image']['error'] == 0) {
    $allowed_types = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    $file_type = $_FILES['item_image']['type'];
    
    if (in_array($file_type, $allowed_types)) {
        $upload_dir = '../uploads/';
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }
        
        $file_extension = pathinfo($_FILES['item_image']['name'], PATHINFO_EXTENSION);
        $image_name = uniqid() . '_' . time() . '.' . $file_extension;
        $upload_path = $upload_dir . $image_name;
        
        if (!move_uploaded_file($_FILES['item_image']['tmp_name'], $upload_path)) {
            http_response_code(500);
            echo json_encode([
                "success" => false,
                "message" => "Failed to upload image"
            ]);
            exit();
        }
    } else {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Invalid image format. Only JPG, PNG, and GIF allowed"
        ]);
        exit();
    }
}

// Insert post
$query = "INSERT INTO posts (user_id, item_name, item_description, location, item_type, item_image, sync_status) 
          VALUES (:user_id, :item_name, :item_description, :location, :item_type, :item_image, TRUE)";

$stmt = $db->prepare($query);
$stmt->bindParam(":user_id", $user_id);
$stmt->bindParam(":item_name", $item_name);
$stmt->bindParam(":item_description", $item_description);
$stmt->bindParam(":location", $location);
$stmt->bindParam(":item_type", $item_type);
$stmt->bindParam(":item_image", $image_name);

if ($stmt->execute()) {
    $post_id = $db->lastInsertId();
    
    http_response_code(201);
    echo json_encode([
        "success" => true,
        "message" => "Post created successfully",
        "data" => [
            "post_id" => $post_id,
            "item_name" => $item_name,
            "item_type" => $item_type,
            "location" => $location,
            "image_url" => $image_name ? "http://localhost/lost_and_found_api/uploads/" . $image_name : null
        ]
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Failed to create post"
    ]);
}
?>
