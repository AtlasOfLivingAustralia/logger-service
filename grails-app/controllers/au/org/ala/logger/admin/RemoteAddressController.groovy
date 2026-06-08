package au.org.ala.logger.admin

import au.org.ala.logger.RemoteAddress
import au.org.ala.web.AlaSecured

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class RemoteAddressController {

    static scaffold = RemoteAddress

}
