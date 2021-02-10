package au.org.ala.logger

import grails.transaction.Transactional
import org.ala.client.model.LogEventVO

import javax.persistence.PersistenceException

class LoggerService {

    def sessionFactory

    /**
     * Create a new log event record with associated log detail records
     *
     * @param incomingLog the LogEventVO carrying the log details
     * @param realIp the 'real' ip address of the request
     * @return the newly created LogEvent object
     * @throws PersistenceException if the database write fails
     */
    @Transactional(readOnly = false)
    def createLog(LogEventVO incomingLog, Map additionalProperties = [:]) {
        log.debug("Creating new log event with details ${incomingLog}")

        assert incomingLog, "incomingLog is a mandatory parameter"

        LogEventType eventType = getValidType(incomingLog.eventTypeId, LogEventType.&get)
        LogReasonType reasonType = getValidType(incomingLog.reasonTypeId, LogReasonType.&get)
        LogSourceType sourceType = getValidType(incomingLog.sourceTypeId, LogSourceType.&get)

        LogEvent event = new LogEvent(comment: incomingLog.comment,
                logEventTypeId: eventType.id,
                logReasonTypeId: reasonType?.id,
                logSourceTypeId: sourceType?.id,
                userEmail: incomingLog.userEmail,
                userIp: incomingLog.getUserIP(),
                sourceUrl: incomingLog.getSourceUrl(),
                userAgent: additionalProperties["userAgent"],
                month: determineMonth(incomingLog.month),
                source: findRemoteAddress(additionalProperties["realIp"])?.hostName)
        event.logDetails = recordCountsToLogDetails(eventType.id, incomingLog.recordCounts, event)

        def result = event.save()

        logWrite(event, result)

        if (result) {
            result
        } else {
            throw new PersistenceException()
        }
    }

    def findRemoteAddress(String ipAddress) {
        def remoteAddress = RemoteAddress.findByIp(ipAddress)
        log.debug("Found remote address ${remoteAddress} for ip ${ipAddress}")
        remoteAddress
    }

    def findLogEvent(Long id) {
        def event = LogEvent.get(id)
        log.debug("Found log event ${event} for id ${id}")
        event
    }

    def getAllEventTypes() {
        def events = LogEventType.list()
        log.debug("Found ${events.size()} log event types")
        events
    }

    def getAllSourceTypes() {
        def sources = LogSourceType.list()
        log.debug("Found ${sources.size()} log source types")
        sources
    }

    def getAllReasonTypes() {
        def reasons = LogReasonType.list()
        log.debug("Found ${reasons.size()} log reason types")
        reasons
    }

    /**
     * Retrieve the total number of records for each month in the specified year.
     * <p/>
     * The year is used in a like clause, so "2014" will find all records for 2014; "20" will find all records from 2000
     * - 2099.
     *
     * @param eventTypeId The event type id to search for
     * @param entityUid The entity Uid to search for
     * @param year The year to use as the prefix of the 'month' field.
     * @return list of pairs: ["month": totalRecords]
     */
    def getLogEventCount(eventTypeId, entityUid, year) {
        log.debug("Summarising log events by eventTypeId ${eventTypeId}, entityUid ${entityUid}, and month ${year}")

        assert eventTypeId && entityUid && year, "All parameters are mandatory"

        EventSummaryBreakdownReasonEntity.withCriteria {
            eq("logEventTypeId", eventTypeId as int)
            eq("entityUid", entityUid)
            like("month", year + "%")

            projections {
                groupProperty("month")

                "month"
                sum("recordCount")
            }
        }
    }

