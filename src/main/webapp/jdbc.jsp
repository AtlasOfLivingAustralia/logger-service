    <%@ page language="java" import="java.sql.*" %>  
    <html>  
    <body>  
    <%
    String username = "alalogger";  
    String password = "sun6800";  
    String database = "logger";  
    try{  
        Class.forName("com.mysql.jdbc.Driver");  
        String url = "jdbc:mysql://ala-authdb1.vm.csiro.au:3306/" + database;  
        Connection con = DriverManager.getConnection(url, username, password);  
        out.println("**** connection: " + con + "</br>");
        Statement stmt;  
        stmt = con.createStatement();  
        ResultSet rs;  
        rs = stmt.executeQuery("select * from log_event_type;");  
        while(rs.next()){  
            int id = rs.getInt("id");  
            String name = rs.getString("name");  
            out.println("***** log_event_type id: " + id + "</br>");
     	}  
        rs.close();  
        stmt.close();  
        con.close();  
      
    }  
    catch(Exception e) {
    	out.println("**** error: " + e + "</br>");
        e.printStackTrace();  
    }  
    %>  
    Test !  
    </body>  
    </html>  