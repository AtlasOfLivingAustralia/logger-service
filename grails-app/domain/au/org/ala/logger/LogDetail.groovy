package au.org.ala.logger

class LogDetail implements Serializable {

    String entityType;
    String entityUid;
    Long recordCount;

    static belongsTo = [logEvent: LogEvent]

    static constraints = {
    }

    static mapping = {
        table "log_detail"
        version false

        id sqlType: "int(11)"
        entityType column: "entity_type"
        entityUid column: "entity_uid", type: "text"
        recordCount column: "record_count"

        logEvent column: "log_event_id", sqlType: "int(11)"
    }

    def toJSON() {
        [entityUid: entityUid,
        recordCount: recordCount,
        entityType: entityType,
        id: id]
    }
}