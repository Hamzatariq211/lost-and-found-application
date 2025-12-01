-- ========================================
-- ALTER DATABASE TO STORE BASE64 IMAGES
-- Lost & Found Application
-- ========================================

-- STEP 1: Backup existing data (optional but recommended)
-- CREATE TABLE posts_backup AS SELECT * FROM posts;

-- STEP 2: Modify the posts table to store base64 data
ALTER TABLE posts
MODIFY COLUMN item_image LONGTEXT DEFAULT NULL
COMMENT 'Stores base64 encoded image data';

-- STEP 3: Clear existing file-based images (they won't work anymore)
UPDATE posts SET item_image = NULL WHERE item_image IS NOT NULL;

-- STEP 4: Verify the change
DESCRIBE posts;

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Check table structure
SHOW COLUMNS FROM posts WHERE Field = 'item_image';

-- Should show:
-- Field: item_image
-- Type: longtext
-- Null: YES
-- Key:
-- Default: NULL

-- ========================================
-- NOTES
-- ========================================
-- LONGTEXT can store up to 4GB of data
-- Perfect for base64 encoded images
-- Base64 images are typically 300KB - 2MB as text

