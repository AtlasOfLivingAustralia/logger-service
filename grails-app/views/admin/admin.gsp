<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta name="layout" content="${grailsApplication.config.skin.layout}" />
</head>
<body>
<div>
  <div>
    <h1>Logger Service Administration</h1>
  </div>
</div>
<div>
  <h2>All web services</h2>
  <p>
    Below is a list of all web services exposed by the logger service.
  </p>
  <ul>
    <li><a href="${request.contextPath}/service/logger/reasons">/service/logger/reasons</a></li>
    <li><a href="${request.contextPath}/service/logger/events">/service/logger/events</a></li>
    <li><a href="${request.contextPath}/service/logger/sources">/service/logger/sources</a></li>
    <li><a href="${request.contextPath}/service/logger/1">/service/logger/1</a></li>
    <li><a href="${request.contextPath}/service/logger/get.json?q=dr143&eventTypeId=1&year=2021">/service/logger/get.json?q=dr143&eventTypeId=1&year=2021</a></li>
    <li><a href="${request.contextPath}/service/reasonBreakdownCSV?entityUid=dr143&eventId=1">/service/reasonBreakdownCSV?entityUid=dr143&eventId=1</a></li>
    <li><a href="${request.contextPath}/service/reasonBreakdown?entityUid=dr143&eventId=1">/service/reasonBreakdown?entityUid=dr143&eventId=1</a></li>
    <li><a href="${request.contextPath}/service/reasonBreakdownMonthly?entityUid=dr143&eventId=1&reasonId=1">/service/reasonBreakdownMonthly?entityUid=dr143&eventId=1&reasonId=1</a></li>
    <li><a href="${request.contextPath}/service/emailBreakdownCSV?entityUid=dr143&eventId=1">/service/emailBreakdownCSV?entityUid=dr143&eventId=1</a></li>
    <li><a href="${request.contextPath}/service/emailBreakdown?entityUid=dr143&eventId=1">/service/emailBreakdown?entityUid=dr143&eventId=1</a></li>
  </ul>
  There is also 1 POST operation at ${request.contextPath}/service/logger to upload a new (JSON) log event.

  <h2>User reports</h2>
  <ul>
    <li><a href="${request.contextPath}/admin/userReport">Download a user report for a set of entities</a></li>
  </ul>

  <h2>Data view</h2>
  <p>
    Below is a list of data views for each table in the logger service database.
  </p>
  <ul>
    <li><a href="${request.contextPath}/admin/logEvent">Log Events</a></li>
    <li><a href="${request.contextPath}/admin/logDetail">Log Details</a></li>
    <li><a href="${request.contextPath}/admin/logEventType">Log Event Types</a></li>
    <li><a href="${request.contextPath}/admin/logReasonType">Log Reason Types</a></li>
    <li><a href="${request.contextPath}/admin/logSourceType">Log Source Types</a></li>
    <li><a href="${request.contextPath}/admin/remoteAddress">Remote Addresses</a></li>

    <li><a href="${request.contextPath}/admin/eventSummaryTotal">Event Summary Totals</a></li>
    <li><a href="${request.contextPath}/admin/eventSummaryBreakdownEmail">Event Summary Breakdown by Email</a></li>
    <li><a href="${request.contextPath}/admin/eventSummaryBreakdownEmailEntity">Event Summary Breakdown by Email and Entity</a></li>
    <li><a href="${request.contextPath}/admin/eventSummaryBreakdownReason">Event Summary Breakdown by Reason</a></li>
    <li><a href="${request.contextPath}/admin/eventSummaryBreakdownReasonEntity">Event Summary Breakdown by Reason and Entity</a></li>
  </ul>

</div>
</body>
</html>