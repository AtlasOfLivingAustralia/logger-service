CREATE DEFINER=`logger`@`%` PROCEDURE `batch_process_event_summary_breakdown_reason`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN

    -- v_ variables are written into the temporary table
    DECLARE v_month INT;
    DECLARE v_event_type_id INT;
    DECLARE v_reason_type_id INT;
    DECLARE v_entity_prefix VARCHAR(2); -- e.g., 'dr', 'co', etc.
    DECLARE v_num_events BIGINT;
    DECLARE v_total_records BIGINT;

    SELECT "DEBUG: event_summary_breakdown_reason", p_start_id, p_end_id;
    -- Drop temporary table if it already exists
    DROP TEMPORARY TABLE IF EXISTS tmp_log_event_summary;
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    -- START: Count log_event (excludes those without log_details) based on month and event type(e.g 1002)
    CREATE TEMPORARY TABLE tmp_log_event_summary (
        month INT,
        log_event_type_id INT,
        log_reason_type_id INT,
        num_log_event BIGINT
    );

    INSERT INTO tmp_log_event_summary (month, log_event_type_id,log_reason_type_id, num_log_event)
    SELECT
        le.month AS month,
            le.log_event_type_id AS log_event_type_id,
            COALESCE(le.log_reason_type_id, -1) AS log_reason_type_id,
            COUNT(le.id) AS num_log_event
    FROM log_event le
    WHERE le.id >= p_start_id
      AND le.id <= p_end_id
      AND EXISTS (
        SELECT 1
        FROM log_detail ld
        WHERE ld.log_event_id = le.id
        )
    GROUP BY le.month, le.log_event_type_id,log_reason_type_id
    ORDER BY le.month, le.log_event_type_id,log_reason_type_id;
    BEGIN
        DECLARE done_summary INT DEFAULT 0;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_summary = 1;
        DECLARE cur_summary CURSOR FOR
           SELECT month, log_event_type_id,log_reason_type_id, num_log_event FROM tmp_log_event_summary;
        OPEN cur_summary;
            read_loop: LOOP
                FETCH cur_summary INTO v_month, v_event_type_id, v_reason_type_id, v_num_events;
                IF done_summary THEN
                        LEAVE read_loop;
                END IF;
                 IF EXISTS (
                        SELECT 1
                        FROM event_summary_breakdown_reason
                        WHERE month = v_month
                          AND log_event_type_id = v_event_type_id
                          AND log_reason_type_id = v_reason_type_id
                    )
                THEN
                    UPDATE event_summary_breakdown_reason
                    SET number_of_events = number_of_events + v_num_events
                    WHERE month = v_month
                      AND log_event_type_id = v_event_type_id
                      AND log_reason_type_id = v_reason_type_id;
                ELSE
                    INSERT INTO event_summary_breakdown_reason (
                        month, log_event_type_id, log_reason_type_id, number_of_events, record_count
                    )
                    VALUES (v_month, v_event_type_id, v_reason_type_id, v_num_events,0);
                END IF;
            END LOOP;
        CLOSE cur_summary;
    END;
-- END: Count log_event (excludes those without log_details) based on month and event type(e.g 1002)


    -- 2. Create temporary table with
    -- aggregated counts of log details and sum of total_records per event type and month
    -- for log events in the specified ID range
    CREATE TEMPORARY TABLE tmp_aggregated_results AS
    SELECT
        le.month AS month,
                le.log_event_type_id AS log_event_type_id,
                COALESCE(le.log_reason_type_id, -1) AS log_reason_type_id,
                LEFT(ld.entity_uid, 2) AS entity_prefix,
                COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
            LEFT JOIN log_detail ld
        ON ld.log_event_id = le.id
        WHERE le.id >= p_start_id
          AND le.id <= p_end_id
        GROUP BY le.month, le.log_event_type_id, le.log_reason_type_id, entity_prefix
        ORDER BY le.log_event_type_id, le.month, le.log_reason_type_id;
    SELECT COUNT(*) AS AGGREATED_RESULT FROM tmp_aggregated_results;
    -- 3. Open cursor and loop through each row
    BEGIN
        DECLARE done INT DEFAULT 0;
        DECLARE cur CURSOR FOR
            SELECT month, log_event_type_id, log_reason_type_id, entity_prefix, total_record_count
            FROM tmp_aggregated_results;
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
        OPEN cur;
              read_loop: LOOP
                FETCH cur INTO v_month, v_event_type_id, v_reason_type_id, v_entity_prefix, v_total_records;
                IF done THEN
                    LEAVE read_loop;
                END IF;

                IF v_entity_prefix = 'dr' THEN
                    UPDATE event_summary_breakdown_reason est SET record_count = record_count + v_total_records
                        WHERE month = v_month
                          AND log_event_type_id = v_event_type_id
                          AND log_reason_type_id = v_reason_type_id;
                END IF;
             END LOOP;
        CLOSE cur;
        DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;
    END;
END