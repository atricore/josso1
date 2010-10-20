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
package org.josso;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.SSOWebConfiguration;
import org.josso.gateway.SecurityDomainRegistry;
import org.josso.gateway.assertion.AssertionManager;
import org.josso.gateway.event.client.SSOEventManagerClient;
import org.josso.gateway.identity.service.SSOIdentityProvider;

/**
 * This class provides a singleton interface to the SSO components
 * This class should be used by any client that needs a reference to a
 * component.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: Lookup.java 620 2008-09-23 13:40:36Z sgonzalez $
 */

public class Lookup {

    private static final Log logger = LogFactory.getLog(Lookup.class);

    /**
     * The name of the resource holding JOSSO configuration
     */
    private String configResourceName;

    /**
     * Single statically instantiated instance of lookup.
     */
    private static final Lookup INSTANCE = new Lookup();

    /**
     * Reference to the component keeper.
     */
    private ComponentKeeper _componentKeeper;

    // ======================================================
    // Used only by Gateway instances
    // ======================================================

    /**
     * Reference to the Cached component instances
     */
    private SSOGateway ssoGateway;


    /**
     * Private constructor so that this class can only be instantiated by the singleton.
     */
    private Lookup() {
    }

    /**
     * Static factory method for getting a reference to the singleton
     * Lookup.
     *
     * @return Lookup
     */
    public static Lookup getInstance() {
        return Lookup.INSTANCE;
    }

    /**
     * Initializes the Lookup instance using the specified resource as configuration resource.
     *
     * @param configResourceName the name of the resource holding josso configuration (agent or gateway)
     */
    public void init(String configResourceName) {
        logger.info("Init resourceName <" + configResourceName + ">");
        this.configResourceName = configResourceName;
    }

    public SSOGateway lookupSSOGateway() throws Exception {

        if (ssoGateway == null) {

            synchronized (this) {
                // Once we have a lock, test again ... maybe another thread instantiated the GWY.
                if (ssoGateway == null) {

                    if (logger.isDebugEnabled())
                        logger.debug("Initializing JOSSO Gateway ... ");

                    ssoGateway = getComponentKeeper().fetchSSOGateway();
                    ssoGateway.initialize();
                }
            }
        }

        return ssoGateway;
    }

    /**
     * Fetches the security domain component.
     *
     * @return a reference to the component specified by name
     */
    public synchronized SecurityDomain lookupSecurityDomain() throws Exception {
        SSOContext ctx = SSOContext.getCurrent();
        if (ctx == null) {
            throw new IllegalStateException("No SSOContext found !");
        }

        SecurityDomain sd = ctx.getSecurityDomain();

        if (sd == null)
            logger.warn("SecurityDomain is null for thread " + Thread.currentThread().getName());

        return sd;
    }

    public SSOWebConfiguration lookupSSOWebConfiguration() throws Exception {
        SecurityDomain sd = lookupSecurityDomain();
        return sd.getSSOWebConfiguration();
    }

    public SSOIdentityProvider lookupSSOIdentityProvider() throws Exception {
        SecurityDomain sd = lookupSecurityDomain();
        return sd.getIdentityProvider();
    }

    public AssertionManager lookupAssertionManager() throws Exception {
        SecurityDomain sd = lookupSecurityDomain();
        return sd.getAssertionManager();
    }


    public SSOEventManagerClient lookupSSOEventManagerClient() {
        try {
            return (SSOEventManagerClient) Class.forName("org.josso.gateway.event.client.SSOEventManagerClientImpl").newInstance();

        } catch (Exception e) {
            logger.error("Cannot instantiate default event manager client : " + e.getMessage(), e);
            return null;
        }
    }


    /**
     * Gets the ComponentKeeper for the system.
     * The first time a component keeper is required. This method tries to get a ComponentKeeperFactory instance
     * to build the ComponentKeeper.
     * <p/>
     * If no factory can be found, the default component keeper is used.
     *
     * @return the ComponentKeeper
     * @see org.josso.spring.SpringComponentKeeperImpl
     * @see ComponentKeeperFactory
     */
    public ComponentKeeper getComponentKeeper() throws Exception {

        if (this._componentKeeper == null) {

            ComponentKeeperFactory factory = ComponentKeeperFactory.getInstance();

            factory.setResourceFileName(this.configResourceName);
            _componentKeeper = factory.newComponentKeeper();
            logger.info("Using ComponentKeeper : " + this._componentKeeper.getClass().getName());
        }

        return this._componentKeeper;
    }

    public static void main(String args[]) throws Exception {
        // Lookup.getInstance().lookupSecurityDomain();
    }


    public SecurityDomainRegistry lookupSecurityDomainRegistry() throws Exception {
        return this.lookupSSOGateway().getSecurityDomainRegistry();
    }
}
