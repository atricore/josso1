<?php
/**
 * PHP Josso lib.  Include this in all pages you want to use josso.
 *
 * @package  org.josso.agent.php
 *
 * @version $Id: josso.php 340 2006-02-09 17:02:13Z sgonzalez $
 * @author Sebastian Gonzalez Oyuela <sgonzalez@josso.org>
 */

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

define('IN_PHPBB', true);
$phpbb_root_path = (defined('PHPBB_ROOT_PATH')) ? PHPBB_ROOT_PATH : './';
$phpEx = substr(strrchr(__FILE__, '.'), 1);
include($phpbb_root_path . 'common.' . $phpEx);

// Start session management
$user->session_begin();
$auth->acl($user->data);
$user->setup();

// Resolve the assertion :

if (isset($_REQUEST['josso_assertion_id'])) {
    $assertionId = $_REQUEST['josso_assertion_id'];
    $ssoSessionId = $josso_agent->resolveAuthenticationAssertion($assertionId);
	setcookie("JOSSO_SESSIONID", $ssoSessionId, 0, "/"); // session cookie ...
	$_COOKIE['JOSSO_SESSIONID'] = $ssoSessionId;
	if ($config['auth_method'] == 'josso') {
		$jossoUser = $josso_agent->getUserInSession();
		if (isset($jossoUser)) {
			$auth->login($jossoUser->getName(), $jossoUser->getName() . 'secret');  //retrieve user password?

$_SESSION['instr_name'] = $jossoUser->getName();
		$_SESSION['instr_id'] = '12345';
		$_SESSION['email'] = $jossoUser->getProperty('email');
		$_SESSION['verified'] = "True";
		$_SESSION['bad_attempt'] = 0;
		}
    }
}

if (isset($_SESSION['JOSSO_ORIGINAL_URL'])) {
	$backToUrl = $_SESSION['JOSSO_ORIGINAL_URL'];
	unset($_SESSION['JOSSO_ORIGINAL_URL']);
} else if (isset($josso_defaultResource))
	$backToUrl = $josso_defaultResource;

// Set P3P Header
$p3pHeaderValue = $josso_agent->getP3PHeaderValue();
if (isset($p3pHeaderValue)) {
    header($josso_agent->getP3PHeaderValue());
}

if (isset($backToUrl)) {
	if (isset($jossoUser)) {
		$backToUrl = str_replace('&amp;', '&', reapply_sid($backToUrl));
	}
    forceRedirect($backToUrl, true);
}

// No page is stored or no session was found, just display an error one ...
?>
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
	<title>JOSSO - PHP Problem</title>
	<meta name="description" content="Java Open Single Signon">
</head>

<body>
    <h1>JOSSO Encountered a Problem!</h1>
    <h2>Either you accessed this page directly or no PHP Session support is available!</h2>
</body>
</html>