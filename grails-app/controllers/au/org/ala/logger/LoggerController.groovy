package au.org.ala.logger

import grails.converters.JSON
import grails.plugins.csv.CSVWriter
import org.ala.client.model.LogEventVO
import org.springframework.http.HttpStatus
import groovy.time.*
import java.text.DateFormat
import java.text.SimpleDateFormat


class LoggerController {

    final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For"
    final String USER_AGENT_HEADER = "user-agent"
    final String UNCLASSIFIED_REASON_TYPE = "unclassified"
    final String UNCLASSIFIED_SOURCE_TYPE = "unclassified"
    final List<String> EMAIL_CATEGORIES = ["edu", "gov", "other", "unspecified"]

    def loggerService

    def index = {
        render(view: "/index")
    }

    def notAuthorised = {}

    /**
     * Create a new event log record. Record details are expected in the JSON object of the POST request in the form of
     * a JSON-rendered {@link LogEventVO}
     * <p/>
     * Example POST url: <pre>.../logger</pre>
     *
     * @return JSON representation of the new log record.
     */
    def save() {
        String ip = request.getHeader(X_FORWARDED_FOR_HEADER) ?: request.getRemoteAddr()
        ip = ip.tokenize(", ")[0] // Sometimes see 2 IP addresses like '3.105.55.111, 3.105.55.111' - grab first value
        log.debug("Received log event from remote host ${request.getRemoteHost()} with ip address ${ip}")

        String userAgent = request.getHeader(USER_AGENT_HEADER) ?: "MOZILLA 5.0"

        // ignore any JSON attribute that is not a property of the LogEventVO class to avoid constructor errors
        List fields = LogEventVO.properties.declaredFields.collect { it.name }
        Map json = request.getJSON().findAll { k, v -> fields.contains(k) && k != "class"}

        LogEventVO incomingLog = new LogEventVO(json);
        Map props = [realIp: ip, userAgent: userAgent]
        log.debug "incomingLog = ${incomingLog} || props = ${props}"
        log.debug "Checking loggerService is not null = ${(loggerService != null)}"

        try {
            LogEvent logEvent = loggerService.createLog(incomingLog, props)
            //log.debug("rendering json: ${logEvent.toJSON()}")
            render logEvent.toJSON()
        } catch (Exception e) {
            handleError(HttpStatus.NOT_ACCEPTABLE, "Failed to create log entry", e)
        }
    }

    /**
     * Retrieve a single specific log event in JSON format. Expects params.id to contain the event log id to search for.
     * <p/>
     * Example url: <pre>.../logger/1</pre>
     *
     * @return JSON representation of the specified event log, or HTTP 404 if no matching record is found
     */
    def getEventLog() {
        def logEvent = loggerService.findLogEvent(params.id as long)

        if (!logEvent) {
            handleError(HttpStatus.NOT_FOUND, "No matching log event of id ${params.id} was found")
        } else {
            render logEvent.toJSON()
        }
    }

    /**
     * Retrieve a monthly breakdown of log events in the form [month: recordCount], queried by event type id and entity uid.
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     * <li>eventTypeId - the event type to query on. Mandatory.
     * <li>q - the entityUid to query on. Mandatory.
     * <li>year - the month pattern to group by. Optional. Defaults to the current year.
     * </ul>
     * <p/>
     * Example url: <pre>.../logger/get.json?q=123&entityTypeId=2&year=201403</pre>
     * <p/>
     * Example response: <pre>{"months":[["201401",123],["201403",3211],["201404",32]]}</pre>get
     *
     * @return monthly breakdown of log events in the form [month: recordCount]
     */
    def monthlyBreakdown() {
        if (!params.q || !params.eventTypeId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing either q (entityUid) or eventTypeId")
        } else {
            String year = params.year ?: Calendar.getInstance().get(Calendar.YEAR) as String
            log.debug "monthlyBreakdown() - ${params.eventTypeId}, ${params.q}, ${year}"
            // the 'q' URL request parameter corresponds to the entityUid field
            def monthlyBreakdown = loggerService.getLogEventCount(params.eventTypeId, params.q, year);

            render ([months: monthlyBreakdown] as JSON)
        }
    }

