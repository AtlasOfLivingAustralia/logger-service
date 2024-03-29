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

import grails.core.GrailsApplication
import grails.plugins.rest.client.RestResponse
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import grails.plugins.rest.client.RestBuilder
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.util.logging.Log4j
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * Test rest functions
 */
@Log4j
@Integration(applicationClass = Application.class)
@Rollback
class RestFunctionalTestSpec extends Specification {

    @Shared RestBuilder rest = new RestBuilder()

    GrailsApplication grailsApplication

    private thisMonth
    private twoMonthsAgo
    private lastYear
    private twoYearsAgo

    private String getBaseUrl() {
        def url = "http://localhost:${serverPort}${grailsApplication.config.getProperty('server.contextPath', String, '')}"
        return url
    }

    def setup() {
        new RemoteAddress(ip: "1.1.1.1", hostName: "Valid 1").save(flush: true)

        new EventSummaryBreakdownReasonEntity(entityUid: "dp123", logEventTypeId: 1000, month: "202102", numberOfEvents: 2, recordCount: 1984, logReasonTypeId: 1).save(flush: true)
        new LogEventType(id: 1000, name: "type1").save(flush: true)
        new LogEventType(id: 1001, name: "OCCURRENCE_RECORDS_VIEWED_ON_MAP").save(flush: true)
        new LogEventType(id: 1002, name: "OCCURRENCE_RECORDS_DOWNLOADED").save(flush: true)
        new LogEventType(id: 2000, name: "IMAGE_VIEWED").save(flush: true)

        new LogReasonType(id: 0, name: "conservation management/planning", rkey: "logger.download.reason.conservation", defaultOrder: 1).save(flush: true)
        new LogReasonType(id: 1, name: "biosecurity management, planning", rkey: "logger.download.reason.biosecurity", defaultOrder: 10).save(flush: true)
        new LogReasonType(id: 10, name: "testing", rkey: "logger.download.reason.testing", defaultOrder: 100).save(flush: true)

        new LogSourceType(id: 0, name: "ALA").save(flush: true)
        new LogSourceType(id: 1, name: "OZCAM").save(flush: true)
        new LogSourceType(id: 2, name: "AVH").save(flush: true)

        use(TimeCategory) {
            thisMonth = getYearAndMonth(new Date())
            twoMonthsAgo = getYearAndMonth(new Date() - 2.months)
            lastYear = getYearAndMonth(new Date() - 10.months)
            twoYearsAgo = getYearAndMonth(new Date() - 2.years)
        }
    }

    def cleanup() {
    }

    def "POST to /logger with a valid request should create a record"() {
        when:
        String IpAddress = "1.1.1.1"

        RestResponse resp = rest.post("${baseUrl}/service/logger", {
            header "X-Forwarded-For", IpAddress
            header "user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.0.2 Safari/605.1.15"
            header "Accept", "application/json"
            contentType("application/json")
            json {
                [
                        "eventTypeId": 1000,
                        "reasonTypeId": 10,
                        "sourceTypeId": 1,
                        "sourceUrl": "https://ala.org.au/example.json",
                        "comment": "For doing some research with..",
                        "month": "2",
                        "userEmail": "fred.smith@bla.gov.au",
                        "userIP": "1.1.1.1",
                        "recordCounts": [ "dp123": 32, "dr143": 22,"ins322": 55 ]
                ]
            }
        })

        def jsonResponse

        if (resp.status == HttpStatus.OK.value()) {
            jsonResponse = new JsonSlurper().parseText(resp.body?:"{}")
        } else {
            jsonResponse = [error: resp.body]
        }

        then:
        resp.status == HttpStatus.OK.value()
        jsonResponse.logEvent?.id != null
        jsonResponse.logEvent?.logDetails?.size() == 3
    }

    def "GET to /service/logger/events should show eventTypes"() {
        when:
        RestResponse resp = rest.get("${baseUrl}/service/logger/events")
        def json = new JsonSlurper().parseText(resp.body?:"{}")

        then:
        resp.status == HttpStatus.OK.value()
        json.logEvent?.size() > 0
        json.logEvent?.id != null

    }

    def "Get record counts should return a result"() {
        when:
        def params = [q: "dp123", eventTypeId: 1000, year: "2021"]
        def paramsString = params.collect { k,v -> "$k=$v" }.join("&")
        RestResponse resp = rest.get("${baseUrl}/service/logger/get.json?${paramsString}")
        def json = resp.body

        then:
        assert json == """{"months":[["202102",1984]]}"""
    }

    def "Get event types should return a result"() {
        when:
        RestResponse resp = rest.get("${baseUrl}/service/logger/events")
        def json = resp.body

        then:
        assert json ==  """[{"name":"type1","id":1000},{"name":"OCCURRENCE_RECORDS_VIEWED_ON_MAP","id":1001},{"name":"OCCURRENCE_RECORDS_DOWNLOADED","id":1002},{"name":"IMAGE_VIEWED","id":2000}]"""
    }

    def "Get reason types should return a result"() {
        when:
        RestResponse resp = rest.get("${baseUrl}/service/logger/reasons")
        def json = resp.body

        then:
        assert json == """[{"rkey":"logger.download.reason.conservation","name":"conservation management/planning","id":0,"deprecated":false},{"rkey":"logger.download.reason.biosecurity","name":"biosecurity management, planning","id":1,"deprecated":false},{"rkey":"logger.download.reason.testing","name":"testing","id":10,"deprecated":false}]"""
    }

    def "Get source types should return a result"() {
        when:
        RestResponse resp = rest.get("${baseUrl}/service/logger/sources")
        def json = resp.body

        then:
        assert json == """[{"name":"ALA","id":0},{"name":"OZCAM","id":1},{"name":"AVH","id":2}]"""
    }

    /**
     * Returns year and month of a Date
     * @param inDate Date passed in
     * @return String of yyyyMM
     */
    private String getYearAndMonth(Date inDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMM")
        String outDate = inDate? dateFormat.format(inDate) : inDate
        outDate
    }
}
