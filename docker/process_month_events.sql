--
-- Designed for debug and test purposes
--

CREATE DEFINER=`logger`@`%` PROCEDURE `process_month_events`(
    IN target_month INT,
    IN process_list VARCHAR(20) -- e.g. '0,2,4'
)
BEGIN
    DECLARE start_id INT;
    DECLARE end_id INT;
    DECLARE pos INT DEFAULT 1;
    DECLARE proc_index CHAR(1);

    -- Get ID range
    SELECT MIN(id) INTO start_id FROM log_event WHERE month = target_month;
    SELECT MAX(id) INTO end_id FROM log_event WHERE month = target_month;

    -- Debug output
    SELECT 'debug:' AS label, start_id AS start_id, end_id AS end_id;

    -- Loop through process_list
    WHILE pos <= CHAR_LENGTH(process_list) DO
        SET proc_index = SUBSTRING(process_list, pos, 1);

        CASE proc_index
            WHEN '0' THEN CALL logger.batch_process_event_summary_totals(start_id, end_id);
            WHEN '1' THEN CALL logger.batch_process_event_summary_breakdown_reason(start_id, end_id);
            WHEN '2' THEN CALL logger.batch_process_event_summary_breakdown_reason_entity(start_id, end_id);
            WHEN '3' THEN CALL logger.batch_process_event_summary_breakdown_reason_entity_source(start_id, end_id);
            WHEN '4' THEN CALL logger.batch_process_event_summary_breakdown_email(start_id, end_id);
            WHEN '5' THEN CALL logger.batch_process_event_summary_breakdown_email_entity(start_id, end_id);
        END CASE;

        SET pos = pos + 2; -- Skip comma
    END WHILE;
END