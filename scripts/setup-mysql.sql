-- ══════════════════════════════════════════════════════
-- SKILLORA — MySQL Production Setup Script
-- Run once on your MySQL server before first deployment
-- Usage: mysql -u root -p < scripts/setup-mysql.sql
-- ══════════════════════════════════════════════════════

-- Create database
CREATE DATABASE IF NOT EXISTS skillora_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Create application user (replace STRONG_PASSWORD)
CREATE USER IF NOT EXISTS 'skillora_user'@'%'
  IDENTIFIED BY 'STRONG_PASSWORD_HERE';

-- Grant privileges
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, DROP, REFERENCES
  ON skillora_db.*
  TO 'skillora_user'@'%';

FLUSH PRIVILEGES;

-- Verify
SELECT User, Host FROM mysql.user WHERE User = 'skillora_user';
SHOW GRANTS FOR 'skillora_user'@'%';
