package au.org.ala.logger.admin

import au.org.ala.logger.EventSummaryBreakdownEmail
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class EventSummaryBreakdownEmailController {

    static scaffold = EventSummaryBreakdownEmail

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        def model = [summarylList: EventSummaryBreakdownEmail.list(params),
                     summaryTotalCount: EventSummaryBreakdownEmail.count(),
                     columns: ["month", "logEventTypeId", "userEmailCategory", "numberOfEvents", "recordCount"],
                     entityName: "EventSummaryBreakdownEmail"]
        render(view: '/summaryIndex', model: model)
    }
}
