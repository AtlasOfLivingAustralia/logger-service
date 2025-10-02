-- Assure the unique key of the event_summary_breakdown_email_entity table has been created before running this procedure
-- ALTER TABLE event_summary_breakdown_email_entity
--     ADD UNIQUE KEY uq_email_entity (
--     month,
--     log_event_type_id,
--     user_email_category,
--     entity_uid
--     );

CREATE DEFINER=`logger`@`%` PROCEDURE `process_event_summary_breakdown_email_entity`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    -- Drop temporary table if it already exists
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    -- Step 1: Aggregate log_detail data by month, event type, email category, and entity_uid
    CREATE TEMPORARY TABLE tmp_aggregated_results AS
    SELECT
        le.month AS month,
            le.log_event_type_id AS log_event_type_id,
            CASE
                WHEN le.user_email IS NULL OR le.user_email = '' THEN 'unspecified'
                WHEN le.user_email LIKE '%.edu%' THEN 'edu'
                WHEN le.user_email LIKE '%.ac.%' THEN 'edu'
                WHEN le.user_email LIKE '%.gov%' THEN 'gov'
                WHEN le.user_email LIKE '%csiro.au' THEN 'gov'
                ELSE 'other'
            END AS user_email_category,
            ld.entity_uid AS entity_uid,
            COUNT(ld.id) AS num_log_details,
            COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
        LEFT JOIN log_detail ld ON ld.log_event_id = le.id
        WHERE le.id BETWEEN p_start_id AND p_end_id AND entity_uid IS NOT NULL
        GROUP BY le.month, le.log_event_type_id, user_email_category, entity_uid;

    -- Step 2: Batch insert or update summary table
    INSERT INTO event_summary_breakdown_email_entity (
        month,
        log_event_type_id,
        user_email_category,
        entity_uid,
        number_of_events,
        record_count
    )
    SELECT
        month,
        log_event_type_id,
        user_email_category,
        entity_uid,
        num_log_details,
        total_record_count
    FROM tmp_aggregated_results
    ON DUPLICATE KEY UPDATE
                     number_of_events = number_of_events + VALUES(number_of_events),
                     record_count = record_count + VALUES(record_count);

    -- Cleanup
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    SELECT 'COMPLETED: event_summary_breakdown_email_entity', p_start_id, p_end_id;
END;