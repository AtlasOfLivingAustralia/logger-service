<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title><g:message code="default.list.label" args="${[entityName]}"/></title>
    <meta name="breadcrumb" content="${entityName}" />
    <g:set var="adminLink" value="${createLink(controller:'admin', action:'index')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>

<body>

<div id="list-${entityName}" class="content scaffold-list" role="main">
    <h1 class="mb-3"><g:message code="default.list.label" args="${[entityName]}"/></h1>

    <g:if test="${flash.message}">
        <div class="alert alert-info" role="status">${flash.message}</div>
    </g:if>

    <div class="table-responsive mb-3">
        <f:table
                collection="${summarylList}"
                class="table table-striped table-hover table-bordered align-middle w-100"
                properties="${columns}"
        />
    </div>

    <div class="d-flex justify-content-center mt-3 gap-2">
        <g:paginate total="${summaryTotalCount ?: 0}" class="pagination"/>
    </div>
</div>
</body>
</html>