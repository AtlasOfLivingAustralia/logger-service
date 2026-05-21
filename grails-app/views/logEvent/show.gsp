<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'logEvent.label', default: 'LogEvent')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
        <meta name="breadcrumb" content="${entityName}" />
        <g:set var="logEventLink" value="${createLink(controller:'logEvent')}"/>
        <meta name="breadcrumbParent" content="${logEventLink},Log event list" />
    </head>
    <body>
    <div id="show-logEvent" class="container py-4" role="main">

        <h3 class="mb-4">
            <g:message code="default.show.label" args="[entityName]" /> - ID: ${logEvent.id}
        </h3>

        <g:if test="${flash.message}">
            <div class="alert alert-info alert-dismissible fade show" role="alert">
                ${flash.message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </g:if>

        <div class="card shadow-sm mb-3">
                <f:display bean="${logEvent}" />
            </div>
        </div>

    </div>
    </body>
</html>
