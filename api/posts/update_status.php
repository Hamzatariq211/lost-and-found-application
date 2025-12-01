<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once '../config/database.php';

$database = new Database();
$conn = $database->getConnection();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Only POST method allowed']);
    exit;
}

$post_id = $_POST['post_id'] ?? 0;
$user_id = $_POST['user_id'] ?? 0;
$status = $_POST['status'] ?? '';

// Validate inputs
if ($post_id == 0) {
    echo json_encode(['success' => false, 'message' => 'Post ID is required']);
    exit;
}

if ($user_id == 0) {
    echo json_encode(['success' => false, 'message' => 'User ID is required']);
    exit;
}

if (!in_array($status, ['active', 'resolved', 'deleted'])) {
    echo json_encode(['success' => false, 'message' => 'Invalid status value']);
    exit;
}

try {
    // Check if the post belongs to the user
    $check_sql = "SELECT user_id FROM posts WHERE post_id = :post_id";
    $check_stmt = $conn->prepare($check_sql);
    $check_stmt->bindParam(':post_id', $post_id, PDO::PARAM_INT);
    $check_stmt->execute();

    $post = $check_stmt->fetch(PDO::FETCH_ASSOC);

    if (!$post) {
        echo json_encode(['success' => false, 'message' => 'Post not found']);
        exit;
    }

    if ($post['user_id'] != $user_id) {
        echo json_encode(['success' => false, 'message' => 'You can only update your own posts']);
        exit;
    }

    // Update the post status
    $update_sql = "UPDATE posts SET status = :status, updated_at = CURRENT_TIMESTAMP WHERE post_id = :post_id";
    $update_stmt = $conn->prepare($update_sql);
    $update_stmt->bindParam(':status', $status, PDO::PARAM_STR);
    $update_stmt->bindParam(':post_id', $post_id, PDO::PARAM_INT);

    if ($update_stmt->execute()) {
        $status_message = $status == 'resolved' ? 'Item marked as returned to owner' :
                         ($status == 'deleted' ? 'Post deleted' : 'Post reactivated');

        echo json_encode([
            'success' => true,
            'message' => $status_message,
            'post_id' => $post_id,
            'status' => $status
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to update post status']);
    }

} catch (PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
}
?>

