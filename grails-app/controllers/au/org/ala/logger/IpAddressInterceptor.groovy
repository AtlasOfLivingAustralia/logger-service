package au.org.ala.logger

import org.springframework.http.HttpStatus


class IpAddressInterceptor {
    def loggerService
    final String X_FORWARDED_FOR = "X-Forwarded-For"

    IpAddressInterceptor () {
        match(controller: "logger", action: "save")
        match(controller: "logger", action: "monthlyBreakdown")
        match(controller: "logger", action: "getEventLog")
    }

    boolean after() { true }

    boolean before() {
        String ip = request.getHeader(X_FORWARDED_FOR) ?: request.getRemoteAddr()
        boolean result = true
        println "findRemoteAddress for ${ip} = ${loggerService.findRemoteAddress(ip)} (via ${request.pathInfo})"

        if (!loggerService.findRemoteAddress(ip)) {
            log.error("Unrecognised ip address ${ip}")
            response.setStatus(HttpStatus.UNAUTHORIZED.value)
            result = false
        }
        println "returning ${result}"
        result
    }

    void afterView() {
        // no-op
    }
}
