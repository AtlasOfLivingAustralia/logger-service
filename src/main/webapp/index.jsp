<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <!-- Latest compiled and minified CSS -->
        <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css">
        <!-- Optional theme -->
        <link rel="stylesheet" href="http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap-theme.min.css">
        <!-- Latest compiled and minified JavaScript -->
        <script src="//netdna.bootstrapcdn.com/bootstrap/3.0.3/js/bootstrap.min.js"></script>
        <title>Logger service</title>
    </head>
    <body>
        <div class="jumbotron">
            <div class="container">
                <h1>Logger web services</h1>
            </div>
        </div>
        <div class="container">
            <p class="lead">
                Below is a list of reporting services that return JSON or CSV.
            </p>
            <ul>
                <li><a href="${pageContext.request.contextPath}/service/logger/reasons">User reasons for download (JSON) </a></li>
                <li><a href="${pageContext.request.contextPath}/service/logger/sources">Sources (JSON) </a></li>
                <li><a href="${pageContext.request.contextPath}/service/logger/events">Events (JSON) </a></li>
                <li><a href="${pageContext.request.contextPath}/service/reasonBreakdown?eventId=1002&entityUid=in4">Downloads breakdown (3 month, 1 year, all)</a></li>
                <li><a href="${pageContext.request.contextPath}/service/reasonBreakdownMonthly?eventId=1002&entityUid=in4">Downloads breakdown by months (example for Australian Museum)</a></li>
                <li><a href="${pageContext.request.contextPath}/service/reasonBreakdownCSV?eventId=1002&entityUid=in4">Downloads by reason breakdown in CSV (example for Australian Museum)</a></li>
                <li><a href="${pageContext.request.contextPath}/service/reasonEmailCSV?eventId=1002&entityUid=in4">Downloads by user category breakdown in CSV (example for Australian Museum)</a></li>
            </ul>
        </div>
	</body>
</html>