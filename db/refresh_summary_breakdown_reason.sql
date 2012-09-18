-- This stored procedure is used to populate the "event_summary_breakdown_reason" and "event_summary_breakdown_reason_entity" tables
-- from all existing log information
delimiter $$

CREATE DEFINER=`root`@`%` PROCEDURE `refresh_summary_breakdown_reason`()
BEGIN
DECLARE current_month varchar(255);
DECLARE done INT DEFAULT 0;
DECLARE cur1 CURSOR FOR SELECT DISTINCT month from log_event;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
OPEN cur1;

DELETE FROM event_summary_breakdown_reason;
DELETE FROM event_summary_breakdown_reason_entity;

the_loop: LOOP
	FETCH cur1 into current_month;

	IF done THEN
		CLOSE cur1;
		LEAVE the_loop;
	END IF;

	-- print progress
	SELECT current_month;

    -- Insert into table for breakdown by reason only
	INSERT INTO event_summary_breakdown_reason (month, log_event_type_id, log_reason_type_id, number_of_events, record_count)
		SELECT le.month, le.log_event_type_id, IFNULL(le.log_reason_type_id, -1), COUNT(DISTINCT ld.log_event_id), SUM(ld.record_count) FROM log_event le INNER JOIN log_detail ld  ON le.id=ld.log_event_id
		WHERE
		le.month = current_month
		GROUP BY le.month, le.log_event_type_id, le.log_reason_type_id;

    -- Insert into table for breakdown by reason and entity
	INSERT INTO event_summary_breakdown_reason_entity (month, log_event_type_id, log_reason_type_id, entity_uid, number_of_events, record_count)
		SELECT le.month, le.log_event_type_id, IFNULL(le.log_reason_type_id, -1), ld.entity_uid, COUNT(DISTINCT ld.log_event_id), SUM(ld.record_count) FROM log_event le INNER JOIN log_detail ld  ON le.id=ld.log_event_id
		WHERE
		le.month = current_month
		GROUP BY le.month, ld.entity_uid, le.log_event_type_id, le.log_reason_type_id;

END LOOP the_loop;

END$$

