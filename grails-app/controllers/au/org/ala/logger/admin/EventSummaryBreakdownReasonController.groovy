package au.org.ala.logger.admin

import au.org.ala.logger.EventSummaryBreakdownReason
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class EventSummaryBreakdownReasonController {

    static scaffold = EventSummaryBreakdownReason

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        def model = [summarylList: EventSummaryBreakdownReason.list(params),
                     summaryTotalCount: EventSummaryBreakdownReason.count(),
                     columns: ["month", "logEventTypeId", "logReasonTypeId", "numberOfEvents", "recordCount"],
                     entityName: "EventSummaryBreakdownReason"]
        render(view: '/summaryIndex', model: model)
    }

}
