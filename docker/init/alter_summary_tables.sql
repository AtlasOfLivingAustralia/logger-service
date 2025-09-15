ALTER TABLE event_summary_totals
    ADD UNIQUE KEY uq_summary_totals (month, log_event_type_id);

ALTER TABLE event_summary_breakdown_reason_entity
    ADD UNIQUE KEY uq_summary_entity (
    month,
    log_event_type_id,
    log_reason_type_id,
    entity_uid
    );