-- Add FCM token column to users table
USE lost_and_found_db;

-- Add fcm_token column if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS fcm_token VARCHAR(255) DEFAULT NULL AFTER mobile_number;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_fcm_token ON users(fcm_token);

-- Verify the column was added
DESCRIBE users;

