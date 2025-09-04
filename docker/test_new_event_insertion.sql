-- Insert a new log event and corresponding log detail for testing - TARGETTING 202509
-- It approved that the current tigger will double count the record_count in event_summary_totals table.

-- Step 1: Delete log_detail records linked to log_event entries from month 202509
SET @target_month = 202601;

delete FROM logger.event_summary_totals where month= @target_month;
delete FROM logger.event_summary_breakdown_reason where month= @target_month;
delete FROM logger.event_summary_breakdown_reason_entity where month= @target_month;
delete FROM logger.event_summary_breakdown_email where month= @target_month;

DELETE FROM log_detail
WHERE log_event_id IN (
    SELECT id FROM log_event WHERE month = @target_month
);

-- Step 2: Delete log_event records for month 202509
DELETE FROM log_event
WHERE month = @target_month;

-- NEW event 1

INSERT INTO `log_event` (
    `comment`,
    `created`,
    `log_event_type_id`,
    `month`,
    `user_email`,
    `user_ip`,
    `source`,
    `user_agent`,
    `log_reason_type_id`,
    `log_source_type_id`,
    `source_url`
) VALUES (
             '1 test-BAI',
             '2025-09-01 09:51:41',
             1002,
             @target_month,
             'qifeng.bai@csiro.au',
             '127.0.0.1',
             'aws-biocache-service-test-2025.test.ala.org.au',
             'ala-hub/8.1.0-SNAPSHOT',
             10,
             0,
             'https://biocache-ws.test.ala.org.au/...'
         );

-- 2. Capture the generated ID
SET @last_log_event_id = LAST_INSERT_ID();

-- 3. Insert into log_detail using the generated ID
-- insert dr
INSERT INTO `log_detail` (
    `entity_type`,
    `entity_uid`,
    `record_count`,
    `log_event_id`
) VALUES (
             1002,
             'dr1000',
             100,
             @last_log_event_id
         );
INSERT INTO `log_detail` (
    `entity_type`,
    `entity_uid`,
    `record_count`,
    `log_event_id`
) VALUES (
             1002,
             'dr1001',
             100,
             @last_log_event_id
         );
         
INSERT INTO `log_detail` (
    `entity_type`,
    `entity_uid`,
    `record_count`,
    `log_event_id`
) VALUES (
             1002,
             'dr1002',
             100,
             @last_log_event_id
         );
         
-- insert co         
INSERT INTO `log_detail` (
    `entity_type`,
    `entity_uid`,
    `record_count`,
    `log_event_id`
) VALUES (
             1002,
             'co100',
             30,
             @last_log_event_id
         );
         
INSERT INTO `log_detail` (
    `entity_type`,
    `entity_uid`,
    `record_count`,
    `log_event_id`
) VALUES (
             1002,
             'co101',
             30,
             @last_log_event_id
         );
         
-- New Event insert again         
--
--       
INSERT INTO `log_event` (
    `comment`,
    `created`,
    `log_event_type_id`,
    `month`,
    `user_email`,
    `user_ip`,
    `source`,
    `user_agent`,
    `log_reason_type_id`,
    `log_source_type_id`,
    `source_url`
) VALUES (
             '2 test -BAI',
             '2025-09-01 09:51:41',
             2000,
             @target_month,
             'qifeng.bai@anu.edu.au',
             '127.0.0.1',
             'aws-biocache-service-test-2025.test.ala.org.au',
             'ala-hub/8.1.0-SNAPSHOT',
             1,
             0,
             'https://biocache-ws.test.ala.org.au/...'
         );

-- 2. Capture the generated ID
SET @last_log_event_id = LAST_INSERT_ID();

-- 3. Insert into log_detail using the generated ID
-- insert dr
INSERT INTO `log_detail` (
    `entity_type`,
    `entity_uid`,
    `record_count`,
    `log_event_id`
) VALUES (
             2000,
             'dr1000',
             25,
             @last_log_event_id
         );
--
-- Aggragation for event_summary_totals      
--
SELECT
	le.month AS month,
				le.log_event_type_id AS log_event_type_id,
				LEFT(ld.entity_uid, 2) AS entity_prefix,
				COUNT(ld.id) AS num_log_details,
				COALESCE(SUM(ld.record_count), 0) AS total_record_count
FROM log_event le
	LEFT JOIN log_detail ld
ON ld.log_event_id = le.id
WHERE le.month = @target_month
GROUP BY le.month, le.log_event_type_id, LEFT(ld.entity_uid, 2)
ORDER BY le.log_event_type_id, le.month;

--
-- Aggragation for event_summary_breakdown_reason
--
SELECT
	le.month AS month,
				le.log_event_type_id AS log_event_type_id,
                le.log_reason_type_id AS log_reason_type_id,
				LEFT(ld.entity_uid, 2) AS entity_prefix,
				COUNT(ld.id) AS num_log_details,
				COALESCE(SUM(ld.record_count), 0) AS total_record_count
FROM log_event le
	LEFT JOIN log_detail ld
ON ld.log_event_id = le.id
WHERE le.month = @target_month
GROUP BY le.month, le.log_event_type_id, le.log_reason_type_id,LEFT(ld.entity_uid, 2)
ORDER BY le.log_event_type_id, le.month;