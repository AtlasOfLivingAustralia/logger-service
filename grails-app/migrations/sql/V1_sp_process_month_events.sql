--
-- Reset the event summary tables and reprocess/aggregate the events for a given month.
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

    -- Loop through process_list
    WHILE pos <= CHAR_LENGTH(process_list) DO
        SET proc_index = SUBSTRING(process_list, pos, 1);

        CASE proc_index
            WHEN '0' THEN
                BEGIN
                    DELETE FROM event_summary_totals WHERE month = target_month;
                    CALL logger.process_event_summary_totals(start_id, end_id);
                    SELECT * FROM event_summary_totals WHERE month = target_month;
                END;
            WHEN '1' THEN
                BEGIN
                    DELETE FROM event_summary_breakdown_reason WHERE month = target_month;
                    CALL logger.process_event_summary_breakdown_reason(start_id, end_id);
                    SELECT * FROM event_summary_breakdown_reason WHERE month = target_month;
                END;
            WHEN '2' THEN
                BEGIN
                    DELETE FROM event_summary_breakdown_reason_entity WHERE month = target_month;
                    CALL logger.process_event_summary_breakdown_reason_entity(start_id, end_id);
                    SELECT * FROM event_summary_breakdown_reason_entity WHERE month = target_month;
                END;
            WHEN '3' THEN
                BEGIN
                    DELETE FROM event_summary_breakdown_reason_entity_source WHERE month = target_month;
                    CALL logger.process_event_summary_breakdown_reason_entity_source(start_id, end_id);
                    SELECT * FROM event_summary_breakdown_reason_entity_source WHERE month = target_month;
                END;
            WHEN '4' THEN
                BEGIN
                    DELETE FROM event_summary_breakdown_email WHERE month = target_month;
                    CALL logger.process_event_summary_breakdown_email(start_id, end_id);
                    SELECT * FROM event_summary_breakdown_email WHERE month = target_month;
                END;
            WHEN '5' THEN
                BEGIN
                    DELETE FROM event_summary_breakdown_email_entity WHERE month = target_month;
                    CALL logger.process_event_summary_breakdown_email_entity(start_id, end_id);
                    SELECT * FROM event_summary_breakdown_email_entity WHERE month = target_month;
                END;
            ELSE
                    -- Ignore unknown index
                 SELECT CONCAT('Ignoring unknown process index: ', proc_index) AS debug_message;
            END CASE;

        SET pos = pos + 2; -- Skip comma
    END WHILE;
END;