<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="layout" content="${grailsApplication.config.skin.layout}" />
	<title>Logger service |  ${grailsApplication.config.skin.orgNameLong}</title>
</head>
<body>
<div class="container">
	<h1>Logger web services</h1>
</div>
<div class="container">
	<p class="lead">
		Below is a list of reporting services that return JSON or CSV.
	</p>
	<ul>
		<li><a href="${request.contextPath}/service/logger/reasons">User reasons codes (JSON) </a></li>
		<li><a href="${request.contextPath}/service/logger/sources">Sources codes (JSON) </a></li>
		<li><a href="${request.contextPath}/service/logger/events">Events codes (JSON) </a></li>
		<li><a href="${request.contextPath}/service/reasonBreakdown?eventId=1002">Downloads reason breakdown (3 month, 1 year, all)</a></li>
		<li><a href="${request.contextPath}/service/sourceBreakdown?eventId=1002">Downloads source breakdown (3 month, 1 year, all)</a></li>
		<li><a href="${request.contextPath}/service/reasonBreakdownMonthly?eventId=1002&entityUid=in4&sourceId=2001">Downloads breakdown by months with optional reasonId and sourceId filters (example for Australian Museum downloads from source ALA4R)</a></li>
		<li><a href="${request.contextPath}/service/reasonBreakdownCSV?eventId=1002">Downloads by reason breakdown in CSV (all records)</a></li>
		<li><a href="${request.contextPath}/service/emailBreakdownCSV?eventId=1002">Downloads by user category breakdown in CSV (all records)</a></li>
		<li><a href="${request.contextPath}/service/sourceBreakdownCSV?eventId=1002">Downloads by source and reason breakdown in CSV (all records)</a></li>
	</ul>
</div>
</body>
</html>