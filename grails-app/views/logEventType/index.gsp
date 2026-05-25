<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Log Event Types</title>
    <meta name="breadcrumb" content="Log Event Type" />
    <g:set var="adminLink" value="${createLink(controller:'admin')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>
<body>
<div class="container py-4">

    <h1>Log Event Types</h1>

    <g:link action="create" class="btn btn-primary mb-3">Create New</g:link>

    <table class="table table-striped table-bordered align-middle">
        <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th width="150">Actions</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${logEventTypeList}" var="type">
            <tr>
                <td> <g:link action="show" id="${type.id}">
                    ${type.id}
                </g:link>
                </td>
                <td>${type.name}</td>
                <td>
                    <g:link action="edit" id="${type.id}" class="btn btn-sm btn-primary">Edit</g:link>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
    <div class="d-flex justify-content-center gap-2">
        <g:paginate total="${logEventTypeCount ?: 0}" class="pagination"/>
    </div>
</div>
</body>
</html>