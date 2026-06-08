package au.org.ala.logger

class UrlMappings {

    static mappings = {

        "/service/logger"(controller: "logger", action: [POST: "save"])
        "/service/logger/"(controller: "logger", action: [POST: "save"])

        "/service/logger/events"(controller: "logger", action: [GET: "getEventTypes"])
        "/service/logger/reasons"(controller: "logger", action: [GET: "getReasonTypes"])
        "/service/logger/sources"(controller: "logger", action: [GET: "getSourceTypes"])

        "/service/logger/get.json"(controller: "logger", action: [GET: "monthlyBreakdown"])

        "/service/reasonBreakdown"(controller: "logger", action: [GET: "getReasonBreakdown"])
        "/service/reasonBreakdown.json"(controller: "logger", action: [GET: "getReasonBreakdown"])
        "/service/reasonBreakdownMonthly"(controller: "logger", action: [GET: "getReasonBreakdownByMonth"])
        "/service/reasonBreakdownByMonthCSV"(controller: "logger", action: [GET: "getReasonBreakdownByMonthCSV"])
        "/service/reasonBreakdownCSV"(controller: "logger", action: [GET: "getReasonBreakdownCSV"])

        "/service/sourceBreakdown"(controller: "logger", action: [GET: "getSourceBreakdown"])
        "/service/sourceBreakdown.json"(controller: "logger", action: [GET: "getSourceBreakdown"])
        "/service/sourceBreakdownCSV"(controller: "logger", action: [GET: "getSourceBreakdownCSV"])

        "/service/emailBreakdown"(controller: "logger", action: [GET: "getEmailBreakdown"])
        "/service/emailBreakdown.json"(controller: "logger", action: [GET: "getEmailBreakdown"])
        "/service/emailBreakdownCSV"(controller: "logger", action: [GET: "getEmailBreakdownCSV"])

        "/service/totalsByType"(controller: "logger", action: [GET: "getTotalsByEventType"])

        "/service/logger/$id"(controller: "logger", action: [GET: "getEventLog"])

        "/service/$entityUid/events/$eventId/counts.json"(controller: "logger", action: [GET: "getEntityBreakdown"])
        "/service/$entityUid/events/$eventId/counts"(controller: "logger", action: [GET: "getEntityBreakdown"])

        "/service/userBreakdown"(controller: "logger",  action: [GET: "getUserBreakdownCSV"])

        "/logger/notAuthorised"(controller: "logger",  action: [GET: "notAuthorised"])

        "/service/userBreakdown"(resource: "logger", includes: ["userBreakdown"])

        "403"(view: '/error')
        "404"(view: '/notFound')
        "500"(view: '/error')

        "/"(controller: "logger")

        // admin screens
        "/admin/"(view: "admin", controller: "admin")
        "/admin/logEvent/$action?/$id?"(controller: "logEvent")
        "/admin/logDetail/$action?/$id?"(controller: "logDetail")
        "/admin/logEventType/$action?/$id?"(controller: "logEventType")
        "/admin/logReasonType/$action?/$id?"(controller: "logReasonType")
        "/admin/logSourceType/$action?/$id?"(controller: "logSourceType")
        "/admin/remoteAddress/$action?/$id?"(controller: "remoteAddress")
        "/admin/eventSummaryBreakdownEmail/$action?/$id?"(controller: "eventSummaryBreakdownEmail")
        "/admin/eventSummaryBreakdownEmailEntity/$action?/$id?"(controller: "eventSummaryBreakdownEmailEntity")
        "/admin/eventSummaryBreakdownReason/$action?/$id?"(controller: "eventSummaryBreakdownReason")
        "/admin/eventSummaryBreakdownReasonEntity/$action?/$id?"(controller: "eventSummaryBreakdownReasonEntity")
        "/admin/eventSummaryTotal/$action?/$id?"(controller: "eventSummaryTotal")

        "/admin/userReport"(controller: "userReport")
        "/admin/userReport/download"(controller: "userReport", action:"download")
        "/admin/userReport/downloadDetailed"(controller: "userReport", action:"downloadDetailed")
        "/service/admin/userReport/download"(controller: "userReport", action:"download")
        "/service/admin/userReport/downloadDetailed"(controller: "userReport", action:"downloadDetailed")


        "/logout/$action?/$id?"(controller:'logout'){
            constraints {
                // apply constraints here
            }
        }
    }
}