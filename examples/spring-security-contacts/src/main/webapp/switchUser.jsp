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
<%@ page import="org.springframework.security.ui.AbstractProcessingFilter" %>
<%@ page import="org.springframework.security.AuthenticationException" %>

<html>
  <head>
    <title>Switch User</title>
  </head>

  <body>
    <h1>Switch to User</h1>

	<P>Valid users:
	<P>
	<P>username <b>rod</b>, password <b>koala</b>
	<P>username <b>dianne</b>, password <b>emu</b>
	<p>username <b>scott</b>, password <b>wombat</b>
	<p>username <b>bill</b>, password <b>wombat</b>
	<p>username <b>bob</b>, password <b>wombat</b>
	<p>username <b>jane</b>, password <b>wombat</b>
	<p>

    <%-- this form-login-page form is also used as the
         form-error-page to ask for a login again.
         --%>
    <c:if test="${not empty param.login_error}">
      <font color="red">
        Your 'su' attempt was not successful, try again.<BR><BR>
        Reason: <%= ((AuthenticationException) session.getAttribute(AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>
      </font>
    </c:if>

    <form action="<c:url value='j_spring_security_switch_user'/>" method="POST">
      <table>
        <tr><td>User:</td><td><input type='text' name='j_username'></td></tr>
        <tr><td colspan='2'><input name="switch" type="submit" value="Switch to User"></td></tr>
      </table>

    </form>

  </body>
</html>
