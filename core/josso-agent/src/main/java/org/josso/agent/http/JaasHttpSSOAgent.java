/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.agent.http;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgentRequest;

/**
 * JAAS Agent implementation.
 * 
 * @org.apache.xbean.XBean element="agent"
 */
public class JaasHttpSSOAgent extends HttpSSOAgent {

	private static final Log log = LogFactory.getLog(JaasHttpSSOAgent.class);
	
	/**
     * Resolves an authentication request using JAAS infrastructure.
     * 
     * @param request containing the SSO Session id.
     * @return null if no principal can be authenticated using the received SSO Session Id
     */
    protected Principal authenticate(SSOAgentRequest request) {

        String ssoSessionId = request.getSessionId();
        if (log.isDebugEnabled()) {
            log.debug("Attempting SSO Session authentication by " + request.getRequester() + ":"  +ssoSessionId);
        }

        try {

            // Look up for JAAS security context configured for JOSSO.
            if (log.isDebugEnabled())
                log.debug("Creating callback handler for " + request.getRequester() + "/" + ssoSessionId);

            CallbackHandler ch  = new SSOGatewayHandler(request.getRequester(), ssoSessionId, request.getNodeId());
            LoginContext lc = new LoginContext("josso", ch);

            // Perform login
            lc.login();

            if (log.isDebugEnabled()) {
                log.debug("SSO Session authenticated " + ssoSessionId);
            }

            // Lookup for specific principal
            if (log.isDebugEnabled()) {
                log.debug("Creating new JOSSO Security Context instance");
            }

            Subject s = lc.getSubject();

            JOSSOSecurityContext ctx = new JOSSOSecurityContext(s);
            return ctx.getCurrentPrincipal();

        } catch (LoginException e) {
            log.error(e.getMessage());
        }

        return null;
    }
    
	@Override
	protected void log(String message) {
		if (debug > 0) {
			log.debug(message);
		}
	}

	@Override
	protected void log(String message, Throwable throwable) {
		if (debug > 0) {
			log.debug(message, throwable);
        }
	}
}
