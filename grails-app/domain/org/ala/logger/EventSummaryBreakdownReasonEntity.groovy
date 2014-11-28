package org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class EventSummaryBreakdownReasonEntity implements Serializable {
    String month
    Integer logEventTypeId
    Integer logReasonTypeId
    String entityUid
    Long numberOfEvents
    Long recordCount

    static constraints = {
        id bindable: true
        month nullable: false, maxSize: 255
        logEventTypeId nullable: false
        logReasonTypeId nullable: false
        entityUid nullable: false, maxSize: 255
        numberOfEvents nullable: true
        recordCount nullable: true
    }

    static mapping = {
        table "event_summary_breakdown_reason_entity"
        version false

        id generator: "assigned", composite: ["month", "logEventTypeId", "logReasonTypeId", "entityUid"]

        month column: "month"
        logEventTypeId column: "log_event_type_id"
        logReasonTypeId column: "log_reason_type_id"
        entityUid column: "entity_uid"
        numberOfEvents column: "number_of_events"
        recordCount column: "record_count"
    }
}
