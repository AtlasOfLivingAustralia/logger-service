package org.ala.logger

import groovy.time.TimeCategory
import org.springframework.http.HttpStatus
import spock.lang.Specification

/**
 * This test performs a simple 'happy scenario' test against each method of the LoggerController to ensure basic
 * end to end functionality is working
 */
class BasicHealthCheckSpec extends Specification {

    private thisMonth
    private twoMonthsAgo
    private lastYear
    private twoYearsAgo

    def setup() {
        use(TimeCategory) {
            thisMonth = new Date().format("yyyyMM")
            twoMonthsAgo = (new Date() - 2.months).format("yyyyMM")
            lastYear = (new Date() - 10.months).format("yyyyMM")
            twoYearsAgo = (new Date() - 2.years).format("yyyyMM")
        }
    }

    def "POST to /logger with a valid request should create a record"() {
        new LogEventType(id: 1000, name: "event1").save()
        new LogReasonType(id: 10, name: "event1", rkey: "rkey1").save()

        def controller = new LoggerController()

        when: "a POST is made to /logger with a valid request"
        controller.request.contentType = "text/json"
        controller.request.content = """{"eventTypeId": 1000, "reasonTypeId":10,"comment":"For doing some research with..","userEmail" : "fred.smith@bla.gov.au","userIP": "123.123.123.123","recordCounts" : { "dp123": 32, "dr143": 22,"ins322": 55 } }""".bytes

        controller.save()

        then: "a new record should be created"
        assert controller.response.status == HttpStatus.OK.value()
        assert controller.response.json.logEvent.id != null
    }

    def "Get record counts should return a result"() {
        new EventSummaryBreakdownReasonEntity(entityUid: "dp123", logEventTypeId: 1000, month: "201411", numberOfEvents: 2, recordCount: 1984, logReasonTypeId: 1).save()

        def controller = new LoggerController()

        when:
        controller.request.contentType = "text/json"
        controller.params << [q: "dp123", eventTypeId: 1000, year: "2014"]

        controller.monthlyBreakdown()

        then:
        assert controller.response.text == """{"months":[["201411",1984]]}"""
    }

    def "Get event types should return a result"() {
        new LogEventType(id: 1000, name: "type1").save()
        new LogEventType(id: 1001, name: "OCCURRENCE_RECORDS_VIEWED_ON_MAP").save()
        new LogEventType(id: 1002, name: "OCCURRENCE_RECORDS_DOWNLOADED").save()
        new LogEventType(id: 2000, name: "IMAGE_VIEWED").save()

        def controller = new LoggerController()

        when:
        controller.getEventTypes()

        then:
        assert controller.response.text == """[{"name":"type1","id":1000},{"name":"OCCURRENCE_RECORDS_VIEWED_ON_MAP","id":1001},{"name":"OCCURRENCE_RECORDS_DOWNLOADED","id":1002},{"name":"IMAGE_VIEWED","id":2000}]"""
    }

    def "Get reason types should return a result"() {
        new LogReasonType(id: 0, name: "conservation management/planning", rkey: "logger.download.reason.conservation").save()
        new LogReasonType(id: 1, name: "biosecurity management, planning", rkey: "logger.download.reason.biosecurity").save()

        def controller = new LoggerController()

        when:
        controller.getReasonTypes()

        then:
        assert controller.response.text == """[{"rkey":"logger.download.reason.conservation","name":"conservation management/planning","id":0},{"rkey":"logger.download.reason.biosecurity","name":"biosecurity management, planning","id":1}]"""
    }

    def "Get source types should return a result"() {
        new LogSourceType(id: 0, name: "ALA").save()
        new LogSourceType(id: 1, name: "OZCAM").save()
        new LogSourceType(id: 2, name: "AVH").save()

        def controller = new LoggerController()

        when:
        controller.getSourceTypes()

        then:
        assert controller.response.text == """[{"name":"ALA","id":0},{"name":"OZCAM","id":1},{"name":"AVH","id":2}]"""
    }

