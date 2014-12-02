package au.org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class EventSummaryBreakdownEmail implements Serializable {
    String month
    Integer logEventTypeId
    String userEmailCategory
    Long numberOfEvents
    Long recordCount

    static constraints = {
        id bindable: true
        month nullable: false, maxSize: 255
        logEventTypeId nullable: false
        userEmailCategory nullable: false
        numberOfEvents nullable: true
        recordCount nullable: true
    }

    static mapping = {
        table "event_summary_breakdown_email"
        version false

        id generator: "assigned", composite: ["month", "logEventTypeId", "userEmailCategory"]

        month column: "month"
        logEventTypeId column: "log_event_type_id"
        userEmailCategory column: "user_email_category"
        numberOfEvents column: "number_of_events"
        recordCount column: "record_count"
    }
}
