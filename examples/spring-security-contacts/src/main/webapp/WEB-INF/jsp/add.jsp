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

<%@ include file="/WEB-INF/jsp/include.jsp" %>
<html>
<head><title>Add New Contact</title></head>
<body>
<h1>Add Contact</h1>
<form method="post">
  <table width="95%" bgcolor="f8f8ff" border="0" cellspacing="0" cellpadding="5">
    <tr>
      <td alignment="right" width="20%">Name:</td>
      <spring:bind path="webContact.name">
        <td width="20%">
          <input type="text" name="name" value="<c:out value="${status.value}"/>">
        </td>
        <td width="60%">
          <font color="red"><c:out value="${status.errorMessage}"/></font>
        </td>
      </spring:bind>
    </tr>
    <tr>
      <td alignment="right" width="20%">Email:</td>
      <spring:bind path="webContact.email">
        <td width="20%">
          <input type="text" name="email" value="<c:out value="${status.value}"/>">
        </td>
        <td width="60%">
          <font color="red"><c:out value="${status.errorMessage}"/></font>
        </td>
      </spring:bind>
    </tr>
  </table>
  <br>
  <spring:hasBindErrors name="webContact">
    <b>Please fix all errors!</b>
  </spring:hasBindErrors>
  <br><br>
  <input name="execute" type="submit" alignment="center" value="Execute">
</form>
<a href="<c:url value="../hello.htm"/>">Home</a>
</body>
</html>
