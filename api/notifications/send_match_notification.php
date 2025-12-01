<?php
// Send notification when a matching found item is posted
// This should be called after a new "found" post is created

header('Content-Type: application/json');
require_once '../config/database.php';
require_once 'fcm_service.php';

$database = new Database();
$conn = $database->getConnection();
$fcm = new FCMNotificationService();

// Get the newly created post details
$post_id = $_POST['post_id'] ?? 0;

if ($post_id == 0) {
    echo json_encode(['success' => false, 'message' => 'Post ID required']);
    exit;
}

try {
    // Get the found post details
    $sql = "SELECT item_name, location FROM posts WHERE post_id = :post_id AND item_type = 'found'";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':post_id', $post_id, PDO::PARAM_INT);
    $stmt->execute();
    $post = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$post) {
        echo json_encode(['success' => false, 'message' => 'Post not found']);
        exit;
    }

    // Find all active lost items that might match
    $sql = "SELECT DISTINCT li.user_id, li.lost_item_id, li.item_name, li.location_lost,
            (
                CASE
                    WHEN LOWER(li.item_name) = LOWER(:item_name) THEN 50
                    WHEN LOWER(li.item_name) LIKE CONCAT('%', LOWER(:item_name), '%') THEN 30
                    WHEN LOWER(:item_name) LIKE CONCAT('%', LOWER(li.item_name), '%') THEN 30
                    ELSE 0
                END +
                CASE
                    WHEN LOWER(li.location_lost) = LOWER(:location) THEN 30
                    WHEN LOWER(li.location_lost) LIKE CONCAT('%', LOWER(:location), '%') THEN 15
                    WHEN LOWER(:location) LIKE CONCAT('%', LOWER(li.location_lost), '%') THEN 15
                    ELSE 0
                END
            ) AS match_score
            FROM lost_items li
            WHERE li.status = 'active'
            HAVING match_score > 30
            ORDER BY match_score DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':item_name', $post['item_name']);
    $stmt->bindParam(':location', $post['location']);
    $stmt->execute();

    $matches = $stmt->fetchAll(PDO::FETCH_ASSOC);
    $notificationsSent = 0;

    foreach ($matches as $match) {
        // Send notification to each user who reported a matching lost item
        $result = $fcm->sendMatchNotification(
            $match['user_id'],
            $post['item_name'],
            $post['location'],
            $match['lost_item_id'],
            $post_id
        );

        if ($result['success']) {
            $notificationsSent++;
        }
    }

    echo json_encode([
        'success' => true,
        'message' => "Sent $notificationsSent notifications for matching lost items",
        'matches_found' => count($matches),
        'notifications_sent' => $notificationsSent
    ]);

} catch (Exception $e) {
    echo json_encode(['success' => false, 'message' => 'Error: ' . $e->getMessage()]);
}
?>

