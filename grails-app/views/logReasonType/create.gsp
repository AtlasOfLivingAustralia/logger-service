<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Create Log Reason Type</title>
    <meta name="breadcrumb" content="Create" />
    <g:set var="parentLink" value="${createLink(controller:'logReasonType')}"/>
    <meta name="breadcrumbParent" content="${parentLink},Log Reason Types" />
</head>
<body>
<div class="container py-4">

    <h2>Create Log Reason Type</h2>

    <g:form action="save" method="POST">

        <div class="mb-3">
            <label for="id" class="form-label">ID <span class="text-danger">*</span></label>
            <g:textField name="id" id="id" value="${logReasonType?.id}" class="form-control" required="true"/>
            <div class="form-text">Must be a unique integer. This value is permanent once saved.</div>
        </div>

        <div class="mb-3">
            <label for="name" class="form-label">Name</label>
            <g:textField name="name" id="name" value="${logReasonType?.name}" class="form-control"/>
        </div>

        <div class="mb-3">
            <label for="rkey" class="form-label">Rkey</label>
            <g:textField name="rkey" id="rkey" value="${logReasonType?.rkey}" class="form-control"/>
        </div>

        <div class="mb-3">
            <label for="defaultOrder" class="form-label">Default Order</label>
            <g:textField name="defaultOrder" id="defaultOrder" value="${logReasonType?.defaultOrder}" class="form-control"/>
        </div>

        <div class="mb-3 form-check">
            <g:checkBox name="isDeprecated" id="isDeprecated" value="${logReasonType?.isDeprecated ?: false}" class="form-check-input"/>
            <label for="isDeprecated" class="form-check-label">Is Deprecated</label>
        </div>

        <g:submitButton name="save" value="Save" class="btn btn-primary"/>
        <g:link action="index" class="btn btn-outline-secondary">Cancel</g:link>

    </g:form>

</div>
</body>
</html>