    /**
     * Retrieve a summary of log events grouped by month, possibly filtered by reason type
     *
     * @param eventTypeId The event type to search for
     * @param entityUid The entityUid to search for. Optional. If not provided, all entityUids starting with 'dr' will be retrieved.
     * @param reasonTypeId The reason type to search for. Optional. If not provided, all reason types will be included.
     * @return list of EventSummaryBreakdownReasonEntity objects with the following fields populated: [month, numberOfEvents, recordCount]
     */
    def getTemporalEventsReasonBreakdown(eventTypeId, entityUid, reasonTypeId) {
        log.debug("Summarising events by reason per month, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}," +
                "and reasonTypeId = ${reasonTypeId})")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = EventSummaryBreakdownReasonEntity.withCriteria {
            eq("logEventTypeId", eventTypeId as int)
            if (entityUid) {
                eq("entityUid", entityUid)
            } else {
                ilike("entityUid", "dr%")
            }

            if (reasonTypeId) {
                eq("logReasonTypeId", reasonTypeId as int)
            }

            projections {
                groupProperty("month")

                "month"
                sum("numberOfEvents")
                sum("recordCount")
            }
        }

        result.collect { k -> new EventSummaryBreakdownReasonEntity(month: k[0], numberOfEvents: k[1], recordCount: k[2]) }
    }

    /**
     * Retrieve a summary of log events grouped by month, possibly filtered by reason type and source type
     *
     * @param eventTypeId The event type to search for
     * @param entityUid The entityUid to search for. Optional. If not provided, all entityUids starting with 'dr' will be retrieved.
     * @param reasonTypeId The reason type to search for. Optional. If not provided, all reason types will be included.
     * @param sourceTypeId The source type to search for. Optional. If not provided, all source types will be included.
     * @return list of EventSummaryBreakdownReasonSourceEntity objects with the following fields populated: [month, numberOfEvents, recordCount]
     */
    def getTemporalEventsSourceBreakdown(eventTypeId, entityUid, reasonTypeId, sourceTypeId) {
        log.debug("Summarising events by reason per month, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}," +
                "reasonTypeId = ${reasonTypeId} and sourceTypeId = ${sourceTypeId})")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = EventSummaryBreakdownReasonSourceEntity.withCriteria {
            eq("logEventTypeId", eventTypeId as int)
            if (entityUid) {
                eq("entityUid", entityUid)
            } else {
                ilike("entityUid", "dr%")
            }

            if (reasonTypeId) {
                eq("logReasonTypeId", reasonTypeId as int)
            }

            if (sourceTypeId) {
                eq("logSourceTypeId", sourceTypeId as int)
            }

            projections {
                groupProperty("month")

                "month"
                sum("numberOfEvents")
                sum("recordCount")
            }
        }

        result.collect { k -> new EventSummaryBreakdownReasonSourceEntity(month: k[0], numberOfEvents: k[1], recordCount: k[2]) }
    }

    /**
     * Retrieve a breakdown of log events grouped by event type
     *
     * @return list of EventSummaryTotal objects
     */
    def getEventTypeBreakdown() {
        log.debug("Summarising events by type")

        def result = EventSummaryTotal.withCriteria {
            projections {
                groupProperty("logEventTypeId")

                "logEventTypeId"
                sum("numberOfEvents")
                sum("recordCount")
            }
        }

        result.collect { k -> new EventSummaryTotal(logEventTypeId: k[0], numberOfEvents: k[1], recordCount: k[2]) }
    }

