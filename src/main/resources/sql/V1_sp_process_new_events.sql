-- Incrementally process new event summaries based on the last processed event ID.
-- The ID stored in event_processing_checkpoint table is 1 due to a single-row design with a conventional, non-zero primary key
CREATE DEFINER=`logger`@`%` PROCEDURE `process_new_events`()
BEGIN
    DECLARE start_id INT;
    DECLARE end_id INT;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;
         -- Get last processed ID (default to 0 if not set)
        SELECT COALESCE(last_processed_event_id, 0) INTO start_id
        FROM event_processing_checkpoint
        WHERE id = 1;

        -- Get latest event ID
        SELECT MAX(id) INTO end_id FROM log_event;

        -- Process all summaries
        IF end_id > start_id + 1 THEN
            CALL process_event_summary_totals(start_id + 1, end_id);
            CALL process_event_summary_breakdown_reason(start_id + 1, end_id);
            CALL process_event_summary_breakdown_reason_entity(start_id + 1, end_id);
            CALL process_event_summary_breakdown_reason_entity_source(start_id + 1, end_id);
            CALL process_event_summary_breakdown_email(start_id + 1, end_id);
            CALL process_event_summary_breakdown_email_entity(start_id + 1, end_id);
            -- Update checkpoint
            INSERT INTO event_processing_checkpoint (id, last_processed_event_id)
            VALUES (1, end_id)
                ON DUPLICATE KEY UPDATE last_processed_event_id = end_id;
        END IF;
    COMMIT;
END