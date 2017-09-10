<%@ page session="false" pageEncoding="UTF-8" contentType="text/plain; charset=UTF-8" %><%
System.out.println("message to system out {1}");
System.err.println("message to system err {2}");
request.getServletContext().log("message to servlet log {3}");
//System.out.println("#" + Thread.currentThread().getContextClassLoader().getClass().getCanonicalName());
%>OK