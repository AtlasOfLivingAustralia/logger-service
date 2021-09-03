<table>
    <thead>
    <tr>
        <g:each in="${domainProperties}" var="p" status="i">
            <g:if test="${domainClass?.javaClass.name.contains('EventSummary')}">
                <th>${p.label}</th>
            </g:if>
            <g:else>
                <g:sortableColumn property="${p.property}" title="${p.label}"/>
            </g:else>
        </g:each>
    </tr>
    </thead>
    <tbody>
    <g:each in="${collection}" var="bean" status="i">
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <g:if test="${domainClass?.javaClass.name.contains('EventSummary')}">
                <g:each in="${domainProperties}" var="p">
                    <td><f:display bean="${bean}" property="${p.name}" displayStyle="${displayStyle ?: 'table'}"/></td>
                </g:each>
            </g:if>
            <g:else>
                <g:each in="${domainProperties}" var="p" status="j">
                    <g:if test="${j == 0}">
                        <td><g:link method="GET" resource="${bean}"><f:display bean="${bean}" property="${p.property}"
                                                                               displayStyle="${displayStyle ?: 'table'}"
                                                                               theme="${theme}"/></g:link></td>
                    </g:if>
                    <g:else>
                        <td><f:display bean="${bean}" property="${p.property}" displayStyle="${displayStyle ?: 'table'}"
                                       theme="${theme}"/></td>
                    </g:else>
                </g:each>
            </g:else>
        </tr>

    </g:each>
    </tbody>
</table>