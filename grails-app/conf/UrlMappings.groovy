class UrlMappings {

    static mappings = {
        "/service/logger/events"(controller: "logger", action: [GET: "getEventTypes"])
        "/service/logger/reasons"(controller: "logger", action: [GET: "getReasonTypes"])
        "/service/logger/sources"(controller: "logger", action: [GET: "getSourceTypes"])

        "/service/logger/get.json"(controller: "logger", action: [GET: "monthlyBreakdown"])

        "/service/reasonBreakdown"(controller: "logger", action: [GET: "getReasonBreakdown"])
        "/service/reasonBreakdown.json"(controller: "logger", action: [GET: "getReasonBreakdown"])
        "/service/reasonBreakdownMonthly"(controller: "logger", action: [GET: "getReasonBreakdownByMonth"])
        "/service/reasonBreakdownCSV"(controller: "logger", action: [GET: "getReasonBreakdownCSV"])

        "/service/emailBreakdown"(controller: "logger", action: [GET: "getEmailBreakdown"])
        "/service/emailBreakdown.json"(controller: "logger", action: [GET: "getEmailBreakdown"])
        "/service/emailBreakdownCSV"(controller: "logger", action: [GET: "getEmailBreakdownCSV"])

        "/service/totalsByType"(controller: "logger", action: [GET: "getTotalsByEventType"])

        "/service/logger/$id"(controller: "logger", action: [GET: "getEventLog"])

        "/service/$entityUid/events/$eventId/counts.json"(controller: "logger", action: [GET: "getEntityBreakdown"])
        "/service/$entityUid/events/$eventId/counts"(controller: "logger", action: [GET: "getEntityBreakdown"])

        "/service/logger"(resource: "logger", includes: ["save"])

        "500"(view: '/error')

        "/"(view: "/index", controller: "logger")

        // admin screens
        "/admin/"(view: "/admin/admin", controller: "admin")
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

        "/logout/$action?/$id?"(controller:'logout'){
            constraints {
                // apply constraints here
            }
        }
    }
}