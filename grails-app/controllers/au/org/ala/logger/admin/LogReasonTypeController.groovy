package au.org.ala.logger.admin

import au.org.ala.logger.LogReasonType
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogReasonTypeController {

    static allowedMethods = [update: "POST", delete: "POST"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        params.sort = 'id'       // sort by id
        params.order = 'asc'

        [logReasonTypeList: LogReasonType.list(params), logReasonTypeCount: LogReasonType.count()]
    }

    def show(Long id) {
        def logReasonType = LogReasonType.get(id)
        if (!logReasonType) {
            flash.message = "LogReasonType not found with id $id"
            redirect action: "index"
            return
        }
        [logReasonType: logReasonType]
    }

    def edit(Long id) {
        def logReasonType = LogReasonType.get(id)
        if (!logReasonType) {
            flash.message = "LogReasonType not found with id $id"
            redirect action: "index"
            return
        }
        [logReasonType: logReasonType]
    }

    @Transactional
    def update() {
        def logReasonType = LogReasonType.get(params.id)
        if (!logReasonType) {
            flash.message = "LogReasonType not found with id ${params.id}"
            redirect action: "index"
            return
        }

        logReasonType.properties = params
        if (!logReasonType.save(flush: true)) {
            flash.message = "Failed to update LogReasonType"
            render view: "edit", model: [logReasonType: logReasonType]
            return
        }

        flash.message = "LogReasonType updated successfully"
        redirect action: "show", id: logReasonType.id
    }

    @Transactional
    def delete() {
        def logReasonType = LogReasonType.get(params.id)
        if (logReasonType) {
            logReasonType.delete(flush: true)
            flash.message = "LogReasonType deleted successfully"
        } else {
            flash.message = "LogReasonType not found with id ${params.id}"
        }
        redirect action: "index"
    }
}