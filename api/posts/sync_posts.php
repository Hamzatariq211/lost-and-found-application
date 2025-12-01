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

// Get posted data (offline posts to sync)
$data = json_decode(file_get_contents("php://input"));

if (!isset($data->posts) || !is_array($data->posts)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Invalid data format"
    ]);
    exit();
}

$synced_posts = [];
$failed_posts = [];

foreach ($data->posts as $post) {
    if (
        !empty($post->item_name) &&
        !empty($post->item_description) &&
        !empty($post->location) &&
        !empty($post->item_type)
    ) {
        $query = "INSERT INTO posts (user_id, item_name, item_description, location, item_type, sync_status) 
                  VALUES (:user_id, :item_name, :item_description, :location, :item_type, TRUE)";
        
        $stmt = $db->prepare($query);
        $stmt->bindParam(":user_id", $user_id);
        $stmt->bindParam(":item_name", $post->item_name);
        $stmt->bindParam(":item_description", $post->item_description);
        $stmt->bindParam(":location", $post->location);
        $stmt->bindParam(":item_type", $post->item_type);
        
        if ($stmt->execute()) {
            $synced_posts[] = [
                "local_id" => $post->local_id ?? null,
                "post_id" => $db->lastInsertId(),
                "item_name" => $post->item_name
            ];
        } else {
            $failed_posts[] = [
                "local_id" => $post->local_id ?? null,
                "item_name" => $post->item_name,
                "error" => "Database error"
            ];
        }
    } else {
        $failed_posts[] = [
            "local_id" => $post->local_id ?? null,
            "error" => "Missing required fields"
        ];
    }
}

http_response_code(200);
echo json_encode([
    "success" => true,
    "message" => "Sync completed",
    "synced_count" => count($synced_posts),
    "failed_count" => count($failed_posts),
    "synced_posts" => $synced_posts,
    "failed_posts" => $failed_posts
]);
?>
