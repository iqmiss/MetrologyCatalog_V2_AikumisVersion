-- Copy of database.sql BUT removed DROPTABLEs & USE railway because FLYWAY is integrated(and in phpadmin i renamed the railway to the metrology)

-- ============================================
-- Metrology Catalog — Database Schema
-- MySQL 8.0+
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- ТАБЛИЦЫ
-- ============================================

CREATE TABLE `companies` (
  `id`         int NOT NULL AUTO_INCREMENT,
  `bin`        varchar(12)  NOT NULL,
  `name`       varchar(255) NOT NULL,
  `address`    varchar(500) DEFAULT NULL,
  `phone`      varchar(20)  DEFAULT NULL,
  `email`      varchar(255) DEFAULT NULL,
  `created_at` timestamp    DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bin` (`bin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `laboratories` (
  `id`         int NOT NULL AUTO_INCREMENT,
  `name`       varchar(255) NOT NULL,
  `address`    varchar(500) DEFAULT NULL,
  `phone`      varchar(20)  DEFAULT NULL,
  `city`       varchar(100) DEFAULT NULL,
  `email`      varchar(255) DEFAULT NULL,
  `created_at` timestamp    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `users` (
  `id`                     int NOT NULL AUTO_INCREMENT,
  `email`                  varchar(255) NOT NULL,
  `password_hash`          varchar(255) NOT NULL,
  `role`                   enum('client','metrolog','manager','director','gen_director','financier','approver','admin') DEFAULT 'client',
  `company_id`             int          DEFAULT NULL,
  `lab_id`                 int          DEFAULT NULL,
  `full_name`              varchar(255) DEFAULT NULL,
  `phone`                  varchar(20)  DEFAULT NULL,
  `is_active`              tinyint(1)   DEFAULT '1',
  `password_reset_token`   varchar(255) DEFAULT NULL,
  `password_reset_expires` datetime     DEFAULT NULL,
  `created_at`             timestamp    DEFAULT CURRENT_TIMESTAMP,
  `updated_at`             timestamp    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_role` (`role`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_lab_id` (`lab_id`),
  CONSTRAINT `users_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE SET NULL,
  CONSTRAINT `users_ibfk_2` FOREIGN KEY (`lab_id`)     REFERENCES `laboratories` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `services` (
  `id`               int NOT NULL AUTO_INCREMENT,
  `name`             varchar(255) NOT NULL,
  `description`      text         DEFAULT NULL,
  `measurement_type` varchar(255) DEFAULT NULL,
  `price`            decimal(10,2) DEFAULT NULL,
  `duration_days`    int          NOT NULL,
  `lab_id`           int          NOT NULL,
  `is_active`        tinyint(1)   DEFAULT '1',
  `standard`         varchar(255) DEFAULT NULL,
  `created_at`       timestamp    DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       timestamp    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_measurement_type` (`measurement_type`),
  KEY `idx_lab_id` (`lab_id`),
  CONSTRAINT `services_ibfk_1` FOREIGN KEY (`lab_id`) REFERENCES `laboratories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `devices` (
  `id`                    int NOT NULL AUTO_INCREMENT,
  `company_id`            int          NOT NULL,
  `type`                  varchar(255) NOT NULL,
  `model`                 varchar(255) DEFAULT NULL,
  `serial_number`         varchar(255) NOT NULL,
  `last_verified_at`      datetime     DEFAULT NULL,
  `next_verification_date` date        DEFAULT NULL,
  `created_at`            timestamp    DEFAULT CURRENT_TIMESTAMP,
  `updated_at`            timestamp    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `serial_number` (`serial_number`),
  KEY `idx_company_id` (`company_id`),
  CONSTRAINT `devices_ibfk_1` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `orders` (
  `id`                   int NOT NULL AUTO_INCREMENT,
  `order_number`         varchar(50)  NOT NULL,
  `client_id`            int          NOT NULL,
  `service_id`           int          NOT NULL,
  `lab_id`               int          NOT NULL,
  `assigned_lab_id`      int          DEFAULT NULL,
  `assigned_at`          datetime     DEFAULT NULL,
  `status`               enum(
                           'pending_contract',
                           'revision',
                           'awaiting_approval',
                           'awaiting_director',
                           'awaiting_payment',
                           'pending_delivery',
                           'awaiting_delivery',
                           'received_in_lab',
                           'in_work',
                           'under_review',
                           'completed',
                           'cancelled',
                           'annulled',
                           'terminated'
                         ) DEFAULT 'pending_contract',
  `price`                decimal(10,2) DEFAULT NULL,
  `due_date`             date         DEFAULT NULL,
  `metrologist_id`       int          DEFAULT NULL,
  `client_comment`       varchar(1000) DEFAULT NULL,
  `manager_comment`      text         DEFAULT NULL,
  `payment_comment`      varchar(500) DEFAULT NULL,
  `invoice_sent`         tinyint(1)   NOT NULL DEFAULT '0',
  `payment_receipt`      mediumtext   DEFAULT NULL,
  `payment_receipt_name` varchar(255) DEFAULT NULL,
  `receipt_uploaded_at`  datetime     DEFAULT NULL,
  `submit_date`          datetime     DEFAULT CURRENT_TIMESTAMP,
  `due_date_completion`  datetime     DEFAULT NULL,
  `created_at`           timestamp    DEFAULT CURRENT_TIMESTAMP,
  `updated_at`           timestamp    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_number` (`order_number`),
  KEY `idx_client_id` (`client_id`),
  KEY `idx_status` (`status`),
  KEY `idx_lab_id` (`lab_id`),
  KEY `service_id` (`service_id`),
  KEY `metrologist_id` (`metrologist_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`client_id`)      REFERENCES `users` (`id`)        ON DELETE RESTRICT,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`service_id`)     REFERENCES `services` (`id`)     ON DELETE RESTRICT,
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`lab_id`)         REFERENCES `laboratories` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `orders_ibfk_4` FOREIGN KEY (`metrologist_id`) REFERENCES `users` (`id`)        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `order_items` (
  `id`            int NOT NULL AUTO_INCREMENT,
  `order_id`      int          NOT NULL,
  `device_type`   varchar(255) NOT NULL,
  `model`         varchar(255) DEFAULT NULL,
  `serial_number` varchar(255) NOT NULL,
  `quantity`      int          NOT NULL DEFAULT '1',
  `created_at`    timestamp    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `contracts` (
  `id`                    int NOT NULL AUTO_INCREMENT,
  `order_id`              int          NOT NULL,
  `contract_number`       varchar(50)  NOT NULL,
  `registration_number`   varchar(50)  NULL,
  `contract_file`         mediumtext   NULL,
  `contract_file_name`    varchar(255) NULL,
  `file_path`             varchar(500) DEFAULT NULL,
  `status`                enum('draft','pending_approval','approved','signed','rejected','annulled','terminated') DEFAULT 'draft',
  `director_signed`       tinyint(1)   DEFAULT '0',
  `director_signed_at`    datetime     DEFAULT NULL,
  `director_signed_by`    int          DEFAULT NULL,
  `approver_signed`       tinyint(1)   NOT NULL DEFAULT '0',
  `approver_signed_at`    datetime     DEFAULT NULL,
  `approver_signed_by`    int          DEFAULT NULL,
  `financier_signed`      tinyint(1)   NOT NULL DEFAULT '0',
  `financier_signed_at`   datetime     DEFAULT NULL,
  `financier_signed_by`   int          DEFAULT NULL,
  `client_signed`         tinyint(1)   DEFAULT '0',
  `client_signed_at`      datetime     DEFAULT NULL,
  `client_signed_by`      int          DEFAULT NULL,
  `gen_director_signed`   tinyint(1)   NOT NULL DEFAULT '0',
  `gen_director_signed_at` datetime    DEFAULT NULL,
  `gen_director_signed_by` int         DEFAULT NULL,
  `rejected_by_role`      varchar(50)  DEFAULT NULL,
  `rejected_reason`       text         DEFAULT NULL,
  `annulled_at`           datetime     DEFAULT NULL,
  `annulled_by`           int          DEFAULT NULL,
  `annulled_reason`       text         DEFAULT NULL,
  `terminated_at`         datetime     DEFAULT NULL,
  `terminated_by`         int          DEFAULT NULL,
  `terminated_reason`     text         DEFAULT NULL,
  `created_at`            timestamp    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  UNIQUE KEY `contract_number` (`contract_number`),
  KEY `idx_status` (`status`),
  CONSTRAINT `contracts_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `results` (
  `id`             int NOT NULL AUTO_INCREMENT,
  `order_id`       int NOT NULL,
  `result_type`    enum('certificate','protocol','report') DEFAULT 'certificate',
  `issued_at`      datetime     DEFAULT NULL,
  `file_path`      varchar(500) DEFAULT NULL,
  `metrologist_id` int          NOT NULL,
  `is_signed`      tinyint(1)   DEFAULT '0',
  `signed_at`      datetime     DEFAULT NULL,
  `created_at`     timestamp    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `metrologist_id` (`metrologist_id`),
  CONSTRAINT `results_ibfk_1` FOREIGN KEY (`order_id`)       REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `results_ibfk_2` FOREIGN KEY (`metrologist_id`) REFERENCES `users` (`id`)  ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `notifications` (
  `id`                int NOT NULL AUTO_INCREMENT,
  `user_id`           int  NOT NULL,
  `order_id`          int  DEFAULT NULL,
  `message`           text NOT NULL,
  `notification_type` enum(
                        'order_status',
                        'document_ready',
                        'reminder',
                        'approval_required',
                        'payment_received',
                        'assigned_to_lab',
                        'receipt_uploaded'
                      ) DEFAULT 'order_status',
  `is_read`           tinyint(1) DEFAULT '0',
  `read_at`           datetime   DEFAULT NULL,
  `created_at`        timestamp  DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `order_id` (`order_id`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`)  REFERENCES `users` (`id`)  ON DELETE CASCADE,
  CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- ТЕСТОВЫЕ ДАННЫЕ
-- ============================================

INSERT INTO `laboratories` (name, address, phone, city) VALUES
('Метрологическая лаборатория №1', 'г. Астана, ул. Абая 5',    '+77001111111', 'Астана'),
('Метрологическая лаборатория №2', 'г. Алматы, ул. Достык 10', '+77002222222', 'Алматы'),
('Метрологическая лаборатория №3', 'г. Карагандa, ул. Ленина 3', '+77003111111', 'Карагандa'),
('Метрологическая лаборатория №4', 'г. Актобе, ул. Абилхайыр хана 12', '+77004111111', 'Актобе');

INSERT INTO `services` (name, description, measurement_type, duration_days, lab_id, standard) VALUES
('Испытания для целей утверждения типа',           'Испытания средств измерений для утверждения типа',                                        'Средства измерений',       1, 1, 'ГОСТ 8.610-2012'),
('Метрологическая аттестация средств измерений',   'Аттестация средств измерений в лабораторных условиях',                                    'Средства измерений',       2, 1, 'ГОСТ 8.016-2021'),
('Методики выполнения измерений',                  'Разработка и аттестация методик выполнения измерений',                                    'Средства измерений',       3, 1, 'ГОСТ 8.497-2009'),
('Аттестация испытательного оборудования',         'Аттестация испытательного и измерительного оборудования',                                 'Испытательное оборудование', 4, 1, 'ГОСТ 8.497-2009'),
('Допуск к применению стандартного образца',       'Экспертиза и допуск зарубежных стандартных образцов',                                     'Стандартные образцы',      5, 1, 'ГОСТ 8.497-2009'),
('Поверка средств измерений',                      'Поверка средств измерений в соответствии с ГОСТ',                                         'Средства измерений',       6, 1, 'ГОСТ 8.497-2009'),
('Калибровка средств измерений',                   'Калибровка средств измерений по эталонам',                                                'Средства измерений',       1, 2, 'ГОСТ 8.497-2009'),
('Изготовление поверительных клейм',               'Изготовление и выдача поверительных клейм',                                               'Поверительные клейма',     2, 2, 'ГОСТ 8.497-2009'),
('Межлабораторные сличения',                       'Организация и проведение межлабораторных сличений',                                       'Средства измерений',       3, 2, 'ГОСТ 8.497-2009'),
('Аттестация поверителей средств измерений',       'Аттестация и переаттестация поверителей средств измерений',                               'Средства измерений',       4, 2, 'ГОСТ 8.497-2009'),
('Признание результатов испытаний',                'Порядок признания результатов испытаний, первичной поверки и метрологической аттестации',  'Средства измерений',       5, 2, 'ГОСТ 8.497-2009'),
('Признание результатов поверки зарубежных орг.', 'Признание результатов калибровки зарубежными метрологическими организациями',             'Средства измерений',       6, 2, 'ГОСТ 8.362-2013');

INSERT INTO `companies` (bin, name, address, phone, email) VALUES
('123456789012', 'ТОО Тест Компания', 'г. Астана, ул. Пушкина 1', '+77003333333', 'test@company.kz');

-- Пароль для всех: password
INSERT INTO `users` (email, password_hash, role, full_name, phone, company_id, lab_id, is_active) VALUES
('client@test.kz',           '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'client',       'Клиентов Клиент',          '+77004444444', 1,    NULL, 1),
('metrolog@test.kz',         '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'metrolog',     'Метробаев Лог',             '+77005555555', NULL, 1,    1),
('metrolog2@test.kz',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'metrolog',     'Логов Метр',                '+77008888888', NULL, 2,    1),
('manager@metrology.kz',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'manager',      'Менеджерович Менеджер',     '+77006666666', NULL, NULL, 1),
('director@metrology.kz',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'director',     'Директоров Директор',       '+77009999999', NULL, NULL, 1),
('gen_director@metrology.kz','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'gen_director', 'Генеральный Директоров',    '+77009000000', NULL, NULL, 1),
('financier@metrology.kz',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'financier',    'Финансов Финансист',        '+77001234567', NULL, NULL, 1),
('approver@metrology.kz',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'approver',     'Согласуев Согласующий',     '+77007654321', NULL, NULL, 1),
('admin@metrology.kz',       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin',        'Админский Стратор',         '+77007777777', NULL, NULL, 1);

-- Тестовые заявки (price = NULL — финансист ещё не объявил)
INSERT INTO `orders` (order_number, client_id, service_id, lab_id, status, due_date) VALUES
('ORD-001', 1, 1, 1, 'completed',        '2026-03-15'),
('ORD-002', 1, 2, 1, 'in_work',          '2026-03-25'),
('ORD-003', 1, 3, 2, 'awaiting_payment', '2026-04-01'),
('ORD-004', 1, 4, 2, 'pending_contract', '2026-04-10');

INSERT INTO `order_items` (order_id, device_type, model, serial_number, quantity) VALUES
(1, 'Манометр',  'МП-100', 'SN-001', 1),
(2, 'Термометр', 'ТЛ-4',   'SN-002', 1),
(3, 'Амперметр', 'Э-378',  'SN-003', 1),
(4, 'Вольтметр', 'В-7-78', 'SN-004', 1);

INSERT INTO `contracts` (order_id, contract_number, status,
  client_signed, director_signed, approver_signed, financier_signed, gen_director_signed) VALUES
(1, 'CNT-001', 'signed', 1, 1, 1, 1, 1),
(2, 'CNT-002', 'draft',  0, 0, 0, 0, 0),
(3, 'CNT-003', 'signed', 1, 1, 1, 1, 1),
(4, 'CNT-004', 'draft',  0, 0, 0, 0, 0);

INSERT INTO `results` (order_id, result_type, issued_at, metrologist_id, is_signed, signed_at) VALUES
(1, 'certificate', NOW(), 2, 1, NOW());