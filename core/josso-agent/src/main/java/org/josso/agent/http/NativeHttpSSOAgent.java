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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgentRequest;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;

/**
 * Native Agent implementation.
 * 
 * @org.apache.xbean.XBean element="agent-native"
 */
public class NativeHttpSSOAgent extends HttpSSOAgent {

	private static final Log log = LogFactory.getLog(NativeHttpSSOAgent.class);

	/**
     * Resolves an authentication request directly against the gateway.
     * 
     * @param request containing the SSO Session id.
     * @return null if no principal can be authenticated using the received SSO Session Id
     */
	protected Principal authenticate(SSOAgentRequest request) {
		String ssoSessionId = request.getSessionId();
        try {
            if (ssoSessionId == null) {
            	log.debug("Session authentication failed : " + ssoSessionId);
                return null;
            }
            
            SSOUser ssoUser = getSSOIdentityManager().findUserInSession(request.getRequester(), ssoSessionId);
            
            log.debug("Session authentication succeeded : " + ssoSessionId);
            return ssoUser;
        } catch (SSOIdentityException e) {
            // Ignore this ... (user does not exist for this session)
            if (log.isDebugEnabled()) {
            	log.debug(e.getMessage());
            }
            return null;
        } catch (Exception e) {
        	log.error("Session authentication failed : " + ssoSessionId, e);
            throw new RuntimeException("Fatal error authenticating session : " + e);
        }
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
