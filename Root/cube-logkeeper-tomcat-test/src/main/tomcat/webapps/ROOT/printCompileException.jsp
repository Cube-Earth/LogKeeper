<%@ page session="false" pageEncoding="UTF-8" contentType="text/plain; charset=UTF-8" %><%
System.out.println(request.getServletName() + ": message to system out");
System.err.println(request.getServletName() + ": message to system err");
request.getServletContext().log(request.getServletName() + ": message to servlet log");
%>OK