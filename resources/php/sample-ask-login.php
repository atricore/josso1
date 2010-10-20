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
 * @version $Id: sample-ask-login.php 366 2006-03-27 17:26:48Z sgonzalez $
 */

// Check fi the user is allowed to access this resource ...
$user = $josso_agent->getUserInSession();

// AUTHENTICATION request :
// User UNKNOWN ... ask for login, user will be redirected back here ! 
if (!isset($user)) {
     jossoRequestLogin();
}

?>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>Sample Partner Application - JOSSO</title>
	<meta name="description" content="Java Open Single Signon">
</head>

<body>

    <h1>This is a simple PHP JOSSO partner application.</h1>

    <h2>You are an AUTHENITCATED user : <?php echo $user->getName();?></h2>

<?php if (!$josso_agent->isUserInRole('role1')) { ?>
    <p>But you DO NOT have access to some of this page content, you are not an AUTHORIZED user!</p>
<?php }else { ?>
    <p>This is the content you can see with propper roles, you are an AUTHORIZED user!.</p>
<?php }?>


</body>