package au.org.ala.logger.admin

import au.org.ala.logger.LogSourceType
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogSourceTypeController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        params.sort = 'id'
        params.order = 'asc'

        [logSourceTypeList: LogSourceType.list(params),
         logSourceTypeCount: LogSourceType.count()]
    }

    def show(Long id) {
        def logSourceType = LogSourceType.get(id)
        if (!logSourceType) {
            flash.message = "Log Source Type not found"
            redirect action: "index"
            return
        }
        [logSourceType: logSourceType]
    }

    def edit(Long id) {
        def logSourceType = LogSourceType.get(id)
        if (!logSourceType) {
            flash.message = "Log Source Type not found"
            redirect action: "index"
            return
        }
        [logSourceType: logSourceType]
    }

    @Transactional
    def update() {
        def logSourceType = LogSourceType.get(params.id as Long)
        if (!logSourceType) {
            flash.message = "Log Source Type not found"
            redirect action: "index"
            return
        }
        logSourceType.properties = params
        logSourceType.save(flush: true)
        redirect action: "show", id: logSourceType.id
    }

    def create() {
        [logSourceType: new LogSourceType()]
    }

    @Transactional
    def save() {
        def logSourceType = new LogSourceType(params)
        logSourceType.id = params.id as Long   // allow custom ID
        logSourceType.save(flush: true)
        redirect action: "show", id: logSourceType.id
    }

    @Transactional
    def delete() {
        def logSourceType = LogSourceType.get(params.id as Long)
        if (logSourceType) {
            logSourceType.delete(flush: true)
            flash.message = "Log Source Type deleted"
        } else {
            flash.message = "Log Source Type not found"
        }
        redirect action: "index"
    }
}