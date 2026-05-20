CREATE TABLE `application_counters` (
  `id`               int NOT NULL AUTO_INCREMENT,
  `service_code`     varchar(10) NOT NULL,
  `subservice_code`  varchar(10) NOT NULL,
  `last_number`      int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_codes` (`service_code`, `subservice_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;