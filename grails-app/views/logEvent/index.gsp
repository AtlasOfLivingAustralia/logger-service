<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>

    <g:set var="entityName" value="${message(code: 'logEvent.label', default: 'LogEvent')}"/>
    <title><g:message code="default.list.label" args="[entityName]"/></title>
</head>

<body>
<div class="nav" role="navigation">
    <ul>
        <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
%{--        <li><a class="home" href="${request.contextPath}/admin"><g:message code="default.home.label"/></a></li>--}%
    </ul>
</div>

<div id="list-logEvent" class="content scaffold-list" role="main">
    <h1><g:message code="default.list.label" args="[entityName]"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>

    <f:table collection="${logEventList}" class="table table-striped table-bordered table-condensed"
             properties="${['month', 'logEventTypeId', 'logSourceTypeId', 'logReasonTypeId', 'userEmail', 'source']}"/>

    <div class="pagination">
        <g:paginate total="${logEventCount ?: 0}"/>
    </div>
</div>
</body>
</html>