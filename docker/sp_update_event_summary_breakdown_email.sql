--Note: it only updates the total number of events and record counts for 'dr' entity only as per the existing trigger logic
CREATE DEFINER=`logger`@`%` PROCEDURE `batch_process_event_summary_breakdown_email`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    DECLARE done INT DEFAULT 0;
    -- v_ variables are written into the temporary table
    DECLARE v_month INT;
    DECLARE v_event_type_id INT;
    DECLARE v_user_email_category VARCHAR(5);
    DECLARE v_entity_prefix VARCHAR(2); -- e.g., 'dr', 'co', etc.
    DECLARE v_num_events INT;
    DECLARE v_total_records INT;

    -- output variables for debugging
    DECLARE current_number_of_events INT DEFAULT 0;
    DECLARE current_record_count INT DEFAULT 0;
    DECLARE updated_number_of_events INT DEFAULT 0;
    DECLARE updated_record_count INT DEFAULT 0;

    -- Declare cursor for iterating over a temporary database stored the aggregated results
    DECLARE cur CURSOR FOR
        SELECT month, log_event_type_id, user_email_category, entity_prefix, num_log_details, total_record_count
        FROM tmp_aggregated_results;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    SELECT "DEBUG: event_summary_breakdown_email", p_start_id, p_end_id;

    -- Drop temporary table if it already exists
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;

    -- 2. Create temporary table with
    -- aggregated counts of log details and sum of total_records per event type and month
    -- for log events in the specified ID range
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
			LEFT(ld.entity_uid, 2) AS entity_prefix,
			COUNT(ld.id) AS num_log_details,
			COALESCE(SUM(ld.record_count), 0) AS total_record_count
        FROM log_event le
            LEFT JOIN log_detail ld
        ON ld.log_event_id = le.id
        WHERE le.id >= p_start_id
          AND le.id <= p_end_id
        GROUP BY le.month,
              le.log_event_type_id,
              user_email_category,
              entity_prefix
        ORDER BY le.log_event_type_id, le.month;

    -- 3. Open cursor and loop through each row
    OPEN cur;
        read_loop: LOOP
                SET done = 0; -- IMPORTANT: reset done flag for each loop, otherwise it will stop after the last iteration
                FETCH cur INTO v_month, v_event_type_id, v_user_email_category, v_entity_prefix, v_num_events, v_total_records;
                IF done THEN
                    LEAVE read_loop;
                END IF;

                SELECT number_of_events, record_count into current_number_of_events, current_record_count
                FROM event_summary_breakdown_email
                WHERE month = v_month
                  AND log_event_type_id = v_event_type_id
                  AND user_email_category = v_user_email_category;

                -- Update the summary table
                IF EXISTS (
                    SELECT 1
                    FROM event_summary_breakdown_email
                    WHERE month = v_month
                      AND log_event_type_id = v_event_type_id
                      AND user_email_category = v_user_email_category
                ) THEN
                    UPDATE event_summary_breakdown_email
                    SET number_of_events = number_of_events + v_num_events
                    WHERE month = v_month
                      AND log_event_type_id = v_event_type_id
                      AND user_email_category = v_user_email_category;
                ELSE
                    INSERT INTO event_summary_breakdown_email (
                        month, log_event_type_id, user_email_category, number_of_events,record_count
                    )
                    VALUES (v_month, v_event_type_id, v_user_email_category, v_num_events,0);
                END IF;
                
                IF v_entity_prefix = 'dr' THEN
                    UPDATE event_summary_breakdown_email est SET record_count = record_count + v_total_records
                        WHERE month = v_month
                          AND log_event_type_id = v_event_type_id
                          AND user_email_category = v_user_email_category;
                END IF;
                
                -- Print the current event summary
                SELECT number_of_events, record_count into updated_number_of_events, updated_record_count
                FROM event_summary_breakdown_email
                WHERE month = v_month
                  AND log_event_type_id = v_event_type_id
                  AND user_email_category = v_user_email_category;

                SELECT
                    'Debug:' AS message,
                    v_month AS month,
                    v_event_type_id AS log_event_type_id,
                    v_user_email_category AS user_email_category,
                    current_number_of_events As previoius_of_events,
                    current_record_count As previous_record_count,
                    updated_number_of_events AS updated_number_of_events,
                    updated_record_count AS updated_record_count;
                END
        LOOP;

    CLOSE cur;

    -- drop temporary table at the end
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;
END