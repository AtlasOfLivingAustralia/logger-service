<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <title>User report download | Logger service |  ${grailsApplication.config.skin.orgNameLong}</title>
</head>
<body>
<div class="jumbotron">
    <div class="container">
        <h1>User report download</h1>
    </div>
</div>
<div class="container">
    <p class="lead">
        Below is a list of reporting services that return JSON or CSV.
    </p>

    <g:form controller="userReport" action="download">

        <p>
            <label for="entityUids">Entity UIDS</label>
            <input id="entityUids" name="entityUids" class="input-xxlarge" type="text" value="dr1305,dr1178,dr528,dr1237,dr529"/>
        </p>
        <p>
            <label for="eventId">Event ID (1002 = downloads)</label>
            <input id="eventId" name="eventId" class="input-medium" type="text" value="1002"/>
        </p>
        <p>
            <label for="fromDate">From month (mmyyyy format e.g. 201607)</label>
            <input id="fromDate" name="fromDate" class="input-large" type="text" value=""/>
        </p>
        <p>
            <label for="toDate">To month (mmyyyy format e.g. 201707)</label>
            <input id="toDate" name="toDate" class="input-large" type="text" value=""/>
        </p>

        <button type="submit" class="btn btn-primary"> <i class="fa-download"></i> Download </button>
    </g:form>
</div>
</body>
</html>