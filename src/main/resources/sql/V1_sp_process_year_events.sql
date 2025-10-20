-- It is used to re-aggregate event data for each month of a specified year.
-- It is designed as a manual operation to be run by a DBA or developer when needed.
CREATE PROCEDURE process_year_events(IN year_val INT)
BEGIN
  DECLARE month_idx INT DEFAULT 1;
  DECLARE month_str VARCHAR(6);

  WHILE month_idx <= 12 DO
    SET month_str = CONCAT(year_val, LPAD(month_idx, 2, '0'));
    SELECT CONCAT('Re-aggregating events for month: ', month_str);
    CALL process_month_events(month_str, '0,1,2,3,4,5');
    SET month_idx = month_idx + 1;
  END WHILE;
END;