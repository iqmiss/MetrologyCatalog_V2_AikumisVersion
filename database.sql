-- ============================================
-- Metrology Catalog — Database Schema
-- MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS service_catalog
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE service_catalog;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- SCHEMA
-- ============================================

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `results`;
DROP TABLE IF EXISTS `contracts`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `devices`;
DROP TABLE IF EXISTS `services`;
DROP TABLE IF EXISTS `laboratories`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `companies`;

CREATE TABLE `companies` (
  `id` int NOT NULL AUTO_INCREMENT,
  `bin` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bin` (`bin`),
  KEY `idx_bin` (`bin`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('client','metrolog','manager','admin') COLLATE utf8mb4_unicode_ci DEFAULT 'client',
  `company_id` int DEFAULT NULL,
  `lab_id` int DEFAULT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `password_reset_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password_reset_expires` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_lab_id` (`lab_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE SET NULL,
  CONSTRAINT `users_ibfk_2` FOREIGN KEY (`lab_id`) REFERENCES `laboratories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `laboratories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `address` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_city` (`city`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `services` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `measurement_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `duration_days` int NOT NULL,
  `lab_id` int NOT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `standard` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_measurement_type` (`measurement_type`),
  KEY `idx_lab_id` (`lab_id`),
  KEY `idx_is_active` (`is_active`),
  CONSTRAINT `services_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `laboratories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `devices` (
  `id` int NOT NULL AUTO_INCREMENT,
  `company_id` int NOT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `serial_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_verified_at` datetime DEFAULT NULL,
  `next_verification_date` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `serial_number` (`serial_number`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_serial_number` (`serial_number`),
  KEY `idx_type` (`type`),
  CONSTRAINT `devices_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `client_id` int NOT NULL,
  `service_id` int NOT NULL,
  `lab_id` int NOT NULL,
  `status` enum('awaiting_payment','awaiting_delivery','received_in_lab','in_work','under_review','completed','cancelled') COLLATE utf8mb4_unicode_ci DEFAULT 'new',
  `total_price` decimal(10,2) NOT NULL,
  `submit_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `due_date` date DEFAULT NULL,
  `completion_date` datetime DEFAULT NULL,
  `metrologist_id` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_number` (`order_number`),
  KEY `service_id` (`service_id`),
  KEY `metrologist_id` (`metrologist_id`),
  KEY `idx_order_number` (`order_number`),
  KEY `idx_client_id` (`client_id`),
  KEY `idx_status` (`status`),
  KEY `idx_lab_id` (`lab_id`),
  KEY `idx_submit_date` (`submit_date`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`lab_id`) REFERENCES `laboratories` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `orders_ibfk_4` FOREIGN KEY (`metrologist_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `device_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `model` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `serial_number` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantity` int NOT NULL DEFAULT '1',
  `unit_price` decimal(10,2) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `contracts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `contract_number` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,

  `client_signed` tinyint(1) DEFAULT '0',
  `client_signed_at` datetime DEFAULT NULL,
  `client_signed_by` int DEFAULT NULL,

  `manager_signed` tinyint(1) DEFAULT '0',
  `manager_signed_at` datetime DEFAULT NULL,
  `manager_signed_by` int DEFAULT NULL,

  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  UNIQUE KEY `contract_number` (`contract_number`),
  KEY `client_signed_by` (`client_signed_by`),
  KEY `manager_signed_by` (`manager_signed_by`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_contract_number` (`contract_number`),
  CONSTRAINT `contracts_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `contracts_ibfk_2` FOREIGN KEY (`client_signed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `contracts_ibfk_3` FOREIGN KEY (`manager_signed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `results` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `result_type` enum('certificate','protocol','report') COLLATE utf8mb4_unicode_ci DEFAULT 'certificate',
  `issued_at` datetime DEFAULT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `metrologist_id` int NOT NULL,
  `is_signed` tinyint(1) DEFAULT '0',
  `signed_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `metrologist_id` (`metrologist_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_result_type` (`result_type`),
  KEY `idx_issued_at` (`issued_at`),
  CONSTRAINT `results_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `results_ibfk_2` FOREIGN KEY (`metrologist_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `order_id` int DEFAULT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `notification_type` enum('order_status','document_ready','reminder') COLLATE utf8mb4_unicode_ci DEFAULT 'order_status',
  `is_read` tinyint(1) DEFAULT '0',
  `read_at` datetime DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- TEST DATA
-- Все пароли: password
-- ============================================

-- Лаборатории
INSERT INTO `laboratories` (name, address, phone, city) VALUES
('Метрологическая лаборатория №1', 'г. Астана, ул. Абая 5', '+77001111111', 'Астана'),
('Метрологическая лаборатория №2', 'г. Алматы, ул. Достык 10', '+77002222222', 'Алматы');

-- Услуги
INSERT INTO `services` (name, description, measurement_type, price, duration_days, lab_id, standard) VALUES
('Поверка манометра', 'Поверка манометров общего назначения по ГОСТ', 'Манометр', 5000.00, 3, 1, 'ГОСТ 8.610-2012'),
('Поверка термометра', 'Поверка термометров лабораторных', 'Термопара', 3500.00, 2, 1, 'ГОСТ 8.016-2021'),
('Поверка амперметра', 'Поверка амперметров переменного тока', 'Амперметр', 4500.00, 3, 2, 'ГОСТ 8.497-2009'),
('Поверка вольтметра', 'Поверка вольтметров цифровых', 'Вольтметр', 4000.00, 2, 2, 'ГОСТ 8.362-2013');

-- Компания
INSERT INTO `companies` (bin, name, address, phone, email) VALUES
('123456789012', 'ТОО Тест Компания', 'г. Астана, ул. Пушкина 1', '+77003333333', 'test@company.kz');

-- Пользователи (пароль для всех: password)
INSERT INTO `users` (email, password_hash, role, full_name, phone, company_id, lab_id, is_active) VALUES
('client@test.kz',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'client',   'Клиентов Клиент',    '+77004444444', 1,    NULL, 1),
('metrolog@test.kz',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'metrolog', 'Метробаев Лог',    '+77005555555', NULL, 1,    1),
('metrolog2@test.kz',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'metrolog', 'Логов Метр','+77008888888', NULL, 2,    1),
('manager@metrology.kz',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'manager',  'Менеджерович Менеджер',  '+77006666666', NULL, NULL, 1),
('admin@metrology.kz',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin',    'Админский Стратор',  '+77007777777', NULL, NULL, 1);

-- Заявки
INSERT INTO `orders` (order_number, client_id, service_id, lab_id, status, total_price, due_date) VALUES
('ORD-001', 1, 1, 1, 'completed',        5000.00, '2026-03-15'),
('ORD-002', 1, 2, 1, 'in_work',          3500.00, '2026-03-25'),
('ORD-003', 1, 3, 2, 'awaiting_payment', 4500.00, '2026-04-01'),
('ORD-004', 1, 4, 2, 'awaiting_payment', 4000.00, '2026-04-10');

-- Приборы в заявках
INSERT INTO `order_items` (order_id, device_type, model, serial_number, quantity, unit_price) VALUES
(1, 'Манометр',  'МП-100',  'SN-001', 1, 5000.00),
(2, 'Термометр', 'ТЛ-4',    'SN-002', 1, 3500.00),
(3, 'Амперметр', 'Э-378',   'SN-003', 1, 4500.00),
(4, 'Вольтметр', 'В-7-78',  'SN-004', 1, 4000.00);

-- Договоры
INSERT INTO `contracts` (order_id, contract_number, client_signed, manager_signed) VALUES
(1, 'CNT-001', 1, 1),
(2, 'CNT-002', 0, 0),
(3, 'CNT-003', 0, 0),
(4, 'CNT-004', 0, 0);

-- Результат для завершённой заявки
INSERT INTO `results` (order_id, result_type, issued_at, metrologist_id, is_signed, signed_at) VALUES
(1, 'certificate', NOW(), 2, 1, NOW());
