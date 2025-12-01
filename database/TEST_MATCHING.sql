-- Test the Lost Items Matching Feature
-- Run this script to create test data and verify matching works

USE lost_and_found_db;

-- First, let's check if the lost_items table exists
SHOW TABLES LIKE 'lost_items';

-- Create the table if it doesn't exist
CREATE TABLE IF NOT EXISTS lost_items (
    lost_item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    item_description TEXT NOT NULL,
    location_lost VARCHAR(200) NOT NULL,
    status ENUM('active', 'found', 'cancelled') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_item_name (item_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Clear existing test data
DELETE FROM lost_items WHERE user_id = 1;
DELETE FROM posts WHERE user_id = 1 AND item_type = 'found';

-- Insert a LOST item (what user reports as lost)
INSERT INTO lost_items (user_id, item_name, item_description, location_lost, status) VALUES
(1, 'Watch', 'Silver watch with black leather strap', 'Cafe', 'active');

-- Insert matching FOUND posts (what others have found)
-- This should match with different cases
INSERT INTO posts (user_id, item_name, item_description, location, item_type, status, created_at) VALUES
(2, 'WATCH', 'A silver wristwatch found on a table', 'CAFE', 'found', 'active', NOW()),
(2, 'watch', 'Black strap watch', 'cafe', 'found', 'active', NOW()),
(2, 'Wrist Watch', 'Silver colored watch', 'Cafeteria', 'found', 'active', NOW()),
(2, 'Watch', 'Found this watch near the counter', 'C110', 'found', 'active', NOW());

-- Test the matching query (same as the API uses)
SELECT
    p.item_name,
    p.location,
    (
        CASE
            WHEN LOWER(p.item_name) = LOWER('Watch') THEN 50
            WHEN LOWER(p.item_name) LIKE CONCAT('%', LOWER('Watch'), '%') THEN 30
            WHEN LOWER('Watch') LIKE CONCAT('%', LOWER(p.item_name), '%') THEN 30
            ELSE 0
        END +
        CASE
            WHEN LOWER(p.location) = LOWER('Cafe') THEN 30
            WHEN LOWER(p.location) LIKE CONCAT('%', LOWER('Cafe'), '%') THEN 15
            WHEN LOWER('Cafe') LIKE CONCAT('%', LOWER(p.location), '%') THEN 15
            ELSE 0
        END
    ) AS match_score
FROM posts p
WHERE p.item_type = 'found' AND p.status = 'active'
HAVING match_score > 0
ORDER BY match_score DESC;

-- Expected Results:
-- 'WATCH' in 'CAFE' -> Score: 80 (50 + 30)
-- 'watch' in 'cafe' -> Score: 80 (50 + 30)
-- 'Wrist Watch' in 'Cafeteria' -> Score: 45 (30 + 15)
-- 'Watch' in 'C110' -> Score: 50 (50 + 0)

