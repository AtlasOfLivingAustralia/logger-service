<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.skin.layout}"/>
    <title>Log Reason Types</title>
    <meta name="breadcrumb" content="Log Reason Types" />
    <g:set var="adminLink" value="${createLink(controller:'admin')}"/>
    <meta name="breadcrumbParent" content="${adminLink},Admin" />
</head>
<body>
<div class="container py-4">

    <h2>Log Reason Types</h2>

    <div class="table-responsive mb-4">
        <table class="table table-striped table-hover table-bordered align-middle w-100">
            <thead class="table-light">
            <tr class="text-center">
                <th>ID</th>
                <th>Name</th>
                <th>Rkey</th>
                <th>Default Order</th>
                <th>Is Deprecated</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${logReasonTypeList}" var="type">
                <tr>
                    <td class="text-center">
                        <g:link action="show" id="${type.id}">${type.id}</g:link>
                    </td>
                    <td>${type.name}</td>
                    <td>${type.rkey}</td>
                    <td class="text-center">${type.defaultOrder}</td>
                    <td class="text-center">${type.isDeprecated ? 'Yes' : 'No'}</td>
                    <td>
                        <g:link action="edit" id="${type.id}" class="btn btn-sm btn-outline-primary">Edit</g:link>
                        <g:form action="delete" method="POST" style="display:inline;">
                            <g:hiddenField name="id" value="${type.id}"/>
                            <g:submitButton name="delete" value="Delete" class="btn btn-sm btn-primary"
                                            onclick="return confirm('Are you sure you want to delete this Log Reason Type?');"/>
                        </g:form>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="d-flex justify-content-center gap-2">
        <g:paginate total="${logReasonTypeCount ?: 0}" class="pagination"/>
    </div>

</div>
</body>
</html>