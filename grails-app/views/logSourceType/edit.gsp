<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Edit Log Source Type</title>
    <meta name="breadcrumb" content="Edit" />
    <g:set var="logSourceLink" value="${createLink(controller:'logSourceType')}"/>
    <meta name="breadcrumbParent" content="${logSourceLink},Log Source Types" />
</head>
<body>
<div class="container py-4">
    <h1>Edit Log Source Type</h1>

    <g:form action="update">
        <g:hiddenField name="id" value="${logSourceType?.id}"/>

        <div class="mb-3">
            <label>Name</label>
            <g:textField name="name" value="${logSourceType?.name}" class="form-control"/>
        </div>

        <g:submitButton name="update" value="Update" class="btn btn-primary"/>
    </g:form>
</div>
</body>
</html>