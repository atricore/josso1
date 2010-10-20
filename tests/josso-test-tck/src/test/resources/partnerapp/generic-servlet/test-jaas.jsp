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

<%--
  Created by IntelliJ IDEA.
  User: sgonzalez
  Date: Nov 29, 2007
  Time: 4:03:53 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>JOSSO Generic Servlet Container plugin test JAAS</title></head>
  <body>
  <br>JAAS Configuration file : <%=System.getProperty("java.security.auth.login.config")%>

  <%if (System.getProperty("java.security.auth.login.config") == null) {
      System.setProperty("java.security.auth.login.config", "./etc/login.config");
  }%>

  <br>JAAS Configuration file (forced when null) : <%=System.getProperty("java.security.auth.login.config")%>
  <br>Jetty Home : <%=System.getProperty("jetty.home")%>
  <br>
  </body>


</html>