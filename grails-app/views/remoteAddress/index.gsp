<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Remote Addresses</title>
    <meta name="breadcrumb" content="Remote Addresses" />
    <g:set var="adminLink" value="${createLink(controller:'admin', action:'index')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>
<body>
<div class="container py-4">
    <h1>Remote Addresses</h1>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th>ID</th>
            <th>IP</th>
            <th>Host Name</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${remoteAddressList}" var="addr">
            <tr>
                <td>
                    <g:link action="show" id="${addr.id}">${addr.id}</g:link>
                </td>
                <td>${addr.ip}</td>
                <td>${addr.hostName}</td>
                <td>
                    <g:link action="edit" id="${addr.id}" class="btn btn-sm btn-outline-primary">Edit</g:link>
                    <g:form action="delete" method="POST" style="display:inline;">
                        <g:hiddenField name="id" value="${addr.id}"/>
                        <g:submitButton name="delete" value="Delete" class="btn btn-sm btn-primary"
                                        onclick="return confirm('Are you sure?');"/>
                    </g:form>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <g:link action="create" class="btn btn-primary">Create New Remote Address</g:link>
</div>
</body>
</html>