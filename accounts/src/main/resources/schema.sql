CREATE TABLE IF NOT EXISTS `account` (
    `account_id` int AUTO_INCREMENT PRIMARY KEY,
    `username` varchar(50) NOT NULL,
    `password` varchar(50) NOT NULL,
    `role` varchar(20) NOT NULL,
    `created_by` varchar(20) NOT NULL,
    `updated_by` varchar(20) DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS `information` (
    `account_id` int NOT NULL,
    `id` int AUTO_INCREMENT PRIMARY KEY,
    `name` varchar(50) NOT NULL,
    `email` varchar(50) NOT NULL,
    `phone` varchar(10) NOT NULL,
    `address` varchar(255) NOT NULL,
    `avatar` varchar(255),
    `created_by` varchar(20) NOT NULL,
    `updated_by` varchar(20) DEFAULT NULL
);