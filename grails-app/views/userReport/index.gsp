<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>User report download  |  ${grailsApplication.config.skin.orgNameLong}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="layout" content="${grailsApplication.config.skin.layout}" />
    <meta name="breadcrumb" content="User report" />
    <g:set var="adminLink" value="${createLink(controller:'admin', action:'index')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>
<body>
<div class="container py-4">
    <h1 class="mb-4">User report download</h1>

    <p class="lead mb-4">
        Below is a list of reporting services that return JSON or CSV.
    </p>

    <g:form controller="userReport" action="download" class="row g-3">

        <div class="col-12">
            <label for="entityUids" class="form-label">Entity UIDS (comma separated e.g. dr523,dr233)</label>
            <input id="entityUids" name="entityUids" type="text" class="form-control"
                   value="dr1305,dr1178,dr528,dr1237,dr529"/>
        </div>

        <div class="col-md-6 col-12">
            <label for="eventId" class="form-label">Event ID (1002 = downloads)</label>
            <input id="eventId" name="eventId" type="text" class="form-control" value="1002"/>
        </div>

        <div class="col-md-6 col-12">
            <label for="months" class="form-label">Months (comma separated, yyyyMM format e.g. 201607)</label>
            <input id="months" name="months" type="text" class="form-control" value=""
                   pattern="^\d{6}(,\d{6})*$"
                   title="Enter months as comma-separated yyyyMM values, e.g. 202301,202302"/>
        </div>

        <div class="col-12">
            <button type="submit" class="btn btn-primary">
                <i class="bi bi-download"></i> Download
            </button>
        </div>

    </g:form>
</div>
</body>
</html>