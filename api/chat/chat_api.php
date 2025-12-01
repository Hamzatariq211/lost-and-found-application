<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once '../config/database.php';

// Initialize database connection
$database = new Database();
$conn = $database->getConnection();

$action = $_GET['action'] ?? $_POST['action'] ?? '';

switch($action) {
    case 'create_room':
        createChatRoom($conn);
        break;
    case 'get_rooms':
        getChatRooms($conn);
        break;
    case 'send_message':
        sendMessage($conn);
        break;
    case 'get_messages':
        getMessages($conn);
        break;
    case 'mark_read':
        markMessagesAsRead($conn);
        break;
    case 'get_unread_count':
        getUnreadCount($conn);
        break;
    default:
        echo json_encode(['success' => false, 'message' => 'Invalid action']);
}

function createChatRoom($conn) {
    $post_id = $_POST['post_id'] ?? 0;
    $sender_id = $_POST['sender_id'] ?? 0;
    $receiver_id = $_POST['receiver_id'] ?? 0;

    if ($post_id == 0 || $sender_id == 0 || $receiver_id == 0) {
        echo json_encode(['success' => false, 'message' => 'Missing required parameters']);
        return;
    }

    try {
        // Check if room already exists (PDO syntax)
        $check_sql = "SELECT room_id FROM chat_rooms WHERE post_id = :post_id AND sender_id = :sender_id AND receiver_id = :receiver_id";
        $check_stmt = $conn->prepare($check_sql);
        $check_stmt->bindParam(':post_id', $post_id, PDO::PARAM_INT);
        $check_stmt->bindParam(':sender_id', $sender_id, PDO::PARAM_INT);
        $check_stmt->bindParam(':receiver_id', $receiver_id, PDO::PARAM_INT);
        $check_stmt->execute();

        $existing_room = $check_stmt->fetch(PDO::FETCH_ASSOC);

        if ($existing_room) {
            echo json_encode(['success' => true, 'room_id' => $existing_room['room_id'], 'existing' => true]);
            return;
        }

        // Create new room (PDO syntax)
        $sql = "INSERT INTO chat_rooms (post_id, sender_id, receiver_id) VALUES (:post_id, :sender_id, :receiver_id)";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':post_id', $post_id, PDO::PARAM_INT);
        $stmt->bindParam(':sender_id', $sender_id, PDO::PARAM_INT);
        $stmt->bindParam(':receiver_id', $receiver_id, PDO::PARAM_INT);

        if ($stmt->execute()) {
            echo json_encode(['success' => true, 'room_id' => $conn->lastInsertId(), 'existing' => false]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Failed to create chat room']);
        }
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function getChatRooms($conn) {
    $user_id = $_GET['user_id'] ?? 0;

    if ($user_id == 0) {
        echo json_encode(['success' => false, 'message' => 'User ID required']);
        return;
    }

    try {
        $sql = "SELECT cr.*, p.item_name, p.item_image,
                CASE WHEN cr.sender_id = :user_id1 THEN u2.full_name ELSE u1.full_name END as other_user_name,
                CASE WHEN cr.sender_id = :user_id2 THEN u2.user_id ELSE u1.user_id END as other_user_id,
                (SELECT message_text FROM chat_messages WHERE room_id = cr.room_id ORDER BY created_at DESC LIMIT 1) as last_message,
                (SELECT created_at FROM chat_messages WHERE room_id = cr.room_id ORDER BY created_at DESC LIMIT 1) as last_message_time,
                (SELECT COUNT(*) FROM chat_messages WHERE room_id = cr.room_id AND sender_id != :user_id3 AND is_read = 0) as unread_count
                FROM chat_rooms cr
                JOIN posts p ON cr.post_id = p.post_id
                JOIN users u1 ON cr.sender_id = u1.user_id
                JOIN users u2 ON cr.receiver_id = u2.user_id
                WHERE (cr.sender_id = :user_id4 OR cr.receiver_id = :user_id5) AND cr.is_active = 1
                ORDER BY cr.updated_at DESC";

        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':user_id1', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id2', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id3', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id4', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id5', $user_id, PDO::PARAM_INT);
        $stmt->execute();

        $rooms = $stmt->fetchAll(PDO::FETCH_ASSOC);

        echo json_encode(['success' => true, 'rooms' => $rooms]);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function sendMessage($conn) {
    $room_id = $_POST['room_id'] ?? 0;
    $sender_id = $_POST['sender_id'] ?? 0;
    $message_text = $_POST['message_text'] ?? '';

    if ($room_id == 0 || $sender_id == 0 || empty($message_text)) {
        echo json_encode(['success' => false, 'message' => 'Missing required parameters']);
        return;
    }

    try {
        $sql = "INSERT INTO chat_messages (room_id, sender_id, message_text) VALUES (:room_id, :sender_id, :message_text)";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':room_id', $room_id, PDO::PARAM_INT);
        $stmt->bindParam(':sender_id', $sender_id, PDO::PARAM_INT);
        $stmt->bindParam(':message_text', $message_text, PDO::PARAM_STR);

        if ($stmt->execute()) {
            // Update chat room updated_at
            $update_sql = "UPDATE chat_rooms SET updated_at = CURRENT_TIMESTAMP WHERE room_id = :room_id";
            $update_stmt = $conn->prepare($update_sql);
            $update_stmt->bindParam(':room_id', $room_id, PDO::PARAM_INT);
            $update_stmt->execute();

            echo json_encode(['success' => true, 'message_id' => $conn->lastInsertId()]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Failed to send message']);
        }
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function getMessages($conn) {
    $room_id = $_GET['room_id'] ?? 0;

    if ($room_id == 0) {
        echo json_encode(['success' => false, 'message' => 'Room ID required']);
        return;
    }

    try {
        $sql = "SELECT cm.*, u.full_name as sender_name
                FROM chat_messages cm
                JOIN users u ON cm.sender_id = u.user_id
                WHERE cm.room_id = :room_id
                ORDER BY cm.created_at ASC";

        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':room_id', $room_id, PDO::PARAM_INT);
        $stmt->execute();

        $messages = $stmt->fetchAll(PDO::FETCH_ASSOC);

        echo json_encode(['success' => true, 'messages' => $messages]);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function markMessagesAsRead($conn) {
    $room_id = $_POST['room_id'] ?? 0;
    $user_id = $_POST['user_id'] ?? 0;

    if ($room_id == 0 || $user_id == 0) {
        echo json_encode(['success' => false, 'message' => 'Missing required parameters']);
        return;
    }

    try {
        $sql = "UPDATE chat_messages SET is_read = 1 WHERE room_id = :room_id AND sender_id != :user_id";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':room_id', $room_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id', $user_id, PDO::PARAM_INT);

        if ($stmt->execute()) {
            echo json_encode(['success' => true]);
        } else {
            echo json_encode(['success' => false, 'message' => 'Failed to mark messages as read']);
        }
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function getUnreadCount($conn) {
    $user_id = $_GET['user_id'] ?? 0;

    if ($user_id == 0) {
        echo json_encode(['success' => false, 'message' => 'User ID required']);
        return;
    }

    try {
        $sql = "SELECT COUNT(*) as unread_count
                FROM chat_messages cm
                JOIN chat_rooms cr ON cm.room_id = cr.room_id
                WHERE (cr.sender_id = :user_id1 OR cr.receiver_id = :user_id2)
                AND cm.sender_id != :user_id3
                AND cm.is_read = 0";

        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':user_id1', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id2', $user_id, PDO::PARAM_INT);
        $stmt->bindParam(':user_id3', $user_id, PDO::PARAM_INT);
        $stmt->execute();

        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        echo json_encode(['success' => true, 'unread_count' => $row['unread_count']]);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'message' => 'Database error: ' . $e->getMessage()]);
    }
}
?>
