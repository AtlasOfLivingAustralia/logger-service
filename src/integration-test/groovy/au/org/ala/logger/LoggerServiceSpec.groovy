package au.org.ala.logger

import grails.test.spock.IntegrationSpec

/**
 * Integration test to verify the behaviour of the queries used to retrieve aggregated data - simple crud operations are
 * not tested, just those operations that perform comparatively complex criteria queries
 */
class LoggerServiceSpec extends IntegrationSpec {

    LoggerService service = new LoggerService()

    def "getEventTypeBreakdown should return the expected grouping"() {
        new EventSummaryTotal(month: "201410", logEventTypeId: 1, numberOfEvents: 2, recordCount: 10).save()
        new EventSummaryTotal(month: "201410", logEventTypeId: 2, numberOfEvents: 3, recordCount: 15).save()
        new EventSummaryTotal(month: "201411", logEventTypeId: 1, numberOfEvents: 4, recordCount: 20).save()
        new EventSummaryTotal(month: "201411", logEventTypeId: 2, numberOfEvents: 5, recordCount: 25).save()

        when:
        def result = service.getEventTypeBreakdown()

        then:
        assert result.size() == 2
        assert result[0].numberOfEvents == 6 && result[0].recordCount == 30
        assert result[1].numberOfEvents == 8 && result[1].recordCount == 40
    }

    def "getLogEventsByReason should return all results ordered by month (descending)"() {
        def r1 = new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        def r2 = new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity2", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        def r4 = new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        def r5 = new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity2", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 7, recordCount: 30).save()

        def expectedOrder = [r4, r5, r1, r2]

        when:
        def result = service.getLogEventsByReason(1, "entity1")

