<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
<title>Log Event Types</title>
<meta name="breadcrumb" content="Log Event Type" />
<g:set var="logEventLink" value="${createLink(controller:'logEventType')}"/>
<meta name="breadcrumbParent" content="${logEventLink},Log Event Types" />
</head>
<body>
<div class="container py-4">

    <h2>Log Event Type Details</h2>

    <p><strong>ID:</strong> ${logEventType.id}</p>
    <p><strong>Name:</strong> ${logEventType.name}</p>

    <div class="mb-3">
        <g:link action="edit" id="${logEventType.id}" class="btn btn-outline-primary">Edit</g:link>
        <g:form  action="delete"  method="DELETE" style="display:inline;">
            <g:hiddenField name="id" value="${logEventType.id}"/>
            <g:submitButton name="delete" value="Delete" class="btn btn-sm btn-primary"
                            onclick="return confirm('Are you sure?');"/>
        </g:form>
    </div>
</div>
</body>