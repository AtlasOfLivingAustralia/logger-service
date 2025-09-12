
-- NOTE： It only updates the record_count for 'dr' entity, as per the existing trigger logic
-- It splits two processes. one for counting number_of_events, another for record_count of 'dr' entity only
-- They cannot combine into one process
-- When we count by event_id group by event_type_id, since an event_id may have multiple event_type_id, the count may be duplicated
CREATE DEFINER=`logger`@`%` PROCEDURE `batch_process_event_summary_totals`(
    IN p_start_id BIGINT,
    IN p_end_id BIGINT
)
BEGIN
    DECLARE total_log_events_to_process BIGINT DEFAULT 0;
    -- v_ variables are written into the temporary table
    DECLARE v_month INT;
    DECLARE v_event_type_id INT;
    DECLARE v_entity_prefix VARCHAR(2); -- e.g., 'dr', 'co', etc.
    DECLARE v_num_events BIGINT;
    DECLARE v_total_records BIGINT;

    -- Drop temporary table if it already exists
	DROP TEMPORARY TABLE IF EXISTS tmp_log_event_summary;
    DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;


    -- START: Count log_event (excludes those without log_details) based on month and event type(e.g 1002)
    CREATE TEMPORARY TABLE tmp_log_event_summary (
        month INT,
        log_event_type_id INT,
        num_log_event INT
    );

   INSERT INTO tmp_log_event_summary (month, log_event_type_id, num_log_event)
       SELECT
            le.month AS month,
            le.log_event_type_id AS log_event_type_id,
            COUNT(le.id) AS num_log_event
        FROM log_event le
        WHERE le.id >= p_start_id
          AND le.id <= p_end_id
          AND EXISTS (
            SELECT 1
            FROM log_detail ld
            WHERE ld.log_event_id = le.id
            )
        GROUP BY le.month, le.log_event_type_id
        ORDER BY le.month, le.log_event_type_id;
        BEGIN
            DECLARE done_summary INT DEFAULT 0;
            DECLARE cur_summary CURSOR FOR
                SELECT month, log_event_type_id, num_log_event FROM tmp_log_event_summary;
            DECLARE CONTINUE HANDLER FOR NOT FOUND SET done_summary = 1;
            OPEN cur_summary;
                read_loop: LOOP
                    SET done_summary = 0;
                    FETCH cur_summary INTO v_month, v_event_type_id, v_num_events;
                    IF done_summary THEN
                        LEAVE read_loop;
                    END IF;
                         IF EXISTS (
                                SELECT 1
                                FROM event_summary_totals
                                WHERE month = v_month
                                  AND log_event_type_id = v_event_type_id
                            )
                        THEN
                            UPDATE event_summary_totals
                            SET number_of_events = number_of_events + v_num_events
                            WHERE month = v_month
                              AND log_event_type_id = v_event_type_id;
                        ELSE
                            INSERT INTO event_summary_totals (
                                month, log_event_type_id, number_of_events, record_count
                            )
                            VALUES (v_month, v_event_type_id, v_num_events,0);
                        END IF;
                 END LOOP;
            CLOSE cur_summary;
        END;
    -- END: Count log_event (excludes those without log_details) based on month and event type(e.g 1002)

    -- 2. START
    -- sum of total_records ('dr entity type only') per event type and month
    -- for log events in the specified ID range
    CREATE TEMPORARY TABLE tmp_aggregated_results AS
		SELECT
			le.month AS month,
						le.log_event_type_id AS log_event_type_id,
						LEFT(ld.entity_uid, 2) AS entity_prefix,
						COUNT(le.id) AS num_log_event,
						COALESCE(SUM(ld.record_count), 0) AS total_record_count
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
		GROUP BY le.month, le.log_event_type_id, entity_prefix
		ORDER BY le.log_event_type_id, le.month;

		-- 3. Open cursor and loop through each row
		BEGIN
		    DECLARE done INT DEFAULT 0;
            DECLARE cur CURSOR FOR
                SELECT month, log_event_type_id,entity_prefix, num_log_event, total_record_count
                FROM tmp_aggregated_results;
            DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
            OPEN cur;
                read_loop: LOOP
                        FETCH cur INTO v_month, v_event_type_id, v_entity_prefix, v_num_events, v_total_records;
                        IF done THEN
                            LEAVE read_loop;
                        END IF;

                                -- todo: need to be clarified
                                -- The current 'trigger' method updates the number of records for 'dr' entity twice
                                -- We believe it intention to  update the number of records of 'dr' ONLY
                                -- Check line 110 as below in the trigger code
                                --                      IF NEW.entity_uid LIKE 'dr%' THEN
                                --                             UPDATE event_summary_totals est SET record_count = record_count + NEW.record_count
                                --                      WHERE est.month = new_month AND est.log_event_type_id = new_log_event_type_id;
                                --                        END IF;

                            -- Update the record_count for 'dr' entity only once
                            IF v_entity_prefix = 'dr' THEN
                                UPDATE event_summary_totals est SET record_count = record_count + v_total_records
                                    WHERE est.month = v_month AND est.log_event_type_id = v_event_type_id;
                            END IF;
                        END LOOP;
            CLOSE cur;
            DROP TEMPORARY TABLE IF EXISTS tmp_aggregated_results;
        END;
    SELECT "COMPLETED: event_summary_breakdown_total", p_start_id, p_end_id;
END
