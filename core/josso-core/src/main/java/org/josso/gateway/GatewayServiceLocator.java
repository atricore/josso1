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
package org.josso.gateway;

import org.josso.gateway.session.service.SSOSessionManagerService;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.identity.service.SSOIdentityProviderService;

/**
 * Locates services provided by the Single Sign-On Gateway.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: GatewayServiceLocator.java 568 2008-07-31 18:39:20Z sgonzalez $
 */
public abstract class GatewayServiceLocator {

    /**
     * The property name of the factory to be used
     */
    private static final String property = "org.josso.gateway.GatewayServiceLocator";

    /**
     * La factory por defecto a ser utilizada
     */
    private static final String factory = "org.josso.gateway.WebserviceGatewayServiceLocator";

    /**
     * We're leaving the constructor public since it need to be instantiated by
     * the ComponentKeeper.
     */
    public GatewayServiceLocator() {
        super();
    }

    /**
     * Instantiates the concrete gateway service locator.
     *
     * @return the concrete service locator.
     */
    public static GatewayServiceLocator newInstance() {

        String n = factory;
        try {
            n = System.getProperty(property, factory);
        } catch (SecurityException e) {
            n = factory;
        }

        try {
            // Loads and instantiates the factory.
            return (GatewayServiceLocator) Class.forName(n).newInstance();

        } catch (ClassNotFoundException e) {
            // La factory no fue encontrada
            throw new ServiceLocatorConfigurationError("Cannot load class " +
                    "GatewayServiceLocator class \"" + n + "\"");
        } catch (InstantiationException e) {
            // La factory no pudo ser instanciada
            throw new ServiceLocatorConfigurationError("Cannot instantiate the " +
                    "specified GatewayServiceLocator class \"" + n + "\"");
        } catch (IllegalAccessException e) {
            // La factory no pudo ser accedida
            throw new ServiceLocatorConfigurationError("Cannot access the specified " +
                    "GatewayServiceLocator class \"" + n + "\"");

        } catch (ClassCastException e) {
            // La factory no era una RpsClientFactory
            throw new ServiceLocatorConfigurationError("The specified class \"" + n +
                    "\" is not instance of \"org.josso.gateway.GatewayServiceLocator\"");
        }

    }

    /**
     * Locates the Single Sign-On Session Manager.
     *
     * @return an instance of the SSO Session Manager.
     * @throws Exception
     */
    public abstract SSOSessionManagerService getSSOSessionManager() throws Exception;

    /**
     * Locates the Single Sign-On Identity Manager.
     *
     * @return an instance of the SSO Identity Manager.
     * @throws Exception
     */
    public abstract SSOIdentityManagerService getSSOIdentityManager() throws Exception;

    /**
     * Locates the Single Sign-On Identity Manager.
     *
     * @return an instance of the SSO Identity Provider.
     * @throws Exception
     */
    public abstract SSOIdentityProviderService getSSOIdentityProvider() throws Exception;

}
