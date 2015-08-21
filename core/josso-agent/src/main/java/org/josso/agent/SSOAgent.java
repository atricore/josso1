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
package org.josso.agent;

import org.josso.gateway.GatewayServiceLocator;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.josso.gateway.session.service.SSOSessionManagerService;

/**
 * An Agent stands in between the Gateway and the Security Domain were partner application reside.
 * It provides transparent security context to partner applications, providing user and role information
 * by invoking the gateway through JAAS.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: SSOAgent.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public interface SSOAgent extends LocalSessionListener {

    /**
     * Starts the Agent.
     */
    void start();

    /**
     * Obtains the principal associated with the given SSO id
     *
     * @param request with the JOSSO Single Sign-On Session Identifier
     * @return the principal associated with the given session identifier
     */
    SingleSignOnEntry processRequest(SSOAgentRequest request);


    /**
     * Stops the Agent.
     */
    void stop();

    /**
     * Configures the Gateway Service Locator to be used by the SSOAgent.
     *
     * @param gsl
     */
    void setGatewayServiceLocator(GatewayServiceLocator gsl);


    /**
     * Obtains the Gateway Service Locator used by the SSOAgent to build
     * the concrete clients needed to query the Gateway services.
     *
     * This getter is need by the JAASLoginModule to know which Gateway Service Locator to use.
     *
     * @return the configured gateway service locator
     */

    /**
     * Gets the SSO Session Manager used by this agent.
     */
    SSOSessionManagerService getSSOSessionManager();

    SSOSessionManagerService getSSOSessionManager(String nodeId);

    /**
     * Gets the SSO Identity Manager used by this agent.
     */
    SSOIdentityManagerService getSSOIdentityManager();

    SSOIdentityManagerService getSSOIdentityManager(String nodeId);

    /**
     * Gets the SSO Identity Manager used by this agent.
     */
    SSOIdentityProviderService getSSOIdentityProvider();

    SSOIdentityProviderService getSSOIdentityProvider(String nodeId);


    /**
     * Configures this agent.
     */
    void setConfiguration(SSOAgentConfiguration cfg);

    /**
     * Getter for the agent configuration.
     */
    SSOAgentConfiguration getConfiguration();

    /**
     * Returns true if the received context is a partner application.
     *
     * @param contextPath
     * @return
     */
    boolean isPartnerApp(String vhost, String contextPath);
}
