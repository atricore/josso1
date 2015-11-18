<?php
/**
 * PHP Josso lib.  Include this in all pages you want to use josso.
 *
 * @package  org.josso.agent.php
 *
 * @version $Id: josso.php 613 2008-08-26 16:42:10Z sgonzalez $
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

require_once('class.jossoagent.php');
require_once('class.jossouser.php');
require_once('class.jossorole.php');

require('josso-cfg.inc');

$josso_isPartnerApp = TRUE;

// Check if this is an ignored resource!
if (isset($josso_ignoredResources)) {
    foreach ($josso_ignoredResources as $josso_ignoredResource) {
        $uri = $_SERVER['REQUEST_URI'];
        if (strncmp($uri, $josso_ignoredResource, strlen($josso_ignoredResource)) == 0) {
            $josso_isPartnerApp = FALSE;
        }
    }
}

// If this is a partner application,
if ($josso_isPartnerApp) {

    // Only available when URI is a partner application!
    session_start();
    $josso_agent = & jossoagent::getNewInstance();
    $ssoSessionId = $josso_agent->accessSession();

    // Set SSO Cookie ...
    $p3pHeaderValue = $josso_agent->getP3PHeaderValue();
    if (isset($p3pHeaderValue)) {
        header($josso_agent->getP3PHeaderValue());
    }

    // Automatic Login
    if (!isset($jossoSession)) {

        // Avoid filtering josso resources like 'josso_security_check', 'josso_login', etc
        $uri = $_SERVER['REQUEST_URI'];

        // If we have an original is because we're already authenticating something
        if (!isset($_SESSION['JOSSO_ORIGINAL_URL']) &&
            strncmp($uri, $josso_agent->getBaseCode().'/josso-security-check.php', strlen($josso_agent->getBaseCode().'/josso-security-check.php')) != 0 &&
            strncmp($uri, $josso_agent->getBaseCode().'/josso-login.php', strlen($josso_agent->getBaseCode().'/josso-login.php'))  != 0 &&
            strncmp($uri, $josso_agent->getBaseCode().'/josso-logout.php', strlen($josso_agent->getBaseCode().'/josso-logout.php')) != 0 ) {

            // Try to perform an automatic login!

            // If we haven't tryed an automatic login before, doit now.

            // Now, work with referer!
            if ($josso_agent->isAutomaticLoginRequired()) {
                jossoRequestOptionalLogin();
            }
        }

    } else {
        if (isset($_SESSION['JOSSO_AUTOMATIC_LOGIN_REFERER'])) {
            unset($_SESSION['JOSSO_AUTOMATIC_LOGIN_REFERER']);
        }
    }

} // END IF : JOSSO IS PARTNER APP


// ---------------------------------------------------------------------------------------------
// Functions that can be used by PHP applications...
// ---------------------------------------------------------------------------------------------


/**
 * Use this function when ever you want to start user authentication.
 */
function jossoRequestLogin() {
	$currentUrl = $_SERVER['REQUEST_URI'];
    jossoRequestLoginForUrl($currentUrl, FALSE);
}


function jossoRequestOptionalLogin() {
    $currentUrl = $_SERVER['REQUEST_URI'] ;
    jossoRequestLoginForUrl($currentUrl, TRUE);
}


/**
 * Use this function when ever you want to logout the current user.
 */
function jossoRequestLogout() {

    $currentUrl = $_SERVER['REQUEST_URI'] ;

    jossoRequestLogoutForUrl($currentUrl);
}


/**
 * Creates a login url for the current page, use to create links to JOSSO login page
 */
function jossoCreateAuthenticationUrl() {

    // Get JOSSO Agent instance
    $josso_agent = & jossoagent::getNewInstance();

    $loginUrl = $josso_agent->getBaseCode().'/josso-authenticate.php';

    return $loginUrl;

}


/**
 * Creates a login url for the current page, use to create links to JOSSO login page
 */
function jossoCreateLoginUrl() {

    // Get JOSSO Agent instance
    $josso_agent = & jossoagent::getNewInstance();

    $currentUrl = $_SERVER['REQUEST_URI'] ;
    $loginUrl = $josso_agent->getBaseCode().'/josso-login.php'. '?josso_current_url=' .  base64_encode($currentUrl);

    return $loginUrl;

}

