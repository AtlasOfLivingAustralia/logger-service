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

        "/"(view:"/index", controller: "logger")
    }
}