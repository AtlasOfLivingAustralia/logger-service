<%@ page contentType="text/html" pageEncoding="UTF-8" session="false" %>
<%@ page import = "java.util.Date" %> 
<%@ page import = "org.springframework.context.ApplicationContext,  
		org.springframework.web.context.support.WebApplicationContextUtils,
		org.ala.jpa.dao.LogEventDao" %>
<% 
ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
LogEventDao logEventDao = (LogEventDao) applicationContext.getBean(LogEventDao.class);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="pageName" content="home"/>
        <title>Atlas of Living Australia - ALA Logger Service</title>
    </head>
    
	<body bgcolor="white">
        <p align="center"><font size=6>Welcome to the Atlas of Living Australia: <strong>ALA Logger Service</strong>.</font></p>
		<hr/>
		<p align="center">
			<font size = 6 color = "#FF0000">current date is :<%= new Date().toString()%></font>
		</p>
		<hr/>
		<h1> Request Information </h1>
		<font size="4">
			JSP Request Method: <%= request.getMethod() %><br>
			Request URI: <%= request.getRequestURI() %><br>
			Request Protocol: <%= request.getProtocol() %><br>
			Servlet path: <%= request.getServletPath() %><br>
			Path info: <%= request.getPathInfo() %><br>
			Server name: <%= request.getServerName() %><br>
			Server port: <%= request.getServerPort() %><br>
			Remote user: <%= request.getRemoteUser() %><br>
			Remote address: <%= request.getRemoteAddr() %><br>
			Remote host: <%= request.getRemoteHost() %><br>
		</font>
		<hr />
		<h1> Record Count </h1>	
		<font size="4">
			Total Log Event Count: <%= logEventDao.executeNativeQuery("SELECT Count(*) FROM log_event") %><br/>
			Total Download Count: <%= logEventDao.executeNativeQuery("SELECT SUM(record_count) FROM log_detail") %><br/>
		</font>		
	</body>    
</html>