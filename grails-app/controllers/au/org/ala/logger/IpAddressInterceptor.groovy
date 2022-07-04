package au.org.ala.logger

import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus

@Slf4j
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
        log.info("X_FORWARDED_FOR header: "+ request.getHeader(X_FORWARDED_FOR))
        log.info("ip: "+ request.getRemoteAddr())
        ip = ip.tokenize(", ")[0] // Sometimes see an IP address = '3.105.55.111, 3.105.55.111' - grab first value
        log.debug "headers = ${request.getHeader('Accept')} | header(X_FORWARDED_FOR) = ${request.getHeader(X_FORWARDED_FOR)} | getRemoteAddr = ${request.getRemoteAddr()}"
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
