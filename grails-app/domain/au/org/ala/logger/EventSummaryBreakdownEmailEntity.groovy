package au.org.ala.logger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class EventSummaryBreakdownEmailEntity implements Serializable {
    String month
    Integer logEventTypeId
    String userEmailCategory
    String entityUid;
    Long numberOfEvents
    Long recordCount

    static constraints = {
        id bindable: true
        month nullable: false, maxSize: 255
        logEventTypeId nullable: false
        userEmailCategory nullable: false
        entityUid nullable: false, maxSize: 255
        numberOfEvents nullable: true
        recordCount nullable: true
    }

    static mapping = {
        table "event_summary_breakdown_email_entity"
        version false

        id generator: "assigned", composite: ["month", "logEventTypeId", "userEmailCategory", "entityUid"]

        month column: "month"
        logEventTypeId column: "log_event_type_id"
        userEmailCategory column: "user_email_category"
        entityUid column: "entity_uid"
        numberOfEvents column: "number_of_events"
        recordCount column: "record_count"
    }
}
