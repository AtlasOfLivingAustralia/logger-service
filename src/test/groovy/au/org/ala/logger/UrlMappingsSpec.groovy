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

import grails.testing.web.UrlMappingsUnitTest
import spock.lang.Specification

class UrlMappingsSpec extends Specification implements UrlMappingsUnitTest<UrlMappings> {
    void "test url mappings"() {
        expect:
        verifyController("admin")
        assertReverseUrlMapping("/service/logger", controller: "logger", action: "getEventTypes")
        assertForwardUrlMapping("/service/logger/reasons", controller: "logger", action: "getReasonTypes")
        assertForwardUrlMapping("/service/logger/sources", controller: "logger", action: "getSourceTypes")

        assertForwardUrlMapping("/service/logger/get.json", controller: "logger", action: "monthlyBreakdown")

        assertForwardUrlMapping("/service/reasonBreakdown", controller: "logger", action: "getReasonBreakdown")
        assertForwardUrlMapping("/service/reasonBreakdown.json", controller: "logger", action: "getReasonBreakdown")
        assertForwardUrlMapping("/service/reasonBreakdownMonthly", controller: "logger", action: "getReasonBreakdownByMonth")
        assertForwardUrlMapping("/service/reasonBreakdownCSV", controller: "logger", action: "getReasonBreakdownCSV")
        assertForwardUrlMapping("/service/reasonBreakdownByMonthCSV", controller: "logger", action: "getReasonBreakdownByMonthCSV")

        assertForwardUrlMapping("/service/emailBreakdown", controller: "logger", action: "getEmailBreakdown")
        assertForwardUrlMapping("/service/emailBreakdown.json", controller: "logger", action: "getEmailBreakdown")
        assertForwardUrlMapping("/service/emailBreakdownCSV", controller: "logger", action: "getEmailBreakdownCSV")

        assertForwardUrlMapping("/service/totalsByType", controller: "logger", action: "getTotalsByEventType")

        assertForwardUrlMapping("/service/logger/1", controller: "logger", action: "getEventLog") {
            id = "1"
        }

        assertForwardUrlMapping("/service/dr143/events/1/counts.json", controller: "logger", action: "getEntityBreakdown") {
            entityUid = "dr143"
            eventId = "1"
        }
        assertForwardUrlMapping("/service/dr143/events/1/counts", controller: "logger", action: "getEntityBreakdown") {
            entityUid = "dr143"
            eventId = "1"
        }

        assertForwardUrlMapping(500, view: "error")
    }

    void "verify url mappings"() {
        when:
        assertReverseUrlMapping("/admin", controller: 'admin', action: 'index')
        assertReverseUrlMapping("/admin", controller: 'admin', action: 'logEvent')

        then:
        noExceptionThrown()
    }
}
