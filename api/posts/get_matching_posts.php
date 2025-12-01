<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once '../config/database.php';

$database = new Database();
$conn = $database->getConnection();

$lost_item_id = $_GET['lost_item_id'] ?? 0;
$item_name = $_GET['item_name'] ?? '';
$location = $_GET['location'] ?? '';

if ($lost_item_id == 0 && empty($item_name)) {
    echo json_encode(['success' => false, 'message' => 'Lost item ID or item name is required']);
    exit;
}

try {
    // Get lost item details if ID provided
    if ($lost_item_id > 0) {
        $sql = "SELECT item_name, location_lost FROM lost_items WHERE lost_item_id = :lost_item_id";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':lost_item_id', $lost_item_id, PDO::PARAM_INT);
        $stmt->execute();
        $lost_item = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($lost_item) {
            $item_name = $lost_item['item_name'];
            $location = $lost_item['location_lost'];
        }
    }

    // Find matching found posts
    // Match based on item name and location similarity
    $sql = "SELECT p.*, u.username, u.full_name, u.mobile_number,
            (
                CASE
                    WHEN LOWER(p.item_name) = LOWER(:item_name) THEN 50
                    WHEN LOWER(p.item_name) LIKE CONCAT('%', LOWER(:item_name), '%') THEN 30
                    WHEN LOWER(:item_name) LIKE CONCAT('%', LOWER(p.item_name), '%') THEN 30
                    ELSE 0
                END +
                CASE
                    WHEN LOWER(p.location) = LOWER(:location) THEN 30
                    WHEN LOWER(p.location) LIKE CONCAT('%', LOWER(:location), '%') THEN 15
                    WHEN LOWER(:location) LIKE CONCAT('%', LOWER(p.location), '%') THEN 15
                    ELSE 0
                END
            ) AS match_score
            FROM posts p
            INNER JOIN users u ON p.user_id = u.user_id
            WHERE p.item_type = 'found' AND p.status = 'active'
            HAVING match_score > 0
            ORDER BY match_score DESC, p.created_at DESC
            LIMIT 20";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':item_name', $item_name);
    $stmt->bindParam(':location', $location);
    $stmt->execute();

    $matching_posts = [];
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $matching_posts[] = [
            "post_id" => $row['post_id'],
            "item_name" => $row['item_name'],
            "item_description" => $row['item_description'],
            "location" => $row['location'],
            "item_type" => $row['item_type'],
            "image_base64" => $row['item_image'],
            "status" => $row['status'],
            "created_at" => $row['created_at'],
            "match_score" => $row['match_score'],
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
        'success' => true,
        'count' => count($matching_posts),
        'search_params' => [
            'item_name' => $item_name,
            'location' => $location
        ],
        'data' => $matching_posts
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>

