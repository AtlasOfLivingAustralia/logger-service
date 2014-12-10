package au.org.ala.logger.admin

import au.org.ala.logger.LogSourceType

class LogSourceTypeController {

    static scaffold = LogSourceType

    def save() {
        def logSourceType = new LogSourceType(params)
        logSourceType.id = params["id"] as Long

        logSourceType.save(flush: true)

        redirect logSourceType
    }
}
