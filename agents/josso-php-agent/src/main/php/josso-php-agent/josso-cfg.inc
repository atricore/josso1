<?php
/**
 * PHP Josso Agent configuration file.
 *
 * @package  org.josso.agent.php
 *
 * @version $Id: josso-cfg.inc 613 2008-08-26 16:42:10Z sgonzalez $
 *
 */

/**
 Copyright (c) 2004, Novascope S.A. and the JOSSO team
 All rights reserved.
 Redistribution and use in source and binary forms, with or
 without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in
   the documentation and/or other materials provided with the
   distribution.

 * Neither the name of the JOSSO team nor the names of its
   contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
*/

// Josso agent configuration
$josso_gatewayLoginUrl = 'http://localhost:8080/josso/signon/login.do';
$josso_gatewayLogoutUrl = 'http://localhost:8080/josso/signon/logout.do';

// WS client configuration :
$josso_endpoint = 'http://localhost:8080';

// This could be also "", it points to the path where JOSSO code is found, for example the josso-security-check.php page.
// Do not end the baseCode with "/".
$josso_agentBasecode = "/josso-php-partnerapp";

$josso_proxyhost = '';
$josso_proxyport = '';
$josso_proxyusername = '';
$josso_proxypassoword = '';

// Min. session access interval for each user, in seconds.
$josso_sessionAccessMinInterval=1;

// $josso_p3pHeaderValue='P3P:CP="CAO PSA OUR"';

// All requests starting with the following uris will be ignored!
$josso_ignoredResources = array('/ignored', '/public', 'partner-login.php');

// Default resource to send the user after login/logout if no saved request is found
$josso_defaultResource = '/index.php?arg1=one';

// $josso_partner_app_ids = array('/josso-php-partnerapp' => 'partnerapp1');

/*
  Automatic login strategies.
  If not defined then DEFAULT strategy is used (with SUFFICIENT mode).

  strategy: DEFAULT, URLBASED, BOT
  mode: REQUIRED, SUFFICIENT, OPTIONAL
*/
/*
$josso_automaticLoginStrategies = array(
    array('strategy' => 'URLBASED', 'mode' => 'REQUIRED', 'urlPatterns' => array('pattern1', 'pattern2')),
    array('strategy' => 'BOT', 'mode' => 'REQUIRED', 'botsFile' => '/path/to/bots.file'),
    array('strategy' => 'URLBASED', 'mode' => 'SUFFICIENT', 'urlPatterns' => array('pattern3')),
    array('strategy' => 'DEFAULT', 'mode' => 'REQUIRED')
);
*/

?>