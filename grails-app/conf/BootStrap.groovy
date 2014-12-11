import au.org.ala.logger.RemoteAddress

class BootStrap {

    def init = { servletContext ->
        if (!RemoteAddress.findByIp("127.0.0.1")) {
            new RemoteAddress(ip: "127.0.0.1", hostName: "localhost").save(flush: true)
        }        
    }

    def destroy = {
    }
}
