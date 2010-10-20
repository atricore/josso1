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

<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>Sample Partner Application A - JOSSO</title>
    <meta name="author" content="sgonzalez@josso.org">
	<meta name="keywords" content="">
	<meta name="description" content="Java Open Single Signon">
</head>

<body>

    <a href="http://www.josso.org"><img src="<%=request.getContextPath()%>/resources/josso-logo.gif" border="0"/></a>

    <p><b>JOSSO Partner Application A</b></p>

    <p>This is a very simple JOSSO partner application, you're accessing a <b>public</b> web resource.</p>
<%  if (request.getUserPrincipal() != null)
    { %>
        <p>Your username is : <b><%=request.getRemoteUser()%></b>&nbsp;<font color="red">(Retrieved from request.getRemoteUser())</font></p>
        <p>To logout click <a href="<%=request.getContextPath()%>/josso_logout/">here</a></p>
<%  }
    else 
    { %>
        <p>You're an anonymous user.</p>
        <p>To login click <a href="<%=request.getContextPath()%>/josso_login/">here</a></p>
        <p>To automatically login, try to access a protected resource : <a href="<%=request.getContextPath()%>/protected/">protected</a></p>
        <p>To automatically login, and see extended user info, try <a href="<%=request.getContextPath()%>/josso/">josso-protected</a></p>
<%  } %>
</body>
</html>