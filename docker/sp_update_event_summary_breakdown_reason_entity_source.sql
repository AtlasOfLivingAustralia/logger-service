CREATE DEFINER=`logger`@`%` PROCEDURE `batch_process_event_summary_breakdown_reason_entity_source`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    DECLARE done INT DEFAULT 0;
    -- v_ variables are written into the temporary table
    DECLARE v_month INT;
    DECLARE v_event_type_id INT;
    DECLARE v_reason_type_id INT;
    DECLARE v_entity_uid VARCHAR(10); -- e.g. 'dr1000'
    DECLARE v_log_source_type_id INT;
    DECLARE v_num_events INT;
    DECLARE v_total_records INT;

    -- output variables for debugging
    DECLARE current_number_of_events INT DEFAULT 0;
    DECLARE current_record_count INT DEFAULT 0;
    DECLARE updated_number_of_events INT DEFAULT 0;
    DECLARE updated_record_count INT DEFAULT 0;

    -- Declare cursor for iterating over a temporary database stored the aggregated results
    DECLARE cur CURSOR FOR
        SELECT month, log_event_type_id, log_reason_type_id, entity_uid, log_source_type_id, num_log_details, total_record_count
        FROM tmp_aggregated_results;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

   SELECT "DEBUG: event_summary_breakdown_reason_entity_source", p_start_id, p_end_id;

    -- Drop temporary table if it already exists
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    -- 2. Create temporary table with
    -- aggregated counts of log details and sum of total_records per event type and month
    -- for log events in the specified ID range
    CREATE TEMPORARY TABLE tmp_aggregated_results AS
        SELECT
			le.month AS month,
			le.log_event_type_id AS log_event_type_id,
			le.log_reason_type_id AS log_reason_type_id,
			ld.entity_uid AS entity_uid,
			le.log_source_type_id AS log_source_type_id,
			COUNT(ld.id) AS num_log_details,
			COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
            LEFT JOIN log_detail ld
        ON ld.log_event_id = le.id
        WHERE le.id >= p_start_id
          AND le.id <= p_end_id
        GROUP BY le.month, log_event_type_id, log_reason_type_id, entity_uid, log_source_type_id
        ORDER BY le.log_event_type_id, le.month;

    -- 3. Open cursor and loop through each row
    OPEN cur;
        read_loop: LOOP
                SET done = 0; -- IMPORTANT: reset done flag for each loop, otherwise it will stop after the last iteration
                FETCH cur INTO v_month, v_event_type_id, v_reason_type_id, v_entity_uid, v_log_source_type_id, v_num_events, v_total_records;
                IF done THEN
                    LEAVE read_loop;
                END IF;

                -- Update the summary table
                IF EXISTS (
                    SELECT 1
                    FROM event_summary_breakdown_reason_entity_source
                    WHERE month = v_month
                      AND log_event_type_id = v_event_type_id
                      AND log_reason_type_id = v_reason_type_id
                      AND entity_uid = v_entity_uid
                      AND log_source_type_id = v_log_source_type_id
                ) THEN
                    UPDATE event_summary_breakdown_reason_entity_source
                    SET number_of_events = number_of_events + v_num_events,
                        record_count = record_count + v_total_records
                    WHERE month = v_month
                      AND log_event_type_id = v_event_type_id
                      AND log_reason_type_id = v_reason_type_id
                      AND entity_uid = v_entity_uid
                      AND log_source_type_id = v_log_source_type_id;
                ELSE
                    INSERT INTO event_summary_breakdown_reason_entity_source (
                        month, log_event_type_id, log_reason_type_id, entity_uid, log_source_type_id, number_of_events,record_count
                    )
                    VALUES (v_month, v_event_type_id, v_reason_type_id, v_entity_uid,v_log_source_type_id, v_num_events,v_total_records);
                END IF;

                -- Print the current event summary
                SELECT number_of_events, record_count into updated_number_of_events, updated_record_count
                FROM event_summary_breakdown_reason_entity_source
                WHERE month = v_month
                  AND log_event_type_id = v_event_type_id
                  AND log_reason_type_id = v_reason_type_id
                  AND entity_uid = v_entity_uid
                  AND log_source_type_id = v_log_source_type_id;
            END
        LOOP;

    CLOSE cur;

    -- drop temporary table at the end
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;
END