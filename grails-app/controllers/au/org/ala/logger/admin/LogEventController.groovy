package au.org.ala.logger.admin

import au.org.ala.logger.LogEvent
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogEventController {

    static scaffold = LogEvent

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        params.sort = params.sort ?: 'month'
        params.order = params.order ?: 'desc'

        def c = LogEvent.createCriteria()
        def r = c.list(max: params.max, offset: params.offset) {
            order(params.sort as String, params.order as String)
        }

        def model = [logEventList : r,
                     logEventCount: LogEvent.count(),
                     columns      : ['month', 'logEventTypeId', 'logSourceTypeId', 'logReasonTypeId', 'userEmail', 'source'],
                     entityName   : "LogEvent"]

        render(view: 'index', model: model)
    }

}