<!doctype html>
<html lang="en">
<head>
  <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
  <meta name="breadcrumb" content="Admin" />
  <meta name="breadcrumbParent" content="${grailsApplication.config.grails.serverURL?:'/'},Logger" />
  <title>Logger service | ${grailsApplication.config.skin.orgNameLong}</title>
</head>

<body>
<div class="container py-4">

  <h1 class="mb-4">Logger Service Administration</h1>

  <!-- ================= All Web Services ================= -->
  <div class="mb-5">
    <h2 class="h4 mb-3">All web services</h2>
    <p class="text-muted">
      Below is a list of all web services exposed by the logger service.
    </p>

    <div class="mb-3">
      <div class="mb-2"><a href="${request.contextPath}/service/logger/reasons">/service/logger/reasons</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/logger/events">/service/logger/events</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/logger/sources">/service/logger/sources</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/logger/1">/service/logger/1</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/logger/get.json?q=dr143&eventTypeId=1&year=2021">/service/logger/get.json?q=dr143&eventTypeId=1&year=2021</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/reasonBreakdownCSV?entityUid=dr143&eventId=1">/service/reasonBreakdownCSV?entityUid=dr143&eventId=1</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/reasonBreakdown?entityUid=dr143&eventId=1">/service/reasonBreakdown?entityUid=dr143&eventId=1</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/reasonBreakdownMonthly?entityUid=dr143&eventId=1&reasonId=1">/service/reasonBreakdownMonthly?entityUid=dr143&eventId=1&reasonId=1</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/emailBreakdownCSV?entityUid=dr143&eventId=1">/service/emailBreakdownCSV?entityUid=dr143&eventId=1</a></div>
      <div class="mb-2"><a href="${request.contextPath}/service/emailBreakdown?entityUid=dr143&eventId=1">/service/emailBreakdown?entityUid=dr143&eventId=1</a></div>
    </div>

    <div class="alert alert-info">
      There is also one <strong>POST</strong> operation at
      <code>${request.contextPath}/service/logger</code>
      to upload a new (JSON) log event.
    </div>
  </div>

  <!-- ================= User Reports ================= -->
  <div class="mb-5">
    <h2 class="h4 mb-3">User reports</h2>
    <div class="mb-2">
      <a href="${request.contextPath}/admin/userReport">
        Download a user report for a set of entities
      </a>
    </div>
  </div>

  <!-- ================= Data View ================= -->
  <div class="mb-5">
    <h2 class="h4 mb-3">Data view</h2>
    <p class="text-muted">
      Below is a list of data views for each table in the logger service database.
    </p>

    <div class="mb-2"><a href="${request.contextPath}/admin/logEvent">Log Events</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/logDetail">Log Details</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/logEventType">Log Event Types</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/logReasonType">Log Reason Types</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/logSourceType">Log Source Types</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/remoteAddress">Remote Addresses</a></div>

    <div class="mb-2"><a href="${request.contextPath}/admin/eventSummaryTotal">Event Summary Totals</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/eventSummaryBreakdownEmail">Event Summary Breakdown by Email</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/eventSummaryBreakdownEmailEntity">Event Summary Breakdown by Email and Entity</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/eventSummaryBreakdownReason">Event Summary Breakdown by Reason</a></div>
    <div class="mb-2"><a href="${request.contextPath}/admin/eventSummaryBreakdownReasonEntity">Event Summary Breakdown by Reason and Entity</a></div>
  </div>

</div>
</body>
</html>