package au.org.ala.logger

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import grails.testing.spring.AutowiredTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.testing.web.interceptor.InterceptorUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification

class IpAddressInterceptorSpec extends Specification implements  AutowiredTest, DomainUnitTest<RemoteAddress>, InterceptorUnitTest<IpAddressInterceptor> {
    public static def VALID_ADDRESS = "1.1.1.1"
    public static def INVALID_ADDRESS = "0.0.0.0"
    LoggerService loggerService

    boolean loadExternalBeans() {
        true
    }

    Closure doWithSpring() {{ ->
        loggerService LoggerService
    }}


    def setup() {
        //interceptor.loggerService = loggerService
    }

    def cleanup() {

    }

    void 'test service is not null'() {
        expect:
        loggerService != null
    }

    void 'test interceptor matches controller methods'() {
        when:
        request.remoteAddr = VALID_ADDRESS
        withRequest(controller: "logger", action: action)

        then:
        interceptor.doesMatch() == sensitive

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

    void 'test interceptor returns expected value for VALID address'() {
        when:
        loggerService = Stub(LoggerService) {
            findRemoteAddress(_) >>  new RemoteAddress(hostName: "validIP", ip: VALID_ADDRESS)
        }
        interceptor.loggerService = loggerService
        request.remoteAddr = VALID_ADDRESS
        withRequest(controller: "logger", action: action)
        def result = interceptor.before()

        then:
        result == sensitive

        where:
        action                      | sensitive
        "save"                      | true
        "monthlyBreakdown"          | true
        "getEventLog"               | true
        "getEventTypes"             | true
        "getReasonTypes"            | true
        "getSourceTypes"            | true
        "getReasonBreakdown"        | true
        "getReasonBreakdownByMonth" | true
        "getReasonBreakdownCSV"     | true
        "getEmailBreakdown"         | true
        "getEmailBreakdownCSV"      | true
        "getTotalsByEventType"      | true
        "getEntityBreakdown"        | true
    }

    void 'test interceptor returns expected value for INVALID address'() {
        when:
        loggerService = Stub(LoggerService) {
            findRemoteAddress(_) >> null
        }
        interceptor.loggerService = loggerService
        request.remoteAddr = VALID_ADDRESS
        withRequest(controller: "logger", action: action)
        def result = interceptor.before()

        then:
        result == sensitive

        where:
        action                      | sensitive
        "save"                      | false
        "monthlyBreakdown"          | false
        "getEventLog"               | false
        "getEventTypes"             | true
        "getReasonTypes"            | true
        "getSourceTypes"            | true
        "getReasonBreakdown"        | true
        "getReasonBreakdownByMonth" | true
        "getReasonBreakdownCSV"     | true
        "getEmailBreakdown"         | true
        "getEmailBreakdownCSV"      | true
        "getTotalsByEventType"      | true
        "getEntityBreakdown"        | true
    }

    void "Test controller execution with interceptor returning false"() {
        given:
        def controller = (LoggerController)mockController(LoggerController)

        when:
        request.remoteAddr = VALID_ADDRESS
        withInterceptors([controller: "logger"]) {
            controller.save()
        }

        then:
        response.status ==  HttpStatus.NOT_ACCEPTABLE.value()
    }

    void "Test controller execution with interceptor returning true"() {
//        setup:
//        interceptor.loggerService = loggerService

        given:
        loggerService = Stub(LoggerService) {
            findRemoteAddress(_) >>  new RemoteAddress(hostName: "validIP", ip: VALID_ADDRESS)
        }
        interceptor.loggerService = loggerService
        def controller = (LoggerController)mockController(LoggerController)

        when:
        request.remoteAddr = VALID_ADDRESS
        withInterceptors([controller: "logger"]) {
            controller.save()
        }
        interceptor.before()

        then:
        response.status ==  HttpStatus.OK.value()
    }

    def "IpAddressFilter should reject unrecognised request.remoteAddr IP addresses for sensitive actions"() {
        setup:
        def ra = new RemoteAddress(hostName: "valid", ip: VALID_ADDRESS).save()
//        interceptor.loggerService = loggerService
//        loggerService = Mock(LoggerService.class) {
//            1 * findRemoteAddress(_) >> [ra] // findRemoteAddress(String ipAddress)
//        }

//        given:
//        def loggerService = Stub(LoggerService) {
//            findRemoteAddress(_) >> null
//        }
//        def controller = (LoggerController)mockController(LoggerController)
//
//        and:
//        interceptor.loggerService = loggerService

        when:
        request.remoteAddr = INVALID_ADDRESS
        withRequest(controller: "logger", action: action)
//        interceptor.before()
//        withFilters(controller: "logger", action: action) {
//            controller."${action}"
//        }
        withInterceptors([controller: "logger"]) {
            controller."${action}"()
        }

        then:
        //interceptor.doesMatch()
        if (sensitive) assert response.status == 406
        if (!sensitive) assert response.status == 200

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
//        new RemoteAddress(hostName: "valid", ipAddress: VALID_ADDRESS).save()
//        defineBeans {
//            loggerService(MockLoggerService)
//        }

        when:
        request.remoteAddr = VALID_ADDRESS
        withRequest(controller: "logger", action: action)
        println "testing action: ${action}"
        interceptor.before()
//        withFilters(controller: "logger", action: action) {
//            controller.action
//        }

        then:
        //1 *
        _ * loggerService.findRemoteAddress(VALID_ADDRESS) >> [ hostName: "valid", ip: VALID_ADDRESS ]// Mock(LoggerService)
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
        new RemoteAddress(hostName: "valid", ipAddress: VALID_ADDRESS).save()
//        defineBeans {
//            loggerService(MockLoggerService)
//        }

        when:
        request.addHeader("X-Forwarded-For", INVALID_ADDRESS)
        withRequest(controller: "logger", action: action)
        //interceptor.before()
//        withFilters(controller: "logger", action: action) {
//            controller.action
//        }

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
//        defineBeans {
//            loggerService(MockLoggerService)
//        }

        when:
        request.addHeader("X-Forwarded-For", VALID_ADDRESS)
        withFilters(controller: "logger", action: action)

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

//    public static class MockLoggerService extends LoggerService {
//
//        public MockLoggerService() {
//        }
//
//        @Override
//        RemoteAddress findRemoteAddress(String ipAddress) {
//            if (VALID_ADDRESS == ipAddress) {
//                new RemoteAddress(hostName: "valid", ipAddress: VALID_ADDRESS)
//            }
//        }
//    }
}