        then:
        assert result.size() == 4
        assert result == expectedOrder
    }

    def "getLogEventsByReason should return 0 results if no entityUid is provided"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()

        when:
        def result = service.getLogEventsByReason(1, null)

        then:
        assert !result
    }

    def "getLogEventsByReason should return 0 results if there are no matches"() {
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()

        when:
        def result = service.getLogEventsByReason(1, "nomatch")

        then:
        assert !result
    }

    def "getLogEventsByEmail should return all results ordered by month (descending)"() {
        def r1 = new EventSummaryBreakdownEmailEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 2, recordCount: 5).save()
        def r2 = new EventSummaryBreakdownEmailEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, userEmailCategory: "gov", numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownEmailEntity(month: "201410", entityUid: "entity2", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 4, recordCount: 15).save()
        def r4 = new EventSummaryBreakdownEmailEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 5, recordCount: 20).save()
        def r5 = new EventSummaryBreakdownEmailEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, userEmailCategory: "other", numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownEmailEntity(month: "201411", entityUid: "entity2", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 7, recordCount: 30).save()

        def expectedOrder = [r4, r5, r1, r2]

        when:
        def result = service.getLogEventsByEmail(1, "entity1")

        then:
        assert result.size() == 4
        assert result == expectedOrder
    }

    def "getLogEventsByEmail should return 0 results if no entityUid is provided"() {
        new EventSummaryBreakdownEmailEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 2, recordCount: 5).save()

        when:
        def result = service.getLogEventsByEmail(1, null)

        then:
        assert !result
    }

    def "getLogEventsByEmail should return 0 results if there are no matches"() {
        new EventSummaryBreakdownEmailEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, userEmailCategory: "edu", numberOfEvents: 2, recordCount: 5).save()

        when:
        def result = service.getLogEventsByEmail(1, "nomatch")

        then:
        assert !result
    }

    def "getLogEventCount should return 0 results if there are no matches"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()

        when:
        def result = service.getLogEventCount(1, "nomatch", "2013")

        then:
        assert !result
    }

    def "getLogEventCount should return log counts grouped by month"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity2", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity2", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 7, recordCount: 30).save()

        when:
        def result = service.getLogEventCount(1, "entity1", "201410")

        then:
        assert result.size() == 1
        assert result[0][0] == "201410" && result[0][1] == 15 // the 3rd record for 201410 is for a different entity
    }

    def "getLogEventCount should perform a 'like' comparison on the year/month parameter"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "entity2", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "entity2", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 7, recordCount: 30).save()

        when:
        def result = service.getLogEventCount(1, "entity1", "2014")

        then:
        assert result.size() == 2
        assert result[0][0] == "201410" && result[0][1] == 15 // the 3rd record for 201410 is for a different entity
        assert result[1][0] == "201411" && result[1][1] == 45 // the 3rd record for 201411 is for a different entity
    }

    def "getTemporalEventsReasonBreakdown should should match eventTypeId, entityUid and reasonTypeId"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 8, recordCount: 35).save()

        when:
        def result = service.getTemporalEventsReasonBreakdown(1, "dr1", 2)

        then:
        assert result.size() == 2
        assert result[0].month == "201410" && result[0].recordCount == 10 && result[0].numberOfEvents == 3
        assert result[1].month == "201411" && result[1].recordCount == 25 && result[1].numberOfEvents == 6
    }

    def "getTemporalEventsReasonBreakdown should should match eventTypeId and entityUid"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 8, recordCount: 35).save()

        when:
        def result = service.getTemporalEventsReasonBreakdown(1, "dr1", null)

        then:
        assert result.size() == 2
        assert result[0].month == "201410" && result[0].recordCount == 15 && result[0].numberOfEvents == 5
        assert result[1].month == "201411" && result[1].recordCount == 60 && result[1].numberOfEvents == 14
    }

    def "getTemporalEventsReasonBreakdown should should default entityUid to like dr% if not provided"() {
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 8, recordCount: 35).save()

        when:
        def result = service.getTemporalEventsReasonBreakdown(1, null, null)

        then:
        assert result.size() == 2
        assert result[0].month == "201410" && result[0].recordCount == 30 && result[0].numberOfEvents == 9
        assert result[1].month == "201411" && result[1].recordCount == 90 && result[1].numberOfEvents == 21
    }

    def "getXYZBreakdown() should filter by eventId, entityUid and date range when all are provided"() {
        new EventSummaryBreakdownReasonEntity(month: "201309", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201310", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201408", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201409", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201409", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 8, recordCount: 35).save()

        when:
        def result = service.getEventsReasonBreakdown(1, "dr1", "201409", "201412")

        then:
        // all reason types are the same, so all records will be grouped together.
        assert result[0].recordCount == 80 && result[0].numberOfEvents == 19
    }

    def "getXYZBreakdown() should retrieve all dates when no date range is specified"() {
        new EventSummaryBreakdownReasonEntity(month: "201309", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201310", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201408", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201409", entityUid: "im1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201409", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr2", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 8, recordCount: 35).save()

        when:
        def result = service.getEventsReasonBreakdown(1, "dr1", null, null)

        then:
        // all reason types are the same, so all records will be grouped together.
        assert result[0].recordCount == 95 && result[0].numberOfEvents == 24
    }

    def "getXYZBreakdown() should retrieve all entities when no entityUid is specified"() {
        new EventSummaryBreakdownReason(month: "201309", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReason(month: "201310", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReason(month: "201407", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReason(month: "201408", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReason(month: "201409", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 5, recordCount: 20).save()
        new EventSummaryBreakdownReason(month: "201410", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 6, recordCount: 25).save()
        new EventSummaryBreakdownReason(month: "201411", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 7, recordCount: 30).save()
        new EventSummaryBreakdownReason(month: "201412", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 8, recordCount: 35).save()

        when:
        def result = service.getEventsReasonBreakdown(1, null, null, null)

        then:
        // all reason types are the same, so all records will be grouped together.
        assert result[0].recordCount == 155 && result[0].numberOfEvents == 39
    }

    def "getXYZBreakdown() should retrieve treat fromDate as an INCLUSIVE and toDate as an EXCLUSIVE condition"() {
        new EventSummaryBreakdownReasonEntity(month: "201409", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 1, numberOfEvents: 2, recordCount: 5).save()
        new EventSummaryBreakdownReasonEntity(month: "201410", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 2, numberOfEvents: 3, recordCount: 10).save()
        new EventSummaryBreakdownReasonEntity(month: "201411", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 3, numberOfEvents: 4, recordCount: 15).save()
        new EventSummaryBreakdownReasonEntity(month: "201412", entityUid: "dr1", logEventTypeId: 1, logReasonTypeId: 4, numberOfEvents: 5, recordCount: 20).save()

        when:
        def result = service.getEventsReasonBreakdown(1, "dr1", "201410", "201412")

        then:
        // should return 201410 (inclusive) and 201411. 201409 is before the fromDate, 201412 = toDate but is exclusive
        assert result.size() == 2
    }

}
