# MySQL → PostgreSQL Migration (TSV-based, existing schema)

This document describes:

1. Exporting MySQL tables to TSV
2. Importing into PostgreSQL
3. Fixing PostgreSQL sequences after import

Assumption:
- PostgreSQL tables already exist
- We are migrating DATA ONLY (not schema)

---

# 1. Export MySQL tables to TSV

## Create output directory

```bash
mkdir -p dump_csv
```

# 1. Dump MySQL tables to TSV (tab-separated values)

log_event needs to be special handled to replace NULL with \N for PostgreSQL compatibility, and to ensure UTF-8 encoding is preserved, 
because its url query field can contain a wide range of characters. 
The other tables can be dumped with default settings as they are less likely to have encoding issues, but you may want to adjust this if you encounter problems.

The reason for using mysql command line is because AWS RDS does not allow writing to files on the server, so we have to dump to local files.
```
mysql -u logger -p logger --batch --raw --default-character-set=utf8mb4 -e "SELECT * FROM log_event" > dump_csv/log_event.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM log_detail" > dump_csv/log_detail.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM log_event_type" > dump_csv/log_event_type.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM log_reason_type" > dump_csv/log_reason_type.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM log_source_type" > dump_csv/log_source_type.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM remote_address" > dump_csv/remote_address.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_summary_totals" > dump_csv/event_summary_totals.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_summary_breakdown_email" > dump_csv/event_summary_breakdown_email.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_summary_breakdown_email_entity" > dump_csv/event_summary_breakdown_email_entity.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_summary_breakdown_reason" > dump_csv/event_summary_breakdown_reason.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_summary_breakdown_reason_entity" > dump_csv/event_summary_breakdown_reason_entity.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_summary_breakdown_reason_entity_source" > dump_csv/event_summary_breakdown_reason_entity_source.tsv
mysql -u logger -p logger --batch --raw -e "SELECT * FROM event_processing_checkpoint" > dump_csv/event_processing_checkpoint.tsv
```

@see export_mysql_logger.sh for an example script.

# 2. Replace some illegal values 
```
sed 's/NULL/\\N/g' log_event.tsv > log_event_pg.tsv
mv log_event.tsv log_event_raw.tsv
mv log_event_pg.tsv log_event.tsv
```
# 3. Import into PostgreSQL
```
psql -U logger -d logger -c "\copy log_event FROM 'dump_csv/log_event.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy log_detail FROM 'dump_csv/log_detail.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy log_event_type FROM 'dump_csv/log_event_type.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy log_reason_type FROM 'dump_csv/log_reason_type.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy log_source_type FROM 'dump_csv/log_source_type.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy remote_address FROM 'dump_csv/remote_address.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_summary_totals FROM 'dump_csv/event_summary_totals.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_summary_breakdown_email FROM 'dump_csv/event_summary_breakdown_email.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_summary_breakdown_email_entity FROM 'dump_csv/event_summary_breakdown_email_entity.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_summary_breakdown_reason FROM 'dump_csv/event_summary_breakdown_reason.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_summary_breakdown_reason_entity FROM 'dump_csv/event_summary_breakdown_reason_entity.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_summary_breakdown_reason_entity_source FROM 'dump_csv/event_summary_breakdown_reason_entity_source.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
psql -U logger -d logger -c "\copy event_processing_checkpoint FROM 'dump_csv/event_processing_checkpoint.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
```
@see import_postgres_logger.sh for an example script.

# 3. Reset sequences
```sql
SELECT setval(pg_get_serial_sequence('log_event', 'id'), (SELECT COALESCE(MAX(id), 1) FROM log_event));
SELECT setval(pg_get_serial_sequence('log_detail', 'id'), (SELECT COALESCE(MAX(id), 1) FROM log_detail));
SELECT setval(pg_get_serial_sequence('remote_address', 'id'), (SELECT COALESCE(MAX(id), 1) FROM remote_address));
```

Note: `log_event_type`, `log_reason_type`, `log_source_type` use assigned integer IDs (no sequence).
`event_summary_*` tables use composite primary keys (no sequence). No sequence reset needed for these.
