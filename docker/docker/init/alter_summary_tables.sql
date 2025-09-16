ALTER TABLE event_summary_totals
    ADD UNIQUE KEY uq_summary_totals (month, log_event_type_id);

ALTER TABLE event_summary_breakdown_reason
    ADD UNIQUE KEY uq_breakdown_reason (
    month,
    log_event_type_id,
    log_reason_type_id
    );

ALTER TABLE event_summary_breakdown_reason_entity
    ADD UNIQUE KEY uq_summary_entity (
    month,
    log_event_type_id,
    log_reason_type_id,
    entity_uid
    );

ALTER TABLE event_summary_breakdown_reason_entity_source
    ADD UNIQUE KEY uq_reason_entity_source (
    month,
    log_event_type_id,
    log_reason_type_id,
    entity_uid,
    log_source_type_id
    );