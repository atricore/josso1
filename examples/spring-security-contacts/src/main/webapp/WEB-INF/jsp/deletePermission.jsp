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
<head><title>Permission Deleted</title></head>
<body>
<h1>Permission Deleted</h1>
<P>
<code>
<c:out value="${model.contact}"/>
</code>
<P>
<code>
<c:out value="${model.sid}"/>
</code>
<code>
<c:out value="${model.permission}"/>
</code>
<p><a href="<c:url value="index.htm"/>">Manage</a>
</body>
</html>
