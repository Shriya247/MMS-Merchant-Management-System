-- Merchant Management System (MMS)
-- MySQL 8.x sample database: 3 tables, 28 sample records.
-- Import into a local MySQL server using MySQL Workbench or mysql CLI.
-- WARNING: This file creates tables if absent and inserts sample records.
-- Run on a new/empty mms database to avoid duplicate primary keys.

CREATE DATABASE IF NOT EXISTS `mms` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `mms`;

CREATE TABLE IF NOT EXISTS `merchants` (
  `merchant_id` int NOT NULL AUTO_INCREMENT,
  `merchant_name` varchar(100) NOT NULL,
  `business_type` varchar(80) NOT NULL,
  `status` varchar(10) NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `payments` (
  `payment_id` int NOT NULL AUTO_INCREMENT,
  `merchant_id` int NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `payment_method` varchar(30) NOT NULL,
  `payment_status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `payment_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`payment_id`),
  KEY `merchant_id` (`merchant_id`),
  CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `settlements` (
  `settlement_id` int NOT NULL AUTO_INCREMENT,
  `payment_id` int NOT NULL,
  `merchant_id` int NOT NULL,
  `settlement_amount` decimal(12,2) NOT NULL,
  `settlement_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `settlement_status` varchar(20) NOT NULL DEFAULT 'COMPLETED',
  PRIMARY KEY (`settlement_id`),
  UNIQUE KEY `payment_id` (`payment_id`),
  KEY `merchant_id` (`merchant_id`),
  CONSTRAINT `settlements_ibfk_1` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`payment_id`),
  CONSTRAINT `settlements_ibfk_2` FOREIGN KEY (`merchant_id`) REFERENCES `merchants` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

START TRANSACTION;

INSERT INTO `merchants` (`merchant_id`, `merchant_name`, `business_type`, `status`, `created_at`) VALUES
(1, 'Fresh Mart', 'Grocery', 'ACTIVE', '2026-10-18 21:13:13'),
(2, 'Tech World', 'Electronics', 'ACTIVE', '2026-10-18 21:15:49'),
(3, 'Urban Store', 'Clothing', 'INACTIVE', '2026-10-18 21:15:49'),
(4, 'Style Hub', 'Fashion', 'ACTIVE', '2026-10-18 21:15:49'),
(5, 'Daily Needs', 'Supermarket', 'ACTIVE', '2026-10-18 21:15:49'),
(6, 'Book Haven', 'Bookstore', 'ACTIVE', '2026-10-18 21:15:49'),
(7, 'Cafe Delight', 'Restaurant', 'ACTIVE', '2026-10-18 21:15:49'),
(8, 'Green Pharmacy', 'Pharmacy', 'ACTIVE', '2026-10-18 21:15:49'),
(9, 'Gadget Zone', 'Electronics', 'INACTIVE', '2026-10-18 21:15:49'),
(10, 'Fresh Bakery', 'Bakery', 'ACTIVE', '2026-10-18 21:15:49'),
(11, 'ABC pharma', 'pharmacy', 'INACTIVE', '2026-10-19 13:47:38');

INSERT INTO `payments` (`payment_id`, `merchant_id`, `amount`, `payment_method`, `payment_status`, `payment_date`) VALUES
(1, 1, 2500.00, 'UPI', 'PENDING', '2026-10-18 21:20:55'),
(2, 2, 15999.00, 'CARD', 'PENDING', '2026-10-18 21:20:55'),
(3, 4, 3499.00, 'UPI', 'PENDING', '2026-10-18 21:20:55'),
(4, 5, 1850.00, 'NET BANKING', 'PENDING', '2026-10-18 21:20:55'),
(5, 6, 1299.00, 'UPI', 'PENDING', '2026-10-18 21:20:55'),
(6, 7, 850.00, 'CARD', 'PENDING', '2026-10-18 21:20:55'),
(7, 8, 2200.00, 'UPI', 'PENDING', '2026-10-18 21:20:55'),
(8, 10, 650.00, 'CARD', 'PENDING', '2026-10-18 21:20:55'),
(9, 1, 4250.00, 'CARD', 'PENDING', '2026-10-18 21:20:55'),
(10, 2, 8999.00, 'NET BANKING', 'PENDING', '2026-10-18 21:20:55'),
(11, 5, 3200.00, 'UPI', 'PENDING', '2026-10-18 21:20:55'),
(12, 4, 5600.00, 'CARD', 'PENDING', '2026-10-18 21:20:55'),
(13, 7, 1750.00, 'UPI', 'PENDING', '2026-10-18 21:20:55'),
(14, 6, 950.00, 'CARD', 'SETTLED', '2026-10-18 21:20:55'),
(15, 10, 1200.00, 'UPI', 'SUCCESS', '2026-10-18 21:20:55');

INSERT INTO `settlements` (`settlement_id`, `payment_id`, `merchant_id`, `settlement_amount`, `settlement_date`, `settlement_status`) VALUES
(1, 14, 6, 950.00, '2026-10-18 23:44:19', 'COMPLETED'),
(2, 15, 10, 1200.00, '2026-10-24 12:07:15', 'COMPLETED');

COMMIT;

-- Verification: expected 11 merchants, 15 payments, 2 settlements.
SELECT
  (SELECT COUNT(*) FROM `merchants`) AS merchants,
  (SELECT COUNT(*) FROM `payments`) AS payments,
  (SELECT COUNT(*) FROM `settlements`) AS settlements;
