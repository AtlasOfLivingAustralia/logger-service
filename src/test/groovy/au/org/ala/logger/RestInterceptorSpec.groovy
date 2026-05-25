package au.org.ala.logger

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class RestInterceptorSpec extends Specification implements InterceptorUnitTest<RestInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test rest interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"logger")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }

    void "Test unreadValidatedTasks interceptor matching"() {
        when: "A request does not matche the interceptor"
        withRequest(controller: "public", action: 'unreadValidatedTasks')

        then: "The interceptor does match"
        interceptor.doesMatch() == false
    }
}
