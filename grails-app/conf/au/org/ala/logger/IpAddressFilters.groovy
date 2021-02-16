/*
 * Copyright (C) 2021 Atlas of Living Australia
 * All Rights Reserved.
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.logger

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