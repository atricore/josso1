<?php

namespace App\Http\Middleware;

use Closure;
use Log;
use jossoagent;

require_once('josso-lib.php');

require_once('class.jossoagent.php');
require_once('class.jossouser.php');
require_once('class.jossorole.php');

/**
 * Resolves an assertion token and creates a local security context for the SSO Agent
 *
 * NOTE: The JOSSOLaravelAgent MUST be executed before this middleware.
 *
 * @see JOSSOLaravelAgent
 *
 * Class JOSSOAssertionConsumerService
 * @package App\Http\Middleware
 */
class JOSSOAssertionConsumerService
{

    /**
     * Handle an incoming request.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \Closure  $next
     * @param  string|null  $guard
     * @return mixed
     */
    public function handle($request, Closure $next, $guard = null)
    {
        $currentUrl = $_SERVER['REQUEST_URI'] ;
        Log::debug('JOSSO Laravel ACS at ' . $currentUrl);

        $josso_agent = jossoagent::getInstance();

        if (isset($_REQUEST['josso_assertion_id'])) {
            $assertionId = $_REQUEST['josso_assertion_id'];

            Log::debug('Resolving assertion ' . $assertionId);

            $ssoSessionId = $josso_agent->resolveAuthenticationAssertion($assertionId);

            Log::debug('Resolved assertion ' . $assertionId . ' to ' . $ssoSessionId);

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
        } else
            $backToUrl = $josso_agent->getDefaultResource();

        // Set P3P Header
        $p3pHeaderValue = $josso_agent->getP3PHeaderValue();
        if (isset($p3pHeaderValue)) {
            header($josso_agent->getP3PHeaderValue());
        }

        if (isset($backToUrl)) {
            forceRedirect($backToUrl, true);
            exit;
        }

        Log::error('ACS Middleware should always redirect to another resource. At least a default resource must be configured');
        forceRedirect('/', true);
        exit;


    }

}
