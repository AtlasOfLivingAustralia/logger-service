SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `logger` DEFAULT CHARACTER SET latin1 ;
USE `logger` ;

-- -----------------------------------------------------
-- Table `logger`.`log_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `logger`.`log_event` ;

CREATE  TABLE IF NOT EXISTS `logger`.`log_event` (
  `id` INT(11) NOT NULL AUTO_INCREMENT ,
  `comment` TEXT NULL DEFAULT NULL ,
  `created` DATETIME NULL DEFAULT NULL ,
  `month` VARCHAR(255) NULL DEFAULT NULL,
  `record_count` INT(11) NULL DEFAULT NULL ,
  `user_email` VARCHAR(255) NULL DEFAULT NULL ,
  `user_ip` VARCHAR(255) NULL DEFAULT NULL ,
  `version` BIGINT(20) NULL DEFAULT NULL ,
  `log_event_type_id` INT(11) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = latin1;


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
  CONSTRAINT `FK761AFDAC9EA6D187`
    FOREIGN KEY (`log_event_id` )
    REFERENCES `logger`.`log_event` (`id` ))
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



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
