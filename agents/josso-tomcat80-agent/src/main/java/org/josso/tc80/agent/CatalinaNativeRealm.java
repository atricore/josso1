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

package org.josso.tc80.agent;

import org.apache.catalina.realm.RealmBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.AbstractSSOAgent;
import org.josso.agent.Lookup;
import org.josso.agent.SSOAgent;
import org.josso.agent.SSOAgentRequest;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.tc80.agent.jaas.CatalinaSSOUser;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Catalina Realm replacement that will authenticate users 
 * directly against the gateway.
 */
public class CatalinaNativeRealm extends RealmBase {
    private static Log log = LogFactory.getLog(CatalinaNativeRealm.class);

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "CatalinaNativeRealm";
    
	@Override
	public Principal authenticate(String username, String credentials) {
		try {
            SSOAgentRequest request = AbstractSSOAgent._currentRequest.get();
            SSOAgent agent = Lookup.getInstance().lookupSSOAgent();

            SSOIdentityManagerService im = request.getConfig(agent).getIdentityManagerService();
            if (im == null)
                im = agent.getSSOIdentityManager();
			
			String requester = "";
			// Check for nulls ?

            if (request != null)
            	requester = request.getRequester();
            else
                log.warn("No SSO Agent request found in thread local variable, can't identify requester");

            SSOUser ssoUser = im.findUserInSession(requester, username);
			
	        Principal principal = null;
	        
	        if (ssoUser != null) {
	        	Subject subject = new Subject();
	        	subject.getPrincipals().add(ssoUser);
                SSORole[] ssoRolePrincipals = im.findRolesBySSOSessionId(requester, username);
	            for (int i=0; i < ssoRolePrincipals.length; i++) {
	                subject.getPrincipals().add(ssoRolePrincipals[i]);
	            }
	            // Return the appropriate Principal for this authenticated Subject
	            principal = createPrincipal(username, subject);
	        }
			
	        return principal;
		} catch (SSOIdentityException e) {
            // Ignore this ... (user does not exist for this session)
            if (log.isDebugEnabled()) {
            	log.debug(e.getMessage());
            }
            return null;
        } catch (Exception e) {
        	log.error("Session authentication failed : " + username, e);
            throw new RuntimeException("Fatal error authenticating session : " + e);
        }
	}

	/**
     * Construct and return a java.security.Principal instance
     * representing the authenticated user for the specified Subject. If no
     * such Principal can be constructed, return null.
     *
     * The Principal constructed is CatalinaSSOUser which is a SSOUser.
     * The Partner Application can access SSOUser-specific properties that are not available
     * in GenericPrincipal.
     *
     * @param subject The Subject representing the logged in user
     */
    protected Principal createPrincipal(String username, Subject subject) {
        return CatalinaSSOUser.newInstance(this, subject);
    }

	@Override
	protected String getName() {
		return name;
	}

	@Override
	protected String getPassword(String username) {
		return null;
	}

	@Override
	protected Principal getPrincipal(String username) {
		return authenticate(username, username);
	}
}
