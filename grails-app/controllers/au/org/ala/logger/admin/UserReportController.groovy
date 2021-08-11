package au.org.ala.logger.admin

import au.org.ala.web.AlaSecured
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import grails.plugins.csv.CSVWriter

/**
 * Controller that supports the download of a user report for events
 */
@AlaSecured(value = "ROLE_ADMIN", redirectController = 'logger', redirectAction = 'notAuthorised')
class UserReportController {

    def loggerService

    def index() {}

    def uidLookupCache = [:]

    def download(){
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing eventId")
        } else if (!params.entityUids) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing entityUids")

        } else {

            List entityUidList = params.entityUids.split(",")
            List months = []
            if(params.months){
                months = (params.months?:'').split(",")
            }

            def results = loggerService.getUserBreakdown(
                    Integer.parseInt(params.eventId),
                    entityUidList,
                    months
            )

            response.contentType = "text/csv"
            response.addHeader("Content-Disposition", "attachment; filename=\"${params.fileName?:'UserDownloadReport.csv'}\"")

            if (results) {
                def csv = new CSVWriter(response.writer, {
                    col1:
                    "email" { it[0] }
                    col2:
                    "UID" { it[1] }
                    col3:
                    "name" { getEntityNameFromUid(it[1]) }
                    col4:
                    "download reason" { it[2] }
                    col5:
                    "number of events" { it[3] }
                    col6:
                    "number of records" { it[4] }
                })

                results.each { e -> csv << e }
            } else {
                response.writer.write("\"email\",\"uid\",\"name\",\"number of events\",\"number of records\"")
            }
            response.writer.flush()
        }
    }

    def downloadDetailed(){
        if (!params.eventId) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing eventId")
        } else if (!params.entityUids) {
            handleError(HttpStatus.BAD_REQUEST, "Request is missing entityUids")

        } else {

            List entityUidList = params.entityUids.split(",")
            List months = []
            if(params.months){
                months = (params.months?:'').split(",")
            }

            def results = loggerService.getUserBreakdownDetailed(
                    Integer.parseInt(params.eventId),
                    entityUidList,
                    months

            )

            response.contentType = "text/csv"
            response.addHeader("Content-Disposition", "attachment; filename=\"${params.fileName?:'UserDownloadReport.csv'}\"")

            if (results) {
                def csv = new CSVWriter(response.writer, {
                    col1:
                    "email" { it[0] }
                    col2:
                    "UID" { it[1] }
                    col3:
                    "name" { getEntityNameFromUid(it[1]) }
                    col4:
                    "download reason" { it[2] }
                    col5:
                    "number of records" { it[3] }
                    col6:
                    "date created" { it[4] }
                    col7:
                    "source" { it[5] }
                })

                results.each { e -> csv << e }
            } else {
                response.writer.write("\"email\",\"uid\",\"name\",\"download reason\",\"number of records\",\"date created\",\"source\"\"")
            }
            response.writer.flush()
        }
    }

    def getEntityNameFromUid(uid){

        if(uidLookupCache[uid]){
            return uidLookupCache[uid]
        }

        def url = grailsApplication.config.collectoryUrl + "/lookup/summary/" + uid
        def js = new JsonSlurper()
        def json = js.parseText(new URL(url).getText())
        if(json.name){
            uidLookupCache[uid] = json.name
        }

        uidLookupCache[uid]
    }

    private def handleError(HttpStatus httpStatus, String logMessage, Throwable e = null) {
        log.error(logMessage, e)
        response.setStatus(httpStatus.value())
        render(status: httpStatus.value(), text: logMessage)
    }
}
