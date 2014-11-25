package org.ala.logger

import spock.lang.Specification

/**
 * This test performs a simple 'happy scenario' test against each method of the LoggerController to ensure basic
 * end to end functionality is working
 */
class BasicHealthCheckSpec extends Specification {

    def "POST to /logger with a valid request should create a record"() {
        def controller = new LoggerController()

        when: "a POST is made to /logger with a valid request"
        controller.request.contentType = "text/json"
        controller.request.content = """{"eventTypeId": 1000, "reasonTypeId":10,"comment":"For doing some research with..","userEmail" : "fred.smith@bla.gov.au","userIP": "123.123.123.123","recordCounts" : { "dp123": 32, "dr143": 22,"ins322": 55 } }""".bytes

        controller.save()

        then: "a new record should be created"
        assert controller.response.json.logEvent.id != null
    }

    def "Get record counts should return a result"() {
        def controller = new LoggerController()

        when:
        controller.request.contentType = "text/json"
        controller.params << [q: "dp123", eventTypeId: 1000, year: 2014]

        controller.monthlyBreakdown()

        then:
        assert controller.response.text == """{"months":[["201411",1984]]}"""
    }

    def "Get event types should return a result"() {
        def controller = new LoggerController()

        when:
        controller.getEventTypes()

        then:
        assert controller.response.text == """[{"name":"type1","id":1000},{"name":"OCCURRENCE_RECORDS_VIEWED_ON_MAP","id":1001},{"name":"OCCURRENCE_RECORDS_DOWNLOADED","id":1002},{"name":"IMAGE_VIEWED","id":2000}]"""
    }

    def "Get reason types should return a result"() {
        def controller = new LoggerController()

        when:
        controller.getReasonTypes()

        then:
        assert controller.response.text == """[{"rkey":"logger.download.reason.conservation","name":"conservation management/planning","id":0},{"rkey":"logger.download.reason.biosecurity","name":"biosecurity management, planning","id":1},{"rkey":null,"name":"reason2","id":2},{"rkey":null,"name":"reason3","id":3},{"rkey":"logger.download.reason.research","name":"scientific research","id":4},{"rkey":"logger.download.reason.collection.mgmt","name":"collection management","id":5},{"rkey":"logger.download.reason.other","name":"other","id":6},{"rkey":"logger.download.reason.ecological.research","name":"ecological research","id":7},{"rkey":"logger.download.reason.systematic.research","name":"systematic research","id":8},{"rkey":"logger.download.reason.other.scientific.research","name":"other scientific research","id":9},{"rkey":null,"name":"reason1","id":10},{"rkey":"dummy","name":"dummy","id":11}]"""
    }

    def "Get source types should return a result"() {
        def controller = new LoggerController()

        when:
        controller.getSourceTypes()

        then:
        assert controller.response.text == """[{"name":"ALA","id":0},{"name":"OZCAM","id":1},{"name":"AVH","id":2}]"""
    }

    def "Get event log should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params.id = 1047000904
        controller.getEventLog()

