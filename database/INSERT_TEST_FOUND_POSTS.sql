-- Create Test Data for Lost and Found Matching
USE lost_and_found_db;

-- Insert test FOUND posts that will match the lost "watch" in "C110"
-- Make sure user_id 2 exists (or change to a user that exists in your database)

-- First check what users exist
SELECT user_id, username, full_name FROM users LIMIT 5;

-- Insert matching FOUND posts with different cases to test matching
INSERT INTO posts (user_id, item_name, item_description, location, item_type, status, created_at) VALUES
-- Exact match (should get score 80)
(1, 'WATCH', 'Found a silver watch in the classroom', 'C110', 'found', 'active', NOW()),

-- Case variation exact match (should get score 80)
(1, 'watch', 'Black watch found on desk', 'c110', 'found', 'active', NOW()),

-- Partial location match (should get score 65)
(1, 'Watch', 'Silver wrist watch', 'C-110', 'found', 'active', NOW()),

-- Partial name match (should get score 60)
(1, 'Wrist Watch', 'Found near the door', 'C110', 'found', 'active', NOW()),

-- Different location but same item (should get score 50)
(1, 'Watch', 'Found in cafeteria', 'Cafe', 'found', 'active', NOW()),

-- Partial matches (should get score 30-45)
(1, 'Smart Watch', 'Apple watch found', 'C111', 'found', 'active', NOW());

-- Verify the data was inserted
SELECT post_id, item_name, location, item_type, status FROM posts WHERE item_type = 'found' ORDER BY created_at DESC;

-- Test the matching query
SELECT
    p.post_id,
    p.item_name,
    p.location,
    (
        CASE
            WHEN LOWER(p.item_name) = LOWER('watch') THEN 50
            WHEN LOWER(p.item_name) LIKE CONCAT('%', LOWER('watch'), '%') THEN 30
            WHEN LOWER('watch') LIKE CONCAT('%', LOWER(p.item_name), '%') THEN 30
            ELSE 0
        END +
        CASE
            WHEN LOWER(p.location) = LOWER('C110') THEN 30
            WHEN LOWER(p.location) LIKE CONCAT('%', LOWER('C110'), '%') THEN 15
            WHEN LOWER('C110') LIKE CONCAT('%', LOWER(p.location), '%') THEN 15
            ELSE 0
        END
    ) AS match_score
FROM posts p
WHERE p.item_type = 'found' AND p.status = 'active'
HAVING match_score > 0
ORDER BY match_score DESC;

