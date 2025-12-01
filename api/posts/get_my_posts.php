<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once '../config/database.php';

$database = new Database();
$conn = $database->getConnection();

$user_id = $_GET['user_id'] ?? 0;

if ($user_id == 0) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit;
}

try {
    $sql = "SELECT p.*, u.username, u.full_name, u.mobile_number
            FROM posts p
            INNER JOIN users u ON p.user_id = u.user_id
            WHERE p.user_id = :user_id
            ORDER BY p.created_at DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $stmt->execute();

    $posts = [];
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $posts[] = [
            "post_id" => $row['post_id'],
            "item_name" => $row['item_name'],
            "item_description" => $row['item_description'],
            "location" => $row['location'],
            "item_type" => $row['item_type'],
            "image_base64" => $row['item_image'],
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

} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>

