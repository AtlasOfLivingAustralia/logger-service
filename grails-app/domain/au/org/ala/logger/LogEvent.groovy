package au.org.ala.logger

import grails.converters.JSON
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(excludes="logDetails")
@EqualsAndHashCode(excludes="logDetails")
class LogEvent {

    String sourceUrl
    String comment
    String month
    String userEmail
    String userIp
    String source
    String userAgent
    Integer logEventTypeId
    Integer logReasonTypeId
    Integer logSourceTypeId
    Date dateCreated
    Set logDetails = []

    static hasMany = [logDetails: LogDetail]

    static constraints = {
        // ordering here determines order in scaffold views (list and show)
        month maxSize: 255, nullable: true
        logEventTypeId nullable: true
        logSourceTypeId nullable: true
        logReasonTypeId nullable: true
        userEmail maxSize: 255, nullable: true
        source maxSize: 255, nullable: true
        sourceUrl nullable: true
        userIp maxSize: 255, nullable: true
        comment maxSize: 255, nullable: true
        userAgent maxSize: 255, nullable: true
    }

    static mapping = {
        table "log_event"
        version false
        sort 'month': 'desc'
        id sqlType: "int(11)"
        sourceUrl column: "source_url", type: "text"
        comment column: "comment", type: "text"
        month column: "month"
        userEmail column: "user_email"
        userIp column: "user_ip"
        source column: "source"
        userAgent column: "user_agent"
        logEventTypeId column: "log_event_type_id"
        logReasonTypeId column: "log_reason_type_id"
        logSourceTypeId column: "log_source_type_id"
        dateCreated column: "created"
    }

    def toJSON() {
        def details = logDetails?.collect({ LogDetail k -> k.toJSON() })

        [logEvent: [logDetails     : details,
                    created        : dateCreated?.getTime(),
                    userEmail      : userEmail,
                    userIp         : userIp,
                    logEventTypeId : logEventTypeId,
                    logReasonTypeId: logReasonTypeId,
                    logSourceTypeId: logSourceTypeId,
                    sourceUrl      : sourceUrl,
                    month          : month,
                    source         : source,
                    userAgent      : userAgent,
                    comment        : comment,
                    id             : id]] as JSON
    }
}
