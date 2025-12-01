-- Lost Items Feature - Database Schema Extension
-- This adds support for users to report lost items and find matching found posts

USE lost_and_found_db;

-- Lost Items Table (separate from posts for better organization)
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

-- Function to find matching posts for a lost item
-- This will be used by the API to show relevant found posts
DELIMITER //

CREATE FUNCTION IF NOT EXISTS match_score(
    lost_name VARCHAR(100),
    found_name VARCHAR(100),
    lost_location VARCHAR(200),
    found_location VARCHAR(200)
) RETURNS INT
DETERMINISTIC
BEGIN
    DECLARE score INT DEFAULT 0;

    -- Exact name match: +50 points
    IF LOWER(lost_name) = LOWER(found_name) THEN
        SET score = score + 50;
    -- Partial name match: +30 points
    ELSEIF LOWER(found_name) LIKE CONCAT('%', LOWER(lost_name), '%')
        OR LOWER(lost_name) LIKE CONCAT('%', LOWER(found_name), '%') THEN
        SET score = score + 30;
    END IF;

    -- Exact location match: +30 points
    IF LOWER(lost_location) = LOWER(found_location) THEN
        SET score = score + 30;
    -- Partial location match: +15 points
    ELSEIF LOWER(found_location) LIKE CONCAT('%', LOWER(lost_location), '%')
        OR LOWER(lost_location) LIKE CONCAT('%', LOWER(found_location), '%') THEN
        SET score = score + 15;
    END IF;

    RETURN score;
END//

DELIMITER ;

-- Sample lost items for testing
INSERT INTO lost_items (user_id, item_name, item_description, location_lost, status) VALUES
(1, 'Watch', 'Silver watch with black leather strap', 'Cafe', 'active'),
(2, 'Water Bottle', 'Blue insulated water bottle', 'C-110', 'active');

