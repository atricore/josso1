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

package org.josso.servlet.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SingleSignOnEntry;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.http.JOSSOSecurityContext;
import org.josso.servlet.agent.jaas.SSOGatewayHandler;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.security.Principal;

/**
 * This agent will authenticate users against JAAS Infrastructure directly.  It will look up for the "josso" login context.
 * You have to configure a JAAS context under the name "josso", delcaring the SSOGatewayLoginModule, for example :
 * <br>
 * <br>
 * <pre>
 * josso {
 *     org.josso.servlet.agent.jaas.SSOGatewayLoginModule required debug=true;
 * };
* </pre>
 *
 * Date: Nov 27, 2007
 * Time: 11:47:26 AM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 *
 */
public class GenericServletSSOAgent extends HttpSSOAgent {

    private static final Log log = LogFactory.getLog(GenericServletSSOAgent.class);

    /**
     * This extension will delegate processing to super class and publish JOSSO Security Context
     * instance in the LocalSession associated to the request.
     */
    protected SingleSignOnEntry execute(SSOAgentRequest request) {
        GenericServletSSOAgentRequest r = (GenericServletSSOAgentRequest) request;
        GenericServletLocalSession localSession = (GenericServletLocalSession) r.getLocalSession();

        SingleSignOnEntry  entry = super.execute(request);

        if (entry != null) {
            if (r.getSecurityContext() != null) {

                if (log.isDebugEnabled())
                    log.debug("Publishing JOSSO Security Context instance in session ["+(entry != null ? entry.ssoId : "<NO-SSO-ID>") +"]");

                localSession.setSecurityContext(r.getSecurityContext());
            }

        } else {
            if (localSession != null) {
                if (log.isDebugEnabled())
                    log.debug("Clearing JOSSO Security Context for session ["+ localSession.getId() +  "]");

                localSession.setSecurityContext(null);
                r.setSecurityContext(null);
            }
        }

        return entry;
    }

    /**
     * Resolves an authentication request using JAAS infrastructure.
     * @param request containing the SSO Session id.
     * @return null if no principal can be authenticated using the received SSO Session Id
     */
    protected Principal authenticate(SSOAgentRequest request) {

        String ssoSessionId = request.getSessionId();
        if (log.isDebugEnabled())
            log.debug("Attempting SSO Session authentication : " + ssoSessionId);

        try {

            // Look up for JAAS security context configured for JOSSO.
            CallbackHandler ch  = new SSOGatewayHandler(request.getRequester(), ssoSessionId);
            LoginContext lc  = new LoginContext("josso", ch);

            // Perform login
            lc.login();

            if (log.isDebugEnabled())
                log.debug("SSO Session authenticated " + ssoSessionId);

            // Lookup for specific principal

            if (log.isDebugEnabled())
                log.debug("Creating new JOSSO Security Context instance");

            Subject s = lc.getSubject();

            GenericServletSSOAgentRequest r = (GenericServletSSOAgentRequest) request;
            JOSSOSecurityContext ctx = new JOSSOSecurityContext (s);
            r.setSecurityContext(ctx);
            
            return ctx.getCurrentPrincipal();


        } catch (LoginException e) {
            log.error(e.getMessage());
        }

        return null;
    }

    protected boolean isAuthenticationAlwaysRequired() {
        return true;
    }

    protected void log(String message) {
        log.debug(message);
    }

    protected void log(String message, Throwable throwable) {
        log.debug(message, throwable);
    }
}
