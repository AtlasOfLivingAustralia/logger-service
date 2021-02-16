package au.org.ala.logger

import org.springframework.http.HttpStatus


class IpAddressInterceptor {
    def loggerService
    final String X_FORWARDED_FOR = "X-Forwarded-For"

    IpAddressInterceptor () {
        match(controller: "logger", action: "save")
        match(controller: "logger", action: "getEventLog")
        match(controller: "logger", action: "monthlyBreakdown")
    }

    boolean after() { true }

    boolean before() {
        String ip = request.getHeader(X_FORWARDED_FOR) ?: request.getRemoteAddr()
        boolean result = true
        log.debug "findRemoteAddress for ${ip} = ${loggerService.findRemoteAddress(ip)} (via ${request.requestURI})"

        if (!loggerService.findRemoteAddress(ip)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value)
            result = false
        }

        result
    }

    void afterView() {
        // no-op
    }
}
