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

$user_id = $_POST['user_id'] ?? 0;
$fcm_token = $_POST['fcm_token'] ?? '';

if ($user_id == 0 || empty($fcm_token)) {
    echo json_encode(['success' => false, 'message' => 'User ID and FCM token are required']);
    exit;
}

try {
    // Check if user exists and update FCM token
    $sql = "UPDATE users SET fcm_token = :fcm_token, updated_at = NOW() WHERE user_id = :user_id";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':fcm_token', $fcm_token);
    $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);

    if ($stmt->execute()) {
        echo json_encode([
            'success' => true,
            'message' => 'FCM token updated successfully'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to update FCM token']);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>