    def "Get event log should return a result"() {
        new LogEventType(id: 1, name: "type1").save()
        def logEvent = new LogEvent(comment: "comment",
                logEventTypeId: 1,
                month: "201412",
                source: "source",
                userEmail: "email@email.com",
                sourceUrl: "http://blabla.com",
                dateCreated: new Date(),
                userIp: "123.123.123.123").save(validate: true, )
        def controller = new LoggerController()

        when:
        controller.params.id = logEvent.id
        controller.getEventLog()

        then:
        assert controller.response.json.logEvent.id == logEvent.id
    }

    def "Get reason breakdown should return a result"() {
        new LogReasonType(id: 1, name: "reason1", rkey: "reason1").save()
        new LogReasonType(id: 2, name: "reason2", rkey: "reason2").save()
        new LogReasonType(id: 3, name: "reason3", rkey: "reason3").save()
        new LogReasonType(id: 4, name: "reason4", rkey: "reason4").save()
        new LogReasonType(id: 5, name: "reason5", rkey: "reason5").save()
        new LogReasonType(id: 6, name: "reason6", rkey: "reason6").save()

        new EventSummaryBreakdownReasonEntity(month: thisMonth, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: thisMonth, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: twoMonthsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: twoMonthsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 4, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: lastYear, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 5, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: lastYear, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 6, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: twoYearsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 5, numberOfEvents: 8, recordCount: 35).save()
        new EventSummaryBreakdownReasonEntity(month: twoYearsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 6, numberOfEvents: 9, recordCount: 40).save()

        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1, entityUid: "dr1"]
        controller.getReasonBreakdown()

