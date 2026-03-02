<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <g:set var="entityName" value="${message(code: 'logEvent.label', default: 'LogEvent')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
    <meta name="breadcrumb" content="${entityName}" />
    <g:set var="adminLink" value="${createLink(controller:'admin', action:'index')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>

<body>
    <div id="list-logEvent" class="container py-4" role="main">

        <h1 class="mb-4">
            <g:message code="default.list.label" args="[entityName]"/>
        </h1>

        <g:if test="${flash.message}">
            <div class="alert alert-info alert-dismissible fade show" role="alert">
                ${flash.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </g:if>

        <div class="table-responsive mb-4">
            <table class="table table-striped table-hover table-bordered align-middle w-100">
                <thead class="table-light">
                <tr class="text-center">
                    <th>Month</th>
                    <th>Log Event Type ID</th>
                    <th>Log Source Type ID</th>
                    <th>Log Reason Type ID</th>
                    <th>User Email</th>
                    <th>Source</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${logEventList}" var="log">
                    <tr>
                        <td class="text-center">${log.month}</td>
                        <td class="text-center">${log.logEventTypeId}</td>
                        <td class="text-center">${log.logSourceTypeId}</td>
                        <td class="text-center">${log.logReasonTypeId}</td>
                        <td>${log.userEmail}</td>
                        <td>${log.source}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>

        <div class="d-flex justify-content-center gap-2">
            <g:paginate total="${logEventCount ?: 0}" class="pagination"/>
        </div>

    </div>
</body>
</html>