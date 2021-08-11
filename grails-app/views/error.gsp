<!DOCTYPE html>
<html>
<head>
	<title><g:if env="development">Grails Runtime Exception II</g:if><g:else>Error</g:else></title>
	<meta name="layout" content="${grailsApplication.config.skin.layout}" />
</head>
<body>
<div class="container">
	<h2><g:message code="error.occurred" default="An error occurred"/></h2>
	<div><g:message code="error.contact_us" default="If this problem persists, please contact support via &quot;Contact us &quot; link at the bottom of the page."/></div>
	<g:if env="development">
		<g:if test="${Throwable.isInstance(exception)}">
			<g:renderException exception="${exception}" />
		</g:if>
		<g:elseif test="${request.getAttribute('javax.servlet.error.exception')}">
			<g:renderException exception="${request.getAttribute('javax.servlet.error.exception')}" />
		</g:elseif>
		<g:else>
			<ul class="errors">
				<li><g:message code="error.occurred" default="An error occurred"/></li>
				<li><g:message code="error.exception" default="Exception"/>: ${exception}</li>
				<li><g:message code="error.message" default="Message"/>: ${message}</li>
				<li><g:message code="error.path" default="Path"/>: ${path}</li>
			</ul>
		</g:else>
	</g:if>
</div>
</body>
</html>
