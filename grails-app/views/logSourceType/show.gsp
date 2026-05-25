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
    </div>
</div>
</body>
</html>