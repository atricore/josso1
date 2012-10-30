<?php
/**
 * PHP Default automatic login class definition.
 */

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

require_once('class.abstract_automatic_login_strategy.php');

/**
 * PHP Default automatic login strategy implementation.
 * @package  org.josso.agent.php
 */
class default_automatic_login_strategy extends abstract_automatic_login_strategy {

	/**
	* Constructor
	* 
	* @access public
	*/
	function default_automatic_login_strategy() {
	}

	/**
	* Componenets must evaluate if automatic login is required for the received request.
	*
	* @return true if automatic login is required, false otherwise
	*/
	function isAutomaticLoginRequired() {
	    // TODO : This is not the best way to avoid loops when no referer is present, the flag should expire and
	    // should not be attached to the SSO Session

	    // The first time we access a partner application, we should attempt an automatic login.
	    $autoLoginExecuted = $_SESSION["JOSSO_AUTOMATIC_LOGIN_EXECUTED"];
	    // If no referer host is found but we did not executed auto login yet, give it a try.
	    if (!isset($autoLoginExecuted)) {
			$_SESSION["JOSSO_AUTOMATIC_LOGIN_EXECUTED"] = TRUE;
			return TRUE;
	    }

	    if (isset($_SERVER['HTTP_REFERER']))
			$referer = $_SERVER['HTTP_REFERER'];

	    // If we have a referer host that differs from our we require an autologinSSs
	    if (isset($referer)) {

            if (isset($_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"]))
			    $oldReferer = $_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"];

			if (isset($oldReferer)) {
			    unset($_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"]);
			    return FALSE;
			}
	
			$protocol = 'http';
			$host = $_SERVER['HTTP_HOST'];
	
			if (isset($_SERVER['HTTPS'])) {
	
			    // This is a secure connection, the default PORT is 443
			    $protocol = 'https';
			    if ($_SERVER['SERVER_PORT'] != 443) {
					$port = $_SERVER['SERVER_PORT'];
			    }
	
			} else {
			    // This is a NON secure connection, the default PORT is 80
			    $protocol = 'http';
			    if ($_SERVER['SERVER_PORT'] != 80) {
					$port = $_SERVER['SERVER_PORT'];
			    }
			}
	
			$baseUrl = $protocol.'://'.$host.(isset($port) ? ':'.$port : '');
	
			if (strncmp($referer, $baseUrl, strlen($baseUrl) != 0)) {
	
			    // Store referer for future reference!
			    $_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"] = $referer;
			    return TRUE;
			}
	    } else {
			if (isset($_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"])) {
			    $oldReferer = $_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"];
			    if (isset($oldReferer)  && strcmp($oldReferer, "NO_REFERER") != 0) {
					unset($_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"]);
					return FALSE;
			    } else {
					$_SESSION["JOSSO_AUTOMATIC_LOGIN_REFERER"] = "NO_REFERER";
					return TRUE;
			    }
			}
	    }

	    return FALSE;
	}
}
?>
