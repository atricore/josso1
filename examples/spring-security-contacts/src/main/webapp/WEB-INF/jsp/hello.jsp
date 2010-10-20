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
<head><title>Contacts Security Demo</title></head>
<body>
<h1>Contacts Security Demo</h1>
<P>Contacts demonstrates the following central Acegi Security capabilities:
<ul>
<li><b>Role-based security</b>. Each principal is a member of certain roles,
    which are used to restrict access to certain secure objects.</li>
<li><b>Domain object instance security</b>. The <code>Contact</code>, the
    main domain object in the application, has an access control list (ACL)
    that indicates who is allowed read, administer and delete the object.</li>
<li><b>Method invocation security</b>. The <code>ContactManager</code> service
   layer bean has a number of secured (protected) and public (unprotected)
   methods.</li>
<li><b>Web request security</b>. The <code>/secure</code> URI path is protected
   by Acegi Security from principals not holding the
   <code>ROLE_USER</code> granted authority.</li>
<li><b>Security unaware application objects</b>. None of the objects
   are aware of the security being implemented by Acegi Security. *</li>
<li><b>Security taglib usage</b>. All of the JSPs use Acegi Security's
   taglib to evaluate security information. *</li>
<li><b>Fully declarative security</b>. Every capability is configured in
   the application context using standard Acegi Security classes. *</li>
<li><b>Database-sourced security data</b>. All of the user, role and ACL
   information is obtained from an in-memory JDBC-compliant database.</li>
<li><b>Integrated form-based and BASIC authentication</b>. Any BASIC
   authentication header is detected and used for authentication. Normal
   interactive form-based authentication is used by default.</li>
<li><b>Remember-me services</b>. Acegi Security's pluggable remember-me
   strategy is demonstrated, with a corresponding checkbox on the login form.</li>
</ul>

* As the application provides an "ACL Administration" use case, those
classes are necessarily aware of security. But no business use cases are.

<p>Please excuse the lack of look 'n' feel polish in this application.
It is about security, after all! :-)

<p>To demonstrate a public method on <code>ContactManager</code>,
here's a random <code>Contact</code>:
<p>
<code>
<c:out value="${contact}"/>
</code>
<p>Get started by clicking "Manage"...
<p><A HREF="<c:url value="secure/index.htm"/>">Manage</a>
<a href="<c:url value="secure/debug.jsp"/>">Debug</a>
</body>
</html>
