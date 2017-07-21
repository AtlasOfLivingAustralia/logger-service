package au.org.ala.logger.admin

import au.org.ala.logger.EventSummaryTotal
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class EventSummaryTotalController {

    static scaffold = EventSummaryTotal


}
