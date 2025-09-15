ALTER TABLE event_summary_breakdown_reason_entity
    ADD UNIQUE KEY uq_summary_entity (
    month,
    log_event_type_id,
    log_reason_type_id,
    entity_uid
    );