    /**
     * List all log event reasons for the specified type and entity
     *
     * @param eventTypeId The event type to filter on
     * @param entityUid The entity to filter on
     * @return list of EventSummaryBreakdownReasonEntity objects
     */
    def getLogEventsByReason(eventTypeId, entityUid) {
        log.debug("Listing all events by reason for eventTypeId ${eventTypeId} and entityUid ${entityUid}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"
        List logEventsByReason

        if (entityUid) {
            logEventsByReason = EventSummaryBreakdownReasonEntity.findAllByLogEventTypeIdAndEntityUid(eventTypeId, entityUid, [sort: "month", order: "desc"])
        } else {
            logEventsByReason = EventSummaryBreakdownReasonEntity.findAllByLogEventTypeId(eventTypeId, [sort: "month", order: "desc"])
        }

        logEventsByReason
    }

    /**
     * List all log event reasons for the specified type and entity
     *
     * @param eventTypeId The event type to filter on
     * @param entityUid The entity to filter on
     * @return list of EventSummaryBreakdownReasonSourceEntity objects
     */
    def getLogEventsBySource(eventTypeId, entityUid) {
        log.debug("Listing all events by reason for eventTypeId ${eventTypeId} and entityUid ${entityUid}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"
        List logEventsBySource

        if (entityUid) {
            logEventsBySource = EventSummaryBreakdownReasonSourceEntity.findAllByLogEventTypeIdAndEntityUid(eventTypeId, entityUid, [sort: "month", order: "desc"])
        } else {
            logEventsBySource = EventSummaryBreakdownReasonSourceEntity.findAllByLogEventTypeId(eventTypeId, [sort: "month", order: "desc"])
        }

        logEventsBySource
    }

    /**
     * Retrieve a breakdown of log events grouped by reason type
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Optional.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of EventSummaryBreakdownReasonEntity objects with the following fields set: [logReasonTypeId, numberOfEvents, recordCount]
     */
    def getEventsReasonBreakdown(eventTypeId, entityUid, fromDate, toDate) {
        log.debug("Summarising events by reason, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}, " +
                "fromDate = ${fromDate} and toDate = ${toDate}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = getBreakdown(eventTypeId,
                entityUid,
                fromDate,
                toDate,
                "logReasonTypeId",
                EventSummaryBreakdownReasonEntity,
                EventSummaryBreakdownReason)

        result.collect { k -> new EventSummaryBreakdownReason(logReasonTypeId: k[0], numberOfEvents: k[1], recordCount: k[2]) }
    }

    /**
     * Retrieve a breakdown of log events grouped by source type
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Optional.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of EventSummaryBreakdownReasonEntity objects with the following fields set: [logReasonTypeId, numberOfEvents, recordCount]
     */
    def getEventsSourceBreakdown(eventTypeId, entityUid, fromDate, toDate) {
        log.debug("Summarising events by reason, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}, " +
                "fromDate = ${fromDate} and toDate = ${toDate}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = getBreakdown(eventTypeId,
                entityUid,
                fromDate,
                toDate,
                "logSourceTypeId",
                EventSummaryBreakdownReasonSourceEntity,
                EventSummaryBreakdownReasonSourceEntity)

        result.collect { k -> [logSourceTypeId: k[0], numberOfEvents: k[1], recordCount: k[2]] }
    }

    /**
     * List all log event email categories for the specified type and entity
     *
     * @param eventTypeId The event type to filter on
     * @param entityUid The entity to filter on
     * @return list of EventSummaryBreakdownEmailEntity objects
     */
    def getLogEventsByEmail(eventTypeId, entityUid) {
        log.debug("Listing all events by reason for eventTypeId ${eventTypeId} and entityUid ${entityUid}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        EventSummaryBreakdownEmailEntity.findAllByLogEventTypeIdAndEntityUid(eventTypeId, entityUid, [sort: "month", order: "desc"])
    }

    /**
     * Retrieve a breakdown of log events grouped by email type
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Optional.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of EventSummaryBreakdownReason objects with the following fields set: [userEmailCategory, numberOfEvents, recordCount]
     */
    def getEventsEmailBreakdown(eventTypeId, entityUid, fromDate, toDate) {
        log.debug("Summarising events by email, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}, " +
                "fromDate = ${fromDate} and toDate = ${toDate}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = getBreakdown(eventTypeId,
                entityUid,
                fromDate,
                toDate,
                "userEmailCategory",
                EventSummaryBreakdownEmailEntity,
                EventSummaryBreakdownEmail)

        result.collect { k -> new EventSummaryBreakdownEmail(userEmailCategory: k[0], numberOfEvents: k[1], recordCount: k[2]) }
    }

    /**
     * Retrieve a breakdown of log events for a specific entity, grouped by month
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Mandatory.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of EventSummaryBreakdownReason objects with the following fields set: [month, numberOfEvents, recordCount]
     */
    def getLogEventsByEntity(eventTypeId, entityUid, fromDate, toDate) {
        log.debug("Summarising log events by entity, with eventTypeId = ${eventTypeId}. entityUid = ${entityUid}" +
                "fromDate = ${fromDate} and toDate = ${toDate}");

        assert eventTypeId && entityUid, "eventTypeId and entityUid are both mandatory parameters"

        def result = getBreakdown(eventTypeId,
                entityUid,
                fromDate,
                toDate,
                "month",
                EventSummaryBreakdownReasonEntity,
                EventSummaryBreakdownReason)

        result.collect { k -> new EventSummaryBreakdownReason(month: k[0], numberOfEvents: k[1], recordCount: k[2]) }
    }

    private getBreakdown(eventTypeId, entityUid, fromDate, toDate, categoryProperty, domainClass, noEntityDomainClass) {
        assert fromDate && toDate || !fromDate && !toDate, "Must supply both a dateFrom and dateTo string or neither"

        (entityUid ? domainClass : noEntityDomainClass).withCriteria {
            eq("logEventTypeId", eventTypeId as int)
            if (entityUid) {
                eq("entityUid", entityUid)
            }
            if (fromDate && toDate) {
                ge("month", fromDate)
                lt("month", toDate)
            }

            projections {
                groupProperty("${categoryProperty}")

                "${categoryProperty}"
                sum("numberOfEvents")
                sum("recordCount")
            }
        }
    }

    def getUserBreakdown(eventTypeId, entityUids, months) {

        def monthsClause = ""
        if(months){
            monthsClause = " and le.month in (:months)"
        }

        def params = [entityUids:entityUids, eventTypeId:eventTypeId]
        if(months){
            params.months  = months
        }


        def results = LogEvent.executeQuery(
                "select le.userEmail, ld.entityUid, le.logReasonTypeId, count(*), sum(ld.recordCount) from LogEvent le " +
                        "join le.logDetails ld " +
                        "where ld.entityUid IN (:entityUids) " +
                        "and le.userEmail is NOT NULL " +
                        "and le.logEventTypeId = :eventTypeId " +
                        monthsClause +
                        "group by le.userEmail, le.logReasonTypeId, ld.entityUid " +
                        "order by le.userEmail",
                params
        )

        results.each {
            def lrt = LogReasonType.findById(it[2])
            if(lrt) {
                it[2] = lrt.name
            } else {
                it[2] = 'Not supplied'
            }
        }

        results
    }

    def getUserBreakdownDetailed(eventTypeId, entityUids, months) {

        def monthsClause = ""
        if(months){
            monthsClause = " and le.month in (:months)"
        }

        def params = [entityUids:entityUids, eventTypeId:eventTypeId]
        if(months){
            params.months  = months
        }

        def results = LogEvent.executeQuery(
                "select le.userEmail, ld.entityUid, le.logReasonTypeId, ld.recordCount, le.dateCreated, le.sourceUrl from LogEvent le " +
                        "join le.logDetails ld " +
                        "where ld.entityUid IN (:entityUids) " +
                        "and le.userEmail is NOT NULL " +
                        "and le.logEventTypeId = :eventTypeId " +
                        monthsClause +
                        "order by le.userEmail, le.dateCreated",
                params
        )

        results.each {
            def lrt = LogReasonType.findById(it[2])
            if(lrt) {
                it[2] = lrt.name
            } else {
                it[2] = 'Not supplied'
            }
        }

        results
    }


    private getValidType(id, finder) {
        def value = null
        if (id != null) {
            value = finder(id)
            if (!value) {
                log.error("Failed to find value for ${id}")
                throw new NoSuchFieldException()
            }
        }
        value
    }

    private static determineMonth(month) {
        if (month != null && month.trim().length() > 3 && month.isInteger()) {
            month.trim();
        } else {
            new Date().format("yyyyMM")
        }
    }

    private static recordCountsToLogDetails(long eventTypeId, Map<String, Integer> recordCounts, LogEvent event) {
        def logDetails = []
        recordCounts.each({
            k, v -> logDetails << new LogDetail(entityType: eventTypeId as String, entityUid: k, recordCount: v, logEvent: event)
        })

        logDetails
    }

    private logWrite(entity, result) {
        if (!result) {
            log.error("Database write failed")
            entity.errors.each { log.error(it) }
        } else {
            log.debug("Database write succeeded for entity ${entity}")
        }
    }
}
