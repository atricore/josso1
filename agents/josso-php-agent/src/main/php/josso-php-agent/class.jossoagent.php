<?php
/**
 * JOSSO Agent class definition.
 *
 * @package org.josso.agent.php
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

/**
 * Include NUSOAP soap client.
 */
require_once('nusoap-legacy/nusoap.php');

require_once('autologin/class.default_automatic_login_strategy.php');
require_once('autologin/class.urlbased_automatic_login_strategy.php');
require_once('autologin/class.bot_automatic_login_strategy.php');

/**
 * PHP Josso Agent implementation based on WS.
 *
 * @package  org.josso.agent.php
 *
 * @author Sebastian Gonzalez Oyuela <sgonzalez@josso.org>
 * @version $Id: class.jossoagent.php 613 2008-08-26 16:42:10Z sgonzalez $
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 *
 */
class jossoagent  {


    // ---------------------------------------
    // JOSSO Agent configuration :
    // ---------------------------------------

    /**
     * WS End-point
     * @var string
     * @access private
     */
    var $endpoint = 'http://localhost:8080';

    /**
     * Use wsdl url instead of end-point
     * @var string
     * @access private
     */
    var $wsdlUrl = '';

    /**
     * SSOSessionManager service path
     * @var string
     * @access private
     */
    var $sessionManagerServicePath = '/josso/services/SSOSessionManagerSoap';

    /**
     * SSOIdentityManager service path
     * @var string
     * @access private
     */
    var $identityManagerServicePath = '/josso/services/SSOIdentityManagerSoap';

    /**
     * SSOIdentityProvider service path
     * @var string
     * @access private
     */
    var $identityProviderServicePath = '/josso/services/SSOIdentityProviderSoap';

    /**
     * WS Proxy Settings
     * @var string
     * @access private
     */
    var $proxyhost = '';

    /**
     * @var string
     * @access private
     */
    var $proxyport = '';

    /**
     * @var string
     * @access private
     */
    var $proxyusername = '';

    /**
     * @var string
     * @access private
     */
    var $proxypassword = '';

    // Gateway
    /**
     * @var string
     * @access private
     */
    var $gatewayLoginUrl;

    /**
     * @var string
     * @access private
     */
    var $gatewayLogoutUrl;

    /**
     * @var string
     * @access private
     */
    var $sessionAccessMinInterval = 1000;

    /**
     * Base path where JOSSO pages  can be found, like josso-security-check.php
     */
    var $baseCode ;

    /**
     * MS P3P HTTP Header value, for IFRAMES compatibility with IE 6+
     */
    var $p3pHeaderValue;

    // ---------------------------------------
    // JOSSO Agent internal state :
    // ---------------------------------------

    /**
     * SOAP Clienty for identity mgr.
     * @var string
     * @access private
     */
    var $identityMgrClient;


    /**
     * SOAP Clienty for identity provider.
     * @var string
     * @access private
     */
    var $identityProviderClient;


    /**
     * SOAP Clienty for session mgr.
     * @var string
     * @access private
     */
    var $sessionMgrClient;

    /**
     * Last occurred error
     * @var string
     * @access private
     */
    var $fault;

    /**
     * Last occurred fault
     * @var string
     * @access private
     */
    var $err;

    /**
     * Automatic login strategies.
     * @var array
     * @access private
     */
    var $automaticStrategies;

    /**
     * Partner application IDs.
     * @var array
     * @access private
     */
    var $partnerAppIDs;

    /**
     * Partner application IDs by hostname.
     * @var array
     * @access private
     */
    var $partnerAppIDsByHosts;

