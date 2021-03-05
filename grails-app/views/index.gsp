<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="layout" content="${grailsApplication.config.skin.layout}" />
	<meta name="breadcrumb" content="Logger Service" />
	<title>Logger service | ${grailsApplication.config.skin.orgNameLong}</title>
	<style type="text/css">
		span.file-type-icon {
			display: inline-block;
			font-family: "Andale Mono";
			font-size: 14px;
			font-weight: bolder;
			color: white;
			background-color: #1B56A0;
			padding: 1px 2px ;
			margin-bottom: 1px;
			opacity: 0.8;
		}
		span.file-type-icon.json {
			background-color: #1B56A0;
		}
		span.file-type-icon.csv {
			background-color: #099114;
		}
		li {
			font-size: 15px;
		}
	</style>
	<script type="text/javascript">
		$(function() {
			$('.tooltips').tooltip({placement: "auto bottom"});
		});
	</script>
</head>
<body>
<div class="container">
	<h1>Logger web services</h1>
</div>
<div class="container">
	<p class="lead">
		Below is a list of reporting services that return JSON or CSV.
	</p>
	<g:set var="jsonTitle" value="View the JSON file"/>
	<g:set var="csvTitle" value="Download CSV file"/>
	<ul>
		<li><a href="${request.contextPath}/service/logger/reasons" title="${jsonTitle}" class="tooltips"><strong>User reasons</strong> codes</a> <span class="file-type-icon json">json</span></li>
		<li><a href="${request.contextPath}/service/logger/sources" title="${jsonTitle}" class="tooltips"><strong>Sources</strong> codes</a> <span class="file-type-icon json">json</span></li>
		<li><a href="${request.contextPath}/service/logger/events" title="${jsonTitle}" class="tooltips"><strong>Events</strong> codes</a> <span class="file-type-icon json">json</span></li>
		<li><a href="${request.contextPath}/service/reasonBreakdown?eventId=1002" title="${jsonTitle}" class="tooltips"><strong>Reason</strong> breakdown (last month, 3 month, 1 year, all)</a> <span class="file-type-icon json">json</span></li>
		<li><a href="${request.contextPath}/service/sourceBreakdown?eventId=1002" title="${jsonTitle}" class="tooltips"><strong>Source</strong> breakdown (last month, 3 month, 1 year, all)</a> <span class="file-type-icon json">json</span></li>
	<li><a href="${request.contextPath}/service/reasonBreakdownMonthly?eventId=1002&sourceId=2001" title="${jsonTitle}" class="tooltips"><strong>Monthly</strong> breakdown (event and record counts only) with optional <code>reasonId</code> and <code>sourceId</code> filters (example for downloads from source ALA4R)</a> <span class="file-type-icon json">json</span></li>
		<li><a href="${request.contextPath}/service/reasonBreakdownCSV?eventId=1002" title="${csvTitle}" class="tooltips"><strong>Reason</strong> breakdown by month (all records)</a> <span class="file-type-icon csv">csv</span></li>
		<li><a href="${request.contextPath}/service/emailBreakdownCSV?eventId=1002" title="${csvTitle}" class="tooltips"><strong>User category</strong> breakdown by month (all records)</a> <span class="file-type-icon csv">csv</span></li>
		<li><a href="${request.contextPath}/service/sourceBreakdownCSV?eventId=1002" title="${csvTitle}" class="tooltips"><strong>Source and reason</strong> breakdown by month (all records)</a> <span class="file-type-icon csv">csv</span></li>
	</ul>
</div>
</body>
</html>