<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Log Source Type Details</title>
    <meta name="breadcrumb" content="Log Source Type" />
    <g:set var="logSourceLink" value="${createLink(controller:'logSourceType')}"/>
    <meta name="breadcrumbParent" content="${logSourceLink},Log Source Types" />
</head>
<body>
<div class="container py-4">
    <h2>Log Source Type Details</h2>

    <p><strong>ID:</strong> ${logSourceType.id}</p>
    <p><strong>Name:</strong> ${logSourceType.name}</p>

    <div class="mb-3">
        <g:link action="edit" id="${logSourceType.id}" class="btn btn-outline-primary">Edit</g:link>
        <g:form action="delete" method="POST" style="display:inline;">
            <g:hiddenField name="id" value="${logSourceType.id}"/>
            <g:submitButton value="Delete" name="delete" class="btn btn-danger"
                            onclick="return confirm('Are you sure you want to delete this Log Source Type?');"/>
        </g:form>
    </div>
</div>
</body>
</html>