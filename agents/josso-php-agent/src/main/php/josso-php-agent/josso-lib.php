<?php

// ---------------------------------------------------------------------------------------------
// Functions that can be used by PHP applications...
// ---------------------------------------------------------------------------------------------

require_once('class.jossoagent.php');
require_once('class.jossouser.php');
require_once('class.jossorole.php');

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
    $josso_agent = jossoagent::getInstance();

    $loginUrl = $josso_agent->getBaseCode().'/josso-authenticate.php';

    return $loginUrl;

}


/**
 * Creates a login url for the current page, use to create links to JOSSO login page
 */
function jossoCreateLoginUrl() {

    // Get JOSSO Agent instance
    $josso_agent = jossoagent::getInstance();

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
    $josso_agent = jossoagent::getInstance();

    if (is_null($backToUrl)) {
        $backToUrl = createBaseUrl() . $_SERVER['REQUEST_URI'] ;
    }

    $logoutUrl =  $josso_agent->getBaseCode().'/josso-logout.php'. '?josso_current_url=' . base64_encode($backToUrl);

    return $logoutUrl;

}


function jossoRequestLoginForUrl($currentUrl, $optional = FALSE) {

    $_SESSION['JOSSO_ORIGINAL_URL'] = $currentUrl;

    // Get JOSSO Agent instance
    $josso_agent = jossoagent::getInstance();
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
    $josso_agent = jossoagent::getInstance();
    $securityCheckUrl = createBaseUrl().$josso_agent->getBaseCode().'/josso-security-check.php';

    return $securityCheckUrl;

}

function jossoRequestLogoutForUrl($currentUrl) {

    $_SESSION['JOSSO_ORIGINAL_URL'] = $currentUrl;

    // Get JOSSO Agent instance
    $josso_agent = jossoagent::getInstance();
    $logoutUrl = $josso_agent->getGatewayLogoutUrl(). '?josso_back_to=' . $currentUrl;

    $logoutUrl = $logoutUrl . createFrontChannelParams();

    // Clear SSO Cookie
    setcookie("JOSSO_SESSIONID", '', 0, "/"); // session cookie ...
    $_COOKIE['JOSSO_SESSIONID'] = '';


    forceRedirect($logoutUrl);

}

function createSecurityContext() {
    if (isset($_REQUEST['josso_assertion_id'])) {
        $assertionId = $_REQUEST['josso_assertion_id'];

        $josso_agent = jossoagent::getInstance();

        $ssoSessionId = $josso_agent->resolveAuthenticationAssertion($assertionId);

        if (!empty($_SERVER['HTTPS']) && !$josso_agent->forceUnsecureSSOCookie()){
            setcookie("JOSSO_SESSIONID", $ssoSessionId, 0, "/","",1); // secure session cookie ...
        }else{
            setcookie("JOSSO_SESSIONID", $ssoSessionId, 0, "/"); // session cookie ...                 1
        }
        $_COOKIE['JOSSO_SESSIONID'] = $ssoSessionId;
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
        forceRedirect($backToUrl, true);
    }
}

function forceRedirect($url,$die=true) {
    if (!headers_sent()) {
        ob_end_clean();
        header("Location: " . $url);
        prepareNonCacheResponse();
        exit();
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
    $josso_agent = jossoagent::getInstance();
    $params = '&josso_partnerapp_host=' . $host . '&josso_partnerapp_id=' . $josso_agent->getRequester();

    return $params;

    // TODO : Support josso_partnerapp_ctx param too ?

}

function prepareNonCacheResponse() {
    header("Cache-Control: no-cache");
    header("Pragma: no-cache");
    header("Expires: 0");
}

?>
