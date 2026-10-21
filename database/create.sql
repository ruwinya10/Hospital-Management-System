CREATE DATABASE IF NOT EXISTS hms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'hms'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON hms.* TO 'hms'@'localhost';
-- Replace this development password before deployment.
