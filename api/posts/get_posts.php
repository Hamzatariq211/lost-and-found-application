<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get query parameters
$item_type = isset($_GET['item_type']) ? $_GET['item_type'] : null;
$search = isset($_GET['search']) ? $_GET['search'] : null;
$limit = isset($_GET['limit']) ? intval($_GET['limit']) : 50;
$offset = isset($_GET['offset']) ? intval($_GET['offset']) : 0;

// Build query
$query = "SELECT p.*, u.username, u.full_name, u.mobile_number 
          FROM posts p 
          INNER JOIN users u ON p.user_id = u.user_id 
          WHERE p.status = 'active'";

if ($item_type && in_array($item_type, ['lost', 'found'])) {
    $query .= " AND p.item_type = :item_type";
}

if ($search) {
    $query .= " AND (p.item_name LIKE :search OR p.item_description LIKE :search OR p.location LIKE :search)";
}

$query .= " ORDER BY p.created_at DESC LIMIT :limit OFFSET :offset";

$stmt = $db->prepare($query);

if ($item_type && in_array($item_type, ['lost', 'found'])) {
    $stmt->bindParam(":item_type", $item_type);
}

if ($search) {
    $search_param = "%{$search}%";
    $stmt->bindParam(":search", $search_param);
}

$stmt->bindParam(":limit", $limit, PDO::PARAM_INT);
$stmt->bindParam(":offset", $offset, PDO::PARAM_INT);

$stmt->execute();

$posts = [];
while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
    $posts[] = [
        "post_id" => $row['post_id'],
        "item_name" => $row['item_name'],
        "item_description" => $row['item_description'],
        "location" => $row['location'],
        "item_type" => $row['item_type'],
        "image_base64" => $row['item_image'],  // Return base64 directly
        "status" => $row['status'],
        "created_at" => $row['created_at'],
        "user" => [
            "user_id" => $row['user_id'],
            "username" => $row['username'],
            "full_name" => $row['full_name'],
            "mobile_number" => $row['mobile_number']
        ]
    ];
}

http_response_code(200);
echo json_encode([
    "success" => true,
    "count" => count($posts),
    "data" => $posts
]);
?>