    /**
     * Retrieve a breakdown of log events by reason for a particular entity
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     * <li>eventId - the event<strong>Type</strong>Id to query on. Mandatory.</li>
     * <li>entityUid - the entityUid to query on. Optional.</li>
     * </ul>
     * <p/>
     * Example url: <pre>.../logger/getReasonBreakdown?eventId=1002&entityUid=in4</pre>
     *
     * @return breakdown of log events by reason in JSON format
     */
    def getReasonBreakdown() {
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing entityUid and/or eventId")
        } else {
            use(TimeCategory) {
                Date nextMonth = nextMonth()

                Map<Integer, String> reasonMap = getReasonMap()

                def results = [:]
                results << ["thisMonth": getReasonBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 1.month, nextMonth, reasonMap)]
                results << ["last3Months": getReasonBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 3.months, nextMonth, reasonMap)]
                results << ["lastYear": getReasonBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 12.months, nextMonth, reasonMap)]
                results << ["all": getReasonBreakdownForPeriod(params.eventId, params.entityUid, null, null, reasonMap)]

                render results as JSON
            }
        }
    }

    /**
     * Retrieve a breakdown of log events by source for a particular entity
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     * <li>eventId - the event<strong>Type</strong>Id to query on. Mandatory.</li>
     * <li>entityUid - the entityUid to query on. Optional.</li>
     * <li>excludeReasonTypeId - the <code>logReasonTypeId</code> to exclude from results (usually &quot;testing&quot;)</li>
     * </ul>
     * <p/>
     * Example url: <pre>.../logger/getReasonBreakdown?eventId=1002&entityUid=in4</pre>
     *
     * @return breakdown of log events by reason in JSON format
     */
    def getSourceBreakdown() {
        if (!params.eventId || !params.entityUid) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing entityUid and/or eventId")
        } else {
            use(TimeCategory) {
                Integer excludeReasonTypeId = params.int("excludeReasonTypeId")
                Date nextMonth = nextMonth()

                Map<Integer, String> sourceMap = getSourceMap()

                def results = [:]
                results << ["thisMonth": getSourceBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 1.month, nextMonth, sourceMap, excludeReasonTypeId)]
                results << ["last3Months": getSourceBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 3.months, nextMonth, sourceMap, excludeReasonTypeId)]
                results << ["lastYear": getSourceBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 12.months, nextMonth, sourceMap, excludeReasonTypeId)]
                results << ["all": getSourceBreakdownForPeriod(params.eventId, params.entityUid, null, null, sourceMap, excludeReasonTypeId)]

                render results as JSON
            }
        }
    }

    /**
     * Generate a CSV file containing all log events for the specified eventType and entity, with the reason for download
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     *     <li>eventId - the logEventTypeId to query on. Mandatory.
     *     <li>entityUid - the entity id to search for.
     * </ul>
     * Example request: <pre>.../logger/reasonBreakdownCSV?eventId=1002&entityUid=in4</pre>
     *
     * @return all log events for the specified eventType and entity in CSV format
     */
    def getReasonBreakdownCSV() {
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing eventId")
        } else {
            Map<Integer, String> reasonMap = getReasonMap()

            def results = loggerService.getLogEventsByReason(params.eventId, params.entityUid)

            response.contentType = "text/csv"
            response.addHeader("Content-Disposition", "attachment; filename=\"downloads-by-reason-${params.entityUid?:'all'}.csv\"")

            if (results) {
                def csv = new CSVWriter(response.writer, {
                    col1:
                    "year" { (it.month as String).substring(0, 4) }
                    col2:
                    "month" { (it.month as String).substring(4) }
                    col3:
                    "reason" { reasonMap.get(it.logReasonTypeId) ?: UNCLASSIFIED_REASON_TYPE }
                    col4:
                    "number of events" { it.numberOfEvents }
                    col5:
                    "number of records" { it.recordCount }
                })

                results.each { e -> csv << e }
            } else {
                response.writer.write("\"year\",\"month\",\"reason\",\"number of events\",\"number of records\"")
            }
            response.writer.flush()
        }
    }

    /**
     * Generate a CSV file containing all log events for the specified eventType and entity, with the source for download
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     *     <li>eventId - the logEventTypeId to query on. Mandatory.
     *     <li>entityUid - the entity id to search for.
     * </ul>
     * Example request: <pre>.../logger/sourceBreakdownCSV?eventId=1002&entityUid=in4</pre>
     *
     * @return all log events for the specified eventType and entity in CSV format
     */
    def getSourceBreakdownCSV() {
        if (!params.eventId || !params.entityUid) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing entityUid and/or eventId")
        } else {
            Map<Integer, String> sourceMap = getSourceMap()
            Map<Integer, String> reasonMap = getReasonMap()

            def results = loggerService.getLogEventsBySource(params.eventId, params.entityUid)

            response.contentType = "text/csv"
            response.addHeader("Content-Disposition", "attachment; filename=\"downloads-by-source-${params.entityUid}.csv\"")

            if (results) {
                def csv = new CSVWriter(response.writer, {
                    col1:
                    "year" { (it.month as String).substring(0, 4) }
                    col2:
                    "month" { (it.month as String).substring(4) }
                    col3:
                    "reason" { reasonMap.get(it.logReasonTypeId) ?: UNCLASSIFIED_REASON_TYPE }
                    col4:
                    "source" { sourceMap.get(it.logSourceTypeId) ?: UNCLASSIFIED_SOURCE_TYPE }
                    col5:
                    "number of events" { it.numberOfEvents }
                    col6:
                    "number of records" { it.recordCount }
                })

                results.each { e -> csv << e }
            } else {
                response.writer.write("\"year\",\"month\",\"source\",\"number of events\",\"number of records\"")
            }
            response.writer.flush()
        }
    }

    /**
     * Retrieve a monthly breakdown of log events for downloads
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     *     <li>eventId - the event <strong>type</strong> to query on. Mandatory.
     *     <li>entityUid - the entity id to search for.
     *     <li>reasonTypeId - the log reason to query on. Optional. If not provided, all reasons will be included
     *     <li>sourceTypeId - the log source to query on. Optional. If not provided, all sources will be included
     *     <li>excludeReasonTypeId - the <code>logReasonTypeId</code> to exclude from results (usually &quot;testing&quot;)
     * </ul>
     * Example request: <pre>.../logger/sourceBreakdownMonthly?eventId=1002&entityUid=in4&reasonId=1</pre>
     * <p/>
     * Example response: <pre>{"temporalBreakdown":{"201212":{"records":344,"events":1},"201311":{"records":1188,"events":4}}}</pre>
     *
     * @return breakdown of log events by month in JSON format
     */
    def getReasonBreakdownByMonth() {
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing eventId parameter")
        } else {
            def results

            if (params.sourceId) {
                results = loggerService.getTemporalEventsSourceBreakdown(params.eventId, params.entityUid, params.reasonId, params.sourceId, params.excludeReasonTypeId)
            } else {
                results = loggerService.getTemporalEventsReasonBreakdown(params.eventId, params.entityUid, params.reasonId, params.excludeReasonTypeId)
            }

            // convert the list of summaries into a map keyed by the category (month) so it can be rendered in the desired JSON formats
            def grouped = results ? results.collectEntries { [(it.month): [records: it.recordCount, events: it.numberOfEvents]] } : [:]

            render ([temporalBreakdown: grouped] as JSON)
        }
    }

    /**
     * Generate a CSV file containing a monthly breakdown of log events for downloads
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     *     <li>eventId - the logEventTypeId to query on. Mandatory.
     *     <li>entityUid - the entity id to search for.
     *     <li>reasonTypeId - the log reason to query on. Optional. If not provided, all reasons will be included
     *     <li>sourceTypeId - the log source to query on. Optional. If not provided, all sources will be included
     *     <li>excludeReasonTypeId - the <code>logReasonTypeId</code> to exclude from results (usually &quot;testing&quot;). Optional. If not provided, all reasons will be included
     * </ul>
     * Example request: <pre>.../logger/reasonBreakdownByMonthCSV?eventId=1002&entityUid=in4&excludeReasonTypeId=10</pre>
     *
     * @return all log events for the specified eventType and entity in CSV format
     */
    def getReasonBreakdownByMonthCSV() {
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing eventId and/or entityUid")
        } else {
            def results

            if (params.sourceId) {
                results = loggerService.getTemporalEventsSourceBreakdown(params.eventId, params.entityUid, params.reasonId, params.sourceId, params.excludeReasonTypeId)
            } else {
                results = loggerService.getTemporalEventsReasonBreakdown(params.eventId, params.entityUid, params.reasonId, params.excludeReasonTypeId)
            }

            // convert the list of summaries into a map keyed by the category (month) so it can be rendered in the desired JSON formats
            //def grouped = results ? results.collectEntries { [(it.month): [records: it.recordCount, events: it.numberOfEvents]] } : [:]

            response.contentType = "text/csv"
            response.addHeader("Content-Disposition", "attachment; filename=\"downloads-by-reason-monthly-${params.entityUid ?: 'all'}.csv\"")

            if (results) {
                def csv = new CSVWriter(response.writer, {
                    col1:
                    "year-month" { (it.month as String) }
                    col2:
                    "year" { (it.month as String).substring(0, 4) }
                    col3:
                    "month" { (it.month as String).substring(4, 6) }
                    col4:
                    "number of events" { it.numberOfEvents }
                    col5:
                    "number of records" { it.recordCount }
                })

                results.each { e -> csv << e }
            } else {
                response.writer.write("\"year-month\",\"year\",\"month\",\"number of events\",\"number of records\"")
            }

            response.writer.flush()
        }
    }

    /**
     * Retrieve a breakdown of log events by email category for a particular entity
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     * <li>eventId - the event<strong>Type</strong>Id to query on. Mandatory.
     * <li>entityUid - the entityUid to query on. Optional.
     * </ul>
     * <p/>
     * Example url: <pre>.../logger/getReasonBreakdown?eventId=1002&entityUid=in4</pre>
     *
     * @return breakdown of log events by reason in JSON format
     */
    def getEmailBreakdown() {
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing the eventId")
        } else {
            use(TimeCategory) {
                Date nextMonth = nextMonth()

                def results = [:]
                results << ["last3Months": getEmailBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 3.months, nextMonth)]
                results << ["all": getEmailBreakdownForPeriod(params.eventId, params.entityUid, null, null)]
                results << ["thisMonth": getEmailBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 1.month, nextMonth)]
                results << ["lastYear": getEmailBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 12.months, nextMonth)]

                render results as JSON
            }
        }
    }

    /**
     * Generate a CSV file containing all log events for the specified eventType and entity, with the user email category
     * <p/>
     * The request is expected to have the following parameters:
     * <ul>
     *     <li>eventId - the logEventTypeId to query on. Mandatory.
     *     <li>entityUid - the entity id to search for. Mandatory.
     * </ul>
     * Example request: <pre>.../logger/emailBreakdownCSV?eventId=1002&entityUid=in4</pre>
     *
     * @return all log events for the specified eventType and entity in CSV format
     */
    def getEmailBreakdownCSV() {
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing eventId")
        } else {
            def results = loggerService.getLogEventsByEmail(params.eventId, params.entityUid)

            response.contentType = "text/csv"
            response.addHeader("Content-Disposition", "attachment; filename=\"downloads-by-email-${params.entityUid}.csv\"")

            if (results) {
                def csv = new CSVWriter(response.writer, {
                    col1:
                    "year" { (it.month as String)?.substring(0, 4) }
                    col2:
                    "month" { (it.month as String)?.substring(4) }
                    col3:
                    "user category" { it.userEmailCategory }
                    col4:
                    "number of events" { it.numberOfEvents }
                    col5:
                    "number of records" { it.recordCount }
                })

                results.each { e -> csv << e }
            } else {
                response.writer.write("\"year\",\"month\",\"user category\",\"number of events\",\"number of records\"")
            }

            response.writer.flush()
        }
    }

    /**
     * Requests are in the format /{entityUid}/events/{eventId}/counts.
     *  <p/>
     *  Optional param:
     *  <ul>
     *    <li>excludeReasonTypeId - the <code>logReasonTypeId</code> to exclude from results (usually &quot;testing&quot;). Optional. If not provided, all reasons will be included
     *  </ul>
     *  Example request: <pre>.../logger/dr143/events/1024/counts.json</pre>
     */
    def getEntityBreakdown() {
        use(TimeCategory) {
            Date nextMonth = nextMonth()
            Integer excludeReasonTypeId = params.int("excludeReasonTypeId")

            def results = [:]
            results << ["all": getEntityBreakdownForPeriod(params.eventId, params.entityUid, null, null, excludeReasonTypeId)]
            results << ["last3Months": getEntityBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 3.months, nextMonth, excludeReasonTypeId)]
            results << ["thisMonth": getEntityBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 1.month, nextMonth, excludeReasonTypeId)]
            results << ["lastYear": getEntityBreakdownForPeriod(params.eventId, params.entityUid, nextMonth - 12.months, nextMonth, excludeReasonTypeId)]

            render results as JSON
        }
    }

    /**
     * Retrieve a breakdown of log events by event type
     * <p/>
     * Example request: <pre>.../logger/totalsByType
     * <p/>
     * Example response: <pre>{"1000":{"records":2706,"events":62},...}</pre>
     *
     * @return breakdown of log events by event type in JSON format
     */
    def getTotalsByEventType() {
        def results = loggerService.getEventTypeBreakdown()

        // convert the list of summaries into a map keyed by the category (month) so it can be rendered in the desired JSON formats
        def grouped = results.collectEntries { [(it.logEventTypeId): [records: it.recordCount, events: it.numberOfEvents]] }

        render ([totals: grouped] as JSON)
    }

    /**
     * List all log event types
     * <p/>
     * Example url: <pre>.../logger/events</pre>
     *
     * @return all log event types in JSON format
     */
    def getEventTypes() {
        render loggerService.getAllEventTypes().collect({k -> [name: k.name, id: k.id]}) as JSON
    }

    /**
     * List all log reason types
     * <p/>
     * Example url: <pre>.../logger/reasons</pre>
     *
     * @return all log reason types in JSON format
     */
    def getReasonTypes() {
        def json = loggerService.getAllReasonTypes().collect({k -> [rkey: k.rkey, name: k.name, id: k.id, deprecated: k.isDeprecated]}) as JSON
        log.debug "getReasonTypes = ${json}"
        render json
    }

    /**
     * List all log source types
     * <p/>
     * Example url: <pre>.../logger/sources</pre>
     *
     * @return all log source types in JSON format
     */
    def getSourceTypes() {
        render loggerService.getAllSourceTypes().collect({k -> [name: k.name, id: k.id]}) as JSON
    }

    // returns a triple of [totalEvents | totalRecords | emailBreakdown] for the requested period.
    private def getEmailBreakdownForPeriod(eventTypeId, entityUid, from, to) {
        def emailSummary = loggerService.getEventsEmailBreakdown(eventTypeId as int, entityUid, getYearAndMonth(from), getYearAndMonth(to))

        def grouped = EMAIL_CATEGORIES.collectEntries { v -> [(v): ["events": 0, "records": 0]] }

        def totalEvents = 0
        def totalRecords = 0

        if (emailSummary) {
            emailSummary.each {
                def entry = grouped[it.userEmailCategory]
                entry["records"] += it.recordCount
                entry["events"] += it.numberOfEvents
                totalEvents += it.numberOfEvents
                totalRecords += it.recordCount
            }
        }

        [events: totalEvents, records: totalRecords, emailBreakdown: grouped]
    }

    // returns a triple of [totalEvents | totalRecords | reasonBreakdown] for the requested period.
    private def getReasonBreakdownForPeriod(eventTypeId, entityUid, from, to, reasonMap) {
        def reasonSummary = loggerService.getEventsReasonBreakdown(eventTypeId as int, entityUid, getYearAndMonth(from), getYearAndMonth(to))

        def grouped = reasonMap.collectEntries { k, v -> [(v): ["events": 0, "records": 0]] }
                .withDefault { ["events": 0, "records": 0] }

        def totalEvents = 0
        def totalRecords = 0

        if (reasonSummary) {
            reasonSummary.each {
                def entry = grouped[reasonMap[it.logReasonTypeId] ?: UNCLASSIFIED_REASON_TYPE]
                entry["records"] += it.recordCount
                entry["events"] += it.numberOfEvents
                totalEvents += it.numberOfEvents
                totalRecords += it.recordCount
            }
        }

        [events: totalEvents, records: totalRecords, reasonBreakdown: grouped]
    }

    // returns a triple of [totalEvents | totalRecords | sourceBreakdown] for the requested period.
    private def getSourceBreakdownForPeriod(eventTypeId, entityUid, from, to, sourceMap, Integer excludeReasonTypeId ) {
        def sourceSummary = loggerService.getEventsSourceBreakdown(eventTypeId as int, entityUid, getYearAndMonth(from), getYearAndMonth(to), excludeReasonTypeId)

        def grouped = sourceMap.collectEntries { k, v -> [(v): ["events": 0, "records": 0]] }
                .withDefault { ["events": 0, "records": 0] }

        def totalEvents = 0
        def totalRecords = 0

        if (sourceSummary) {
            sourceSummary.each {
                def entry = grouped[sourceMap[it.logSourceTypeId] ?: UNCLASSIFIED_SOURCE_TYPE]
                entry["records"] += it.recordCount
                entry["events"] += it.numberOfEvents
                totalEvents += it.numberOfEvents
                totalRecords += it.recordCount
            }
        }

        [events: totalEvents, records: totalRecords, sourceBreakdown: grouped]
    }

    // returns a tuple of [totalEvents | totalRecords] for the requested period.
    private def getEntityBreakdownForPeriod(eventTypeId, entityUid, from, to, excludeReasonTypeId) {
        def entitySummary = loggerService.getLogEventsByEntity(eventTypeId as int, entityUid, getYearAndMonth(from), getYearAndMonth(to), excludeReasonTypeId)

        def totalEvents = 0
        def totalRecords = 0

        if (entitySummary) {
            entitySummary.each {
                totalEvents += it.numberOfEvents
                totalRecords += it.recordCount
            }
        }

        [numberOfEvents: totalEvents, numberOfEventItems: totalRecords]
    }

    private def handleError(HttpStatus httpStatus, String logMessage, Throwable e = null) {
        log.error(logMessage, e)
        response.setStatus(httpStatus.value())
        render(status: httpStatus.value(), text: logMessage)
    }

    private getReasonMap() {
        Map<Integer, String> reasonMap = loggerService.getAllReasonTypes().collectEntries({
            [it.id as Integer, it.name]
        })
        reasonMap
    }

    private getSourceMap() {
        Map<Integer, String> sourceMap = loggerService.getAllSourceTypes().collectEntries({
            [it.id as Integer, it.name]
        })
        sourceMap
    }

    /**
     * Returns first day of next month
     * @return Date
     */
    private Date nextMonth() {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_MONTH, 1);
        Date nextMonth = date.getTime() + 1.month
        nextMonth
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
