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
<%@ page import="org.josso.gateway.identity.SSOUser" %>
<%@ page contentType="application/json; charset=UTF-8" language="java" %>
<%
out.write("{\n");
if (request.getUserPrincipal() != null) {
    SSOUser ssoUser = (SSOUser) request.getUserPrincipal();
    out.write("\"ssouser\": {\n");
    out.write("  \"username\": \"" + request.getUserPrincipal().getName() + "\",");
    String groups = "";
    String prefix = "";
    for (int i = 0 ; i < ssoUser.getProperties().length ; i++) {
        if (!ssoUser.getProperties()[i].getName().equals("groups")) {
            out.write("  \"" + ssoUser.getProperties()[i].getName() + "\": \"" + ssoUser.getProperties()[i].getValue() + "\", ");
        } else {
            groups += prefix + "\"" + ssoUser.getProperties()[i].getValue() + "\"";
            prefix = ",";
        }
    }
    out.write("  \"groups\": ["+groups+"], ");
    out.write("  \"properties\": " + ssoUser.getProperties().length);
    out.write("}\n");
}
out.write("}\n");
%>