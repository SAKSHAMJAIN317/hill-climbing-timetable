-- SmartSched AI - MySQL 8 Database Setup Script
-- Run this script using MySQL CLI or MySQL Workbench:
--   mysql -u root -p < database-setup.sql

CREATE DATABASE IF NOT EXISTS smartsched_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Optional: Create a dedicated application user
-- CREATE USER IF NOT EXISTS 'smartsched_user'@'localhost' IDENTIFIED BY 'smartsched_pass123';
-- GRANT ALL PRIVILEGES ON smartsched_db.* TO 'smartsched_user'@'localhost';
-- FLUSH PRIVILEGES;

USE smartsched_db;
