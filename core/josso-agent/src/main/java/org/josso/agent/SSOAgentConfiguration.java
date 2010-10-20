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

import java.util.List;

/**
 *
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAgentConfiguration.java 598 2008-08-16 05:41:50Z gbrigand $
 */

public interface SSOAgentConfiguration {

    /**
     * Adds a new SSO Partner application configuration based on the received web context and ignored web resources' names list.
     *
     * @param appWebCtx           partner application web context, i.e. /myApp
     * @param ignoredWebResources optional, an array of web resource collection definition names.
     */
    void addSSOPartnerApp(String id, String appWebCtx, String[] ignoredWebResources);

    /**
     * Adds a new SSO Partner application configuration based on the received web context and ignored web resources' names list.
     *
     * @param id                  configuration identifier
     * @param vhost               host or virtual host, optional
     * @param appWebCtx           partner application web context, i.e. /myApp
     * @param ignoredWebResources optional, an array of web resource collection definition names.
     */
    void addSSOPartnerApp(String id, String vhost, String appWebCtx, String[] ignoredWebResources, SecurityContextPropagationConfig secCtxPropConfig);

    /**
     * Adds a new configuration
     */
    void addSSOPartnerApp(SSOPartnerAppConfig cfg);

    /**
     * Removes the configuration associated with the received web context
     */
    void removeSSOPartnerApp(String appWebCtx);

    /**
     * Lists all configurations
     */
    List<SSOPartnerAppConfig> getSsoPartnerApps();

    /**
     * Sets the entire list of configurations, replacing the current list.
     */
    void setSsoPartnerApps(List<SSOPartnerAppConfig> apps);


}