    /**
     * @return jossoagent a new Josso PHP Agent instance.
     */
    function getNewInstance() {
        // Get config variable values from josso.inc.
        global $josso_gatewayLoginUrl, $josso_gatewayLogoutUrl, $josso_endpoint, $josso_wsdl_url, $josso_proxyhost,
        $josso_proxyport, $josso_proxyusername, $josso_proxypassword, $josso_agentBasecode, $josso_p3pHeaderValue,
        $josso_sessionManagerServicePath, $josso_identityManagerServicePath, $josso_identityProviderServicePath,
        $josso_automaticLoginStrategies, $josso_partner_app_ids, $josso_partnerapp_vhosts, $josso_sessionAccessMinInterval;

        return new jossoagent($josso_gatewayLoginUrl,
            $josso_gatewayLogoutUrl,
            $josso_endpoint,
            $josso_wsdl_url,
            $josso_proxyhost,
            $josso_proxyport,
            $josso_proxyusername,
            $josso_proxypassword,
            $josso_agentBasecode,
            $josso_p3pHeaderValue,
            $josso_sessionManagerServicePath,
            $josso_identityManagerServicePath,
            $josso_identityProviderServicePath,
            $josso_automaticLoginStrategies,
            $josso_partner_app_ids,
            $josso_partnerapp_vhosts,
            $josso_sessionAccessMinInterval);
    }

    /**
     * constructor
     *
     * @access private
     *
     * @param    string $josso_gatewayLoginUrl
     * @param    string $josso_gatewayLogoutUrl
     * @param    string $josso_endpoint SOAP server
     * @param    string $josso_proxyhost
     * @param    string $josso_proxyport
     * @param    string $josso_proxyusername
     * @param    string $josso_proxypassword
     */
    function jossoagent($josso_gatewayLoginUrl, $josso_gatewayLogoutUrl, $josso_endpoint,$josso_wsdl_url,
                        $josso_proxyhost, $josso_proxyport, $josso_proxyusername, $josso_proxypassword, $josso_agentBasecode, $josso_p3pHeaderValue,
                        $josso_sessionManagerServicePath, $josso_identityManagerServicePath, $josso_identityProviderServicePath,
                        $josso_automaticLoginStrategies, $josso_partner_app_ids, $josso_partnerapp_vhosts, $josso_sessionAccessMinInterval) {

        // WS Config
        $this->endpoint = $josso_endpoint;
        $this->wsdlUrl = $josso_wsdl_url;
        $this->proxyhost = $josso_proxyhost;
        $this->proxyport = $josso_proxyport;
        $this->proxyusername = $josso_proxyusername;
        $this->proxypassoword = $josso_proxypassword;
        $this->baseCode = $josso_agentBasecode;

        // Agent config
        $this->gatewayLoginUrl = $josso_gatewayLoginUrl;
        $this->gatewayLogoutUrl = $josso_gatewayLogoutUrl;


        // Others
        $this->p3pHeaderValue = $josso_p3pHeaderValue;

        if (isset($josso_sessionAccessMinInterval)) {
            $this->sessionAccessMinInterval = $josso_sessionAccessMinInterval;
        }

        if (isset($josso_sessionManagerServicePath)) {
            $this->sessionManagerServicePath = $josso_sessionManagerServicePath;
            if (!$this->startsWith($this->sessionManagerServicePath, '/')) {
                $this->sessionManagerServicePath = '/' . $this->sessionManagerServicePath;
            }
        }

        if (isset($josso_identityManagerServicePath)) {
            $this->identityManagerServicePath = $josso_identityManagerServicePath;
            if (!$this->startsWith($this->identityManagerServicePath, '/')) {
                $this->identityManagerServicePath = '/' . $this->identityManagerServicePath;
            }
        }

        if (isset($josso_identityProviderServicePath)) {
            $this->identityProviderServicePath = $josso_identityProviderServicePath;
            if (!$this->startsWith($this->identityProviderServicePath, '/')) {
                $this->identityProviderServicePath = '/' . $this->identityProviderServicePath;
            }
        }

        if (isset($josso_automaticLoginStrategies)) {
            foreach ($josso_automaticLoginStrategies as $as) {
                if ($as['strategy'] == 'DEFAULT') {
                    $defaultAutoLoginStrategy = new default_automatic_login_strategy();
                    $defaultAutoLoginStrategy->setMode($as['mode']);
                    $this->automaticStrategies[sizeof($this->automaticStrategies)] = $defaultAutoLoginStrategy;
                } else if ($as['strategy'] == 'URLBASED') {
                    $urlBasedAutoLoginStrategy = new urlbased_automatic_login_strategy();
                    $urlBasedAutoLoginStrategy->setMode($as['mode']);
                    $urlPatterns = $as['urlPatterns'];
                    $urlBasedAutoLoginStrategy->setUrlPatterns($urlPatterns);
                    $this->automaticStrategies[sizeof($this->automaticStrategies)] = $urlBasedAutoLoginStrategy;
                } else if ($as['strategy'] == 'BOT') {
                    $botAutoLoginStrategy = new bot_automatic_login_strategy();
                    $botAutoLoginStrategy->setMode($as['mode']);
                    $botAutoLoginStrategy->setBotsFile($as['botsFile']);
                    $this->automaticStrategies[sizeof($this->automaticStrategies)] = $botAutoLoginStrategy;
                }
            }
        } else {
            $defaultAutoLoginStrategy = new default_automatic_login_strategy();
            $defaultAutoLoginStrategy->setMode('SUFFICIENT');
            $this->automaticStrategies[0] = $defaultAutoLoginStrategy;
        }

        if (isset($josso_partner_app_ids)) {
            $this->partnerAppIDs = $josso_partner_app_ids;
        }
        if (isset($josso_partnerapp_vhosts)) {
            $this->partnerAppIDsByHosts = $josso_partnerapp_vhosts;
        }
    }

