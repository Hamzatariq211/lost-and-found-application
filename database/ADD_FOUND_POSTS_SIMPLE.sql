-- Simple script to add test FOUND posts to the posts table
-- Run this in phpMyAdmin

USE lost_and_found_db;

-- First, let's see what users exist
SELECT user_id, username FROM users;

-- Now insert test FOUND posts into the posts table
-- Change user_id if needed based on users in your database
INSERT INTO posts (user_id, item_name, item_description, location, item_type, status) VALUES
(1, 'WATCH', 'Found a silver watch in the classroom', 'C110', 'found', 'active'),
(1, 'watch', 'Black watch found on desk', 'c110', 'found', 'active'),
(1, 'Watch', 'Silver wrist watch', 'C-110', 'found', 'active'),
(1, 'Wrist Watch', 'Found near the door', 'C110', 'found', 'active'),
(1, 'Watch', 'Found in cafeteria', 'Cafe', 'found', 'active'),
(1, 'Smart Watch', 'Apple watch found', 'C111', 'found', 'active');

-- Verify the posts were added to the posts table
SELECT post_id, item_name, location, item_type, status
FROM posts
WHERE item_type = 'found'
ORDER BY post_id DESC;

