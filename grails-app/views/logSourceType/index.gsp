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
                    <g:form action="delete" method="POST" style="display:inline;">
                        <g:hiddenField name="id" value="${type.id}"/>
                        <g:submitButton name="delete" value="Delete" class="btn btn-sm btn-primary"
                                        onclick="return confirm('Are you sure?');"/>
                    </g:form>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <g:link action="create" class="btn btn-primary">Create New Log Source Type</g:link>
</div>
</body>
</html>