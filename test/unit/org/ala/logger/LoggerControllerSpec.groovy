package org.ala.logger

import grails.test.mixin.TestFor
import groovy.time.TimeCategory
import org.springframework.http.HttpStatus
import spock.lang.Specification

import javax.persistence.PersistenceException

@TestFor(LoggerController)
class LoggerControllerSpec extends Specification {

    final VALID_JSON_REQUEST = """{ "eventTypeId": 1000, "comment":"test comment", "userEmail" : "fred@somewhere.gov.au", "userIP": "123.123.123.123", "recordCounts" : { "uid1": 100, "uid2": 200,} }""";

    private LoggerController controller
    private LoggerService loggerService
    private reasonTypes = []

    private String thisMonth
    private String nextMonth
    private String last3Months
    private String last12Months

    def setup() {
        controller = new LoggerController();
        loggerService = Mock(LoggerService)
        controller.loggerService = loggerService

        for (i in 1..10) {
            def reason = new LogReasonType(name: "reason${i}", rkey: "rkey${i}")
            reason.setId(i)
            reasonTypes << reason
        }
        loggerService.getAllReasonTypes() >> reasonTypes

        use(TimeCategory) {
            thisMonth = new Date().format("yyyyMM")
            nextMonth = (new Date() + 1.month).format("yyyyMM")
            last3Months = (new Date() - 2.months).format("yyyyMM")
            last12Months = (new Date() - 11.months).format("yyyyMM")
        }
    }

    def cleanup() {
    }

    def "save() should invoke the logger service save method"() {
        when: "the controller is sent a valid JSON request for a new log event"
        request.json = VALID_JSON_REQUEST
        controller.save()

        then: "the logger service createLog method should be called"
        1 * loggerService.createLog(!null, !null)
    }

    def "save() should return a HTTP 406 (not acceptable) if an exception occurs while saving"() {
        when: "an exception is thrown from the logger service's createLog method"
        request.json = VALID_JSON_REQUEST
        loggerService.createLog(*_) >> { throw new PersistenceException("test") }
        controller.save()

        then: "a http 406 (NOT_ACCEPTABLE) should be returned"
        assert response.status == HttpStatus.NOT_ACCEPTABLE.value()
    }

    def "save() should pull the 'user-agent' header from the request and save it with the log_event"() {
        when: "a request is made to save a new log event"
        request.addHeader("user-agent", "someUserAgent")
        request.json = VALID_JSON_REQUEST
        controller.save()

        then: "the user-agent header should be passed to the service as an additional parameter"
        1 * loggerService.createLog(*_) >> { arguments ->
            Map additionalParamsArg = arguments[1]
            assert additionalParamsArg["userAgent"] == "someUserAgent"
        }
    }

    def "Find log event should return 404 when no match is found"() {
        when: "there is no matching log event"
        params.id = 10
        loggerService.findLogEvent(_) >> null
        controller.getEventLog()

        then: "a http 404 should be returned"
        assert response.status == HttpStatus.NOT_FOUND.value()
    }

    def "Find log event should return a JSON view of the matching event"() {
        when: "there is a matching log event"
        params.id = 10
        LogEvent log = new LogEvent();
        log.setComment("test comment")
        log.setId(1)
        log.setLogEventTypeId(1)
        log.setLogReasonTypeId(10)
        log.setLogSourceTypeId(20)
        log.setMonth("201411")
        log.setSource("someSource")
        log.setSourceUrl("http://some.source.com")
        log.setUserEmail("fred@somewhere.gov.au")
        log.setUserIp("123.123.123.123")
        LogDetail detail1 = new LogDetail()
        detail1.setId(1)
        detail1.setEntityType("type1")
        detail1.setEntityUid("uid1")
        detail1.setLogEvent(log)
        detail1.setRecordCount(100)
        LogDetail detail2 = new LogDetail()
        detail2.setId(2)
        detail2.setEntityType("type2")
        detail2.setEntityUid("uid2")
        detail2.setLogEvent(log)
        detail2.setRecordCount(200)
        log.logDetails << detail1
        log.logDetails << detail2

        loggerService.findLogEvent(_) >> log
        controller.getEventLog();

        then: "the log event should be returned in JSON format"
        assert response.json.logEvent.userEmail == "fred@somewhere.gov.au"
        assert response.json.logEvent.userIp == "123.123.123.123"
        assert response.json.logEvent.logEventTypeId == 1
        assert response.json.logEvent.logReasonTypeId == 10
        assert response.json.logEvent.logSourceTypeId == 20
        assert response.json.logEvent.sourceUrl == "http://some.source.com"
        assert response.json.logEvent.month == "201411"
        assert response.json.logEvent.source == "someSource"
        assert response.json.logEvent.comment == "test comment"
        assert response.json.logEvent.id: 1
        assert response.json.logEvent.logDetails[0].entityUid == "uid1"
        assert response.json.logEvent.logDetails[0].recordCount == 100
        assert response.json.logEvent.logDetails[0].entityType == "type1"
        assert response.json.logEvent.logDetails[0].id == 1
        assert response.json.logEvent.logDetails[1].entityUid == "uid2"
        assert response.json.logEvent.logDetails[1].recordCount == 200
        assert response.json.logEvent.logDetails[1].entityType == "type2"
        assert response.json.logEvent.logDetails[1].id == 2
    }

