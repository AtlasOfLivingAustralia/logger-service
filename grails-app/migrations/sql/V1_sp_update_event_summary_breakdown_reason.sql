-- Assure the unique key of the event_summary_breakdown_reason table has been created before running this procedure
-- ALTER TABLE event_summary_breakdown_reason
--     ADD UNIQUE KEY uq_breakdown_reason (
--     month,
--     log_event_type_id,
--     log_reason_type_id
--     );

CREATE DEFINER=`logger`@`%` PROCEDURE `process_event_summary_breakdown_reason`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    SELECT "DEBUG: event_summary_breakdown_reason", p_start_id, p_end_id;

    -- Cleanup any previous temp tables
    DROP TEMPORARY TABLE IF EXISTS tmp_log_event_summary;
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    -- Phase 1: Count number_of_events per (month, event_type, reason_type)
    CREATE TEMPORARY TABLE tmp_log_event_summary (
        month INT,
        log_event_type_id INT,
        log_reason_type_id INT,
        num_log_event BIGINT
    );

    INSERT INTO tmp_log_event_summary (month, log_event_type_id, log_reason_type_id, num_log_event)
        SELECT
            le.month,
            le.log_event_type_id,
            COALESCE(le.log_reason_type_id, -1),
            COUNT(le.id)
        FROM log_event le
        WHERE le.id BETWEEN p_start_id AND p_end_id
          AND EXISTS (
            SELECT 1 FROM log_detail ld WHERE ld.log_event_id = le.id
        )
        GROUP BY le.month, le.log_event_type_id, le.log_reason_type_id;

    -- Batch insert/update number_of_events
    INSERT INTO event_summary_breakdown_reason (
        month, log_event_type_id, log_reason_type_id, number_of_events, record_count)
        SELECT
            month, log_event_type_id, log_reason_type_id, num_log_event, 0
        FROM tmp_log_event_summary
        ON DUPLICATE KEY UPDATE
            number_of_events = number_of_events + VALUES(number_of_events);

    -- Phase 2: Update record_count for 'dr' entity only
    CREATE TEMPORARY TABLE tmp_aggregated_results AS
        SELECT
            le.month,
            le.log_event_type_id,
            COALESCE(le.log_reason_type_id, -1) AS log_reason_type_id,
            LEFT(ld.entity_uid, 2) AS entity_prefix,
            COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
            LEFT JOIN log_detail ld ON ld.log_event_id = le.id
        WHERE le.id BETWEEN p_start_id AND p_end_id
        GROUP BY le.month, le.log_event_type_id, le.log_reason_type_id, entity_prefix;

    -- Batch update record_count for 'dr' entity only
    UPDATE event_summary_breakdown_reason est
        JOIN tmp_aggregated_results tmp
    ON est.month = tmp.month
        AND est.log_event_type_id = tmp.log_event_type_id
        AND est.log_reason_type_id = tmp.log_reason_type_id
        SET est.record_count = est.record_count + tmp.total_record_count
    WHERE tmp.entity_prefix = 'dr';

    -- Cleanup
    DROP TEMPORARY TABLE IF EXISTS tmp_log_event_summary;
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    SELECT "COMPLETED: event_summary_breakdown_reason", p_start_id, p_end_id;
END;
