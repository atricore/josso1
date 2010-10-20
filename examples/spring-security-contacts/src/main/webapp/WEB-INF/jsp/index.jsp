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
<head><title>Your Contacts</title></head>
<body>
<h1><security:authentication property="principal.username"/>'s Contacts</h1>
<P>
<table cellpadding=3 border=0>
<tr><td><b>id</b></td><td><b>Name</b></td><td><b>Email</b></td></tr>
<c:forEach var="contact" items="${model.contacts}">
  <tr>
  <td>
      <c:out value="${contact.id}"/>
  </td>
  <td>
      <c:out value="${contact.name}"/>
  </td>
  <td>
      <c:out value="${contact.email}"/>
  </td>
  <security:accesscontrollist domainObject="${contact}" hasPermission="8,16">
    <td><A HREF="<c:url value="del.htm"><c:param name="contactId" value="${contact.id}"/></c:url>">Del</A></td>
  </security:accesscontrollist>
  <security:accesscontrollist domainObject="${contact}" hasPermission="16">
    <td><A HREF="<c:url value="adminPermission.htm"><c:param name="contactId" value="${contact.id}"/></c:url>">Admin Permission</A></td>
  </security:accesscontrollist>
  </tr>
</c:forEach>
</table>
<p><a href="<c:url value="add.htm"/>">Add</a>   <p><a href="<c:url value="../j_spring_security_logout"/>">Logoff</a> (also clears any remember-me cookie)
</body>
</html>