    def "monthlyBreakdown requires request parameter 'q' for the entityUid"() {
        when: "a request is made with no 'q' parameter"
        params.eventTypeId = 12
        controller.monthlyBreakdown()

        then: "a http 400 (BAD_REQUEST) should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "monthlyBreakdown requires request parameter 'entityTypeId'"() {
        when: "a request is made with no 'eventTypeId' parameter"
        params.q = 12
        controller.monthlyBreakdown()

        then: "a http 400 (BAD_REQUEST) should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "monthlyBreakdown should default the year parameter to the current year if not provided"() {
        def thisYear = Calendar.getInstance().get(Calendar.YEAR) as String

        when: "a request is made with no 'year' parameter"
        params.q = 12
        params.eventTypeId = 1
        loggerService.getLogEventCount(_, _, _) >> [[201401, 123], [201403, 3211], [201404, 32]]
        controller.monthlyBreakdown()

        then: "then the controller should default the parameter to the current year"
        1 * loggerService.getLogEventCount(_, _, thisYear)
    }

    def "monthlyBreakdown returns a list of monthly record numbers"() {
        when: "a request is made with valid parameters"
        params.q = 12
        params.eventTypeId = 1
        params.year = "2014"
        loggerService.getLogEventCount(_, _, _) >> [["201401", 123], ["201403", 3211], ["201404", 32]]
        controller.monthlyBreakdown()

        then: "a valid response should be returned"
        assert response.status == HttpStatus.OK.value()
        assert response.json.months[0][0] == "201401" && response.json.months[0][1] == 123
        assert response.json.months[1][0] == "201403" && response.json.months[1][1] == 3211
        assert response.json.months[2][0] == "201404" && response.json.months[2][1] == 32
    }

    def "getReasonBreakdown should return 0 counts for all reason types when given an unrecognised entityUid"() {
        when: "a request is made with an unrecognised entityUid"
        params.entityUid = "unknown"
        params.eventId = 1000
        loggerService.getEventsReasonBreakdown(_, _, _, _) >> []
        controller.getReasonBreakdown()

        then: "a valid response with 0 counts should be returned"
        assert response.json.all.events == 0 && response.json.all.records == 0
        assert response.json.last3months.events == 0 && response.json.last3months.records == 0
        assert response.json.thisMonth.events == 0 && response.json.thisMonth.records == 0
        assert response.json.lastYear.events == 0 && response.json.lastYear.records == 0
        for (r in reasonTypes) {
            assert response.json.all.reasonBreakdown."${r.name}".events == 0
            assert response.json.last3months.reasonBreakdown."${r.name}".events == 0
            assert response.json.thisMonth.reasonBreakdown."${r.name}".events == 0
            assert response.json.lastYear.reasonBreakdown."${r.name}".events == 0
        }
    }