        then:
        assert controller.response.json.thisMonth.events == 5 && controller.response.json.thisMonth.records == 15
        assert controller.response.json.thisMonth.reasonBreakdown.reason1.events == 2 && controller.response.json.thisMonth.reasonBreakdown.reason1.records == 5
        assert controller.response.json.last3months.events == 14 && controller.response.json.last3months.records == 50
        assert controller.response.json.last3months.reasonBreakdown.reason3.events == 4 && controller.response.json.last3months.reasonBreakdown.reason3.records == 15
        assert controller.response.json.lastYear.events == 27 && controller.response.json.lastYear.records == 105
        assert controller.response.json.lastYear.reasonBreakdown.reason5.events == 6 && controller.response.json.lastYear.reasonBreakdown.reason5.records == 25
        assert controller.response.json.all.events == 44 && controller.response.json.all.records == 180
    }

    def "Get email breakdown should return a result"() {
        new EventSummaryBreakdownEmailEntity(month: thisMonth, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownEmailEntity(month: thisMonth, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "gov", numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownEmailEntity(month: twoMonthsAgo, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownEmailEntity(month: twoMonthsAgo, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "gov", numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownEmailEntity(month: lastYear, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownEmailEntity(month: lastYear, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "gov", numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownEmailEntity(month: twoYearsAgo, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 8, recordCount: 35).save()
        new EventSummaryBreakdownEmailEntity(month: twoYearsAgo, entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "gov", numberOfEvents: 9, recordCount: 40).save()

        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1, entityUid: "dr1"]
        controller.getEmailBreakdown()

        then:
        assert controller.response.json.thisMonth.events == 5 && controller.response.json.thisMonth.records == 15
        assert controller.response.json.thisMonth.emailBreakdown.edu.events == 2 && controller.response.json.thisMonth.emailBreakdown.edu.records == 5
        assert controller.response.json.last3months.events == 14 && controller.response.json.last3months.records == 50
        assert controller.response.json.last3months.emailBreakdown.gov.events == 8 && controller.response.json.last3months.emailBreakdown.gov.records == 30
        assert controller.response.json.lastYear.events == 27 && controller.response.json.lastYear.records == 105
        assert controller.response.json.lastYear.emailBreakdown.edu.events == 12 && controller.response.json.lastYear.emailBreakdown.edu.records == 45
        assert controller.response.json.all.events == 44 && controller.response.json.all.records == 180
    }

    def "Get monthly reason breakdown should return a result"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()


        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1, entityUid: "dr1"]
        controller.getReasonBreakdownByMonth()

        then:
        assert controller.response.json.temporalBreakdown."201411".records == 10 && controller.response.json.temporalBreakdown."201411".events == 3
        assert controller.response.json.temporalBreakdown."201410".records == 5 && controller.response.json.temporalBreakdown."201410".events == 2
    }

    def "Get totals by event type should return a result"() {
        new EventSummaryTotal(month: "201410", logEventTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryTotal(month: "201411", logEventTypeId: 2, numberOfEvents: 3, recordCount: 10).save()

        def controller = new LoggerController()

        when:
        controller.getTotalsByEventType()

        then:
        assert controller.response.json.totals."1".records == 5 && controller.response.json.totals."1".events == 2
        assert controller.response.json.totals."2".records == 10 && controller.response.json.totals."2".events == 3
    }

    def "Get reason breakdown csv should return a result"() {
        new LogReasonType(id: 1, name: "reason1", rkey: "reason1").save()
        new LogReasonType(id: 2, name: "reason2", rkey: "reason2").save()
        new LogReasonType(id: 3, name: "reason3", rkey: "reason3").save()

        new EventSummaryBreakdownReasonEntity(month: "201409", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201311", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 4, recordCount: 15).save()

        def controller = new LoggerController()
        when:
        controller.params << [eventId: 1, entityUid: "dr1"]
        controller.getReasonBreakdownCSV()

        then:
        assert controller.response.text == "\"year\",\"month\",\"reason\",\"number of events\",\"number of records\"\n" +
                "\"2014\",\"10\",\"reason3\",\"4\",\"15\"\n" +
                "\"2014\",\"09\",\"reason1\",\"2\",\"5\"\n" +
                "\"2013\",\"11\",\"reason2\",\"3\",\"10\""
    }

    def "Get email breakdown csv should return a result"() {
        new EventSummaryBreakdownEmailEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownEmailEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "gov", numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownEmailEntity(month: "201309", entityUid: "dr1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 4, recordCount: 15).save()

        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1, entityUid: "dr1"]
        controller.getEmailBreakdownCSV()

        then:
        assert controller.response.text == "\"year\",\"month\",\"user category\",\"number of events\",\"number of records\"\n" +
                "\"2014\",\"10\",\"edu\",\"2\",\"5\"\n" +
                "\"2014\",\"10\",\"gov\",\"3\",\"10\"\n" +
                "\"2013\",\"09\",\"edu\",\"4\",\"15\""
    }

    def "Get entity breakdown should return a result"() {
        new LogReasonType(id: 1, name: "reason1", rkey: "reason1").save()
        new LogReasonType(id: 2, name: "reason2", rkey: "reason2").save()
        new LogReasonType(id: 3, name: "reason3", rkey: "reason3").save()
        new LogReasonType(id: 4, name: "reason4", rkey: "reason4").save()
        new LogReasonType(id: 5, name: "reason5", rkey: "reason5").save()
        new LogReasonType(id: 6, name: "reason6", rkey: "reason6").save()

        new EventSummaryBreakdownReasonEntity(month: thisMonth, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: thisMonth, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: twoMonthsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: twoMonthsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 4, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: lastYear, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 5, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: lastYear, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 6, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: twoYearsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 5, numberOfEvents: 8, recordCount: 35).save()
        new EventSummaryBreakdownReasonEntity(month: twoYearsAgo, entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 6, numberOfEvents: 9, recordCount: 40).save()

        def controller = new LoggerController()

        when:
        controller.params << [entityUid: "dr1", eventId: 1]
        controller.getEntityBreakdown()

        then:
        assert controller.response.json.thisMonth.numberOfEvents == 5 && controller.response.json.thisMonth.numberOfEventItems == 15
        assert controller.response.json.last3months.numberOfEvents == 14 && controller.response.json.last3months.numberOfEventItems == 50
        assert controller.response.json.lastYear.numberOfEvents == 27 && controller.response.json.lastYear.numberOfEventItems == 105
        assert controller.response.json.all.numberOfEvents == 44 && controller.response.json.all.numberOfEventItems == 180
    }
}
