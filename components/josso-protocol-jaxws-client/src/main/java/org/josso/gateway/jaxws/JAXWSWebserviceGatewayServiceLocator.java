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
import org.josso.gateway.session.service.SSOSessionManagerService;

/**
 *
 * @org.apache.xbean.XBean element="jaxws-service-locator"
 * Service Locator for Gateway Services available as Webservices.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: JAXWSWebserviceGatewayServiceLocator.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public class JAXWSWebserviceGatewayServiceLocator extends GatewayServiceLocator {

    private static final Log logger = LogFactory.getLog(JAXWSWebserviceGatewayServiceLocator.class);

    /**
     * Package private Constructor so that it can only be instantiated
     * by the GatewayServiceLocator Class.
     */
    public JAXWSWebserviceGatewayServiceLocator() {
    }

    /**
     * Locates the SSO Session Manager Service Webservice implementation.
     *
     * @return the SSO session manager WS implementation.
     * @throws Exception
     */
    public SSOSessionManagerService getSSOSessionManager() throws Exception {

       return null;
    }

    /**
     * Locates the SSO Identity Manager Service Webservice implementation.
     *
     * @return the SSO session manager WS implementation.
     * @throws Exception
     */
    public SSOIdentityManagerService getSSOIdentityManager() throws Exception {
       return null;
    }

    /**
     * Locates the SSO Identity Provider Service Webservice implementation.
     *
     * @return the SSO identity provider manager WS implementation.
     * @throws Exception
     */
    public SSOIdentityProviderService getSSOIdentityProvider() throws Exception {
        return null;
    }


}
