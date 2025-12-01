<?php
/**
 * Get all users - for Firebase migration
 */
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

try {
    $query = "SELECT user_id, full_name, username, email, mobile_number, fcm_token,
              is_active, created_at, updated_at
              FROM users
              WHERE is_active = 1
              ORDER BY created_at DESC";

    $stmt = $db->prepare($query);
    $stmt->execute();

    $users = [];
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $users[] = [
            'user_id' => (int)$row['user_id'],
            'full_name' => $row['full_name'],
            'username' => $row['username'],
            'email' => $row['email'],
            'mobile_number' => $row['mobile_number'] ?? '',
            'fcm_token' => $row['fcm_token'] ?? '',
            'is_active' => (bool)$row['is_active'],
            'created_at' => $row['created_at'],
            'updated_at' => $row['updated_at']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'count' => count($users),
        'data' => $users
    ]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error fetching users: ' . $e->getMessage()
    ]);
}
?>