    /**
     * Gets the authnenticated jossouser, if any.
     *
     * @return jossouser the authenticated user information.
     * @access public
     */
    function getUserInSession() {

        $sessionId = $this->getSessionId();
        if (!isset($sessionId)) {
            return ;
        }

        // SOAP Invocation
        $soapclient = $this->getIdentityMgrSoapClient();

        if (isset($this->wsdlUrl))
            $findUserInSessionRequest = array('FindUserInSessionRequest' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        else
            $findUserInSessionRequest = array('FindUserInSession' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        $findUserInSessionResponse  = $soapclient->call('findUserInSession', $findUserInSessionRequest,
            'urn:org:josso:gateway:ws:1.2:protocol', '', false, null, 'document', 'literal');

        if (! $this->checkError($soapclient)) {
            return $this->newUser($findUserInSessionResponse['SSOUser']);
        }

    }

    /**
     * Gets the jossouser for the given session id.
     *
     * @param string $sessionId session id.
     *
     * @return jossouser
     * @access public
     */
    function findUserBySSOSessionId($sessionId) {
        if (!isset($sessionId)) {
            return ;
        }

        // SOAP Invocation
        $soapclient = $this->getIdentityMgrSoapClient();
        if (isset($this->wsdlUrl))
            $findUserInSessionRequest = array('FindUserInSessionRequest' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        else
            $findUserInSessionRequest = array('FindUserInSession' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        $findUserInSessionResponse  = $soapclient->call('findUserInSession', $findUserInSessionRequest,
            'urn:org:josso:gateway:ws:1.2:protocol', '', false, null, 'document', 'literal');

        if (! $this->checkError($soapclient)) {
            return $this->newUser($findUserInSessionResponse['SSOUser']);
        }

    }

    /**
     * Returns true if current authenticated user is associated to the received role.
     * If no user is logged in, returns false.
     *
     * @param string $rolename the name of the role.
     *
     * @return bool
     * @access public
     */
    function isUserInRole($rolename) {
        $user = $this->getUserInSession();
        $sessionId = $this->getSessionId();
        if (!isset($sessionId)) {
            return FALSE;
        }

        $roles = $this->findRolesBySSOSessionId($sessionId) ;

        foreach($roles as $role) {
            if ($role->getName() == $rolename)
                return TRUE;
        }
        return FALSE;
    }

    /**
     * Returns all roles associated to the given username.
     *
     * @deprecated use findRolesBySSOSessionId
     * @return jossorole[] an array with all jossorole instances
     * @access public
     */
    function findRolesBySSOSessionId ($sessionId) {

        // SOAP Invocation
        $soapclient = $this->getIdentityMgrSoapClient();
        if (isset($this->wsdlUrl))
            $findRolesBySSOSessionIdRequest = array('FindRolesBySSOSessionIdRequest' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        else
            $findRolesBySSOSessionIdRequest = array('FindRolesBySSOSessionId' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));

        $findRolesBySSOSessionIdResponse = $soapclient->call('findRolesBySSOSessionId', $findRolesBySSOSessionIdRequest,
            'urn:org:josso:gateway:ws:1.2:protocol', '', false, null, 'document', 'literal');

        if (! $this->checkError($soapclient)) {
            // Build array of roles
            $i = 0;
            $result = $findRolesBySSOSessionIdResponse['roles'];

            if (sizeof($result) == 1) {
                $roles[0] = $this->newRole($result);
            } else {
                foreach($result as $roledata) {
                    $roles[$i] = $this->newRole($roledata);
                    $i++;
                }
            }

            return $roles;
        }

    }

    /**
     * Sends a keep-alive notification to the SSO server so that SSO session is not lost.
     * @access public
     */
    function accessSession() {

        // Check if a session ID is pressent.
        $sessionId = $this->getSessionid();
        if (!isset($sessionId ) || $sessionId == '') {
            return '';
        }

        // Check last access time :
        // $lastAccessTime = $_SESSION['JOSSO_LAST_ACCESS_TIME'];
        // $now = time();

        // Assume that _SESSION is set.
        $soapclient = $this->getSessionMgrSoapClient();
        if (isset($this->wsdlUrl))
            $accessSessionRequest = array('AccessSessionRequest' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        else
            $accessSessionRequest = array('AccessSession' => array('ssoSessionId' => $sessionId, 'requester' => $this->getRequester()));
        $accessSessionResponse  = $soapclient->call('accessSession', $accessSessionRequest,
            'urn:org:josso:gateway:ws:1.2:protocol', '', false, null, 'document', 'literal');

        if ($this->checkError($soapclient)) {
            return '';
        }

        return $accessSessionResponse['ssoSessionId'];

    }

    /**
     *      1) Required     - The LoginModule is required to succeed.
     *			If it succeeds or fails, authentication still continues
     *			to proceed down the LoginModule list.
     *
     *      3) Sufficient   - The LoginModule is not required to
     *			succeed.  If it does succeed, control immediately
     *			returns to the application (authentication does not
     *			proceed down the LoginModule list).
     *			If it fails, authentication continues down the
     *			LoginModule list.
     *
     *      4) Optional     - The LoginModule is not required to
     *			succeed.  If it succeeds or fails,
     *			authentication still continues to proceed down the
     *			LoginModule list.
     */
    function isAutomaticLoginRequired() {
        // If any required module returns false, this will be false
        $requiredFlag = null;

        // If any sufficient module returns true, this will be true
        $sufficientFlag = null;

        foreach ($this->automaticStrategies as $automaticStrategy) {

            if ($automaticStrategy->getMode() == 'SUFFICIENT') {
                if ($automaticStrategy->isAutomaticLoginRequired()) {
                    $sufficientFlag = TRUE;
                    break; // Stop evaluation
                }
            }

            if ($automaticStrategy->getMode() == 'REQUIRED') {
                if (!$automaticStrategy->isAutomaticLoginRequired()) {
                    $requiredFlag = FALSE;
                } else if (!isset($requiredFlag)) {
                    $requiredFlag = TRUE;
                }
            }

            // This does not affect the outcome of the evaluation
            if ($automaticStrategy->getMode() == 'OPTIONAL') {
                $automaticStrategy->isAutomaticLoginRequired();
            }

        }

        // If any required module returned a value, use it.
        if (isset($requiredFlag)) {
            return $requiredFlag;
        }

        // If any sufficient modules returned a value, use it; otherwise return false.
        return isset($sufficientFlag) && $sufficientFlag;
    }

    /**
     * Returns the URL where the user should be redireted to authenticate.
     *
     * @return string the configured login url.
     *
     * @access public
     */
    function getGatewayLoginUrl() {
        return $this->gatewayLoginUrl;
    }

    /**
     * Returns the SSO Session ID given an assertion id.
     *
     * @param string $assertionId
     *
     * @return string, the SSO Session associated with the given assertion.
     *
     * @access public
     */
    function resolveAuthenticationAssertion($assertionId) {
        // SOAP Invocation
        $soapclient = $this->getIdentityProvdierSoapClient();

        if (isset($this->wsdlUrl))
            $resolveAuthenticationAssertionRequest = array('ResolveAuthenticationAssertionRequest' => array('assertionId' => $assertionId, 'requester' => $this->getRequester()));
        else
            $resolveAuthenticationAssertionRequest = array('ResolveAuthenticationAssertion' => array('assertionId' => $assertionId, 'requester' => $this->getRequester()));
        $resolveAuthenticationAssertionResponse = $soapclient->call('resolveAuthenticationAssertion', $resolveAuthenticationAssertionRequest,
            'urn:org:josso:gateway:ws:1.2:protocol', '', false, null, 'document', 'literal');

        if (! $this->checkError($soapclient)) {
            // Return SSO Session ID
            return $resolveAuthenticationAssertionResponse['ssoSessionId'];
        }

    }

    /**
     * Returns the Assertion ID given an username and password.
     *
     * @param string $username
     * @param string $password
     *
     * @return string, the Assertion ID.
     *
     * @access public
     */
    function assertIdentityWithSimpleAuthentication($username, $password) {
        // SOAP Invocation
        $soapclient = $this->getIdentityProvdierSoapClient();

        if (isset($this->wsdlUrl))
            $assertIdentityWithSimpleAuthenticationRequest = array('AssertIdentityWithSimpleAuthenticationRequest' => array('username' => $username, 'password' => $password));
        else
            $assertIdentityWithSimpleAuthenticationRequest = array('AssertIdentityWithSimpleAuthentication' => array('username' => $username, 'password' => $password));
        $assertIdentityWithSimpleAuthenticationResponse = $soapclient->call('assertIdentityWithSimpleAuthentication', $assertIdentityWithSimpleAuthenticationRequest,
            'urn:org:josso:gateway:ws:1.2:protocol', '', false, null, 'document', 'literal');

        if (! $this->checkError($soapclient)) {
            // Return Assertion ID
            return $assertIdentityWithSimpleAuthenticationResponse['assertionId'];
        }

    }

    /**
     * Returns the URL where the user should be redireted to logout.
     *
     * @return string the configured logout url.
     *
     * @access public
     */
    function getGatewayLogoutUrl() {
        return $this->gatewayLogoutUrl;
    }

    /**
     * Returns the base path where JOSSO code is stored.
     */
    function getBaseCode() {
        return $this->baseCode;
    }

    /**
     * Returns P3P header value
     */
    function getP3PHeaderValue() {
        return $this->p3pHeaderValue;
    }

    /**
     * Allows client applications to access error messages
     *
     * @access public
     */
    function getError() {
        return $this->err;
    }

    /**
     * Allows client applications to access error messages
     *
     * @access public
     */
    function getFault() {
        return $this->fault;
    }

    //----------------------------------------------------------------------------------------
    // Protected methods intended to be invoked only within this class or subclasses.
    //----------------------------------------------------------------------------------------

    /**
     * Gets current JOSSO session id, if any.
     *
     * @access private
     */
    function getSessionId() {
        if (isset($_COOKIE['JOSSO_SESSIONID']))
            return $_COOKIE['JOSSO_SESSIONID'];
    }

    /**
     * Factory method to build a user from soap data.
     *
     * @param array user information as received from WS.
     * @return jossouser a new jossouser instance.
     *
     * @access private
     */
    function newUser($data) {
        // Build a new jossouser
        $username = $data['name'];
        $properties = $data['properties'];

        $user = new jossouser($username, $properties);

        return $user;
    }

    /**
     * Factory method to build a role from soap data.
     *
     * @param array role information as received from WS.
     * @return jossorole a new jossorole instance
     *
     * @access private
     */
    function newRole($data) {
        // Build a new jossorole
        $rolename = $data['!name'];
        $role = new jossorole($rolename);
        return $role;
    }

    /**
     * Checks if an error occured with the received soapclient and stores information in agent state.
     *
     * @access private
     */
    function checkError($soapclient) {
        // Clear old error/fault information.
        unset($this->fault);
        unset($this->err);

        // Check for a fault
        if ($soapclient->fault) {
            $this->fault = $soapclient->fault;
            return TRUE;
        } else {
            // Check for errors
            if ($soapclient->error_str != '') {
                $this->err = $soapclient->error_str;
                return TRUE;
            }
        }

        // No errors ...
        return FALSE;

    }

    /**
     * Gets the soap client to access identity service.
     *
     * @access private
     */
    function getIdentityMgrSoapClient() {
        // Lazy load the propper soap client
        if (!isset($this->identityMgrClient)) {
            if (isset($this->wsdlUrl)) {
                $this->identityMgrClient = new nusoap_client($this->wsdlUrl , true,
                    $this->proxyhost, $this->proxyport, $this->proxyusername, $this->proxypassword);
                if (isset($this->endpoint) && isset($this->identityManagerServicePath)) {
                    $this->identityMgrClient->setEndpoint($this->endpoint . $this->identityManagerServicePath);
                }
            }
            else {
                $this->identityMgrClient = new nusoap_client($this->endpoint . $this->identityManagerServicePath, false,
                    $this->proxyhost, $this->proxyport, $this->proxyusername, $this->proxypassword);
            }
            // Sets default encoding to UTF-8 ...
            $this->identityMgrClient->soap_defencoding = 'UTF-8';
            $this->identityMgrClient->decodeUTF8(false);
        }
        return $this->identityMgrClient;
    }

    /**
     * Gets the soap client to access identity provider.
     *
     * @access private
     */
    function getIdentityProvdierSoapClient() {
        // Lazy load the propper soap client
        if (!isset($this->identityProviderClient)) {
            if (isset($this->wsdlUrl)) {
                $this->identityProviderClient = new nusoap_client($this->wsdlUrl , true,
                    $this->proxyhost, $this->proxyport, $this->proxyusername, $this->proxypassword);
                if (isset($this->endpoint) && isset($this->identityProviderServicePath)) {
                    $this->identityProviderClient->setEndpoint($this->endpoint . $this->identityProviderServicePath);
                }
            }
            else {
                $this->identityProviderClient = new nusoap_client($this->endpoint . $this->identityProviderServicePath, false,
                    $this->proxyhost, $this->proxyport, $this->proxyusername, $this->proxypassword);
            }
            // Sets default encoding to UTF-8 ...
            $this->identityProviderClient->soap_defencoding = 'UTF-8';
            $this->identityProviderClient->decodeUTF8(false);
        }
        return $this->identityProviderClient;
    }


    /**
     * Gets the soap client to access session service.
     *
     * @access private
     */
    function getSessionMgrSoapClient() {
        // Lazy load the propper soap client
        if (!isset($this->sessionMgrClient)) {
            // SSOSessionManager SOAP Client
            if (isset($this->wsdlUrl)) {
                $this->sessionMgrClient = new nusoap_client($this->wsdlUrl , true,
                    $this->proxyhost, $this->proxyport, $this->proxyusername, $this->proxypassword);
                if (isset($this->endpoint) && isset($this->sessionManagerServicePath)) {
                    $this->sessionMgrClient->setEndpoint($this->endpoint . $this->sessionManagerServicePath);
                }
            }
            else {
                $this->sessionMgrClient = new nusoap_client($this->endpoint . $this->sessionManagerServicePath, false,
                    $this->proxyhost, $this->proxyport, $this->proxyusername, $this->proxypassword);
            }
        }
        return $this->sessionMgrClient;

    }

    /**
     *
     * Determines whether the beginning of the first given string matches the second string.
     *
     * @return bool
     * @access private
     */
    function startsWith($haystack,$needle,$case=true) {
        if ($case) {
            return (strcmp(substr($haystack, 0, strlen($needle)),$needle)===0);
        }
        return (strcasecmp(substr($haystack, 0, strlen($needle)),$needle)===0);
    }

    /**
     *
     * Gets the request context path.
     *
     * @return string
     * @access private
     */
    function getContextPath() {
        $contextPath = null;
        $requestUrl = null;
        $requester = null;

        if (isset($_SESSION['JOSSO_ORIGINAL_URL'])) {
            $requestUrl = $_SESSION['JOSSO_ORIGINAL_URL'];
        }

        if ((!isset($requestUrl) || $requestUrl == "") && isset($_GET['josso_current_url'])) {
            $requestUrl = $_GET['josso_current_url'];
        }

        if (!isset($requestUrl) || $requestUrl == "") {
            $requestUrl = $_SERVER['REQUEST_URI'];
        }

        if (isset($requestUrl)) {
            if ($this->startsWith($requestUrl, '/')) {
                $separatorIndex = strpos($requestUrl, '/', 1);
                if ($separatorIndex !== FALSE) {
                    $contextPath = substr($requestUrl, 0, $separatorIndex);
                } else {
                    $contextPath = $requestUrl;
                }
            } else if ($this->startsWith($requestUrl, 'http://') || $this->startsWith($requestUrl, 'https://')) {
                $requestUrl = strstr($requestUrl, '://');
                $separatorIndex = strpos($requestUrl, '/', 3);
                if ($separatorIndex !== FALSE) {
                    $requestUrl = substr($requestUrl, $separatorIndex);
                    $separatorIndex = strpos($requestUrl, '/', 1);
                    if ($separatorIndex !== FALSE) {
                        $contextPath = substr($requestUrl, 0, $separatorIndex);
                    } else {
                        $contextPath = $requestUrl;
                    }
                } else {
                    $contextPath = '/';
                }
            }
        }


        if (!isset($contextPath)) {
            //error_log("JOSSO : No context path found, forcing '/'");
            $contextPath = '/';
        }

        $partnerAppIDs = $this->getPartnerAppIDs();
        if (isset($partnerAppIDs[$contextPath])) {
            $requester = $partnerAppIDs[$contextPath];
        }

        if (!isset($requester)) {
            $requester = $partnerAppIDs['/'];
            if (isset($requester));
                $contextPath = '/';
        }

        if (!isset($contextPath) || $contextPath == "") {
            error_log("JOSSO : Cannot resolve context path (no request URI)");
        }

        return $contextPath;
    }

    /**
     *
     * Gets the partner application id associated with the the current context path.
     *
     * @return string
     * @access private
     */
    function getRequester() {
        $partnerAppIDs = $this->getPartnerAppIDs();

        if (isset($partnerAppIDs)) {

            $contextPath = $this->getContextPath();

            if(!array_key_exists($contextPath, $partnerAppIDs)) {
                error_log("JOSSO : No partner application defined for context " . $contextPath);
            } else {
                $requester = $partnerAppIDs[$contextPath];
            }

            if (isset($requester)) {
                return $requester;
            }
            error_log("JOSSO : Cannot find requester for " . $contextPath);
        }
        return null;
    }

    /**
     *
     * Gets the context/appID mappings.
     *
     * @return array
     * @access private
     */
    function getPartnerAppIDs() {
        $partnerAppIDs = $this->partnerAppIDs;
        if (isset($this->partnerAppIDsByHosts)) {
            if (array_key_exists('HTTP_HOST', $_SERVER)) {
                $host_name = $_SERVER['HTTP_HOST'];
                // need to cater for `host:port` since some "buggy" SAPI(s) have been known to return the port too, see http://goo.gl/bFrbCO
                $strpos = strpos($host_name, ':');
                if ($strpos !== false) {
                    $host_name = substr($host_name, 0, $strpos);
                }
            }
            // resolve partner map if one is found
            $partnerAppIDs = isset($this->partnerAppIDsByHosts[$host_name]) ? $this->partnerAppIDsByHosts[$host_name] : $partnerAppIDs;
        }
        return $partnerAppIDs;
    }
}
?>
