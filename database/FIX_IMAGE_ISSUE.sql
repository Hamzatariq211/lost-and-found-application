-- ========================================
-- SOLUTION FOR IMAGE DISPLAY ISSUE
-- Lost & Found Application
-- ========================================

-- PROBLEM: Images aren't displaying because:
-- 1. Sample data has fake image filenames that don't exist
-- 2. The current setup is correct (stores filename, not base64)
-- 3. Just need to clean up the database

-- ========================================
-- STEP 1: Clean up existing fake sample posts
-- ========================================

-- Remove all sample posts with fake images
DELETE FROM posts WHERE item_image IN ('water_bottle.jpg', 'watch.jpg', 'notebook.jpg', 'charger.jpg');

-- Or if you want to keep the posts but remove the fake image references:
UPDATE posts SET item_image = NULL WHERE item_image IN ('water_bottle.jpg', 'watch.jpg', 'notebook.jpg', 'charger.jpg');

-- ========================================
-- STEP 2: Verify your posts table structure (should be correct as-is)
-- ========================================

-- The posts table structure is CORRECT - it stores filenames, not base64
-- item_image VARCHAR(255) - stores the filename like "abc123_1234567890.jpg"
-- The PHP API handles base64 conversion and file saving

-- If you need to recreate the posts table:
/*
DROP TABLE IF EXISTS posts;

CREATE TABLE IF NOT EXISTS posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    item_description TEXT NOT NULL,
    location VARCHAR(200) NOT NULL,
    item_type ENUM('lost', 'found') NOT NULL,
    item_image VARCHAR(255) DEFAULT NULL,  -- Stores filename, NOT base64
    status ENUM('active', 'resolved', 'deleted') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    sync_status BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_item_type (item_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
*/

-- ========================================
-- STEP 3: Verify uploads folder exists
-- ========================================

-- Make sure this folder exists:
-- E:\xampp\htdocs\lost_and_found_api\uploads\

-- If it doesn't exist, create it with write permissions

-- ========================================
-- QUICK FIX: Run this single query
-- ========================================

-- This removes all fake sample image references
UPDATE posts SET item_image = NULL WHERE item_image LIKE '%.jpg' AND item_image NOT LIKE '%_%';

-- ========================================
-- EXPLANATION
-- ========================================

-- Your current setup is CORRECT:
-- 1. App sends: Base64 image
-- 2. PHP decodes: Converts base64 to actual image file
-- 3. PHP saves: Stores file as "abc123_1234567890.jpg" in uploads/
-- 4. Database stores: Just the filename "abc123_1234567890.jpg"
-- 5. API returns: Full URL "http://10.0.2.2/lost_and_found_api/uploads/abc123_1234567890.jpg"
-- 6. App displays: Glide loads the image from URL

-- The issue was:
-- - Sample data had fake filenames like "water_bottle.jpg"
-- - These files don't exist in uploads folder
-- - So images couldn't load

-- After running the cleanup query above:
-- - Old posts will have NULL images (show "No Image")
-- - New posts created through app will have real images
-- - Images will display correctly!

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Check all posts
SELECT post_id, item_name, item_image, created_at FROM posts;

-- Check posts with images
SELECT post_id, item_name, item_image FROM posts WHERE item_image IS NOT NULL;

-- Check posts without images
SELECT post_id, item_name FROM posts WHERE item_image IS NULL;

