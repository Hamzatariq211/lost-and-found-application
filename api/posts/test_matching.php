<?php
// Test script to verify the matching API works correctly
// Access this at: http://localhost/lost_and_found_api/posts/test_matching.php

header('Content-Type: application/json');
require_once '../config/database.php';

$database = new Database();
$conn = $database->getConnection();

echo "<h2>Lost Items Matching Test</h2>";
echo "<pre>";

// Test 1: Check if lost_items table exists
echo "\n1. Checking if lost_items table exists...\n";
try {
    $stmt = $conn->query("SHOW TABLES LIKE 'lost_items'");
    $result = $stmt->fetch();
    if ($result) {
        echo "✅ lost_items table exists\n";
    } else {
        echo "❌ lost_items table does NOT exist - Run LOST_ITEMS_FEATURE.sql first!\n";
    }
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

// Test 2: Check for lost items
echo "\n2. Checking for lost items in database...\n";
try {
    $stmt = $conn->query("SELECT * FROM lost_items WHERE status = 'active' LIMIT 5");
    $lost_items = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "Found " . count($lost_items) . " active lost items:\n";
    foreach ($lost_items as $item) {
        echo "  - ID: {$item['lost_item_id']}, Name: {$item['item_name']}, Location: {$item['location_lost']}\n";
    }
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

// Test 3: Check for found posts
echo "\n3. Checking for found posts in database...\n";
try {
    $stmt = $conn->query("SELECT * FROM posts WHERE item_type = 'found' AND status = 'active' LIMIT 5");
    $found_posts = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "Found " . count($found_posts) . " active found posts:\n";
    foreach ($found_posts as $post) {
        echo "  - ID: {$post['post_id']}, Name: {$post['item_name']}, Location: {$post['location']}\n";
    }
} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

// Test 4: Test case-insensitive matching
echo "\n4. Testing case-insensitive matching...\n";
$test_name = 'Watch';
$test_location = 'Cafe';

try {
    $sql = "SELECT p.item_name, p.location,
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
            WHERE p.item_type = 'found' AND p.status = 'active'
            HAVING match_score > 0
            ORDER BY match_score DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':item_name', $test_name);
    $stmt->bindParam(':location', $test_location);
    $stmt->execute();

    $matches = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo "Testing with: Name='$test_name', Location='$test_location'\n";
    echo "Found " . count($matches) . " matching posts:\n";

    foreach ($matches as $match) {
        echo "  - Name: '{$match['item_name']}' | Location: '{$match['location']}' | Score: {$match['match_score']}\n";
    }

    if (count($matches) == 0) {
        echo "\n❌ NO MATCHES FOUND!\n";
        echo "Possible issues:\n";
        echo "  1. No 'found' posts exist in database\n";
        echo "  2. All found posts have different names/locations\n";
        echo "  3. Create test data by running TEST_MATCHING.sql\n";
    } else {
        echo "\n✅ Matching is working correctly!\n";
    }

} catch (Exception $e) {
    echo "❌ Error: " . $e->getMessage() . "\n";
}

echo "\n</pre>";

// Also output as JSON for API testing
$response = [
    'test' => 'Lost Items Matching',
    'lost_items_table_exists' => isset($result) && $result ? true : false,
    'lost_items_count' => isset($lost_items) ? count($lost_items) : 0,
    'found_posts_count' => isset($found_posts) ? count($found_posts) : 0,
    'test_matches' => isset($matches) ? count($matches) : 0,
    'matches' => isset($matches) ? $matches : []
];

echo "\n<h3>JSON Response:</h3>";
echo "<pre>" . json_encode($response, JSON_PRETTY_PRINT) . "</pre>";
?>

