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
<%@ page import="org.josso.gateway.identity.SSOUser"%>

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

    <p>This is a very simple JOSSO partner application, that uses <b>josso</b> api to get extra information the user</p>

<%  // Check if we have a principal ...
    if (request.getUserPrincipal() != null)
    { %>
        <p>Your username is : <b><%=request.getRemoteUser()%></b>&nbsp;<font color="red">(Retrieved from : request.getRemoteUser())</font></p>
        <p>Your properties, if any, are : &nbsp;<font color="red">(Retrieved from : SSOUser ssoUser = (SSOUser) request.getUserPrincipal();)</font></p>
<%      // Cast the principal to a josso specific user, and iterate over its properties.
        SSOUser ssoUser = (SSOUser) request.getUserPrincipal();
        for (int i = 0 ; i < ssoUser.getProperties().length ; i++)
        { %>
            <p><%=ssoUser.getProperties()[i].getName()%> : <%=ssoUser.getProperties()[i].getValue()%> </p>
<%      } %>
<%  }
    else
    { %>
        <p>You're an anonymous user ...</p>
<%  } %>
    <b><a href="<%=request.getContextPath()%>">home</a></b>
</body>
</html>