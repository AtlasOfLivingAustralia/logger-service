package logger.service

import org.springframework.http.HttpStatus

/**
 * Filter to check that the source IP address is in the list of allowed addresses.
 *
 * If the IP Address is not in the list, then a HTTP 401 Unauthorised is returned.
 */
class IpAddressFilters {

    final String X_FORWARDED_FOR = "X-Forwarded-For"

    def loggerService

    def filters = {
        all(controller: "logger", action: "save|monthlyBreakdown|getEventLog") {
            before = {
                String ip = request.getHeader(X_FORWARDED_FOR) ?: request.getRemoteAddr()

                if (!loggerService.findRemoteAddress(ip)) {
                    log.error("Unrecognised ip address ${ip}")
                    response.setStatus(HttpStatus.UNAUTHORIZED.value)
                    false
                }
            }
        }
    }
}
