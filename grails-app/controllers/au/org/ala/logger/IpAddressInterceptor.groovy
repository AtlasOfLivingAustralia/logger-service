package au.org.ala.logger

import groovy.util.logging.Log4j
import org.springframework.http.HttpStatus

@Log4j
class IpAddressInterceptor {
    def loggerService
    final String X_FORWARDED_FOR = "X-Forwarded-For"

    IpAddressInterceptor () {
        match(controller: "logger", action: "save")
        match(controller: "logger", action: "getEventLog")
        //match(controller: "logger", action: "monthlyBreakdown")
    }

    boolean after() { true }

    boolean before() {
        String ip = request.getHeader(X_FORWARDED_FOR) ?: request.getRemoteAddr()
        log.debug "headers = ${request.getHeader('Accept')}"
        boolean result = true
        log.debug "Checking if ${ip} == ${loggerService.findRemoteAddress(ip)} (via ${request.requestURI})"

        if (!loggerService.findRemoteAddress(ip)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value)
            result = false
        }
        log.debug "result is ${result}"

        result
    }

    void afterView() {
        // no-op
    }
}
