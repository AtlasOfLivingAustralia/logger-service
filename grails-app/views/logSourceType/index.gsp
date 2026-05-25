<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Log Source Types</title>
    <meta name="breadcrumb" content="Log Source Types" />
    <g:set var="adminLink" value="${createLink(controller:'admin', action:'index')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>
<body>
<div class="container py-4">
    <h1>Log Source Types</h1>

    <table class="table table-bordered">
        <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${logSourceTypeList}" var="type">
            <tr>
                <td>
                    <g:link action="show" id="${type.id}">${type.id}</g:link>
                </td>
                <td>${type.name}</td>
                <td>
                    <g:link action="edit" id="${type.id}" class="btn btn-sm btn-outline-primary">Edit</g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="d-flex justify-content-center gap-2">
        <g:paginate total="${logSourceTypeCount ?: 0}" class="pagination"/>
    </div>

    <g:link action="create" class="btn btn-primary">Create New Log Source Type</g:link>
</div>
</body>
</html>