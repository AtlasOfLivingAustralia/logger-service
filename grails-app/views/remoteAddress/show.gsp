<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Remote Address Details</title>
    <meta name="breadcrumb" content="Remote Address" />
    <g:set var="remoteAddressLink" value="${createLink(controller:'remoteAddress')}"/>
    <meta name="breadcrumbParent" content="${remoteAddressLink},Remote Addresses" />
</head>
<body>
<div class="container py-4">
    <h2>Remote Address Details</h2>

    <p><strong>ID:</strong> ${remoteAddress.id}</p>
    <p><strong>IP:</strong> ${remoteAddress.ip}</p>
    <p><strong>Host Name:</strong> ${remoteAddress.hostName}</p>

    <div class="mb-3">
        <g:link action="edit" id="${remoteAddress.id}" class="btn btn-outline-primary">Edit</g:link>
        <g:form  action="delete"  method="DELETE" style="display:inline;">
            <g:hiddenField name="id" value="${remoteAddress.id}"/>
            <g:submitButton name="delete" value="Delete" class="btn btn-sm btn-primary"
                            onclick="return confirm('Are you sure?');"/>
        </g:form>
    </div>
</div>
</body>
</html>