    def "getReasonBreakdown should return correct counts for recognised entities"() {
        when: "a request is made with a recognised entityUid"
        params.entityUid = "dr143"
        params.eventId = 1000
        loggerService.getEventsReasonBreakdown(_, _, _, _) >> [new EventSummaryBreakdownReason(logReasonTypeId: 1, numberOfEvents: 3, recordCount: 30),
                                                               new EventSummaryBreakdownReason(logReasonTypeId: 2, numberOfEvents: 7, recordCount: 70)]
        controller.getReasonBreakdown()

        then: "a valid response with correct counts should be returned"

        assert response.json.all.events == 10 && response.json.all.records == 100
        assert response.json.last3months.events == 10 && response.json.last3months.records == 100
        assert response.json.thisMonth.events == 10 && response.json.thisMonth.records == 100
        assert response.json.lastYear.events == 10 && response.json.lastYear.records == 100
        assert response.json.all.reasonBreakdown.reason1.events == 3 && response.json.all.reasonBreakdown.reason1.records == 30
        assert response.json.all.reasonBreakdown.reason2.events == 7 && response.json.all.reasonBreakdown.reason2.records == 70
    }

    def "getReasonBreakdown should look for this month, last 3 months, last 12 months and all time"() {
        when: "a breakdown is requested"
        params << [entityUid: "dr143", eventId: 1000]
        loggerService.getEventsReasonBreakdown(_, _, _, _) >> [new EventSummaryBreakdownReason(logReasonTypeId: 1, numberOfEvents: 3, recordCount: 30),
                                                               new EventSummaryBreakdownReason(logReasonTypeId: 2, numberOfEvents: 7, recordCount: 70)]
        controller.getReasonBreakdown()

        then: "the service method should be invoked 4 times with the relevant date ranges"
        1 * loggerService.getEventsReasonBreakdown(_, _, null, null) // all time
        1 * loggerService.getEventsReasonBreakdown(_, _, thisMonth, nextMonth) // this month
        1 * loggerService.getEventsReasonBreakdown(_, _, last3Months, nextMonth) // last 3 months
        1 * loggerService.getEventsReasonBreakdown(_, _, last12Months, nextMonth) // last 12 months

    }

    def "getReasonBreakdown should collate results from difference date ranges correctly"() {
        when: "a breakdown is requested"
        params << [entityUid: "dr143", eventId: 1000]
        loggerService.getEventsReasonBreakdown(_, _, null, null) >> [new EventSummaryBreakdownReason(logReasonTypeId: 1, numberOfEvents: 3, recordCount: 30)]
        loggerService.getEventsReasonBreakdown(_, _, thisMonth, nextMonth) >> [new EventSummaryBreakdownReason(logReasonTypeId: 2, numberOfEvents: 6, recordCount: 40)]
        loggerService.getEventsReasonBreakdown(_, _, last3Months, nextMonth) >> [new EventSummaryBreakdownReason(logReasonTypeId: 3, numberOfEvents: 8, recordCount: 50)]
        loggerService.getEventsReasonBreakdown(_, _, last12Months, nextMonth) >> [new EventSummaryBreakdownReason(logReasonTypeId: 4, numberOfEvents: 10, recordCount: 60)]
        controller.getReasonBreakdown()

        then: "the results should be collated properly"
        assert response.json.all.events == 3 && response.json.all.records == 30
        assert response.json.last3months.events == 8 && response.json.last3months.records == 50
        assert response.json.thisMonth.events == 6 && response.json.thisMonth.records == 40
        assert response.json.lastYear.events == 10 && response.json.lastYear.records == 60
    }

    def "getReasonBreakdown should set reason name to 'unclassified' if an unrecognised reason is found"() {
        when: "the retrieved data contains an unrecognised reason type"
        params.entityUid = "dr143"
        params.eventId = 1000
        loggerService.getEventsReasonBreakdown(_, _, _, _) >> [new EventSummaryBreakdownReason(logReasonTypeId: 11111, numberOfEvents: 3, recordCount: 30)]
        controller.getReasonBreakdown()

        then: "'unclassified' should be used as the reason name in the response"
        assert response.json.all.reasonBreakdown.unclassified.events == 3 && response.json.all.reasonBreakdown.unclassified.records == 30
    }

    def "getReasonBreakdown requires an eventId parameter"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        controller.getReasonBreakdown()

