package au.org.ala.logger.admin

import au.org.ala.logger.LogReasonType
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogReasonTypeController {

    static scaffold = LogReasonType

    def save() {
        def logReasonType = new LogReasonType(params)
        logReasonType.id = params["id"] as Long

        logReasonType.save(flush: true)

        redirect logReasonType
    }
}
