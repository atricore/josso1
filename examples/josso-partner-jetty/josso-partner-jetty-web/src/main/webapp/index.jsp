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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>



<html>

<body>

<h3>Start Jetty with JAAS enabled : ./jetty.sh  run etc/jetty.xml etc/jetty-jaas.xml</h3>
<ul>
    <li><a href="<%=request.getContextPath()%>/protected-managed.jsp">Protected Managed (A Servlet Filter will trigger authentication)</a></li>
    <li><a href="<%=request.getContextPath()%>/protected-delegated.jsp">Protected Delegated (The page code triggers authentication)</a></li>
</ul>
</body>
</html>