/**
 * Creates a logout url, use to create links to JOSSO logout page.
 * Use null for backToUrl parameter if you want to go back to the current page after logout.
 * For logout url on protected page pass a backToUrl that points to some public page (e.g. home page)
 * in order to avoid immediate redirection to josso login page.
 */
function jossoCreateLogoutUrl($backToUrl) {

    // Get JOSSO Agent instance
    $josso_agent = & jossoagent::getNewInstance();

	if (is_null($backToUrl)) {
		$backToUrl = createBaseUrl() . $_SERVER['REQUEST_URI'] ;
    }

	$logoutUrl =  $josso_agent->getBaseCode().'/josso-logout.php'. '?josso_current_url=' . base64_encode($backToUrl);

    return $logoutUrl;

}


function jossoRequestLoginForUrl($currentUrl, $optional = FALSE) {

    $_SESSION['JOSSO_ORIGINAL_URL'] = $currentUrl;

    // Get JOSSO Agent instance
    $josso_agent = & jossoagent::getNewInstance();
    $securityCheckUrl = createBaseUrl().$josso_agent->getBaseCode().'/josso-security-check.php';

    $loginUrl = $josso_agent->getGatewayLoginUrl(). '?josso_back_to=' . $securityCheckUrl;

    if ($optional) {
        $loginUrl = $loginUrl . '&josso_cmd=login_optional' ;
    } else {
        if (isset($_GET['josso_force_authn'])) {
            $forceAuthn = $_GET['josso_force_authn'];
            if (!empty($forceAuthn) && strtolower($forceAuthn) != "false") {
                $loginUrl = $loginUrl . '&josso_cmd=login_force';
            }
        }

        if (isset($_GET['josso_authn_ctx'])) {
            $authnCtx = $_GET['josso_authn_ctx'];
            if (!empty($authnCtx)) {
                $loginUrl = $loginUrl . '&josso_authn_ctx=' . $authnCtx;
            }
        }
    }

    $loginUrl = $loginUrl . createFrontChannelParams();

    forceRedirect($loginUrl);

}

function jossoSecurityCheckUrl() {

    // Get JOSSO Agent instance
    $josso_agent = & jossoagent::getNewInstance();
    $securityCheckUrl = createBaseUrl().$josso_agent->getBaseCode().'/josso-security-check.php';

    return $securityCheckUrl;

}

function jossoRequestLogoutForUrl($currentUrl) {

    $_SESSION['JOSSO_ORIGINAL_URL'] = $currentUrl;

    // Get JOSSO Agent instance
    $josso_agent = & jossoagent::getNewInstance();
    $logoutUrl = $josso_agent->getGatewayLogoutUrl(). '?josso_back_to=' . $currentUrl;

    $logoutUrl = $logoutUrl . createFrontChannelParams();

    // Clear SSO Cookie
    setcookie("JOSSO_SESSIONID", '', 0, "/"); // session cookie ...
    $_COOKIE['JOSSO_SESSIONID'] = '';


    forceRedirect($logoutUrl);

}

function forceRedirect($url,$die=true) {
    if (!headers_sent()) {
        ob_end_clean();
        header("Location: " . $url);
        prepareNonCacheResponse();
    }
    printf('<HTML>');
    printf('<META http-equiv="Refresh" content="0;url=%s">', $url);
    printf('<BODY onload="try {self.location.href="%s" } catch(e) {}"><a href="%s">Redirect </a></BODY>', $url, $url);
    printf('</HTML>');
    if ($die)
        die();
}

function createBaseUrl() {
    // ReBuild securityCheck URL
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

    return $protocol.'://'.$host.(isset($port) ? ':'.$port : '');

}

function createFrontChannelParams() {
    // Add some request parameters like host name
    $host = $_SERVER['HTTP_HOST'];
    $josso_agent = & jossoagent::getNewInstance();
    $params = '&josso_partnerapp_host=' . $host . '&josso_partnerapp_id=' . $josso_agent->getRequester();

    return $params;

    // TODO : Support josso_partnerapp_ctx param too ?

}

function prepareNonCacheResponse() {
    header("Cache-Control", "no-cache");
    header("Pragma", "no-cache");
    header("Expires", "0");
}

?>

