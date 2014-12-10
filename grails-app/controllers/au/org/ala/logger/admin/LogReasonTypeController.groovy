package au.org.ala.logger.admin

import au.org.ala.logger.LogReasonType

class LogReasonTypeController {

    static scaffold = LogReasonType

    def save() {
        def logReasonType = new LogReasonType(params)
        logReasonType.id = params["id"] as Long

        logReasonType.save(flush: true)

        redirect logReasonType
    }
}
