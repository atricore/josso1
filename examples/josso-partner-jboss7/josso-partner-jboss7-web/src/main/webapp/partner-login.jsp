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
<html>
<body>
    <h1>JOSSO Custom Login screen</h1>
    <p>
    This is a sample login form for basic authentication (username/password) implemented outside JOSSO Gateway web application.<br>
    It is the simplest way to customize your login screen.  For more advanced customization, take a look at JOSSO Branding documentation.
    <br>
    Configure the custom login page URL in <strong>josso-gateway-web.xml</strong> file, see <i>customLoginURL</i> property.
    </p>

    <p>You will have to point the form action attribute to the local agent authenticaiton URL:
        <b>/josso_authentication/</b> (don't forget the trailing slash)
    </p>

    <!-- Check if this is an error or not ...  -->
    <% if (request.getParameter("josso_error_type") != null) { %>
        <font color="red">Invalid login information</font>
    <% } %>

    <h3>JOSSO Custom Login Form</h3>
    <p>
        You can also embed this form in any other page within your applicaiton.
    </p>
    <p>
    <form name="jossoLoginForm" method="post" action="<%=request.getContextPath()%>/josso_authentication/">
        <!-- This hidden field is very important, do not forget it -->
        <input type="hidden" name="josso_cmd" value="login">

        <table border="0" cellpadding="0" cellspacing="5">
            <tr><td>username:</td><td><input type="text" name="josso_username" size="10"></td></tr>
            <tr><td>password:</td><td><input type="password" name="josso_password" size="10"></td></tr>
            <tr><td colspan="2" align="center"><input type="submit" value="Login" ></td></tr>
        </table>
    </form>
    </p>
</body>
</html>