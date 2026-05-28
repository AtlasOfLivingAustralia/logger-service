package au.org.ala.logger.admin

import au.org.ala.logger.LogSourceType
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogSourceTypeController {

    static scaffold = LogSourceType

    /**
     * todo
     * The id field in the domain class and the other three: LogReasonType, LogEventType, LogReasonType is defined as "id:assigned"
     * This means that when saving an instance of the domain class, the id must be explicitly set before calling the save() method.
     * And also affect the 'delete' method, as the id must be provided to identify which instance to delete.
     *
     * "id:assigned" should be removed
     *
     * @return
     */
    @Transactional
    def save() {
        def logSourceType = new LogSourceType(params)
        logSourceType.id = params["id"] as Long
        logSourceType.save(flush: true)
        redirect logSourceType
    }
}
