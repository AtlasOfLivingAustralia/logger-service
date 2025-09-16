--
--  ALTER TABLE event_summary_breakdown_reason_entity_source
--     ADD UNIQUE KEY uq_reason_entity_source (
--     month,
--     log_event_type_id,
--     log_reason_type_id,
--     entity_uid,
--     log_source_type_id
--     );

DELIMITER $$
CREATE DEFINER=`logger`@`%` PROCEDURE `batch_process_event_summary_breakdown_reason_entity_source`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    DECLARE updated_number_of_events INT DEFAULT 0;
    DECLARE updated_record_count INT DEFAULT 0;

    SELECT "DEBUG: event_summary_breakdown_reason_entity_source", p_start_id, p_end_id;

    -- Drop temp table if it exists
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    -- Aggregate log details and record counts
    CREATE TEMPORARY TABLE tmp_aggregated_results AS
        SELECT
            le.month,
            le.log_event_type_id,
            COALESCE(le.log_reason_type_id, -1) AS log_reason_type_id,
            ld.entity_uid,
            COALESCE( le.log_source_type_id, -1) AS  log_source_type_id,
            COUNT(DISTINCT ld.id) AS num_log_details,
            COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
                 LEFT JOIN log_detail ld ON ld.log_event_id = le.id
        WHERE le.id BETWEEN p_start_id AND p_end_id
          AND ld.entity_uid IS NOT NULL
        GROUP BY le.month, le.log_event_type_id, le.log_reason_type_id, ld.entity_uid, le.log_source_type_id;

    -- Batch insert/update into summary table
    INSERT INTO event_summary_breakdown_reason_entity_source (
        month,
        log_event_type_id,
        log_reason_type_id,
        entity_uid,
        log_source_type_id,
        number_of_events,
        record_count )
        SELECT
            month,
            log_event_type_id,
            log_reason_type_id,
            entity_uid,
            log_source_type_id,
            num_log_details,
            total_record_count
        FROM tmp_aggregated_results
        ON DUPLICATE KEY UPDATE
                             number_of_events = number_of_events + VALUES(number_of_events),
                             record_count = record_count + VALUES(record_count);

    -- Cleanup
    DROP TABLE tmp_aggregated_results;
END $$
DELIMITER ;