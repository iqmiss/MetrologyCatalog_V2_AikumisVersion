CREATE TABLE `payment_events` (
  `id`                int NOT NULL AUTO_INCREMENT,
  `order_id`          int NOT NULL,
  `amount`            decimal(10,2) NOT NULL,
  `application_code`  varchar(20) DEFAULT NULL,
  `confirmed_at`      timestamp DEFAULT CURRENT_TIMESTAMP,
  `sent_to_1c`        tinyint(1) DEFAULT '0',
  `sent_at`           datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `payment_events_ibfk_1` FOREIGN KEY (`order_id`)
    REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;