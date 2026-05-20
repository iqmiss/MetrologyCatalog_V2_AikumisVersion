CREATE TABLE `chat_messages` (
  `id`                  int NOT NULL AUTO_INCREMENT,
  `order_id`            int NOT NULL,
  `sender_id`           int NOT NULL,
  `sender_role`         varchar(50) NOT NULL,
  `message_text`        text DEFAULT NULL,
  `attachment_base64`   mediumtext DEFAULT NULL,
  `attachment_name`     varchar(255) DEFAULT NULL,
  `sent_at`             timestamp DEFAULT CURRENT_TIMESTAMP,
  `is_read`             tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_sender_id` (`sender_id`),
  CONSTRAINT `chat_messages_ibfk_1` FOREIGN KEY (`order_id`)
    REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chat_messages_ibfk_2` FOREIGN KEY (`sender_id`)
    REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;