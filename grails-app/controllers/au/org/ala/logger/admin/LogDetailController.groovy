package au.org.ala.logger.admin

import au.org.ala.logger.LogDetail
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class LogDetailController {

    static scaffold = LogDetail

}
