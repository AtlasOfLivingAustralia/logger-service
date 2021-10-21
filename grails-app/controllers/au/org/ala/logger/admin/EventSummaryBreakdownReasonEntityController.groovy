package au.org.ala.logger.admin

import au.org.ala.logger.EventSummaryBreakdownReasonEntity
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class EventSummaryBreakdownReasonEntityController {

    static scaffold = EventSummaryBreakdownReasonEntity

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        def model = [summarylList: EventSummaryBreakdownReasonEntity.list(params),
                     summaryTotalCount: EventSummaryBreakdownReasonEntity.count(),
                     columns: ["month", "logEventTypeId", "logReasonTypeId", "entityUid", "numberOfEvents", "recordCount"],
                     entityName: "EventSummaryBreakdownReasonEntity"]
        render(view: '/summaryIndex', model: model)
    }

}
