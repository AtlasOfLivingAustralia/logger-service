package logger.service

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.FiltersUnitTestMixin
import org.ala.logger.LoggerController
import org.ala.logger.LoggerService
import org.ala.logger.RemoteAddress
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(IpAddressFilters)
@TestMixin(FiltersUnitTestMixin)
@Mock([LoggerController, LoggerService])
@Unroll
class IpAddressFilterSpec extends Specification {

    public static def VALID_ADDRESS = "valid"
    public static def INVALID_ADDRESS = "invalid"

    def controller = Mock(LoggerController)

    def setup() {

    }

    def "IpAddressFilter should reject unrecognised request.remoteAddr IP addresses for sensitive actions"() {
        setup:
        defineBeans {
            loggerService(MockLoggerService)
        }

        when:
        request.remoteAddr = INVALID_ADDRESS

        withFilters(controller: "logger", action: action) {
            controller.action
        }

        then:
        if (sensitive) assert response.status == HttpStatus.UNAUTHORIZED.value()
        if (!sensitive) assert response.status != HttpStatus.UNAUTHORIZED.value()

        where:
        action                      | sensitive
        "save"                      | true
        "monthlyBreakdown"          | true
        "getEventLog"               | true
        "getEventTypes"             | false
        "getReasonTypes"            | false
        "getSourceTypes"            | false
        "getReasonBreakdown"        | false
        "getReasonBreakdownByMonth" | false
        "getReasonBreakdownCSV"     | false
        "getEmailBreakdown"         | false
        "getEmailBreakdownCSV"      | false
        "getTotalsByEventType"      | false
        "getEntityBreakdown"        | false
    }

    def "IpAddressFilter should accept recognised request.remoteAddr IP addresses for sensitive actions"() {
        setup:
        defineBeans {
            loggerService(MockLoggerService)
        }

        when:
        request.remoteAddr = VALID_ADDRESS

        withFilters(controller: "logger", action: action) {
            controller.action
        }

        then:
        assert response.status != HttpStatus.UNAUTHORIZED.value()

        where:
        action                      | sensitive
        "save"                      | true
        "monthlyBreakdown"          | true
        "getEventLog"               | true
        "getEventTypes"             | false
        "getReasonTypes"            | false
        "getSourceTypes"            | false
        "getReasonBreakdown"        | false
        "getReasonBreakdownByMonth" | false
        "getReasonBreakdownCSV"     | false
        "getEmailBreakdown"         | false
        "getEmailBreakdownCSV"      | false
        "getTotalsByEventType"      | false
        "getEntityBreakdown"        | false
    }

    def "IpAddressFilter should reject unrecognised X-Forwarded-For IP addresses for sensitive actions"() {
        setup:
        defineBeans {
            loggerService(MockLoggerService)
        }

        when:
        request.addHeader("X-Forwarded-For", INVALID_ADDRESS)

        withFilters(controller: "logger", action: action) {
            controller.action
        }

        then:
        if (sensitive) assert response.status == HttpStatus.UNAUTHORIZED.value()
        if (!sensitive) assert response.status != HttpStatus.UNAUTHORIZED.value()

        where:
        action                      | sensitive
        "save"                      | true
        "monthlyBreakdown"          | true
        "getEventLog"               | true
        "getEventTypes"             | false
        "getReasonTypes"            | false
        "getSourceTypes"            | false
        "getReasonBreakdown"        | false
        "getReasonBreakdownByMonth" | false
        "getReasonBreakdownCSV"     | false
        "getEmailBreakdown"         | false
        "getEmailBreakdownCSV"      | false
        "getTotalsByEventType"      | false
        "getEntityBreakdown"        | false
    }

    def "IpAddressFilter should accept recognised X-Forwarded-For IP addresses for sensitive actions"() {
        setup:
        defineBeans {
            loggerService(MockLoggerService)
        }

        when:
        request.addHeader("X-Forwarded-For", VALID_ADDRESS)

        withFilters(controller: "logger", action: action) {
            controller.action
        }

        then:
        assert response.status != HttpStatus.UNAUTHORIZED.value()

        where:
        action                      | sensitive
        "save"                      | true
        "monthlyBreakdown"          | true
        "getEventLog"               | true
        "getEventTypes"             | false
        "getReasonTypes"            | false
        "getSourceTypes"            | false
        "getReasonBreakdown"        | false
        "getReasonBreakdownByMonth" | false
        "getReasonBreakdownCSV"     | false
        "getEmailBreakdown"         | false
        "getEmailBreakdownCSV"      | false
        "getTotalsByEventType"      | false
        "getEntityBreakdown"        | false
    }

    public static class MockLoggerService extends LoggerService {

        public MockLoggerService() {
        }

        @Override
        def findRemoteAddress(String ipAddress) {
            if (VALID_ADDRESS == ipAddress) {
                new RemoteAddress(hostName: "valid", ipAddress: VALID_ADDRESS)
            }
        }
    }
}
