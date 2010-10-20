<?php /*
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
  */ ?>
<html>
<body>
    <h1>JOSSO Custom Login screen</h1>
    <p>
    This is a sample login form for basic authentication (username/password) implemented outside JOSSO Gateway web application.<br>
    It is the simplest way to customize your login screen.  For more advanced customization, take a look at JOSSO Branding documentation.
    <br>
    Configure the custom login page URL in <strong>josso-gateway-web.xml</strong> file, see <i>customLoginURL</i> property.
    </p>

    <p>You will have to point the form action attribute to the local agent authenticaiton URL using the PHP Fuction
        <b>jossoCreateAuthenticationUrl()</b>
    </p>

    <!-- Check if this is an error or not ...  -->

<?php
    $user = $josso_agent->getUserInSession();
    if (isset($user)) {
        // Never should be here!
        echo 'Username : ' . $user->getName() . '<br><br>';
    }
?>

    <!-- Check if this is an error or not ...  -->
    <?php if (isset($_REQUEST[('josso_error_type')])) { ?>
        <font color="red">Invalid login information</font>
    <?php } ?>

    <h3>JOSSO Custom Login Form</h3>
    <p>
        You can also embed this form in any other page within your applicaiton.
    </p>
    <p>
    <form name="jossoLoginForm" method="post" action="<?php echo jossoCreateAuthenticationUrl()?>">

        <!-- ================================================================== -->
        <!-- The following hidden fields are very important, do not forget them -->
        <!-- ================================================================== -->

        <input type="hidden" name="josso_cmd" value="login">

        <!-- Required if this is not not your main login form -->
        <input type="hidden" name="josso_back_to" value="<?php echo jossoSecurityCheckUrl()?>" />

        <!-- USE YOUR OWN FORM URL FOR THIS VALUE, Required if this is not your main login form -->
        <input type="hidden" name="josso_on_error" value="http://josso-php/partner-login.php"/>

        <!-- ================================================================== -->
        <table border="0" cellpadding="0" cellspacing="5">
            <tr><td>username:</td><td><input type="text" name="josso_username" size="10"></td></tr>
            <tr><td>password:</td><td><input type="password" name="josso_password" size="10"></td></tr>
            <tr><td colspan="2" align="center"><input type="submit" value="Login" ></td></tr>
        </table>

    </form>
    </p>
</body>
</html>
