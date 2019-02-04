# logger-service   [![Build Status](https://travis-ci.org/AtlasOfLivingAustralia/logger-service.svg?branch=master)](https://travis-ci.org/AtlasOfLivingAustralia/logger-service)

# Installation

The logger service is deployed using Ansible. Scripts reside in the [ala-install](https://github.com/AtlasOfLivingAustralia/ala-install) repository: ansible/logger-standalone.yml and ansible/roles/logger-service. For testing, there is a vagrant inventory in ansible/inventories/vagrant/logger-service-vagrant

# Database setup

SQL scripts to create the database schema, stored procedures and initial reference are in stored with the ansible build script in the ala-install repository under ansible/logger-service/files/db.

# Archiving log_event and log_detail tables.

The two tables `log_event` and `log_detail` will grow large over time. The content of these tables is summarised in the `event_summary_*` tables.

These summary tables are incremented by inserts using the stored procedure defined in [update_breakdown_summary_information.sql](https://github.com/AtlasOfLivingAustralia/ala-install/blob/master/ansible/roles/logger-service/files/db/update_breakdown_summary_information.sql) which is triggered on inserts to `log_detail`.

## Steps to archive

### 1. Rename log_event and log_detail and re-create new blank log_event and log_detail.
```
RENAME TABLE log_detail to archive_20190204_log_detail;
RENAME TABLE log_event  to archive_20190204_log_event;
CREATE TABLE log_detail like archive_20190204_log_detail;
CREATE TABLE log_event  like archive_20190204_log_event;
```
### 2. Backup existing summaries

```
CREATE TABLE archive_20190204_event_summary_breakdown_email                LIKE event_summary_breakdown_email; 
CREATE TABLE archive_20190204_event_summary_breakdown_email_entity         LIKE event_summary_breakdown_email_entity;
CREATE TABLE archive_20190204_event_summary_breakdown_reason               LIKE event_summary_breakdown_reason;
CREATE TABLE archive_20190204_event_summary_breakdown_reason_entity        LIKE event_summary_breakdown_reason_entity;
CREATE TABLE archive_20190204_event_summary_breakdown_reason_entity_source LIKE event_summary_breakdown_reason_entity_source;
CREATE TABLE archive_20190204_event_summary_totals                         LIKE event_summary_totals;
```
```
INSERT archive_20190204_event_summary_breakdown_email 
SELECT * FROM event_summary_breakdown_email;
INSERT archive_20190204_event_summary_breakdown_email_entity
SELECT * FROM event_summary_breakdown_email_entity;
INSERT archive_20190204_event_summary_breakdown_reason
SELECT * FROM event_summary_breakdown_reason;
INSERT archive_20190204_event_summary_breakdown_reason_entity
SELECT * FROM event_summary_breakdown_reason_entity;
INSERT archive_20190204_event_summary_breakdown_reason_entity_source
SELECT * FROM event_summary_breakdown_reason_entity_source;
INSERT archive_20190204_event_summary_totals
SELECT * FROM event_summary_totals;
```

### 3. Reinitialise the TRIGGER

Reinitialise the TRIGGER by re-running [update_breakdown_summary_information.sql](https://github.com/AtlasOfLivingAustralia/ala-install/blob/master/ansible/roles/logger-service/files/db/update_breakdown_summary_information.sql)

### Recreating summary tables with archived summaries

If the event_summary_* tables need to be recreated (this isnt usually required, only ina disaster recovery situation) with the inclusion of archived summaries, the counts can be aggregated from archived summaries and current summaries like so:

```
create table tmp_event_summary_breakdown_email AS
select month, log_event_type_id, user_email_category, sum(number_of_events), sum(record_count) from (
    select month, log_event_type_id, user_email_category, number_of_events, record_count from event_summary_breakdown_email union all
    select month, log_event_type_id, user_email_category, number_of_events, record_count from archive_20190204_event_summary_breakdown_email
) x group by month, log_event_type_id, user_email_category;
```

Once these temporary tables are created the event_summary_* (in this case event_summary_breakdown_email) can be dropped, and the tmp_event_summary_breakdown_email renamed.

```
RENAME TABLE tmp_event_summary_breakdown_email to event_summary_breakdown_email;
```
