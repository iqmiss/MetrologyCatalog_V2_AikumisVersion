CREATE TABLE `application_field_values` (
  `id`            int NOT NULL AUTO_INCREMENT,
  `order_id`      int NOT NULL,
  `field_key`     varchar(100) NOT NULL,
  `field_value`   text DEFAULT NULL,
  `row_index`     int DEFAULT '0',
  `filled_by_role` varchar(50) DEFAULT 'client',
  `updated_at`    timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `afv_ibfk_1` FOREIGN KEY (`order_id`)
    REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;