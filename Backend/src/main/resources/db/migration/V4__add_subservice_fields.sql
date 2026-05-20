CREATE TABLE `subservice_fields` (
  `id`            int NOT NULL AUTO_INCREMENT,
  `subservice_id` int NOT NULL,
  `field_key`     varchar(100) NOT NULL,
  `label_ru`      varchar(255) NOT NULL,
  `field_type`    enum('text','number','date','select','file') NOT NULL DEFAULT 'text',
  `required`      tinyint(1) DEFAULT '1',
  `options_json`  text DEFAULT NULL,
  `sort_order`    int DEFAULT '0',
  `is_repeating`  tinyint(1) DEFAULT '0',
  `created_at`    timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_subservice_id` (`subservice_id`),
  CONSTRAINT `subservice_fields_ibfk_1` FOREIGN KEY (`subservice_id`)
    REFERENCES `subservices` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;