        then: "a http 400 bad request should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getReasonBreakdownMonthly requires an eventId parameter"() {
        when: "a request is made with no 'eventId' parameter"
        params.entityUid = "dr143"
        controller.getReasonBreakdownByMonth()

        then: "a http 400 (BAD_REQUEST) should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getReasonBreakdownMonthly requires an entityUid parameter"() {
        when: "a request is made with no 'entityUid' parameter"
        params.eventId = 1000
        controller.getReasonBreakdownByMonth()

        then: "a http 400 (BAD_REQUEST) should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getReasonBreakdownMonthly returns an empty JSON response when no matching records are found"() {
        when:
        params << [eventId: 1000, entityUid: "dr143"]
        loggerService.getTemporalEventsReasonBreakdown(_, _, _) >> []
        controller.getReasonBreakdownByMonth()

        then:
        assert response.text == "{\"temporalBreakdown\":{}}"
    }

    def "getReasonBreakdownMonthly should return correct counts when given a valid request"() {
        when:
        params << [eventId: 1000, entityUid: "dr143"]
        loggerService.getTemporalEventsReasonBreakdown(_, _, _) >> [new EventSummaryBreakdownReasonEntity(month: "201410", recordCount: 20, numberOfEvents: 4),
                                                                    new EventSummaryBreakdownReasonEntity(month: "201411", recordCount: 10, numberOfEvents: 2)]
        controller.getReasonBreakdownByMonth()

        then:
        assert response.json.temporalBreakdown."201410".records == 20 && response.json.temporalBreakdown."201410".events == 4
        assert response.json.temporalBreakdown."201411".records == 10 && response.json.temporalBreakdown."201411".events == 2
    }

    def "getReasonBreakdownCSV requires an eventId parameter"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        controller.getReasonBreakdownCSV()

        then: "a http 400 bad request should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getReasonBreakdownCSV should return a CSV with just the column headers for an unrecognised entityUid"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        controller.getReasonBreakdownCSV()

        then: "a http 400 bad request should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getReasonBreakdownCSV should return a CSV with just the column headers for unrecognised ids"() {
        when: "a request is made with an unrecognised eventId and/or entityUid parameter"
        params.entityUid = "1234"
        params.eventId = 100

        loggerService.getLogEventsByReason(_, _) >> []
        controller.getReasonBreakdownCSV()

        then: "a csv with just the column headers should be returned"
        assert response.status == HttpStatus.OK.value()
        assert response.text == "\"year\",\"month\",\"reason\",\"number of events\",\"number of records\""
        assert response.contentType == "text/csv"
        assert response.getHeader("Content-Disposition") == "attachment; filename=\"downloads-by-reason-1234.csv\""
    }

    def "getReasonBreakdownCSV should return a CSV with correct counts for a valid request"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        params.eventId = 100

        loggerService.getLogEventsByReason(_, _) >> [new EventSummaryBreakdownReasonEntity(logReasonTypeId: 1, month: "201411", recordCount: 10, numberOfEvents: 2),
                                                     new EventSummaryBreakdownReasonEntity(logReasonTypeId: 2, month: "201411", recordCount: 200, numberOfEvents: 20)]
        controller.getReasonBreakdownCSV()

        then: "a csv with the correct counts should be returned"
        assert response.status == HttpStatus.OK.value()
        assert response.text == "\"year\",\"month\",\"reason\",\"number of events\",\"number of records\"\n" +
                "\"2014\",\"11\",\"reason1\",\"2\",\"10\"\n" +
                "\"2014\",\"11\",\"reason2\",\"20\",\"200\""
        assert response.contentType == "text/csv"
        assert response.getHeader("Content-Disposition") == "attachment; filename=\"downloads-by-reason-1234.csv\""
    }

    def "getEmailBreakdown should return 0 counts for unrecognised entities"() {
        when: "a request is made with an unrecognised entityUid"
        params.entityUid = "unknown"
        params.eventId = 1000
        loggerService.getEventsEmailBreakdown(_, _, _, _) >> []
        controller.getEmailBreakdown()

        then: "a valid response with 0 counts should be returned"
        assert response.json.all.events == 0 && response.json.all.records == 0
        assert response.json.last3months.events == 0 && response.json.last3months.records == 0
        assert response.json.thisMonth.events == 0 && response.json.thisMonth.records == 0
        assert response.json.lastYear.events == 0 && response.json.lastYear.records == 0
        assert response.json.all.emailBreakdown.edu.events == 0
        assert response.json.all.emailBreakdown.gov.events == 0
        assert response.json.all.emailBreakdown.other.events == 0
        assert response.json.all.emailBreakdown.unspecified.events == 0
    }

    def "getEmailBreakdown should return correct counts for recognised entities"() {
        when: "a request is made with a recognised entityUid"
        params.entityUid = "unknown"
        params.eventId = 1000
        loggerService.getEventsEmailBreakdown(_, _, _, _) >> [new EventSummaryBreakdownEmail(userEmailCategory: "edu", numberOfEvents: 3, recordCount: 30),
                                                              new EventSummaryBreakdownEmail(userEmailCategory: "gov", numberOfEvents: 7, recordCount: 70)]
        controller.getEmailBreakdown()

        then: "a valid response with correct counts should be returned"
        assert response.json.all.events == 10 && response.json.all.records == 100
        assert response.json.last3months.events == 10 && response.json.last3months.records == 100
        assert response.json.thisMonth.events == 10 && response.json.thisMonth.records == 100
        assert response.json.lastYear.events == 10 && response.json.lastYear.records == 100
        assert response.json.all.emailBreakdown.edu.events == 3
        assert response.json.all.emailBreakdown.gov.events == 7
        assert response.json.all.emailBreakdown.other.events == 0
        assert response.json.all.emailBreakdown.unspecified.events == 0
    }

    def "getEmailBreakdown should look for this month, last 3 months, last 12 months and all time"() {
        when: "a breakdown is requested"
        params << [entityUid: "dr143", eventId: 1000]
        loggerService.getEventsEmailBreakdown(_, _, _, _) >> [new EventSummaryBreakdownEmail(userEmailCategory: "edu", numberOfEvents: 3, recordCount: 30),
                                                              new EventSummaryBreakdownEmail(userEmailCategory: "org", numberOfEvents: 7, recordCount: 70)]
        controller.getEmailBreakdown()

        then: "the service method should be invoked 4 times with the relevant date ranges"
        1 * loggerService.getEventsEmailBreakdown(_, _, null, null) // all time
        1 * loggerService.getEventsEmailBreakdown(_, _, thisMonth, nextMonth) // this month
        1 * loggerService.getEventsEmailBreakdown(_, _, last3Months, nextMonth) // last 3 months
        1 * loggerService.getEventsEmailBreakdown(_, _, last12Months, nextMonth) // last 12 months
    }

    def "getEmailBreakdown should collate results from difference date ranges correctly"() {
        when: "a breakdown is requested"
        params << [entityUid: "dr143", eventId: 1000]
        loggerService.getEventsEmailBreakdown(_, _, null, null) >> [new EventSummaryBreakdownEmail(userEmailCategory: "edu", numberOfEvents: 3, recordCount: 30)]
        loggerService.getEventsEmailBreakdown(_, _, thisMonth, nextMonth) >> [new EventSummaryBreakdownEmail(userEmailCategory: "gov", numberOfEvents: 6, recordCount: 40)]
        loggerService.getEventsEmailBreakdown(_, _, last3Months, nextMonth) >> [new EventSummaryBreakdownEmail(userEmailCategory: "other", numberOfEvents: 8, recordCount: 50)]
        loggerService.getEventsEmailBreakdown(_, _, last12Months, nextMonth) >> [new EventSummaryBreakdownEmail(userEmailCategory: "unspecified", numberOfEvents: 10, recordCount: 60)]
        controller.getEmailBreakdown()

        then: "the results should be collated properly"
        assert response.json.all.events == 3 && response.json.all.records == 30
        assert response.json.last3months.events == 8 && response.json.last3months.records == 50
        assert response.json.thisMonth.events == 6 && response.json.thisMonth.records == 40
        assert response.json.lastYear.events == 10 && response.json.lastYear.records == 60
    }

    def "getEmailBreakdown requires an eventId parameter"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        controller.getEmailBreakdown()

        then: "a http 400 bad request should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getEmailBreakdownCSV requires an eventId parameter"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        controller.getEmailBreakdownCSV()

        then: "a http 400 bad request should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getEmailBreakdownCSV should return a CSV with just the column headers for an unrecognised entityUid"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        controller.getEmailBreakdownCSV()

        then: "a http 400 bad request should be returned"
        assert response.status == HttpStatus.BAD_REQUEST.value()
    }

