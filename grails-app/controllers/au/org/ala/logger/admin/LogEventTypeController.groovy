package au.org.ala.logger.admin

import au.org.ala.logger.LogEventType

class LogEventTypeController {

    static scaffold = LogEventType

    def save() {
        def logEventType = new LogEventType(params)
        logEventType.id = params["id"] as Long

        logEventType.save(flush: true)

        redirect logEventType
    }
}
