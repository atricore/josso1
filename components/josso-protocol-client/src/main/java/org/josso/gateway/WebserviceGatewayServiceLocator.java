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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.identity.service.SSOIdentityProviderService;
import org.josso.gateway.identity.service.WebserviceSSOIdentityManager;
import org.josso.gateway.identity.service.WebserviceSSOIdentityProvider;
import org.josso.gateway.session.service.SSOSessionManagerService;
import org.josso.gateway.session.service.WebserviceSSOSessionManager;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityManager;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityManagerWSLocator;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityProvider;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityProviderWSLocator;
import org.josso.gateway.ws._1_2.wsdl.SSOSessionManager;
import org.josso.gateway.ws._1_2.wsdl.SSOSessionManagerWSLocator;
/**
 *
 * @org.apache.xbean.XBean element="ws-service-locator"
 * Service Locator for Gateway Services available as Webservices.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: WebserviceGatewayServiceLocator.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public class WebserviceGatewayServiceLocator extends GatewayServiceLocator {

    private static final Log logger = LogFactory.getLog(WebserviceGatewayServiceLocator.class);

    private static final String TRANSPORT_SECURITY_NONE = "none";
    private static final String TRANSPORT_SECURITY_CONFIDENTIAL = "confidential";

    private String _endpoint;
    private String _username;
    private String _transportSecurity = TRANSPORT_SECURITY_NONE;
    private String _servicesWebContext;
    private String _sessionManagerServicePath;
    private String _identityManagerServicePath;
    private String _identityProviderServicePath;

    /**
     * Package private Constructor so that it can only be instantiated
     * by the GatewayServiceLocator Class.
     */
    public WebserviceGatewayServiceLocator() {
    }

    /**
     * Locates the SSO Session Manager Service Webservice implementation.
     *
     * @return the SSO session manager WS implementation.
     * @throws Exception
     */
    public SSOSessionManagerService getSSOSessionManager() throws Exception {

        SSOSessionManagerWSLocator ssoManagerServiceLocator = new SSOSessionManagerWSLocator();
        String smEndpoint = getSSOSessionManagerEndpoint();
        logger.debug("Using SSOSessionManager endpoint '" + smEndpoint + "'");
        ssoManagerServiceLocator.setSSOSessionManagerSoapEndpointAddress(smEndpoint);

        // Lookup ...
        SSOSessionManager ssoSessionManagerWebservice = ssoManagerServiceLocator.getSSOSessionManagerSoap();
        WebserviceSSOSessionManager wsm = new WebserviceSSOSessionManager(ssoSessionManagerWebservice);

        return wsm;
    }

    /**
     * Locates the SSO Identity Manager Service Webservice implementation.
     *
     * @return the SSO session manager WS implementation.
     * @throws Exception
     */
    public SSOIdentityManagerService getSSOIdentityManager() throws Exception {
        SSOIdentityManagerWSLocator ssoIdentityManagerServiceLocator = new SSOIdentityManagerWSLocator();
        String imEndpoint = getSSOIdentityManagerEndpoint();
        logger.debug("Using SSOIdentityManager endpoint '" + imEndpoint + "'");
        ssoIdentityManagerServiceLocator.setSSOIdentityManagerSoapEndpointAddress(imEndpoint);

        // Lookup
        SSOIdentityManager ssoIdentityManagerWebservice = ssoIdentityManagerServiceLocator.getSSOIdentityManagerSoap();
        WebserviceSSOIdentityManager wim = new WebserviceSSOIdentityManager(ssoIdentityManagerWebservice);

        return wim;
    }

    /**
     * Locates the SSO Identity Provider Service Webservice implementation.
     *
     * @return the SSO identity provider manager WS implementation.
     * @throws Exception
     */
    public SSOIdentityProviderService getSSOIdentityProvider() throws Exception {
        SSOIdentityProviderWSLocator ssoIdentityProviderServiceLocator = new SSOIdentityProviderWSLocator();
        String ipEndpoint = getSSOIdentityProviderEndpoint();
        logger.debug("Using SSOIdentityProvider endpoint '" + ipEndpoint + "'");
        ssoIdentityProviderServiceLocator.setSSOIdentityProviderSoapEndpointAddress(ipEndpoint);

        // Lookup
        SSOIdentityProvider ssoIdentityProviderWebservice = ssoIdentityProviderServiceLocator.getSSOIdentityProviderSoap();
        WebserviceSSOIdentityProvider wip = new WebserviceSSOIdentityProvider(ssoIdentityProviderWebservice);

        return wip;
    }

    /**
     * Builds the endpoint base string.
     *
     * @return the endpoint base
     */
    public String getEndpointBase() {
        return (_transportSecurity.equalsIgnoreCase(TRANSPORT_SECURITY_CONFIDENTIAL) ? "https" : "http") +
                "://" + _endpoint + "/";

    }
    
    /**
     * Builds the SSOSessionManager endpoint string.
     *
     * @return the SSOSessionManager endpoint
     */
    public String getSSOSessionManagerEndpoint() {
    	if (_sessionManagerServicePath != null) {
    		return getEndpointBase() + _sessionManagerServicePath;
    	} else {
    		return getEndpointBase() + (_servicesWebContext != null ? _servicesWebContext : "josso") + 
    				"/services/SSOSessionManagerSoap";
    	}
    }
    
    /**
     * Builds the SSOIdentityManager endpoint string.
     *
     * @return the SSOIdentityManager endpoint
     */
    public String getSSOIdentityManagerEndpoint() {
    	if (_identityManagerServicePath != null) {
    		return getEndpointBase() + _identityManagerServicePath;
    	} else {
    		return getEndpointBase() + (_servicesWebContext != null ? _servicesWebContext : "josso") + 
    				"/services/SSOIdentityManagerSoap";
    	}
    }
    
    /**
     * Builds the SSOIdentityProvider endpoint string.
     *
     * @return the SSOIdentityProvider endpoint
     */
    public String getSSOIdentityProviderEndpoint() {
    	if (_identityProviderServicePath != null) {
    		return getEndpointBase() + _identityProviderServicePath;
    	} else {
    		return getEndpointBase() + (_servicesWebContext != null ? _servicesWebContext : "josso") + 
    				"/services/SSOIdentityProviderSoap";
    	}
    }

    //----------------------------------------------------------------- Configuration Properties

    /**
     * SOAP end point, e.g. localhost:8080
     */
    public void setEndpoint(String endpoint) {
        _endpoint = endpoint;
    }

    /**
     * SOAP end point, e.g. localhost:8080
     */
    public String getEndpoint() {
        return _endpoint;
    }

    /**
     * SOAP end point services web context, e.g. myjosso
     */
    public void setServicesWebContext(String servicesWebContext) {
        _servicesWebContext = servicesWebContext;
    }

    /**
     * SOAP end point services web context, e.g. myjosso
     */
    public String getServicesWebContext() {
        return _servicesWebContext;
    }

    /**
     * Set the username used to authenticate SOAP messages.
     *
     * @param username the username used to authenticate the SOAP message.
     */
    public void setUsername(String username) {
        WebserviceClientAuthentication.setUsername(username);
        _username = username;
    }

    /**
     * Getter for username used to authenticate SOAP messages.
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Set the password used to authenticate SOAP messages.
     *
     * @param password the password used to authenticate the SOAP message.
     */
    public void setPassword(String password) {
        WebserviceClientAuthentication.setPassword(password);
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
        _transportSecurity = transportSecurity;
    }

    /**
     * Transport security used in SOAP messages, valid values are : none|confidential
     */
    public String getTransportSecurity() {
        return _transportSecurity;
    }

	/**
	 * @return the sessionManagerServicePath
	 */
	public String getSessionManagerServicePath() {
		return _sessionManagerServicePath;
	}

	/**
	 * Set the SSOSessionManager service full path (everything that goes after the endpoint).
	 * 
	 * @param sessionManagerServicePath the sessionManagerServicePath to set
	 */
	public void setSessionManagerServicePath(String sessionManagerServicePath) {
		_sessionManagerServicePath = sessionManagerServicePath;
	}

	/**
	 * @return the identityManagerServicePath
	 */
	public String getIdentityManagerServicePath() {
		return _identityManagerServicePath;
	}

	/**
	 * Set the SSOIdentityManager service full path (everything that goes after the endpoint).
	 * 
	 * @param identityManagerServicePath the identityManagerServicePath to set
	 */
	public void setIdentityManagerServicePath(String identityManagerServicePath) {
		_identityManagerServicePath = identityManagerServicePath;
	}

	/**
	 * @return the identityProviderServicePath
	 */
	public String getIdentityProviderServicePath() {
		return _identityProviderServicePath;
	}

	/**
	 * Set the SSOIdentityProvider service full path (everything that goes after the endpoint).
	 * 
	 * @param identityProviderServicePath the identityProviderServicePath to set
	 */
	public void setIdentityProviderServicePath(
			String identityProviderServicePath) {
		_identityProviderServicePath = identityProviderServicePath;
	}

}
