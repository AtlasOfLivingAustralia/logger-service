package au.org.ala.logger.admin

import au.org.ala.logger.LogEventType
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogEventTypeController {

    static allowedMethods = [
            save  : "POST",
            update: "POST",
            delete: "POST"
    ]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond LogEventType.list(params),
                model: [logEventTypeCount: LogEventType.count()]
    }

    def create() {
        respond new LogEventType(params)
    }

    @Transactional
    def save() {
        def logEventType = new LogEventType(params)

        if (params.id) {
            logEventType.id = params.long('id')
        }

        if (!logEventType.validate()) {
            render view: 'create', model: [logEventType: logEventType]
            return
        }

        logEventType.save(flush: true)

        flash.message = "LogEventType ${logEventType.id} created"
        redirect action: "show", id: logEventType.id
    }

    def show(Long id) {
        respond LogEventType.get(id)
    }

    def edit(Long id) {
        respond LogEventType.get(id)
    }

    @Transactional
    def update(Long id) {
        def logEventType = LogEventType.get(id)

        if (!logEventType) {
            redirect action: "index"
            return
        }

        logEventType.properties = params

        if (!logEventType.validate()) {
            render view: 'edit', model: [logEventType: logEventType]
            return
        }

        logEventType.save(flush: true)

        flash.message = "LogEventType ${logEventType.id} updated"
        redirect action: "show", id: logEventType.id
    }

    @Transactional
    def delete(Long id) {
        def logEventType = LogEventType.get(id)
        if (logEventType) {
            logEventType.delete(flush: true)
            flash.message = "Deleted successfully"
        }
        redirect action: "index"
    }
}
