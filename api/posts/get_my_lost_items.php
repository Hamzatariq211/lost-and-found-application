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
    $sql = "SELECT * FROM lost_items
            WHERE user_id = :user_id AND status = 'active'
            ORDER BY created_at DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $stmt->execute();

    $lost_items = [];
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $lost_items[] = [
            "lost_item_id" => $row['lost_item_id'],
            "item_name" => $row['item_name'],
            "item_description" => $row['item_description'],
            "location_lost" => $row['location_lost'],
            "status" => $row['status'],
            "created_at" => $row['created_at']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'count' => count($lost_items),
        'data' => $lost_items
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>