    def "getEmailBreakdownCSV should return a CSV with just the column headers for unrecognised ids"() {
        when: "a request is made with an unrecognised eventId and/or entityUid parameter"
        params.entityUid = "1234"
        params.eventId = 100
        loggerService.getLogEventsByEmail(_, _) >> []
        controller.getEmailBreakdownCSV()

        then: "a csv with just the column headers should be returned"
        assert response.status == HttpStatus.OK.value()
        assert response.text == "\"year\",\"month\",\"user category\",\"number of events\",\"number of records\""
        assert response.contentType == "text/csv"
        assert response.getHeader("Content-Disposition") == "attachment; filename=\"downloads-by-email-1234.csv\""
    }

    def "getEmailBreakdownCSV should return a CSV with correct counts for a valid request"() {
        when: "a request is made without the eventId parameter"
        params.entityUid = "1234"
        params.eventId = 100
        loggerService.getLogEventsByEmail(_, _) >> [new EventSummaryBreakdownEmailEntity(userEmailCategory: "edu", month: "201411", recordCount: 10, numberOfEvents: 2),
                                                    new EventSummaryBreakdownEmailEntity(userEmailCategory: "gov", month: "201411", recordCount: 200, numberOfEvents: 20)]
        controller.getEmailBreakdownCSV()

        then: "a csv with the correct counts should be returned"
        assert response.status == HttpStatus.OK.value()
        assert response.text == "\"year\",\"month\",\"user category\",\"number of events\",\"number of records\"\n" +
                "\"2014\",\"11\",\"edu\",\"2\",\"10\"\n" +
                "\"2014\",\"11\",\"gov\",\"20\",\"200\""
        assert response.contentType == "text/csv"
        assert response.getHeader("Content-Disposition") == "attachment; filename=\"downloads-by-email-1234.csv\""
    }

