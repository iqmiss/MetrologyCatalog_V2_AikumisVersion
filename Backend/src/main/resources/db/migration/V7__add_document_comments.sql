CREATE TABLE `document_comments` (
  `id`                int NOT NULL AUTO_INCREMENT,
  `order_id`          int NOT NULL,
  `commenter_id`      int NOT NULL,
  `commenter_role`    varchar(50) NOT NULL,
  `highlighted_text`  text DEFAULT NULL,
  `comment_text`      text NOT NULL,
  `resolved`          tinyint(1) DEFAULT '0',
  `created_at`        timestamp DEFAULT CURRENT_TIMESTAMP,
  `resolved_at`       datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  CONSTRAINT `doc_comments_ibfk_1` FOREIGN KEY (`order_id`)
    REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `doc_comments_ibfk_2` FOREIGN KEY (`commenter_id`)
    REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;