<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Create Log Source Type</title>
    <meta name="breadcrumb" content="Create" />
    <g:set var="logSourceLink" value="${createLink(controller:'logSourceType')}"/>
    <meta name="breadcrumbParent" content="${logSourceLink},Log Source Types" />
</head>
<body>
<div class="container py-4">
    <h1>Create Log Source Type</h1>

    <g:form action="save">
        <div class="mb-3">
            <label>ID</label>
            <g:textField name="id" value="${logSourceType?.id}" class="form-control"/>
        </div>

        <div class="mb-3">
            <label>Name</label>
            <g:textField name="name" value="${logSourceType?.name}" class="form-control"/>
        </div>

        <g:submitButton name="save" value="Save" class="btn btn-primary"/>
    </g:form>
</div>
</body>
</html>