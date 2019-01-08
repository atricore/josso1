<?php
/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: 07/01/19
 * Time: 15:11
 */

namespace App\Http\Middleware;

use Closure;
use Log;
use jossoagent;
use jossouser;

require_once('josso-lib.php');

require_once('class.jossoagent.php');
require_once('class.jossouser.php');
require_once('class.jossorole.php');

class JOSSOLaravelAgent
{

    var $init = false;

    /**
     * Initializes the PHP SSO Agent
     */
    public function initSSO() {

        if ($this->init)
            return;

        require ('josso-cfg.inc');

        session_start();

        jossoagent::init($josso_gatewayLoginUrl, $josso_gatewayLogoutUrl, $josso_endpoint, $josso_wsdl_url, $josso_proxyhost,
            $josso_proxyport, $josso_proxyusername, $josso_proxypassoword, $josso_agentBasecode, $josso_p3pHeaderValue,
            $josso_sessionManagerServicePath, $josso_identityManagerServicePath, $josso_identityProviderServicePath,
            $josso_automaticLoginStrategies, $josso_partner_app_ids, $josso_partnerapp_vhosts, $josso_sessionAccessMinInterval,
            $forceUnsecureSSOCookie, $josso_ignoredResources, $josso_defaultResource);

        $josso_agent = jossoagent::getInstance();
        $josso_agent->setSoapclientDebugLevel(0); // Set to 1 to record debug information. $josso_agent->getIdentityMgrDebugStr(), etc.

        $this->init = true;
    }

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

        $this->initSSO();

        $currentUrl = $_SERVER['REQUEST_URI'] ;
        Log::debug('JOSSO Laravel Agent at ' . $currentUrl);

        $josso_agent = jossoagent::getInstance();

        // This resource should be ignored by the SSO agent (see josso-cfg.inc josso_ignoredResources)
        if ($josso_agent->isResourceIgnored($currentUrl))
            return $next($request);

        // Requires authentication
        if (!$this->accessSession()) {
            Log::debug('Requesting login at :' . $josso_agent->getGatewayLoginUrl());
            jossoRequestLoginForUrl($currentUrl, FALSE);
            exit;
        }

        // Automatic login (TODO)

        return $next($request);
    }

    /**
     * @return bool TRUE if there is a valid SSO session.
     */
    public function accessSession()
    {
        $josso_agent = jossoagent::getInstance();
        $ssoSessionId = $josso_agent->accessSession();

        if ($josso_agent->getSoapclientDebugLevel() != 0) {
            Log::debug($josso_agent->getSessionMgrDebugStr());
        }

        Log::debug("SSO Sesison ID " . $ssoSessionId);

        return isset($ssoSessionId) && strlen($ssoSessionId) > 0;

    }
}
