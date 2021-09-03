package au.org.ala.logger.admin

import au.org.ala.logger.EventSummaryBreakdownEmailEntity
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class EventSummaryBreakdownEmailEntityController {

    static scaffold = EventSummaryBreakdownEmailEntity

    def index() {
        params.offset = params.offset ?: 0
        params.max = params.max ?: 10
        def model = [summarylList: EventSummaryBreakdownEmailEntity.list(params),
                     summaryTotalCount: EventSummaryBreakdownEmailEntity.count(),
                     columns: ["month", "logEventTypeId", "userEmailCategory", "entityUid", "numberOfEvents", "recordCount"],
                     entityName: "EventSummaryBreakdownEmailEntity"]
        render(view: '/summaryIndex', model: model)
    }

}
