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
    protected static final String property = "org.josso.gateway.GatewayServiceLocator";

    /**
     * La factory por defecto a ser utilizada
     */
    protected static final String factory = "org.josso.gateway.WebserviceGatewayServiceLocator";
    protected static final String TRANSPORT_SECURITY_NONE = "none";
    protected static final String TRANSPORT_SECURITY_CONFIDENTIAL = "confidential";
    private String endpoint;
    private String username;
    private String transportSecurity = TRANSPORT_SECURITY_NONE;
    private String servicesWebContext;
    private String sessionManagerServicePath;
    private String identityManagerServicePath;
    private String identityProviderServicePath;
    private String password;

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

    /**
     * Builds the endpoint base string.
     *
     * @return the endpoint base
     */
    public String getEndpointBase() {
        return (transportSecurity.equalsIgnoreCase(TRANSPORT_SECURITY_CONFIDENTIAL) ? "https" : "http") +
                "://" + endpoint + "/";

    }

    /**
     * Builds the SSOSessionManager endpoint string.
     *
     * @return the SSOSessionManager endpoint
     */
    public String getSSOSessionManagerEndpoint() {
    	if (sessionManagerServicePath != null) {
    		return getEndpointBase() + sessionManagerServicePath;
    	} else {
    		return getEndpointBase() + (servicesWebContext != null ? servicesWebContext : "josso") +
    				"/services/SSOSessionManagerSoap";
    	}
    }

    /**
     * Builds the SSOIdentityManager endpoint string.
     *
     * @return the SSOIdentityManager endpoint
     */
    public String getSSOIdentityManagerEndpoint() {
    	if (identityManagerServicePath != null) {
    		return getEndpointBase() + identityManagerServicePath;
    	} else {
    		return getEndpointBase() + (servicesWebContext != null ? servicesWebContext : "josso") +
    				"/services/SSOIdentityManagerSoap";
    	}
    }

    /**
     * Builds the SSOIdentityProvider endpoint string.
     *
     * @return the SSOIdentityProvider endpoint
     */
    public String getSSOIdentityProviderEndpoint() {
    	if (identityProviderServicePath != null) {
    		return getEndpointBase() + identityProviderServicePath;
    	} else {
    		return getEndpointBase() + (servicesWebContext != null ? servicesWebContext : "josso") +
    				"/services/SSOIdentityProviderSoap";
    	}
    }

    /**
     * SOAP end point, e.g. localhost:8080
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * SOAP end point, e.g. localhost:8080
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * SOAP end point services web context, e.g. myjosso
     */
    public void setServicesWebContext(String servicesWebContext) {
        this.servicesWebContext = servicesWebContext;
    }

    /**
     * SOAP end point services web context, e.g. myjosso
     */
    public String getServicesWebContext() {
        return servicesWebContext;
    }

    /**
     * Getter for username used to authenticate SOAP messages.
     */
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return "*";
    }

    /**
     * Transport security used in SOAP messages, valid values are : none, confidential
     *
     * @param transportSecurity valid values are none, confidential
     */
    public void setTransportSecurity(String transportSecurity) {
        this.transportSecurity = transportSecurity;
    }

    /**
     * Transport security used in SOAP messages, valid values are : none|confidential
     */
    public String getTransportSecurity() {
        return transportSecurity;
    }

    /**
	 * @return the sessionManagerServicePath
	 */
	public String getSessionManagerServicePath() {
		return sessionManagerServicePath;
	}

    /**
	 * Set the SSOSessionManager service full path (everything that goes after the endpoint).
	 *
	 * @param sessionManagerServicePath the sessionManagerServicePath to set
	 */
	public void setSessionManagerServicePath(String sessionManagerServicePath) {
		this.sessionManagerServicePath = sessionManagerServicePath;
	}

    /**
	 * @return the identityManagerServicePath
	 */
	public String getIdentityManagerServicePath() {
		return identityManagerServicePath;
	}

    /**
	 * Set the SSOIdentityManager service full path (everything that goes after the endpoint).
	 *
	 * @param identityManagerServicePath the identityManagerServicePath to set
	 */
	public void setIdentityManagerServicePath(String identityManagerServicePath) {
		this.identityManagerServicePath = identityManagerServicePath;
	}

    /**
	 * @return the identityProviderServicePath
	 */
	public String getIdentityProviderServicePath() {
		return identityProviderServicePath;
	}

    /**
	 * Set the SSOIdentityProvider service full path (everything that goes after the endpoint).
	 *
	 * @param identityProviderServicePath the identityProviderServicePath to set
	 */
	public void setIdentityProviderServicePath(
			String identityProviderServicePath) {
		this.identityProviderServicePath = identityProviderServicePath;
	}

    /**
     * Set the username used to authenticate SOAP messages.
     *
     * @param username the username used to authenticate the SOAP message.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Set the password used to authenticate SOAP messages.
     *
     * @param password the password used to authenticate the SOAP message.
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
