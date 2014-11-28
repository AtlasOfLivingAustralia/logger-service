package org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class EventSummaryBreakdownReason implements Serializable {
    String month
    Integer logEventTypeId
    Integer logReasonTypeId
    Long numberOfEvents
    Long recordCount

    static constraints = {
        id bindable: true
        month nullable: false, maxSize: 255
        logEventTypeId nullable: false
        logReasonTypeId nullable: false
        numberOfEvents nullable: true
        recordCount nullable: true
    }

    static mapping = {
        table "event_summary_breakdown_reason"
        version false

        id generator: "assigned", composite: ["month", "logEventTypeId", "logReasonTypeId"]

        month column: "month"
        logEventTypeId column: "log_event_type_id"
        logReasonTypeId column: "log_reason_type_id"
        numberOfEvents column: "number_of_events"
        recordCount column: "record_count"
    }
}
