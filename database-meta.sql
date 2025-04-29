-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS `meta`;
USE meta;

-- 데이터베이스 유저 생성
GRANT ALL PRIVILEGES ON `meta`.* TO `root`@`localhost`;
GRANT ALL PRIVILEGES ON `meta`.* TO `root`@`%`;