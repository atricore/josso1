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

<%@ page import="org.josso.samples.ejb.PartnerComponent,
                 org.josso.samples.ejb.PartnerComponentHome,
                 javax.naming.InitialContext,
                 javax.rmi.PortableRemoteObject"%>
<%@ page import="org.apache.commons.logging.Log"%>
<%@ page import="org.apache.commons.logging.LogFactory"%>
<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>Sample Partner Application - JOSSO</title>
    <meta name="author" content="gbrigand@josso.org">
	<meta name="keywords" content="">
	<meta name="description" content="Java Open Single Signon">
</head>

<body>

    <p>This is a very simple JOSSO partner application, you're accessing a <b>protected</b> web resource.</p>

    <p>Your username is : <b><%=request.getRemoteUser()%></b>&nbsp;<font color="red">(Retrieved from request.getRemoteUser())</font></p>

    <%
        String echo2 = "";
        String whoAmI = "";

        try
        {
           InitialContext ic = new InitialContext();
           Object ref =  ic.lookup("josso/samples/PartnerComponentEJB");
           PartnerComponentHome partnerComponentHome = (PartnerComponentHome)
                                PortableRemoteObject.narrow(
                                           ref,
                                           PartnerComponentHome.class
                                );

           // Create a partner component instance
           PartnerComponent partnerComponent = partnerComponentHome.create();

           // Invoke a partner component operation that will retrieve current principal.
           echo2 = partnerComponent.echo("Hello World");
           whoAmI = partnerComponent.whoAmI();

        }
        catch(Exception e)
        {
    %>
    <p>Error Invoking Partner Component :<%=e%></p>
    <%
            // Add some logging information for debug ...
            Log logger = LogFactory.getLog("org.josso.samples.partnerapp.ejb");
            Throwable t = e;
            while (t != null) {
                logger.error(t, t);
                t = t.getCause();
            }

        }
    %>
    <p>EJB Partner Component response = <%=echo2%></p>
    <p>EJB Partner Component user information = <%=whoAmI%></p>
</body>
</html>