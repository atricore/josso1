<%--
  ~ JOSSO: Java Open Single Sign-On
  ~
  ~ Copyright 2004-2009, Atricore, Inc.
  ~
  ~ This is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as
  ~ published by the Free Software Foundation; either version 2.1 of
  ~ the License, or (at your option) any later version.
  ~
  ~ This software is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with this software; if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  ~
  --%>

<%@ page import="java.util.Enumeration" %>
<%--
~ JOSSO: Java Open Single Sign-On
~
~ Copyright 2004-2008, Atricore, Inc.
~
~ This is free software; you can redistribute it and/or modify it
~ under the terms of the GNU Lesser General Public License as
~ published by the Free Software Foundation; either version 2.1 of
~ the License, or (at your option) any later version.
~
~ This software is distributed in the hope that it will be useful,
~ but WITHOUT ANY WARRANTY; without even the implied warranty of
~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
~ Lesser General Public License for more details.
~
~ You should have received a copy of the GNU Lesser General Public
~ License along with this software; if not, write to the Free
~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
--%>

<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>Sample Partner Application - JOSSO</title>
    <meta name="author" content="sgonzalez@josso.org">
	<meta name="keywords" content="">
	<meta name="description" content="Java Open Single Signon">
</head>

<body>

    <a href="http://www.josso.org"><img src="<%=request.getContextPath()%>/resources/josso-logo.gif" border="0"/></a>

    <p>This is a very simple JOSSO partner application, you're accessing a <b>protected</b> web resource.</p>
    <p>Your username is : <b><%=request.getRemoteUser()%></b>&nbsp;<font color="red">(Retrieved from request.getRemoteUser())</font></p>

    <p>The Login url is : <b><a href="<%=request.getContextPath()%>/josso_login/"><%=request.getContextPath()%>/josso_login/</a></b>. </p>
    <p>The Logout url is : <b><a href="<%=request.getContextPath()%>/josso_logout/"><%=request.getContextPath()%>/josso_logout/</a></b>. </p>
    <p>When using in your own applications, don't forget the trailing slash "/" in both urls!</p>
    <b><a href="<%=request.getContextPath()%>">home</a></b>

    <p>Http Headers are :</p>
    <%
     out.println("<ul>");
     java.util.Enumeration names = request.getHeaderNames();
     while (names.hasMoreElements()) {
       String name = (String) names.nextElement();
       Enumeration values = request.getHeaders(name);

       while (values.hasMoreElements()) {
           String value = (String)values.nextElement();

           out.println(" <li>     <b>" + name + "=</b>" + value +"</li>");
       }

     }
     out.println("</ul>");
     %>


</body>
</html>