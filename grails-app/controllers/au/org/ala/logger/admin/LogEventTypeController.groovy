package au.org.ala.logger.admin

import au.org.ala.logger.LogEventType
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogEventTypeController {

    static scaffold = LogEventType

    def save() {
        def logEventType = new LogEventType(params)
        logEventType.id = params["id"] as Long

        logEventType.save(flush: true)

        redirect logEventType
    }
}
