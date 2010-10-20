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

package org.josso.seam.console;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.josso.agent.Lookup;
import org.josso.agent.SSOAgent;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.signon.Constants;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import java.util.Map;

/**
 * This an authenticator for Seam that lets Seam grab signon
 * credentials from JOSSO.
 * 
 * The authenticator obtains the JOSSO session id, and attempts
 * to obtain the active JOSSO session. If an active session is
 * found it sets the username, and the roles given to this
 * user into Seam context.
 * 
 * @author <a href="mailto:kurt.stam@osconsulting.org">kurt.stam&064;osconsulting.org</a>
 * 
 */
@Name("jossoAuthenticator")
public class JossoAuthenticator {
    
    private static final long serialVersionUID = 1L;
    
    @Logger
    Log log;
    
    @In 
    FacesContext facesContext;

    @In
    Identity identity;
    
    public void checkLogin() {
        final boolean isLoggedIn = identity.isLoggedIn();
        // user may already be logged in - check
        if (isLoggedIn) {
          return;
        }
        authenticate();
    }
    
    public boolean authenticate() 
    {
        Map map = facesContext.getExternalContext().getRequestCookieMap();
        String sessionId=null;
        if (map.containsKey(Constants.JOSSO_SINGLE_SIGN_ON_COOKIE)) {
            sessionId = ((Cookie) map.get(Constants.JOSSO_SINGLE_SIGN_ON_COOKIE)).getValue();
        }
        try {
            if (sessionId != null && !"".equals(sessionId)) {
                SSOAgent jossoAgent = Lookup.getInstance().lookupSSOAgent();
                // TODO : Send requester !
                SSOSession session = jossoAgent.getSSOSessionManager().getSession(null, sessionId);
                String username = session.getUsername();
                identity.setUsername(username);
                identity.setPassword(username);
                log.info( "User " + username + " logged into Seam via JossoAuthenticator module.");
                SSORole[] roles = jossoAgent.getSSOIdentityManager().findRolesBySSOSessionId(null, sessionId );
                for (int i=0; i<roles.length; i++) {
                    String role = roles[i].getName();
                    log.info( "User " + username + " adding role " + role);
                    identity.addRole(role);
                }
                return true;
            } else {
                log.error("No JOSSO session found: " + sessionId + ". User not authenticated.");
            }
        } catch (NoSuchSessionException e) {
            log.error("NoSuchSessionException : " + sessionId + ". User not authenticated.");
        } catch (Exception e) {
            log.error(e.getMessage() + ". User not authenticated.", e);
        }
        return false;
    }
}