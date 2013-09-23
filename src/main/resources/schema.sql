SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `logger` DEFAULT CHARACTER SET utf8 ;
USE `logger` ;

-- -----------------------------------------------------
-- Table `logger`.`log_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `logger`.`log_event` ;

CREATE  TABLE IF NOT EXISTS `logger`.`log_event` (
  `id` int(11) NOT NULL auto_increment,
  `comment` text,
  `created` datetime default NULL,
  `log_event_type_id` int(11) default NULL,
  `month` varchar(255) default NULL,
  `user_email` varchar(255) default NULL,
  `user_ip` varchar(255) default NULL,
  `source` varchar(255) default NULL,
  `log_reason_type_id` int(11) default NULL,
  `log_source_type_id` int(11) default NULL,
  `source_url` text,
  PRIMARY KEY  (`id`),
  KEY `SUMMARYINDEX1` (`id`,`month`,`log_event_type_id`,`log_reason_type_id`),
  KEY `SUMMARYINDEX2` (`id`,`month`,`log_event_type_id`)
) ENGINE=MyISAM AUTO_INCREMENT= 7 DEFAULT CHARSET=latin1;



-- -----------------------------------------------------
-- Table `logger`.`log_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `logger`.`log_detail` ;

CREATE  TABLE IF NOT EXISTS `logger`.`log_detail` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `entity_type` VARCHAR(255) NULL DEFAULT NULL ,
  `entity_uid` VARCHAR(255) NULL DEFAULT NULL ,
  `record_count` INT(11) NULL DEFAULT NULL ,
  `version` BIGINT(20) NULL DEFAULT NULL ,
  `log_event_id` INT(11) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `FK761AFDAC9EA6D187` (`log_event_id` ASC) ,
  KEY `ENTITYUIDINDEX` (`entity_uid`))
ENGINE = InnoDB
AUTO_INCREMENT = 11
DEFAULT CHARACTER SET = latin1;


-- -----------------------------------------------------
-- Table `logger`.`log_event_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `logger`.`log_event_type` ;

CREATE  TABLE IF NOT EXISTS `logger`.`log_event_type` (
  `id` INT(11) NOT NULL ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

-- -----------------------------------------------------
-- Table `logger`.`log_reason_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `logger`.`log_reason_type` ;

CREATE  TABLE IF NOT EXISTS `logger`.`log_reason_type` (
  `id` INT(11) NOT NULL ,
  `rkey` VARCHAR(255) NULL DEFAULT NULL ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;

DROP TABLE IF EXISTS `logger`.`log_source_type` ;

CREATE  TABLE IF NOT EXISTS `logger`.log_source_type (
  `id` INT(11) NOT NULL ,
  `name` VARCHAR(255) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
