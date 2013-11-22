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

package org.josso.gatein.agent;

import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SingleSignOnEntry;
import org.josso.servlet.agent.GenericServletLocalSession;
import org.josso.servlet.agent.GenericServletSSOAgent;
import org.josso.servlet.agent.GenericServletSSOAgentRequest;
import org.gatein.wci.security.Credentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @org.apache.xbean.XBean element="agent"
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 *
 */
public class GateinSSOAgent extends GenericServletSSOAgent {

    private static final Log log = LogFactory.getLog(GateinSSOAgent.class);

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
                String principal = r.getSecurityContext().getCurrentPrincipal().getName();

                if (log.isDebugEnabled()) {
                    log.debug("-----------------------------------------------------------");
                    log.debug("SessionId: "+entry.ssoId);
                    log.debug("Principal: "+principal);
                    log.debug("-----------------------------------------------------------");
                }

                Credentials credentials = new Credentials(principal, "");
                localSession.setAttribute(Credentials.CREDENTIALS, credentials);
                localSession.setAttribute("username", principal);
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

}
