package au.org.ala.logger.admin

import au.org.ala.logger.RemoteAddress
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional
import org.springframework.dao.DataIntegrityViolationException


@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class RemoteAddressController {

    static scaffold = RemoteAddress
    static allowedMethods = [delete: "POST"]


    @Transactional
    def delete(Long id) {
        if (id == null) {
            flash.message = "Missing id"
            redirect(action: "index")
            return
        }

        RemoteAddress remoteAddress = RemoteAddress.get(id)
        if (!remoteAddress) {
            flash.message = "RemoteAddress not found: ${id}"
            redirect(action: "index")
            return
        }

        try {
            remoteAddress.delete(flush: true)
            flash.message = "RemoteAddress ${id} deleted"
            redirect(action: "index")
        } catch (DataIntegrityViolationException e) {
            log.error("Delete blocked for RemoteAddress id=${id}", e)
            flash.message = "Cannot delete RemoteAddress ${id}"
            redirect(action: "show", id: id)
        } catch (Exception e) {
            log.error("Delete failed for RemoteAddress id=${id}", e)
            flash.message = "Delete failed: ${e.class.simpleName}"
            redirect(action: "show", id: id)
        }
    }

}
