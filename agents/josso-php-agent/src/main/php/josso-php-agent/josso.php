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


require_once('josso-lib.php');

require_once('class.jossoagent.php');
require_once('class.jossouser.php');
require_once('class.jossorole.php');

require('josso-cfg.inc');

session_start();

// Create initializes the agent, if it hasn't been initialized previously
jossoagent::init($josso_gatewayLoginUrl, $josso_gatewayLogoutUrl, $josso_endpoint, $josso_wsdl_url, $josso_proxyhost,
    $josso_proxyport, $josso_proxyusername, $josso_proxypassoword, $josso_agentBasecode, $josso_p3pHeaderValue,
    $josso_sessionManagerServicePath, $josso_identityManagerServicePath, $josso_identityProviderServicePath,
    $josso_automaticLoginStrategies, $josso_partner_app_ids, $josso_partnerapp_vhosts, $josso_sessionAccessMinInterval,
    $forceUnsecureSSOCookie, $josso_ignoredResources, $josso_defaultResource);

$josso_agent = jossoagent::getInstance();

$josso_agent->setSoapclientDebugLevel(0);

$josso_isPartnerApp = 1;

// Check if this is an ignored resource!
if ($josso_agent->isResourceIgnored($_SERVER['REQUEST_URI'])) {
    $josso_isPartnerApp = 0;
}

// If this is a partner application,
if ($josso_isPartnerApp == 1) {

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

?>
