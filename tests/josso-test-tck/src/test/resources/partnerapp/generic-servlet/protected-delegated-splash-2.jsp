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

<%@ page import="org.josso.agent.Constants" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.josso.agent.http.JOSSOSecurityContext" %>
<%@ page import="org.josso.agent.http.WebAccessControlUtil" %>
<%--
~ JOSSO: Java Open Single Sign-On
~
~ Copyright 2004-2008, Atricore, Inc.
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
--%>

<%--
  Created by IntelliJ IDEA.
  User: sgonzalez
  Date: Nov 28, 2007
  Time: 3:24:34 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%

    // Obtain a JOSSO security context instance, if none is found is because user has not been authenticated.
    JOSSOSecurityContext ctx = WebAccessControlUtil.getSecurityContext(request);
    if (ctx == null) {
        // User has not been authenticated, ask him to login, this will trigger the login process,  storing current URL and
        // Redirecting user to JOSSO Gateway Login page :
        WebAccessControlUtil.askForLogin(request, response);
    } else if (!ctx.isUserInRole("role1")) {
        // User has been authenticated but does not have role1, return a 403 FORBIDDEN error.
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    // OK! User is authenticated and has role1 !!

%>
<html>
  <head><title>Simple JOSSO protected page in delegated mode</title></head>
  <body>This is a simple JSP that will only be accessible if user has been custom authenticated, using default redirect and is in role <b>role1</b>
  <p>This is default splash resource.</p>
  <p>Http Headers are :</p>
  <%
   out.println("<ul>");
   java.util.Enumeration names = request.getHeaderNames();
   while (names.hasMoreElements()) {
     String name = (String) names.nextElement();
     Enumeration values = request.getHeaders(name);

     while (values.hasMoreElements()) {
         String value = (String)values.nextElement();

         out.println(" <li>     <b>" + name + "=</b>" + value +"</li>");
     }

   }
   out.println("</ul>");
   %>
   </body>	
</html>