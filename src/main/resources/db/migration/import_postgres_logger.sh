#!/bin/bash

set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <postgres_password>"
  exit 1
fi

PASSWORD="$1"

USER="logger"
DB="logger"
HOST="localhost"
OUT="dump_csv"

export PGPASSWORD="$PASSWORD"

PSQL_CMD="psql -U $USER -d $DB -h $HOST -w"

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
  echo "Importing $t ..."
  $PSQL_CMD -c "\copy $t FROM '$OUT/$t.tsv' WITH (FORMAT text, DELIMITER E'\t', HEADER true)"
done

unset PGPASSWORD

echo "Done."