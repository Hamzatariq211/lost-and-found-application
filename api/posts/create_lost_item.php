<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once '../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

$database = new Database();
$conn = $database->getConnection();

// Get POST data
$user_id = $_POST['user_id'] ?? 0;
$item_name = $_POST['item_name'] ?? '';
$item_description = $_POST['item_description'] ?? '';
$location_lost = $_POST['location_lost'] ?? '';

// Validation
if ($user_id == 0) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit;
}

if (empty($item_name) || empty($item_description) || empty($location_lost)) {
    echo json_encode(['success' => false, 'message' => 'All fields are required']);
    exit;
}

try {
    $sql = "INSERT INTO lost_items (user_id, item_name, item_description, location_lost)
            VALUES (:user_id, :item_name, :item_description, :location_lost)";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);
    $stmt->bindParam(':item_name', $item_name);
    $stmt->bindParam(':item_description', $item_description);
    $stmt->bindParam(':location_lost', $location_lost);

    if ($stmt->execute()) {
        $lost_item_id = $conn->lastInsertId();

        http_response_code(201);
        echo json_encode([
            'success' => true,
            'message' => 'Lost item reported successfully',
            'lost_item_id' => $lost_item_id
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to create lost item report']);
    }

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>

