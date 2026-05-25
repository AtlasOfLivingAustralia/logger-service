package au.org.ala.logger.admin

import au.org.ala.logger.LogSourceType
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogSourceTypeController {

    static scaffold = LogSourceType

    @Transactional
    def save() {
        def logSourceType = new LogSourceType(params)
        logSourceType.id = params["id"] as Long

        logSourceType.save(flush: true)

        redirect logSourceType
    }
}