    def "getTotalsByEventType should return correct counts"() {
        when: "a valid request is made"
        loggerService.getEventTypeBreakdown() >> [new EventSummaryTotal(logEventTypeId: 1000, numberOfEvents: 10, recordCount: 100),
                                                  new EventSummaryTotal(logEventTypeId: 200, numberOfEvents: 20, recordCount: 200)]
        controller.getTotalsByEventType()

        then: "the correct counts should be returned in JSON format"
        assert response.json.totals != null & controller.response.json.totals != ""
        assert response.json.totals."1000".events == 10 && response.json.totals."1000".records == 100
        assert response.json.totals."200".events == 20 && response.json.totals."200".records == 200
    }

    def "getEntityBreakdown should return 0 counts for unrecognised entities"() {
        when: "a request is made with an unrecognised entityUid"
        params.entityUid = "unknown"
        params.eventId = 1000
        loggerService.getLogEventsByEntity(_, _, _, _) >> []
        controller.getEntityBreakdown()

        then: "a valid response with 0 counts should be returned"
        assert response.json.all.numberOfEvents == 0 && response.json.all.numberOfEventItems == 0
        assert response.json.last3months.numberOfEvents == 0 && response.json.last3months.numberOfEventItems == 0
        assert response.json.thisMonth.numberOfEvents == 0 && response.json.thisMonth.numberOfEventItems == 0
        assert response.json.lastYear.numberOfEvents == 0 && response.json.lastYear.numberOfEventItems == 0
    }

    def "getEntityBreakdown should return correct counts for recognised entities"() {
        when: "a request is made with a recognised entityUid"
        params.entityUid = "unknown"
        params.eventId = 1000
        loggerService.getLogEventsByEntity(_, _, _, _) >> [new EventSummaryBreakdownReason(numberOfEvents: 3, recordCount: 30),
                                                           new EventSummaryBreakdownReason(numberOfEvents: 7, recordCount: 70)]
        controller.getEntityBreakdown()

        then: "a valid response with correct counts should be returned"
        assert response.json.all.numberOfEvents == 10 && response.json.all.numberOfEventItems == 100
        assert response.json.last3months.numberOfEvents == 10 && response.json.last3months.numberOfEventItems == 100
        assert response.json.thisMonth.numberOfEvents == 10 && response.json.thisMonth.numberOfEventItems == 100
        assert response.json.lastYear.numberOfEvents == 10 && response.json.lastYear.numberOfEventItems == 100
    }

