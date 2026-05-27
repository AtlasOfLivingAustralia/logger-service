package au.org.ala.logger.admin

import au.org.ala.logger.RemoteAddress
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional
import org.springframework.dao.DataIntegrityViolationException

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class RemoteAddressController {

    static scaffold = RemoteAddress
    static allowedMethods = [save: "POST", update: "POST", delete: ["DELETE", "POST"]]

    @Transactional
    def update() {
        Long id = params.id as Long
        String ip = params.ip?.trim()

        RemoteAddress instance = RemoteAddress.get(id)
        if (!instance) {
            flash.message = "RemoteAddress not found: ${id}"
            redirect action: "index"
            return
        }

        // Check if the new IP already exists on a DIFFERENT record
        RemoteAddress existing = RemoteAddress.findByIp(ip)
        if (existing && existing.id != id) {
            flash.error = "IP address '${ip}' is already used by another record."
            render view: "edit", model: [remoteAddress: instance]
            return
        }

        instance.ip = ip
        instance.hostName = params.hostName?.trim()

        if (!instance.save(flush: true)) {
            render view: "edit", model: [remoteAddress: instance]
            return
        }

        flash.message = "Remote address updated successfully."
        redirect action: "show", id: instance.id
    }

    @Transactional
    def save() {
        String ip = params.ip?.trim()

        if (RemoteAddress.findByIp(ip)) {
            flash.error = "IP address '${ip}' already exists."
            render view: "create", model: [remoteAddress: new RemoteAddress(params)]
            return
        }

        RemoteAddress instance = new RemoteAddress(params)
        if (!instance.save(flush: true)) {
            render view: "create", model: [remoteAddress: instance]
            return
        }

        flash.message = "Remote address '${ip}' created successfully."
        redirect action: "show", id: instance.id
    }

    @Transactional
    def delete(Long id) {
        if (id == null) {
            flash.message = "Missing id"
            redirect(action: "index")
            return
        }
        RemoteAddress instance = RemoteAddress.get(id)
        if (!instance) {
            flash.message = "RemoteAddress not found: ${id}"
            redirect(action: "index")
            return
        }
        try {
            instance.delete(flush: true)
            flash.message = "RemoteAddress ${id} deleted"
            redirect(action: "index")
        } catch (DataIntegrityViolationException e) {
            log.error("Delete blocked for RemoteAddress id=${id}", e)
            flash.message = "Cannot delete: record is in use"
            redirect(action: "show", id: id)
        } catch (Exception e) {
            log.error("Delete failed for RemoteAddress id=${id}", e)
            flash.message = "Delete failed: ${e.message}"
            redirect(action: "show", id: id)
        }
    }
}
