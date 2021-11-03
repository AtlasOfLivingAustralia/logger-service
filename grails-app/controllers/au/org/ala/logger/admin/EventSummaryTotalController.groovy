package au.org.ala.logger.admin

import au.org.ala.logger.EventSummaryTotal
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class EventSummaryTotalController {

    static scaffold = EventSummaryTotal

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        def model = [summarylList: EventSummaryTotal.list(params),
                     summaryTotalCount: EventSummaryTotal.count(),
                     columns: ["month", "logEventTypeId", "numberOfEvents", "recordCount"],
                     entityName: "EventSummaryTotal"]
        render(view: '/summaryIndex', model: model)
    }
}
