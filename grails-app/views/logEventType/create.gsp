<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Create Log Event Type</title>
    <meta name="breadcrumb" content="New" />
    <g:set var="logEventLink" value="${createLink(controller:'logEventType')}"/>
    <meta name="breadcrumbParent" content="${logEventLink},Log Event Types" />
</head>
<body>
<div class="container py-4">

    <h2>Create Log Event Type</h2>

    <g:form action="save" method="POST">

        <div class="mb-3">
            <label for="id" class="form-label">ID</label>
            <g:textField name="id" id="id" value="${logEventType?.id}" class="form-control"/>
        </div>

        <div class="mb-3">
            <label for="name" class="form-label">Name</label>
            <g:textField name="name" id="name" value="${logEventType?.name}" class="form-control"/>
        </div>

        <div class="mb-3">
            <g:submitButton name="create" value="Save" class="btn btn-primary"/>
            <g:link action="index" class="btn btn-outline-secondary">Cancel</g:link>
        </div>

    </g:form>

</div>
</body>
</html>