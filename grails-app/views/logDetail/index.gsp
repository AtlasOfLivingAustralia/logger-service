<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <g:set var="entityName" value="${message(code: 'logDetail.label', default: 'LogDetail')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
    <meta name="breadcrumb" content="${entityName}" />
    <g:set var="adminLink" value="${createLink(controller:'admin', action:'index')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>

<body>
    <div id="list-logDetail" class="content scaffold-list" role="main">
        <h1><g:message code="default.list.label" args="[entityName]"/></h1>

        <g:if test="${flash.message}">
            <div class="alert alert-info" role="status">${flash.message}</div>
        </g:if>

        <table class="table table-striped table-hover table-bordered w-100">
            <thead>
            <tr>
                <th>Entity Type</th>
                <th>Entity UID</th>
                <th>Log Event ID</th>
                <th>Record Count</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${logDetailList}" var="ld">
                <tr>
                    <td>
                        <g:link controller="logEventType" action="show" id="${ld.entityType}">
                            ${ld.entityType}
                        </g:link>
                    </td>
                    <td>${ld.entityUid}</td>
                    <td>
                        <g:link controller="logEvent" action="show" id="${ld.logEvent?.id}">
                            ${ld.logEvent?.id}
                        </g:link>
                    </td>
                    <td>${ld.recordCount}</td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <div class="d-flex justify-content-center mt-3 gap-2">
            <g:paginate total="${logDetailCount ?: 0}" class="pagination"/>
        </div>
    </div>
</body>
</html>