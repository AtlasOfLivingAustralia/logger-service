<div class="table-responsive mb-4">
    <table class="table table-striped table-hover table-bordered align-middle w-100">
        <thead class="table-light text-center">
        <tr>
            <g:each in="${domainProperties}" var="p" status="i">
                <g:if test="${domainClass?.javaClass.name.contains('EventSummary')}">
                    <th scope="col">${p.label}</th>
                </g:if>
                <g:else>
                    <g:sortableColumn property="${p.property}" title="${p.label}" class="text-center"/>
                </g:else>
            </g:each>
        </tr>
        </thead>
        <tbody>
        <g:each in="${collection}" var="bean" status="i">
            <tr class="${(i % 2) == 0 ? 'table-light' : ''}">
                <g:if test="${domainClass?.javaClass.name.contains('EventSummary')}">
                    <g:each in="${domainProperties}" var="p">
                        <td class="text-center">
                            <f:display bean="${bean}" property="${p.name}" displayStyle="${displayStyle ?: 'table'}"/>
                        </td>
                    </g:each>
                </g:if>
                <g:else>
                    <g:each in="${domainProperties}" var="p" status="j">
                        <g:if test="${j == 0}">
                            <td class="text-start">
                                <g:link method="GET" resource="${bean}">
                                    <f:display bean="${bean}" property="${p.property}"
                                               displayStyle="${displayStyle ?: 'table'}"
                                               theme="${theme}"/>
                                </g:link>
                            </td>
                        </g:if>
                        <g:else>
                            <td class="text-start">
                                <f:display bean="${bean}" property="${p.property}"
                                           displayStyle="${displayStyle ?: 'table'}"
                                           theme="${theme}"/>
                            </td>
                        </g:else>
                    </g:each>
                </g:else>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>