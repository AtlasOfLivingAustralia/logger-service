<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Log Reason Type Details</title>
    <meta name="breadcrumb" content="Log Reason Type" />
    <g:set var="parentLink" value="${createLink(controller:'logReasonType')}"/>
    <meta name="breadcrumbParent" content="${parentLink},Log Reason Types" />
</head>
<body>
<div class="container py-4">

    <h2>Log Reason Type Details</h2>

    <p><strong>ID:</strong> ${logReasonType.id}</p>
    <p><strong>Name:</strong> ${logReasonType.name}</p>
    <p><strong>Rkey:</strong> ${logReasonType.rkey}</p>
    <p><strong>Default Order:</strong> ${logReasonType.defaultOrder}</p>
    <p><strong>Is Deprecated:</strong> ${logReasonType.isDeprecated ? 'Yes' : 'No'}</p>

    <div class="mb-3">
        <g:link action="edit" id="${logReasonType.id}" class="btn btn-outline-primary">Edit</g:link>

        <g:form action="delete" method="POST" style="display:inline;">
            <g:hiddenField name="id" value="${logReasonType.id}"/>
            <g:submitButton name="delete" value="Delete" class="btn btn-primary"
                            onclick="return confirm('Are you sure you want to delete this Log Reason Type?');"/>
        </g:form>
    </div>

</div>
</body>
</html>