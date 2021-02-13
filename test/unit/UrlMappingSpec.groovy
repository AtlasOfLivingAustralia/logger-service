

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import au.org.ala.logger.LoggerController
import spock.lang.Specification

@TestFor(UrlMappings)
@Mock(LoggerController)
class UrlMappingSpec extends Specification {

    def "test mappings"() {
        expect:
        assertForwardUrlMapping("/service/logger/events", controller: "logger", action: "getEventTypes")
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
}
