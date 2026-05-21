<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Create Remote Address</title>
    <meta name="breadcrumb" content="Create" />
    <g:set var="remoteAddressLink" value="${createLink(controller:'remoteAddress')}"/>
    <meta name="breadcrumbParent" content="${remoteAddressLink},Remote Addresses" />
</head>
<body>
<div class="container py-4">
    <h1>Create Remote Address</h1>

    <g:form action="save">

        <div class="mb-3">
            <label>IP</label>
            <g:textField name="ip" value="${remoteAddress?.ip}" class="form-control"/>
        </div>

        <div class="mb-3">
            <label>Host Name</label>
            <g:textField name="hostName" value="${remoteAddress?.hostName}" class="form-control"/>
        </div>

        <g:submitButton name="create" value="Save" class="btn btn-primary"/>
    </g:form>
</div>
</body>
</html>