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
 * Redirects the user to the corresponding login URL in the IdP
 *
 * NOTE: The JOSSOLaravelAgent MUST be executed before this middleware.
 *
 * @see JOSSOLaravelAgent
 *
 * Class JOSSOLogin
 * @package App\Http\Middleware
 */
class JOSSOLogin
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
        $josso_agent = jossoagent::getInstance();
        jossoRequestLoginForUrl($josso_agent->getDefaultResource(), false);
        exit;
    }
}
