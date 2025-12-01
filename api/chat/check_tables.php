<?php
header('Content-Type: application/json');
require_once '../config/database.php';

$database = new Database();
$conn = $database->getConnection();

if (!$conn) {
    echo json_encode([
        'success' => false,
        'message' => 'Database connection failed'
    ]);
    exit;
}

try {
    // Check if tables exist
    $tables_to_check = ['users', 'posts', 'chat_rooms', 'chat_messages'];
    $existing_tables = [];
    $missing_tables = [];

    foreach ($tables_to_check as $table) {
        $result = $conn->query("SHOW TABLES LIKE '$table'");
        if ($result->num_rows > 0) {
            // Get row count
            $count_result = $conn->query("SELECT COUNT(*) as count FROM $table");
            $count_row = $count_result->fetch_assoc();
            $existing_tables[$table] = $count_row['count'];
        } else {
            $missing_tables[] = $table;
        }
    }

    // Check if chat_rooms table has correct structure
    $structure_check = [];
    if (in_array('chat_rooms', array_keys($existing_tables))) {
        $structure = $conn->query("DESCRIBE chat_rooms");
        $columns = [];
        while ($row = $structure->fetch_assoc()) {
            $columns[] = $row['Field'];
        }
        $structure_check['chat_rooms_columns'] = $columns;
    }

    if (in_array('chat_messages', array_keys($existing_tables))) {
        $structure = $conn->query("DESCRIBE chat_messages");
        $columns = [];
        while ($row = $structure->fetch_assoc()) {
            $columns[] = $row['Field'];
        }
        $structure_check['chat_messages_columns'] = $columns;
    }

    echo json_encode([
        'success' => count($missing_tables) === 0,
        'existing_tables' => $existing_tables,
        'missing_tables' => $missing_tables,
        'structure' => $structure_check,
        'message' => count($missing_tables) === 0 ?
            'All tables exist' :
            'Missing tables: ' . implode(', ', $missing_tables)
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>

