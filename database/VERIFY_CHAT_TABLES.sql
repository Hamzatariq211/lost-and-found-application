-- Quick verification script - Run this in phpMyAdmin
-- Database: lost_and_found_db

-- Check if chat tables exist
SELECT
    'chat_rooms' as table_name,
    COUNT(*) as row_count
FROM chat_rooms
UNION ALL
SELECT
    'chat_messages' as table_name,
    COUNT(*) as row_count
FROM chat_messages;

-- Check table structure
DESCRIBE chat_rooms;
DESCRIBE chat_messages;

-- Check if we have users and posts
SELECT 'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'posts', COUNT(*) FROM posts;

-- If you see errors, the tables don't exist. Run COMPLETE_SETUP.sql

