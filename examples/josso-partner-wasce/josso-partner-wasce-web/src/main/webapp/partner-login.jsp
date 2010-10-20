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
<p>
This is a sample login form for basic authentication (username/password) implemented outside josso web app.<br>
You should configure gateway login urls in your your josso-agent-config.xml file to point to this form.
</p>
<p>Sample josso-agent-config.xml : </p>
<pre>
    ...
    &lt;gatewayLoginUrl&gt;http://localhost:8080/partnerapp/partner-login.jsp&lt;/gatewayLoginUrl&gt;
    &lt;gatewayLoginErrorUrl&gt;http://localhost:8080/partnerapp/partner-login.jsp?error=true&lt;/gatewayLoginErrorUrl&gt;
    ...
</pre>

<p>You will have to point the form action attribute to your josso gateway. This sample uses : <b>/josso/signon/usernamePasswordLogin.do</b></p>
<!-- Check if this is an error or not ...  -->
<% if (request.getParameter("error") != null) { %>

    <font color="red">Invalid login information</font>
<% } %>

<form name="usernamePasswordLoginForm" method="post" action="/josso/signon/usernamePasswordLogin.do">
    <!-- This is important !!! -->
    <input type="hidden" name="josso_cmd" value="login">
    <input type="hidden" name="josso_back_to" value="<%=(request.getParameter("josso_back_to") != null ? request.getParameter("josso_back_to") : "")%>">
    
    <table border="0" cellpadding="0" cellspacing="5">
        <tr><td>username:</td><td><input type="text" name="josso_username" size="10"></td></tr>
        <tr><td>password:</td><td><input type="password" name="josso_password" size="10"></td></tr>
        <tr><td colspan="2" align="center"><input type="submit" value="Login" ></td></tr>
    </table>
</form>