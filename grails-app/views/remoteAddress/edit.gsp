<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Edit Remote Address</title>
    <meta name="breadcrumb" content="Edit" />
    <g:set var="remoteAddressLink" value="${createLink(controller:'remoteAddress')}"/>
    <meta name="breadcrumbParent" content="${remoteAddressLink},Remote Addresses" />
</head>
<body>
<div class="container py-4">
    <h1>Edit Remote Address</h1>

    <g:if test="${flash.error}">
        <div class="alert alert-danger">${flash.error}</div>
    </g:if>
    <g:if test="${flash.message}">
        <div class="alert alert-success">${flash.message}</div>
    </g:if>

    <g:form action="update" method="POST">
        <g:hiddenField name="id" value="${remoteAddress?.id}"/>

        <div class="mb-3">
            <label>IP</label>
            <g:textField name="ip" value="${remoteAddress?.ip}" class="form-control"/>
        </div>

        <div class="mb-3">
            <label>Host Name</label>
            <g:textField name="hostName" value="${remoteAddress?.hostName}" class="form-control"/>
        </div>

        <g:submitButton name="update" value="Update" class="btn btn-primary"/>
    </g:form>
</div>
</body>
</html>