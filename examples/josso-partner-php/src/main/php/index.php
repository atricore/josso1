<?php
/**
JOSSO: Java Open Single Sign-On

Copyright 2004-2008, Atricore, Inc.

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

/**
 * php JOSSO parnter application sample
 *
 * @version $Id: index.php 613 2008-08-26 16:42:10Z sgonzalez $
 */
?>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>Sample Partner Application - JOSSO</title>
	<meta name="description" content="Java Open Single Signon">
</head>

<body>
    <h1>This is a very simple PHP JOSSO partner application</h1>
<?php
// jossoagent is automatically instantiated by josso.php,
// declared in auto_prepend_file property of php.ini.
// If you do not use auto_prepend feature, include josso.php in all your pages

// Get current SSO User and SSO Session information,
$user = $josso_agent->getUserInSession();

$sessionId = $josso_agent->getSessionId();

// Check if user is authenticated
if (isset($user)) {

    // Display USER INFORMATION

    // Username associated to authenticated user
    echo 'Username : ' . $user->getName() . '<br><br>';

    // Get a specific user property
    echo 'user.name=' . $user->getProperty('user.name') . '<br><br>';

    // Get all user properties
    $properties = $user->getProperties();
    if (is_array($properties)) {
        foreach ($properties as $property) {
            echo $property['name'] . '=' . $property['value'] . '<br>';
        }
    }

	// Get all user roles
	$roles = $josso_agent->findRolesBySSOSessionId($sessionId);
	echo '<h2>Roles</h2>';
	foreach ($roles as $role) {
		echo $role->getName() . '<br>';
	}

	// Check if user belongs to a specific role
	if ($josso_agent->isUserInRole('role1')) {
		echo '<h3>user is in role1</h3>';
	}

	echo 'Click <a href="'.jossoCreateLogoutUrl(null).'">here</a> to logout ...<br>';

	echo '<p>SSO Session ID : ' . $sessionId . '</p>';


} else {

    // User is unknown..
    echo '<h2>you are an annonymous user ...</h2>';

	echo 'Click <a href="'.jossoCreateLoginUrl().'">here</a> to login ...';

}
?>

</body>
</html>