#!/bin/bash

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <mysql_password>"
  exit 1
fi

PASSWORD="$1"

USER="logger"
DB="logger"
OUT="dump_csv"

mkdir -p "$OUT"

MYSQL_CMD="mysql -u $USER -p$PASSWORD $DB --batch --raw --default-character-set=utf8mb4"

tables=(
  log_event
  log_detail
  log_event_type
  log_reason_type
  log_source_type
  remote_address
  event_summary_totals
  event_summary_breakdown_email
  event_summary_breakdown_email_entity
  event_summary_breakdown_reason
  event_summary_breakdown_reason_entity
  event_summary_breakdown_reason_entity_source
  event_processing_checkpoint
)

for t in "${tables[@]}"; do
  echo "Exporting $t ..."
  $MYSQL_CMD -e "SELECT * FROM $t" > "$OUT/$t.tsv"
done

echo "Done."