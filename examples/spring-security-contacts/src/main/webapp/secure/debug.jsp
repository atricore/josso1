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

<%@ page import="org.springframework.security.context.SecurityContextHolder" %>
<%@ page import="org.springframework.security.Authentication" %>
<%@ page import="org.springframework.security.GrantedAuthority" %>
<%@ page import="org.springframework.security.adapters.AuthByAdapter" %>

<%
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) { %>
			Authentication object is of type: <%= auth.getClass().getName() %><BR><BR>
			Authentication object as a String: <%= auth.toString() %><BR><BR>

			Authentication object holds the following granted authorities:<BR><BR>
<%			GrantedAuthority[] granted = auth.getAuthorities();
			for (int i = 0; i < granted.length; i++) { %>
				<%= granted[i].toString() %> (getAuthority(): <%= granted[i].getAuthority() %>)<BR>
<%			}

			if (auth instanceof AuthByAdapter) { %>
				<BR><B>SUCCESS! Your container adapter appears to be properly configured!</B><BR><BR>
<%			} else { %>
				<BR><B>SUCCESS! Your web filters appear to be properly configured!</B><BR>
<%			}

		} else { %>
			Authentication object is null.<BR>
			This is an error and your Acegi Security application will not operate properly until corrected.<BR><BR>
<%		}
%>
