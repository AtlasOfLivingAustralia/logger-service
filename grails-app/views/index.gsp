<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="layout" content="${grailsApplication.config.skin.layout}" />
	<meta name="breadcrumb" content="Logger Service" />
	<title>Logger service | ${grailsApplication.config.skin.orgNameLong}</title>

	<script>
		document.addEventListener("DOMContentLoaded", function () {
			var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
			tooltipTriggerList.map(function (tooltipTriggerEl) {
				return new bootstrap.Tooltip(tooltipTriggerEl)
			})
		})
	</script>
</head>
<body>
<div class="container py-4">
	<h1 class="mb-4">Logger web services</h1>
</div>

<div class="container">
	<div class="d-flex align-items-center mb-3">
		<div class="lead mb-0">
			Below is a list of reporting services that return JSON or CSV.
		</div>

		<g:link controller="admin" action="index"
				class="btn btn-sm btn-outline-primary ms-auto">
			Admin
		</g:link>
	</div>

	<g:set var="jsonTitle" value="View the JSON file"/>
	<g:set var="csvTitle" value="Download CSV file"/>

	<div class="list-group list-group-flush">
		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/logger/reasons"
			   title="${jsonTitle}"
			   data-bs-toggle="tooltip"
			   data-bs-placement="top">
				<strong>User reasons</strong> codes
			</a>
			<span class="badge bg-secondary ms-2">json</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/logger/sources"
			   title="${jsonTitle}"
			   data-bs-toggle="tooltip">
				<strong>Sources</strong> codes
			</a>
			<span class="badge bg-secondary ms-2">json</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/logger/events"
			   title="${jsonTitle}"
			   data-bs-toggle="tooltip">
				<strong>Events</strong> codes
			</a>
			<span class="badge bg-secondary ms-2">json</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/reasonBreakdown?eventId=1002&entityUid=in4"
			   title="${jsonTitle}"
			   data-bs-toggle="tooltip">
				<strong>Reason</strong> breakdown
			</a>
			<span class="badge bg-secondary ms-2">json</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/sourceBreakdown?eventId=1002&entityUid=in4"
			   title="${jsonTitle}"
			   data-bs-toggle="tooltip">
				<strong>Source</strong> breakdown breakdown (last month, 3 month, 1 year, all, example for downloads from Australian Museum)
			</a>
			<span class="badge bg-secondary ms-2">json</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/reasonBreakdownMonthly?eventId=1002&sourceId=2001&entityUid=in4"
			   title="${jsonTitle}"
			   data-bs-toggle="tooltip">
				<strong>Reason Monthly</strong> breakdown (event and record counts only) with optional <code>reasonId</code> and <code>sourceId</code> filters (example for downloads from source ALA4R)
			</a>
			<span class="badge bg-secondary ms-2">json</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/reasonBreakdownCSV?eventId=1002&entityUid=in4"
			   title="${csvTitle}"
			   data-bs-toggle="tooltip">
				<strong>Reason</strong> breakdown by month (all records, example for Australian Museum)
			</a>
			<span class="badge bg-success ms-2">csv</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/emailBreakdownCSV?eventId=1002&entityUid=in4"
			   title="${csvTitle}"
			   data-bs-toggle="tooltip">
				<strong>User category</strong> breakdown by month (all records, example for downloads from Australian Museum)
			</a>
			<span class="badge bg-success ms-2">csv</span>
		</div>

		<div class="list-group-item d-flex align-items-center">
			<a href="${request.contextPath}/service/sourceBreakdownCSV?eventId=1002&entityUid=in4"
			   title="${csvTitle}"
			   data-bs-toggle="tooltip">
				<strong>Source and reason</strong> breakdown by month (all records, example for downloads from Australian Museum)
			</a>
			<span class="badge bg-success ms-2">csv</span>
		</div>
	</div>
</div>
</body>
</html>
