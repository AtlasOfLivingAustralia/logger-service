package au.org.ala.logger.admin

import au.org.ala.logger.RemoteAddress
import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional

@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class RemoteAddressController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index(Integer max) {
        params.max = Math.min(max ?: 20, 100)
        [remoteAddressList: RemoteAddress.list(params).sort { it.id }, remoteAddressCount: RemoteAddress.count()]
    }

    def show(Long id) {
        def remoteAddress = RemoteAddress.get(id)
        if (!remoteAddress) {
            flash.message = "RemoteAddress not found"
            redirect action: "index"
            return
        }
        [remoteAddress: remoteAddress]
    }

    def create() {
        [remoteAddress: new RemoteAddress(params)]
    }

    @Transactional
    def save() {
        def remoteAddress = new RemoteAddress(params)
        remoteAddress.id = params.id ? params.id as Long : null
        remoteAddress.save(flush: true)
        redirect action: "show", id: remoteAddress.id
    }

    def edit(Long id) {
        def remoteAddress = RemoteAddress.get(id)
        if (!remoteAddress) {
            flash.message = "RemoteAddress not found"
            redirect action: "index"
            return
        }
        [remoteAddress: remoteAddress]
    }

    @Transactional
    def update() {
        def remoteAddress = RemoteAddress.get(params.id as Long)
        if (!remoteAddress) {
            flash.message = "RemoteAddress not found"
            redirect action: "index"
            return
        }
        remoteAddress.properties = params
        remoteAddress.save(flush: true)
        redirect action: "show", id: remoteAddress.id
    }

    @Transactional
    def delete(Long id) {
        def remoteAddress = RemoteAddress.get(id)
        if (remoteAddress) {
            remoteAddress.delete(flush: true)
        }
        redirect action: "index"
    }
}