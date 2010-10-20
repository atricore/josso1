<?php

/**
JOSSO: Java Open Single Sign-On

Copyright 2004-2009, Atricore, Inc.

This is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of
the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this software; if not, write to the Free
Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<body onload="document.forms[0].submit()">
    <noscript>
        <p>
            <strong>Note:</strong> Since your browser does not support JavaScript,
            you must press the Continue button once to proceed.
        </p>
    </noscript>
    <form action="<?php print($josso_agent->getGatewayLoginUrl())?>"
        method="post" name="usernamePasswordLoginForm"
        enctype="application/x-www-form-urlencoded">
    
        <div>
            <?php foreach ($_REQUEST as $name => $param) {
                print "\n            <input type=\"hidden\" value=\"$param\" name=\"$name\" />";
            }

            ?>
            <noscript><input type="submit" value="Continue"/></noscript>
        </div>
    </form>
</body>
</html>

