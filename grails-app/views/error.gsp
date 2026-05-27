<!DOCTYPE html>
<html>
<head>
	<title><g:if env="development">Grails Runtime Exception II</g:if><g:else>Error</g:else></title>
	<g:set var="layoutName"
		   value="${grailsApplication.config.getProperty('skin.layout', String, 'main')}"/>
	<meta name="layout" content="${layoutName}"/>
</head>
<body>
<div class="container">
	<h2><g:message code="error.occurred" default="An error occurred"/></h2>
	<div><g:message code="error.contact_us" default="If this problem persists, please contact support via &quot;Contact us &quot; link at the bottom of the page."/></div>
	<g:if env="development">
		<g:set var="statusCode" value="${request.getAttribute('javax.servlet.error.status_code')}"/>
		<g:set var="errorUri" value="${request.getAttribute('javax.servlet.error.request_uri')}"/>
		<g:set var="errorException" value="${request.getAttribute('javax.servlet.error.exception')}"/>

		<ul class="errors" style="background:#fff3cd;padding:1em;border:1px solid #ffc107;border-radius:4px;">
			<li><strong>HTTP Status:</strong> ${statusCode}</li>
			<li><strong>URI:</strong> ${errorUri}</li>
			<li><strong>Request Method:</strong> ${request.method}</li>
			<g:if test="${statusCode == 405}">
				<li><strong>Diagnosis:</strong> HTTP 405 Method Not Allowed —
					the controller's <code>allowedMethods</code> rejected this request method.
					The form sent <strong>${request.method}</strong> but the action requires a different HTTP verb (e.g. DELETE).
					Fix: add <code>&lt;input type="hidden" name="_method" value="DELETE"/&gt;</code> to the form,
					or change <code>static allowedMethods = [delete: "POST"]</code> in the controller.
				</li>
			</g:if>
		</ul>

		<g:if test="${Throwable.isInstance(errorException)}">
			<g:renderException exception="${errorException}" />
		</g:if>
		<g:elseif test="${Throwable.isInstance(exception)}">
			<g:renderException exception="${exception}" />
		</g:elseif>
	</g:if>
</div>
</body>
</html>
