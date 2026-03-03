<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Edit Log Event Type</title>
    <meta name="breadcrumb" content="Edit Log Event Type" />
    <g:set var="logEventLink" value="${createLink(controller:'logEventType')}"/>
    <meta name="breadcrumbParent" content="${logEventLink},Log Event Types" />
</head>
<body>
<div class="container py-4">

    <h2>Edit Log Event Type</h2>

    <g:form action="update" method="POST">
        <g:hiddenField name="id" value="${logEventType?.id}"/>

        <div class="mb-3">
            <label for="name" class="form-label">Name</label>
            <g:textField name="name" id="name" value="${logEventType?.name}" class="form-control"/>
        </div>

        <div class="mb-3">
            <g:submitButton name="update" value="Update" class="btn btn-primary"/>
            <g:link action="show" id="${logEventType?.id}" class="btn btn-outline-secondary">Cancel</g:link>
        </div>
    </g:form>

</div>
</body>
</html>