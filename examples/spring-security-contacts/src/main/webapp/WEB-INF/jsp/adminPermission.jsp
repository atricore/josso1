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
<head><title>Administer Permissions</title></head>
<body>
<h1>Administer Permissions</h1>
<P>
<code>
<c:out value="${model.contact}"/>
</code>
<P>
<table cellpadding=3 border=0>
<c:forEach var="acl" items="${model.acl.entries}">
    <tr>
      <td>
        <code>
          <c:out value="${acl}"/>
        </code>
      </td>
      <td>
      <A HREF="<c:url value="deletePermission.htm"><c:param name="contactId" value="${model.contact.id}"/><c:param name="sid" value="${acl.sid.principal}"/><c:param name="permission" value="${acl.permission.mask}"/></c:url>">Del</A>
      </td>
    </tr>
</c:forEach>
</table>
<p><a href="<c:url value="addPermission.htm"><c:param name="contactId" value="${model.contact.id}"/></c:url>">Add Permission</a>   <a href="<c:url value="index.htm"/>">Manage</a>
</body>
</html>
