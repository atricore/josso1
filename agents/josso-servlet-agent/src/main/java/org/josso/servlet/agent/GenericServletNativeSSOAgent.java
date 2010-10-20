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

import java.security.Principal;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SingleSignOnEntry;
import org.josso.agent.http.JOSSOSecurityContext;
import org.josso.agent.http.NativeHttpSSOAgent;
import org.josso.gateway.identity.SSORole;

/**
 * This agent will authenticate users directly against the gateway.
 * 
 * @org.apache.xbean.XBean element="agent-native"
 */
public class GenericServletNativeSSOAgent extends NativeHttpSSOAgent {

    private static final Log log = LogFactory.getLog(GenericServletNativeSSOAgent.class);

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
     * Resolves an authentication request directly against the gateway.
     * 
     * @param request containing the SSO Session id.
     * @return null if no principal can be authenticated using the received SSO Session Id
     */
    protected Principal authenticate(SSOAgentRequest request) {

        String ssoSessionId = request.getSessionId();
        
        Principal ssoUser = super.authenticate(request);
        if (ssoUser != null) {
        	Subject subject = new Subject();
        	subject.getPrincipals().add(ssoUser);
            SSORole[] ssoRolePrincipals = getRoleSets(request.getRequester(), ssoSessionId);
            for (int i=0; i < ssoRolePrincipals.length; i++) {
                subject.getPrincipals().add(ssoRolePrincipals[i]);
                log.debug("Added SSORole Principal to the Subject : " + ssoRolePrincipals [i]);
            }
        	GenericServletSSOAgentRequest r = (GenericServletSSOAgentRequest) request;
            JOSSOSecurityContext ctx = new JOSSOSecurityContext(subject);
            r.setSecurityContext(ctx);
        }
        
        return ssoUser;
    }

    @Override
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
