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

// Get posted JSON data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (
    !isset($data->item_name) || empty($data->item_name) ||
    !isset($data->item_description) || empty($data->item_description) ||
    !isset($data->location) || empty($data->location) ||
    !isset($data->item_type) || empty($data->item_type)
) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "All fields are required (item_name, item_description, location, item_type)"
    ]);
    exit();
}

// Validate item_type
if (!in_array($data->item_type, ['lost', 'found'])) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Invalid item_type. Must be 'lost' or 'found'"
    ]);
    exit();
}

// Handle base64 image - store directly in database
$image_base64 = null;
if (isset($data->item_image_base64) && !empty($data->item_image_base64)) {
    // Store the base64 string directly (keep the data URI format)
    $image_base64 = $data->item_image_base64;

    // Validate it's a valid base64 image
    if (!preg_match('/^data:image\/(jpeg|jpg|png|gif);base64,/', $image_base64)) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Invalid image format. Must be base64 encoded image"
        ]);
        exit();
    }
}

// Insert post with base64 image stored directly in database
$query = "INSERT INTO posts (user_id, item_name, item_description, location, item_type, item_image, sync_status)
          VALUES (:user_id, :item_name, :item_description, :location, :item_type, :item_image, TRUE)";

$stmt = $db->prepare($query);
$stmt->bindParam(":user_id", $user_id);
$stmt->bindParam(":item_name", $data->item_name);
$stmt->bindParam(":item_description", $data->item_description);
$stmt->bindParam(":location", $data->location);
$stmt->bindParam(":item_type", $data->item_type);
$stmt->bindParam(":item_image", $image_base64);

if ($stmt->execute()) {
    $post_id = $db->lastInsertId();

    http_response_code(201);
    echo json_encode([
        "success" => true,
        "message" => "Post created successfully",
        "data" => [
            "post_id" => $post_id,
            "item_name" => $data->item_name,
            "item_type" => $data->item_type,
            "location" => $data->location,
            "has_image" => $image_base64 !== null
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
