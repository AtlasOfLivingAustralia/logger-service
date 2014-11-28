package org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class EventSummaryTotal implements Serializable {
    String month
    Integer logEventTypeId
    Long numberOfEvents
    Long recordCount

    static constraints = {
        id bindable: true
        month nullable: false, maxSize: 255
        logEventTypeId nullable: false
        numberOfEvents nullable: true
        recordCount nullable: true
    }

    static mapping = {
        table "event_summary_totals"
        version false

        id generator: "assigned", composite: ["month", "logEventTypeId"]

        month column: "month"
        logEventTypeId column: "log_event_type_id"
        numberOfEvents column: "number_of_events"
        recordCount column: "record_count"
    }
}