        then:
        assert controller.response.text == """{"logEvent":{"logDetails":[{"entityUid":"dp123","recordCount":32,"entityType":"1000","id":205},{"entityUid":"ins322","recordCount":55,"entityType":"1000","id":206},{"entityUid":"dr143","recordCount":22,"entityType":"1000","id":204}],"created":1416788671000,"userEmail":"fred.smith@bla.gov.au","userIp":"123.123.123.123","logEventTypeId":1000,"logReasonTypeId":10,"logSourceTypeId":null,"sourceUrl":null,"month":"201411","source":null,"comment":"For doing some research with..","id":1047000904}}"""
    }

    def "Get reason breakdown should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params.eventId = 1000
        controller.getReasonBreakdown()

        then:
        assert controller.response.text == """{"thisMonth":{"events":62,"records":1364,"reasonBreakdown":{"conservation management/planning":{"events":0,"records":0},"biosecurity management, planning":{"events":3,"records":66},"reason2":{"events":1,"records":22},"reason3":{"events":3,"records":66},"scientific research":{"events":2,"records":44},"collection management":{"events":5,"records":110},"other":{"events":3,"records":66},"ecological research":{"events":1,"records":22},"systematic research":{"events":3,"records":66},"other scientific research":{"events":10,"records":220},"reason1":{"events":17,"records":374},"dummy":{"events":0,"records":0},"unclassified":{"events":14,"records":308}}},"last3months":{"events":62,"records":1364,"reasonBreakdown":{"conservation management/planning":{"events":0,"records":0},"biosecurity management, planning":{"events":3,"records":66},"reason2":{"events":1,"records":22},"reason3":{"events":3,"records":66},"scientific research":{"events":2,"records":44},"collection management":{"events":5,"records":110},"other":{"events":3,"records":66},"ecological research":{"events":1,"records":22},"systematic research":{"events":3,"records":66},"other scientific research":{"events":10,"records":220},"reason1":{"events":17,"records":374},"dummy":{"events":0,"records":0},"unclassified":{"events":14,"records":308}}},"lastYear":{"events":62,"records":1364,"reasonBreakdown":{"conservation management/planning":{"events":0,"records":0},"biosecurity management, planning":{"events":3,"records":66},"reason2":{"events":1,"records":22},"reason3":{"events":3,"records":66},"scientific research":{"events":2,"records":44},"collection management":{"events":5,"records":110},"other":{"events":3,"records":66},"ecological research":{"events":1,"records":22},"systematic research":{"events":3,"records":66},"other scientific research":{"events":10,"records":220},"reason1":{"events":17,"records":374},"dummy":{"events":0,"records":0},"unclassified":{"events":14,"records":308}}},"all":{"events":62,"records":1364,"reasonBreakdown":{"conservation management/planning":{"events":0,"records":0},"biosecurity management, planning":{"events":3,"records":66},"reason2":{"events":1,"records":22},"reason3":{"events":3,"records":66},"scientific research":{"events":2,"records":44},"collection management":{"events":5,"records":110},"other":{"events":3,"records":66},"ecological research":{"events":1,"records":22},"systematic research":{"events":3,"records":66},"other scientific research":{"events":10,"records":220},"reason1":{"events":17,"records":374},"dummy":{"events":0,"records":0},"unclassified":{"events":14,"records":308}}}}"""
    }

    def "Get email breakdown should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params.eventId = 1000
        controller.getEmailBreakdown()

        then:
        assert controller.response.text == """{"last3months":{"events":62,"records":1364,"emailBreakdown":{"edu":{"events":2,"records":44},"gov":{"events":6,"records":132},"other":{"events":54,"records":1188},"unspecified":{"events":0,"records":0}}},"all":{"events":62,"records":1364,"emailBreakdown":{"edu":{"events":2,"records":44},"gov":{"events":6,"records":132},"other":{"events":54,"records":1188},"unspecified":{"events":0,"records":0}}},"thisMonth":{"events":62,"records":1364,"emailBreakdown":{"edu":{"events":2,"records":44},"gov":{"events":6,"records":132},"other":{"events":54,"records":1188},"unspecified":{"events":0,"records":0}}},"lastYear":{"events":62,"records":1364,"emailBreakdown":{"edu":{"events":2,"records":44},"gov":{"events":6,"records":132},"other":{"events":54,"records":1188},"unspecified":{"events":0,"records":0}}}}"""
    }

    def "Get monthly reason breakdown should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1000, entityUid: "dr143"]
        controller.getReasonBreakdownByMonth()

        then:
        assert controller.response.text == """{"temporalBreakdown":{"201411":{"records":1364,"events":62}}}"""
    }

    def "Get totals by event type should return a result"() {
        def controller = new LoggerController()

        when:
        controller.getTotalsByEventType()

        then:
        assert controller.response.text == """{"totals":{"1000":{"records":2706,"events":62}}}"""
    }

    def "Get reason breakdown csv should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1000, entityUid: "dp123"]
        controller.getReasonBreakdownCSV()

        then:
        assert controller.response.text == "\"year\",\"month\",\"reason\",\"number of events\",\"number of records\"\n" +
                "\"2014\",\"11\",\"reason1\",\"17\",\"544\"\n" +
                "\"2014\",\"11\",\"other scientific research\",\"10\",\"320\"\n" +
                "\"2014\",\"11\",\"systematic research\",\"3\",\"96\"\n" +
                "\"2014\",\"11\",\"ecological research\",\"1\",\"32\"\n" +
                "\"2014\",\"11\",\"other\",\"3\",\"96\"\n" +
                "\"2014\",\"11\",\"collection management\",\"5\",\"160\"\n" +
                "\"2014\",\"11\",\"scientific research\",\"2\",\"64\"\n" +
                "\"2014\",\"11\",\"reason3\",\"3\",\"96\"\n" +
                "\"2014\",\"11\",\"reason2\",\"1\",\"32\"\n" +
                "\"2014\",\"11\",\"biosecurity management, planning\",\"3\",\"96\"\n" +
                "\"2014\",\"11\",\"unclassified\",\"14\",\"448\""
    }

    def "Get email breakdown csv should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params << [eventId: 1000, entityUid: "dp123"]
        controller.getEmailBreakdownCSV()

        then:
        assert controller.response.text == "\"year\",\"month\",\"user category\",\"number of events\",\"number of records\"\n" +
                "\"2014\",\"11\",\"other\",\"54\",\"1728\"\n" +
                "\"2014\",\"11\",\"gov\",\"6\",\"192\"\n" +
                "\"2014\",\"11\",\"edu\",\"2\",\"64\""
    }

    def "Get entity breakdown should return a result"() {
        def controller = new LoggerController()

        when:
        controller.params << [entityUid: "dr143", eventId: 1000]
        controller.getEntityBreakdown()

        then:
        // response should be something like this: {"all":{"numberOfEvents":62,"numberOfEventItems":1364},"last3months":{"numberOfEvents":62,"numberOfEventItems":1364},"thisMonth":{"numberOfEvents":62,"numberOfEventItems":1364},"lastYear":{"numberOfEvents":62,"numberOfEventItems":1364}}"""
        assert controller.response.json.all.numberOfEvents > 0 && controller.response.json.all.numberOfEventItems > 0
        assert controller.response.json.last3months.numberOfEvents > 0 && controller.response.json.last3months.numberOfEventItems > 0
        assert controller.response.json.thisMonth.numberOfEvents > 0 && controller.response.json.thisMonth.numberOfEventItems > 0
        assert controller.response.json.lastYear.numberOfEvents > 0 && controller.response.json.lastYear.numberOfEventItems > 0
    }
}
