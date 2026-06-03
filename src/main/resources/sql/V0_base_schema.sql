-- Base schema for logger service
-- This creates all the tables required for the application

-- Lookup Tables
CREATE TABLE IF NOT EXISTS `log_event_type` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `log_reason_type` (
  `id` int(11) NOT NULL,
  `rkey` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `default_order` int(11) DEFAULT NULL,
  `is_deprecated` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `log_source_type` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Main Event Table
CREATE TABLE IF NOT EXISTS `log_event` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `source_url` text,
  `comment` text,
  `month` varchar(255) DEFAULT NULL,
  `user_email` varchar(255) DEFAULT NULL,
  `user_ip` varchar(255) DEFAULT NULL,
  `source` varchar(255) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `log_event_type_id` int(11) DEFAULT NULL,
  `log_reason_type_id` int(11) DEFAULT NULL,
  `log_source_type_id` int(11) DEFAULT NULL,
  `created` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_log_event_type` (`log_event_type_id`),
  KEY `fk_log_reason_type` (`log_reason_type_id`),
  KEY `fk_log_source_type` (`log_source_type_id`),
  CONSTRAINT `fk_log_event_type` FOREIGN KEY (`log_event_type_id`) REFERENCES `log_event_type` (`id`),
  CONSTRAINT `fk_log_reason_type` FOREIGN KEY (`log_reason_type_id`) REFERENCES `log_reason_type` (`id`),
  CONSTRAINT `fk_log_source_type` FOREIGN KEY (`log_source_type_id`) REFERENCES `log_source_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Event Detail Table
CREATE TABLE IF NOT EXISTS `log_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entity_type` varchar(255) DEFAULT NULL,
  `entity_uid` varchar(255) DEFAULT NULL,
  `record_count` bigint(20) DEFAULT NULL,
  `log_event_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_log_detail_event` (`log_event_id`),
  CONSTRAINT `fk_log_detail_event` FOREIGN KEY (`log_event_id`) REFERENCES `log_event` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Remote Address Cache Table
CREATE TABLE IF NOT EXISTS `remote_address` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(255) NOT NULL,
  `host_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ip` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Summary Tables
CREATE TABLE IF NOT EXISTS `event_summary_totals` (
  `month` varchar(255) NOT NULL,
  `log_event_type_id` int(11) NOT NULL,
  `number_of_events` bigint(20) DEFAULT NULL,
  `record_count` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`month`, `log_event_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `event_summary_breakdown_email` (
  `month` varchar(255) NOT NULL,
  `log_event_type_id` int(11) NOT NULL,
  `user_email_category` varchar(255) NOT NULL,
  `number_of_events` bigint(20) DEFAULT NULL,
  `record_count` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`month`, `log_event_type_id`, `user_email_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `event_summary_breakdown_email_entity` (
  `month` varchar(255) NOT NULL,
  `log_event_type_id` int(11) NOT NULL,
  `user_email_category` varchar(255) NOT NULL,
  `entity_uid` varchar(255) NOT NULL,
  `number_of_events` bigint(20) DEFAULT NULL,
  `record_count` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`month`, `log_event_type_id`, `user_email_category`, `entity_uid`),
  INDEX `esbee_entity_uid` (`entity_uid`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `event_summary_breakdown_reason` (
  `month` varchar(255) NOT NULL,
  `log_event_type_id` int(11) NOT NULL,
  `log_reason_type_id` int(11) NOT NULL DEFAULT -1,
  `number_of_events` bigint(20) NOT NULL,
  `record_count` bigint(20) NOT NULL,
  PRIMARY KEY (`month`, `log_event_type_id`, `log_reason_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `event_summary_breakdown_reason_entity` (
  `month` varchar(255) NOT NULL,
  `log_event_type_id` int(11) NOT NULL,
  `log_reason_type_id` int(11) NOT NULL DEFAULT -1,
  `entity_uid` varchar(255) NOT NULL,
  `number_of_events` bigint(20) NOT NULL,
  `record_count` bigint(20) NOT NULL,
  PRIMARY KEY (`month`, `log_event_type_id`, `log_reason_type_id`, `entity_uid`),
  INDEX `esbree_entity_uid` (`entity_uid`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `event_summary_breakdown_reason_entity_source` (
  `month` varchar(255) NOT NULL,
  `log_event_type_id` int(11) NOT NULL,
  `log_reason_type_id` int(11) NOT NULL DEFAULT -1,
  `entity_uid` varchar(255) NOT NULL,
  `log_source_type_id` int(11) NOT NULL,
  `number_of_events` bigint(20) NOT NULL,
  `record_count` bigint(20) NOT NULL,
  PRIMARY KEY (`month`, `log_event_type_id`, `log_reason_type_id`, `entity_uid`, `log_source_type_id`),
  INDEX `esbrees_entity_uid` (`entity_uid`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
