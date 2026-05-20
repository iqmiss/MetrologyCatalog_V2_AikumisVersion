CREATE TABLE `order_demands` (
  `id`            int NOT NULL AUTO_INCREMENT,
  `order_id`      int NOT NULL,
  `created_by`    int NOT NULL,
  `demand_text`   text NOT NULL,
  `status`        enum('open','fulfilled') NOT NULL DEFAULT 'open',
  `created_at`    timestamp DEFAULT CURRENT_TIMESTAMP,
  `fulfilled_at`  datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `order_demands_ibfk_1` FOREIGN KEY (`order_id`)
    REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_demands_ibfk_2` FOREIGN KEY (`created_by`)
    REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;