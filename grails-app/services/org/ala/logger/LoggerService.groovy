package org.ala.logger

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
    def createLog(LogEventVO incomingLog, String realIp) {
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
                month: determineMonth(incomingLog.month),
                source: findRemoteAddress(realIp)?.hostName)
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
        def remoteAddress = RemoteAddress.get(ipAddress)
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

        def query = "select month, sum(record_count) as record_count from event_summary_breakdown_reason_entity"
        query += " where log_event_type_id = :eventTypeId and entity_uid = :entityUid and month like :year group by month"

        def sqlQuery = sessionFactory.currentSession.createSQLQuery(query)

        sqlQuery.with {
            setParameter("eventTypeId", eventTypeId)
            setParameter("entityUid", entityUid)
            setParameter("year", year + "%")

            list()
        }
    }

    /**
     * Retrieve a summary of log events grouped by month, possibly filtered by reason type
     *
     * @param eventTypeId The event type to search for
     * @param entityUid The entityUid to search for. Optional. If not provided, all entityUids starting with 'dr' will be retrieved.
     * @param reasonTypeId The reason type to search for. Optional. If not provided, all reason types will be included.
     * @return list of Summary objects with the following fields populated: [month, eventCount, recordCount]
     */
    def getTemporalEventsReasonBreakdown(eventTypeId, entityUid, reasonTypeId) {
        log.debug("Summarising events by reason per month, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}," +
                "and reasonTypeId = ${reasonTypeId})")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def query = "select month, SUM(number_of_events) as number_of_events, SUM(record_count) as record_count from "
        query += "event_summary_breakdown_reason_entity e where log_event_type_id = :eventTypeId and entity_uid "
        query += entityUid ? " = :entityUid" : " like 'dr%'"

        if (reasonTypeId) {
            query += " and log_reason_type_id = :reasonTypeId"
        }
        query += " group by month"

        def sqlQuery = sessionFactory.currentSession.createSQLQuery(query)

        def result = sqlQuery.with {
            setParameter("eventTypeId", eventTypeId)

            if (entityUid) {
                setParameter("entityUid", entityUid)
            }
            if (reasonTypeId) {
                setParameter("reasonTypeId", reasonTypeId)
            }

            list()
        }

        result.collect { k -> new Summary(month: k[0], eventCount: k[1], recordCount: k[2]) }
    }

    /**
     * Retrieve a breakdown of log events grouped by event type
     *
     * @return list of Summary objects with the following fields populated: [eventTypeId, eventCount, recordCount]
     */
    def getEventTypeBreakdown() {
        log.debug("Summarising events by type")

        def query = "select log_event_type_id, sum(number_of_events), sum(record_count) " +
                "from event_summary_totals GROUP BY log_event_type_id"

        def result = sessionFactory.currentSession.createSQLQuery(query).list()

        result.collect { k -> new Summary(eventTypeId: k[0], eventCount: k[1], recordCount: k[2]) }
    }

    /**
     * List all log event reasons for the specified type and entity
     *
     * @param eventTypeId The event type to filter on
     * @param entityUid The entity to filter on
     * @return list of Summary objects with the following fields populated: [month, reasonTypeId, eventCount, recordCount]
     */
    def getLogEventsByReason(eventTypeId, entityUid) {
        log.debug("Listing all events by reason for eventTypeId ${eventTypeId} and entityUid ${entityUid}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def query = "select month, log_reason_type_id, number_of_events, record_count " +
                "from event_summary_breakdown_reason_entity " +
                "where log_event_type_id = :eventTypeId and entity_uid = :entityUid " +
                "order by month desc"

        def result = sessionFactory.currentSession.createSQLQuery(query).with {
            setParameter("eventTypeId", eventTypeId)
            setParameter("entityUid", entityUid)
            list()
        }

        result.collect { k -> new Summary(month: k[0], reasonTypeId: k[1], eventCount: k[2], recordCount: k[3]) }
    }

    /**
     * Retrieve a breakdown of log events grouped by reason type
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Optional.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of Summary objects with the following fields set: [reasonTypeId, eventCount, recordCount]
     */
    def getEventsReasonBreakdown(eventTypeId, entityUid, fromDate, toDate) {
        log.debug("Summarising events by reason, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}, " +
                "fromDate = ${fromDate} and toDate = ${toDate}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = getBreakdown(eventTypeId,
                entityUid,
                fromDate,
                toDate,
                "log_reason_type_id",
                "event_summary_breakdown_reason_entity",
                "event_summary_breakdown_reason")

        result.collect { k -> new Summary(reasonTypeId: k[0], eventCount: k[1], recordCount: k[2]) }
    }

    /**
     * List all log event email categories for the specified type and entity
     *
     * @param eventTypeId The event type to filter on
     * @param entityUid The entity to filter on
     * @return list of Summary objects with the following fields populated: [month, emailCategory, numberOfEvents, numberOfRecords]
     */
    def getLogEventsByEmail(eventTypeId, entityUid) {
        log.debug("Listing all events by reason for eventTypeId ${eventTypeId} and entityUid ${entityUid}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def query = "select month, user_email_category, number_of_events, record_count " +
                "from event_summary_breakdown_email_entity " +
                "where log_event_type_id = :eventTypeId and entity_uid = :entityUid " +
                "order by month desc"

        def result = sessionFactory.currentSession.createSQLQuery(query).with {
            setParameter("eventTypeId", eventTypeId)
            setParameter("entityUid", entityUid)
            list()
        }

        result.collect { k -> new Summary(month: k[0], userEmail: k[1], eventCount: k[2], recordCount: k[3]) }
    }

    /**
     * Retrieve a breakdown of log events grouped by email type
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Optional.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of Summary objects with the following fields set: [userEmail, eventCount, recordCount]
     */
    def getEventsEmailBreakdown(eventTypeId, entityUid, fromDate, toDate) {
        log.debug("Summarising events by email, with eventTypeId = ${eventTypeId}, entityUid = ${entityUid}, " +
                "fromDate = ${fromDate} and toDate = ${toDate}")

        assert eventTypeId, "eventTypeId is a mandatory parameter"

        def result = getBreakdown(eventTypeId,
                entityUid,
                fromDate,
                toDate,
                "user_email_category",
                "event_summary_breakdown_email_entity",
                "event_summary_breakdown_email")

        result.collect { k -> new Summary(userEmail: k[0], eventCount: k[1], recordCount: k[2]) }
    }

    /**
     * Retrieve a breakdown of log events for a specific entity, grouped by month
     *
     * @param eventTypeId The event type to search for. Mandatory.
     * @param entityUid The entity to search for. Mandatory.
     * @param fromDate The first year/month (yyyyMM) in the range to search for. Inclusive.
     * @param toDate The last year/month (yyyyMM) in the range to search for. Exclusive.
     * @return list of Summary objects with the following fields set: [month, eventCount, recordCount]
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
                "event_summary_breakdown_reason_entity",
                "event_summary_breakdown_reason")

        result.collect { k -> new Summary(month: k[0], eventCount: k[1], recordCount: k[2]) }
    }

    private
    def getBreakdown(eventTypeId, entityUid, fromDate, toDate, categoryColumn, withEntityTable, withoutEntityTable) {
        assert fromDate && toDate || !fromDate && !toDate, "Must supply both a dateFrom and dateTo string or neither"

        def query = "select ${categoryColumn}, SUM(number_of_events) as number_of_events, SUM(record_count) as record_count from "
        query += "${entityUid ? withEntityTable : withoutEntityTable} e where log_event_type_id = :eventTypeId"
        if (entityUid) {
            query += " and entity_uid = :entityUid"
        }
        if (fromDate && toDate) {
            query += " and month >= :from and month < :to"
        }
        query += " group by ${categoryColumn}"

        def sqlQuery = sessionFactory.currentSession.createSQLQuery(query)

        sqlQuery.with {
            setParameter("eventTypeId", eventTypeId)

            if (entityUid) setParameter("entityUid", entityUid)
            if (fromDate && toDate) {
                setParameter("from", fromDate);
                setParameter("to", toDate)
            }

            list()
        }
    }

    private def getValidType(id, finder) {
        def value = null
        if (id) {
            value = finder(id)
            if (!value) {
                log.error("Failed to find value for ${id}")
                throw new NoSuchFieldException()
            }
        }
        value
    }

    private static def determineMonth(month) {
        if (month != null && month.trim().length() > 3 && month.isInteger) {
            month.trim();
        } else {
            new Date().format("yyyyMM")
        }
    }

    private static def recordCountsToLogDetails(long eventTypeId, Map<String, Integer> recordCounts, LogEvent event) {
        def logDetails = []
        recordCounts.each({
            k, v -> logDetails << new LogDetail(entityType: eventTypeId as String, entityUid: k, recordCount: v, logEvent: event)
        })

        logDetails
    }

    private def logWrite(entity, result) {
        if (!result) {
            log.error("Database write failed")
            entity.errors.each { log.error(it) }
        } else {
            log.debug("Database write succeeded for entity ${entity}")
        }
    }
}
