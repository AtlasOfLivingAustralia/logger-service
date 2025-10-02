-- Assure the unique key of the event_summary_breakdown_email table has been created before running this procedure
-- ALTER TABLE event_summary_breakdown_email
--     ADD UNIQUE KEY uq_month_email_eventTypeId (
--     month,
--     log_event_type_id,
--     user_email_category
--     );

CREATE DEFINER=`logger`@`%` PROCEDURE `process_event_summary_breakdown_email`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    -- Count events by month, event type, email category
    BEGIN
        -- Drop temporary table if it already exists
        DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_events;

        -- Step 1: Aggregate log_detail data by month, event type, email category, and entity prefix
        CREATE TEMPORARY TABLE tmp_aggregated_events AS
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
                    COUNT(DISTINCT le.id) AS total_number_of_events
                FROM log_event le
                LEFT JOIN log_detail ld ON ld.log_event_id = le.id
                WHERE le.id BETWEEN p_start_id AND p_end_id
                GROUP BY le.month, le.log_event_type_id, user_email_category;

        -- Step 2: Batch insert or update number_of_events
        INSERT INTO event_summary_breakdown_email (
            month,
            log_event_type_id,
            user_email_category,
            number_of_events,
            record_count
        )
        SELECT
            month,
            log_event_type_id,
            user_email_category,
            total_number_of_events,
            0
        FROM tmp_aggregated_events
        ON DUPLICATE KEY UPDATE
            number_of_events = number_of_events + total_number_of_events;
    End;

    BEGIN
        DROP TEMPORARY TABLE IF EXISTS tmp_sum_records;

        -- Step 1: Aggregate log_detail data by month, event type, email category, and entity prefix 'DR' only
        CREATE TEMPORARY TABLE tmp_sum_records AS
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
            LEFT(ld.entity_uid, 2) AS entity_prefix,
            COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
        LEFT JOIN log_detail ld ON ld.log_event_id = le.id
        WHERE le.id BETWEEN p_start_id AND p_end_id AND LEFT(ld.entity_uid, 2) = 'dr'
        GROUP BY le.month, le.log_event_type_id, user_email_category, entity_prefix;

        -- Step 2: Batch UPDATE total_record_count for 'dr' entity only
        UPDATE event_summary_breakdown_email est
            JOIN tmp_sum_records tmp
        ON est.month = tmp.month
            AND est.log_event_type_id = tmp.log_event_type_id
            AND est.user_email_category = tmp.user_email_category
        SET est.record_count = est.record_count + tmp.total_record_count;
        -- Cleanup
       DROP TEMPORARY TABLE IF EXISTS tmp_sum_records;
    End;

    SELECT 'COMPLETED: event_summary_breakdown_email', p_start_id, p_end_id;
END;