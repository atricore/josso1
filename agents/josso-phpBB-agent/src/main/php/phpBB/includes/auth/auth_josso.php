<?php
/**
*
* JOSSO auth plug-in for phpBB3
*
* @package login
* @version $Id$
*
*/

/**
* @ignore
*/
if (!defined('IN_PHPBB'))
{
	exit;
}

/**
* Called in acp_board while setting authentication plugins
*/
function init_josso()
{	
	return false;
}

/**
* Login function
*/
function login_josso(&$username, &$password)
{
	global $db, $josso_agent;

	// Get current SSO User and SSO Session information,
	$jossoUser = $josso_agent->getUserInSession();
	
	// Check if user is authenticated
	if (isset($jossoUser)) {

	      $sql = 'SELECT user_id, username, user_password, user_passchg, user_email, user_type
		      FROM ' . USERS_TABLE . "
		      WHERE username = '" . $db->sql_escape($jossoUser->getName()) . "'";
	      $result = $db->sql_query($sql);
	      $row = $db->sql_fetchrow($result);
	      $db->sql_freeresult($result);

	      if ($row)
	      {
		      // User inactive...
		      if ($row['user_type'] == USER_INACTIVE || $row['user_type'] == USER_IGNORE)
		      {
			      return array(
				      'status'		=> LOGIN_ERROR_ACTIVE,
				      'error_msg'		=> 'ACTIVE_ERROR',
				      'user_row'		=> $row,
			      );
		      }

		      // Successful login...
		      return array(
			      'status'		=> LOGIN_SUCCESS,
			      'error_msg'		=> false,
			      'user_row'		=> $row,
		      );
	      }

	      // this is the user's first login so create an empty profile
	      return array(
		      'status'		=> LOGIN_SUCCESS_CREATE_PROFILE,
		      'error_msg'		=> false,
		      'user_row'		=> user_row_josso($jossoUser, $password),
	      );

	}

	return array(
		'status'	=> LOGIN_ERROR_EXTERNAL_AUTH,
		'error_msg'	=> 'LOGIN_ERROR_EXTERNAL_AUTH_JOSSO',
		'user_row'	=> array('user_id' => ANONYMOUS),
	);
}

/**
* Autologin function
*
* @return array containing the user row or empty if no auto login should take place
*/
function autologin_josso()
{
	return array();
}

/**
* Logout function
*/
function logout_josso(&$user, &$new_session)
{
	if (isset($_SESSION["OPTIONAL_LOGIN_EXECUTED"])) {
		unset($_SESSION["OPTIONAL_LOGIN_EXECUTED"]);
	}

	if (isset($_SESSION["JOSSO_AUTOMATIC_LOGIN_EXECUTED"])) {
		unset($_SESSION["JOSSO_AUTOMATIC_LOGIN_EXECUTED"]);
	}

	if (isset($_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"])) {
		unset($_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"]);
	}

	forceRedirect(jossoCreateLogoutUrl(null));
}

/**
* The session validation function checks whether the user is still logged in
*
* @return boolean true if the given user is authenticated or false if the session should be closed
*/
function validate_session_josso(&$user)
{
	global $josso_agent;

	$ssoSessionId = $josso_agent->accessSession();
	if (isset($ssoSessionId) && $ssoSessionId != '') {
		return true;
	} else {
		// Clear SSO Cookie
		setcookie("JOSSO_SESSIONID", '', 0, "/");
		$_COOKIE['JOSSO_SESSIONID'] = '';
		return false;
	}
}

/**
* This function generates an array which can be passed to the user_add function in order to create a user
*/
function user_row_josso($jossoUser, $password)
{
	global $db, $config, $user, $josso_agent;

	$additionalGroups = array();

	$sessionId = $josso_agent->getSessionId();
	$roles = $josso_agent->findRolesBySSOSessionId($sessionId);
	foreach ($roles as $role) {
		if (startsWith($role->getName(), 'PHPBB_')) {
			$roleName = substr($role->getName(), 6);
			if (!isset($defaultGroup)) {
				// TODO: Use first group as default group?
				$defaultGroup = $roleName;
			} else {
				$additionalGroups[] = $roleName;
			}
		}
	}

	if (!isset($defaultGroup)) {
		//trigger_error('NO_GROUP');
		$defaultGroup = 'REGISTERED';
	}

	// first retrieve default group id
	$sql = 'SELECT group_id
		FROM ' . GROUPS_TABLE . "
		WHERE group_name = '" . $defaultGroup . "'
		AND group_type = " . GROUP_SPECIAL;
	$result = $db->sql_query($sql);
	$row = $db->sql_fetchrow($result);
	$db->sql_freeresult($result);

	if (!$row) {
		// TODO: Maybe we should create all nonexistent groups?
		trigger_error('NO_GROUP');
	}

	// generate user account data
	return array(
		'username'		=> $jossoUser->getName(),
		'user_password' => phpbb_hash($password),
		'user_email'	=> $jossoUser->getProperty('email'),
		'group_id'		=> (int) $row['group_id'],
		'user_type'		=> USER_NORMAL,
		'user_ip'		=> $user->ip,
		'user_new'		=> ($config['new_member_post_limit']) ? 1 : 0,
		'josso_groups'	=> $additionalGroups,
	);
}

function startsWith($haystack,$needle,$case=true) {
	if ($case) {
		return (strcmp(substr($haystack, 0, strlen($needle)),$needle)===0);
	}
	return (strcasecmp(substr($haystack, 0, strlen($needle)),$needle)===0);
}
?>