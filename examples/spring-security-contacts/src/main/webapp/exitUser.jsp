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

<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>

<%@ page import="org.springframework.security.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.Authentication" %>
<%@ page import="org.springframework.security.ui.AbstractProcessingFilter" %>
<%@ page import="org.springframework.security.AuthenticationException" %>

<html>
  <head>
    <title>Exit User</title>
  </head>

  <body>
    <h1>Exit User</h1>

    <c:if test="${not empty param.login_error}">
      <font color="red">
        Your 'Exit User' attempt was not successful, try again.<BR><BR>
        Reason: <%= ((AuthenticationException) session.getAttribute(AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>
      </font>
    </c:if>

    <form action="<c:url value='j_spring_security_exit_user'/>" method="POST">
      <table>
        <tr><td>Current User:</td><td>

         <%
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) { %>

			<%= auth.getPrincipal().toString() %>

		<% } %>



         </td></tr>
        <tr><td colspan='2'><input name="exit" type="submit" value="Exit"></td></tr>
      </table>

    </form>

  </body>
</html>
