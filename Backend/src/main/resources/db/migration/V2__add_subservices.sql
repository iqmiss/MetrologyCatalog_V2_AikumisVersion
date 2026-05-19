CREATE TABLE `subservices` (
  `id`           int NOT NULL AUTO_INCREMENT,
  `service_id`   int NOT NULL,
  `name`         varchar(255) NOT NULL,
  `code`         varchar(10)  NOT NULL,
  `description`  text         DEFAULT NULL,
  `is_active`    tinyint(1)   DEFAULT '1',
  `created_at`   timestamp    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_service_id` (`service_id`),
  CONSTRAINT `subservices_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;