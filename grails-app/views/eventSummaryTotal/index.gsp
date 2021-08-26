<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>

    <g:set var="entityName" value="${message(code: 'EventSummaryTotal.label', default: 'EventSummaryTotal')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav" role="navigation">
        <li><a class="home" href="${request.contextPath}/admin"><g:message code="default.home.label"/></a></li>
    </ul>
</div>

<div id="list-eventSummaryTotal" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>

    <f:table collection="${eventSummaryTotalList}" class="table table-striped table-bordered table-condensed"
             properties="${['month', 'logEventTypeId', 'numberOfEvents', 'recordCount']}"/>

    <div class="pagination">
        <g:paginate total="${eventSummaryTotalCount ?: 0}"/>
    </div>
</div>
</body>
</html>