    def "getEntityBreakdown should look for this month, last 3 months, last 12 months and all time"() {
        when: "a breakdown is requested"
        params << [entityUid: "dr143", eventId: 1000]
        loggerService.getLogEventsByEntity(_, _, _, _) >> [new EventSummaryBreakdownReason(reasonTypeId: 1, eventCount: 3, recordCount: 30),
                                                           new EventSummaryBreakdownReason(reasonTypeId: 2, eventCount: 7, recordCount: 70)]
        controller.getEntityBreakdown()

        then: "the service method should be invoked 4 times with the relevant date ranges"
        1 * loggerService.getLogEventsByEntity(_, _, null, null) // all time
        1 * loggerService.getLogEventsByEntity(_, _, thisMonth, nextMonth) // this month
        1 * loggerService.getLogEventsByEntity(_, _, last3Months, nextMonth) // last 3 months
        1 * loggerService.getLogEventsByEntity(_, _, last12Months, nextMonth) // last 12 months
    }

    def "getEntityBreakdown should collate results from difference date ranges correctly"() {
        when: "a breakdown is requested"
        params << [entityUid: "dr143", eventId: 1000]
        loggerService.getLogEventsByEntity(_, _, null, null) >> [new EventSummaryBreakdownReason(numberOfEvents: 3, recordCount: 30)]
        loggerService.getLogEventsByEntity(_, _, thisMonth, nextMonth) >> [new EventSummaryBreakdownReason(numberOfEvents: 6, recordCount: 40)]
        loggerService.getLogEventsByEntity(_, _, last3Months, nextMonth) >> [new EventSummaryBreakdownReason(numberOfEvents: 8, recordCount: 50)]
        loggerService.getLogEventsByEntity(_, _, last12Months, nextMonth) >> [new EventSummaryBreakdownReason(numberOfEvents: 10, recordCount: 60)]
        controller.getEntityBreakdown()

        then: "the results should be collated properly"
        assert response.json.all.numberOfEvents == 3 && response.json.all.numberOfEventItems == 30
        assert response.json.last3months.numberOfEvents == 8 && response.json.last3months.numberOfEventItems == 50
        assert response.json.thisMonth.numberOfEvents == 6 && response.json.thisMonth.numberOfEventItems == 40
        assert response.json.lastYear.numberOfEvents == 10 && response.json.lastYear.numberOfEventItems == 60
    }

    def "getAllEventTypes should return event types in the correct JSON format"() {
        when:
        def event1 = new LogEventType(name: "event1")
        event1.setId(1)
        def event2 = new LogEventType(name: "event2")
        event2.setId(2)
        def event3 = new LogEventType(name: "event3")
        event3.setId(3)

        loggerService.getAllEventTypes() >> [event1, event2, event3]
        controller.getEventTypes()

        then:
        assert response.text == """[{"name":"event1","id":1},{"name":"event2","id":2},{"name":"event3","id":3}]"""
    }

    def "getAllSourceTypes should return event types in the correct JSON format"() {
        when:
        def source1 = new LogSourceType(name: "source1")
        source1.setId(1)
        def source2 = new LogSourceType(name: "source2")
        source2.setId(2)
        def source3 = new LogSourceType(name: "source3")
        source3.setId(3)

        loggerService.getAllSourceTypes() >> [source1, source2, source3]
        controller.getSourceTypes()

        then:
        assert response.text == """[{"name":"source1","id":1},{"name":"source2","id":2},{"name":"source3","id":3}]"""
    }

    def "getAllReasonTypes should return event types in the correct JSON format"() {
        loggerService = Mock(LoggerService)
        controller.loggerService = loggerService

        when:
        def reason1 = new LogReasonType(name: "reason1", rkey: "key1")
        reason1.setId(1)
        def reason2 = new LogReasonType(name: "reason2", rkey: "key2")
        reason2.setId(2)
        def reason3 = new LogReasonType(name: "reason3", rkey: "key3")
        reason3.setId(3)

        loggerService.getAllReasonTypes() >> [reason1, reason2, reason3]
        controller.getReasonTypes()

        then:
        assert response.text == """[{"rkey":"key1","name":"reason1","id":1},{"rkey":"key2","name":"reason2","id":2},{"rkey":"key3","name":"reason3","id":3}]"""
    }
}
