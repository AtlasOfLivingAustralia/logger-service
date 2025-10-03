-- NOTE: Assure a unique key of the event_summary_breakdown_reason_entity table has been created before running this procedure
--
-- ALTER TABLE event_summary_breakdown_reason_entity
--     ADD UNIQUE KEY uq_summary_entity (
--     month,
--     log_event_type_id,
--     log_reason_type_id,
--     entity_uid
--     );

CREATE PROCEDURE `process_event_summary_breakdown_reason_entity`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN

    SELECT "STARTED: event_summary_breakdown_reason_entity", p_start_id, p_end_id;
    -- Drop temporary table if it already exists
    DROP TEMPORARY TABLE IF EXISTS tmp_log_event_summary;
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

     -- START: Count log_event (excludes those without log_details) based on month and event type(e.g 1002)
    BEGIN
         CREATE TEMPORARY TABLE tmp_log_event_summary (
            month INT,
            log_event_type_id INT,
            log_reason_type_id INT,
            entity_uid VARCHAR(255),
            num_log_event BIGINT
        );

        INSERT INTO tmp_log_event_summary (month, log_event_type_id,log_reason_type_id, entity_uid, num_log_event)
        SELECT
            le.month AS month,
                le.log_event_type_id AS log_event_type_id,
                COALESCE(le.log_reason_type_id, -1) AS log_reason_type_id,
                ld.entity_uid AS entity_uid,
                COUNT(DISTINCT le.id) AS num_log_event
        FROM log_event le
            LEFT JOIN log_detail ld
            ON ld.log_event_id = le.id
        WHERE le.id >= p_start_id
          AND le.id <= p_end_id
          AND EXISTS (
            SELECT 1
            FROM log_detail ld
            WHERE ld.log_event_id = le.id
            )
        GROUP BY le.month, le.log_event_type_id,log_reason_type_id, entity_uid
        ORDER BY le.month, le.log_event_type_id,log_reason_type_id, entity_uid;

        -- Upsert into summary table
         -- Using ON DUPLICATE KEY UPDATE to handle existing records
         -- Assumes a unique constraint on (month, log_event_type_id, log_reason_type_id, entity_uid)
        INSERT INTO event_summary_breakdown_reason_entity (
            month,
            log_event_type_id,
            log_reason_type_id,
            entity_uid,
            number_of_events,
            record_count
        )
        SELECT
            month,
            log_event_type_id,
            log_reason_type_id,
            entity_uid,
            num_log_event,
            0
        FROM tmp_log_event_summary
        ON DUPLICATE KEY UPDATE
                     number_of_events = number_of_events + VALUES(number_of_events);
        SELECT COUNT(*) AS event_record_inserted FROM tmp_log_event_summary;
    END;
    -- END: Count log_event (excludes those without log_details) based on month and event type(e.g 1002)


    -- 2. Create temporary table with
    -- aggregated counts of log details and sum of total_records per event type and month
    -- for log events in the specified ID range
    BEGIN
        CREATE TEMPORARY TABLE tmp_aggregated_results AS
            SELECT
                le.month AS month,
                le.log_event_type_id AS log_event_type_id,
                COALESCE(le.log_reason_type_id, -1) AS log_reason_type_id,
                ld.entity_uid AS entity_uid,
                COALESCE(SUM(ld.record_count), 0) AS total_record_count
            FROM log_event le
                LEFT JOIN log_detail ld
            ON ld.log_event_id = le.id
            WHERE le.id >= p_start_id
              AND le.id <= p_end_id
            GROUP BY le.month, log_event_type_id, log_reason_type_id, entity_uid
            ORDER BY le.log_event_type_id, le.month;

        -- Batch update using JOIN
        UPDATE event_summary_breakdown_reason_entity est
            JOIN tmp_aggregated_results tmp
        ON est.month = tmp.month
            AND est.log_event_type_id = tmp.log_event_type_id
            AND est.log_reason_type_id = tmp.log_reason_type_id
            AND est.entity_uid = tmp.entity_uid
            SET est.record_count = est.record_count + tmp.total_record_count;

        -- drop temporary table at the end
        DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;
    END;
    SELECT "COMPLETED: event_summary_breakdown_reason_entity", p_start_id, p_end_id;
END;