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
 */

package org.josso.wls92.agent.mbeans;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.wls92.agent.WLSAgentServletFilter;
import weblogic.management.security.ProviderMBean;
import weblogic.security.provider.PrincipalValidatorImpl;
import weblogic.security.spi.*;

import javax.security.auth.login.AppConfigurationEntry;
import javax.servlet.Filter;
import java.util.HashMap;

/**
 * Date: Nov 20, 2007
 * Time: 1:05:08 PM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class JOSSOAuthenticationProviderImpl implements AuthenticationProviderV2 , ServletAuthenticationFilter {

    private static final Log logger = LogFactory.getLog(JOSSOAuthenticationProviderImpl.class);

    /**
     * a description of this provider
     */
    private String description;

    private Filter[] filters;

    /**
     * how this provider's login module should be used during the JAAS login
     */
    private AppConfigurationEntry.LoginModuleControlFlag controlFlag;

    /**
     * Initialize the JOSSO authenticator.
     *
     * @param mbean    A ProviderMBean that holds the JOSSO authenticator's
     *                 configuration data.  This mbean must be an instance of the JOSSO
     *                 authenticator's mbean.
     * @param services The SecurityServices gives access to the auditor
     *                 so that the provider can to post audit events.
     *                 <p/>
     *                 The JOSSO authenticator doesn't use this parameter.
     * @see weblogic.security.spi.SecurityProvider
     */
    public void initialize(ProviderMBean mbean, SecurityServices services) {

        if (logger.isDebugEnabled())
            logger.debug("JOSSOAuthenticationProviderImpl.initialize");

        // Cast the mbean from a generic ProviderMBean to a JOSSOAuthenticatorMBean.
        JOSSOAuthenticatorMBean myMBean = (JOSSOAuthenticatorMBean) mbean;

        // Set the description to the JOSSO authenticator's mbean's description and version
        description = myMBean.getDescription() + "\n" + myMBean.getVersion();

        // Extract the JAAS control flag from the JOSSO authenticator's mbean.
        // This flag controls how the JOSSO authenticator's login module is used
        // by the JAAS login, both for authentication and for identity assertion.
        String flag = myMBean.getControlFlag();
        if (flag.equalsIgnoreCase("REQUIRED")) {
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
        } else if (flag.equalsIgnoreCase("OPTIONAL")) {
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
        } else if (flag.equalsIgnoreCase("REQUISITE")) {
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
        } else if (flag.equalsIgnoreCase("SUFFICIENT")) {
            controlFlag = AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
        } else {
            throw new IllegalArgumentException("invalid flag value" + flag);
        }

        try {

            // For an agent lookup
            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-agent-config.xml"); // For spring compatibility ...
            lookup.lookupSSOAgent().start();

            if (logger.isDebugEnabled())
                logger.debug("Creating new JOSSO Servlet Agent Filter instance ...");

            filters = new Filter[1];
            filters[0] = new WLSAgentServletFilter();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
    }

    /**
     * Get the JOSSO authenticator's description.
     *
     * @return A String containing a brief description of the JOSSO authenticator.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Shutdown the JOSSO authenticator.
     * <p/>
     * A no-op.
     */
    public void shutdown() {
        if (logger.isDebugEnabled())
            logger.debug("JOSSOAuthenticationProviderImpl.shutdown");
    }

    /**
     * Create a JAAS AppConfigurationEntry (which tells JAAS
     * how to create the login module and how to use it).
     * This helper method is used both for authentication mode
     * and identity assertion mode.
     *
     * @param options A HashMap containing the options to pass to the
     *                JOSSO authenticator's login module.
     * @return An AppConfigurationEntry that tells JAAS how to use the JOSSO
     *         authenticator's login module.
     */
    private AppConfigurationEntry getConfiguration(HashMap options) {
        // make sure to specify the JOSSO authenticator's login module
        // and to use the control flag from the JOSSO authenticator's mbean.
        return new
                AppConfigurationEntry(
                    "org.josso.wls92.agent.jaas.SSOGatewayLoginModuleImpl",
                    controlFlag,
                    options );
    }

    /**
     * Create a JAAS AppConfigurationEntry (which tells JAAS
     * how to create the login module and how to use it) when
     * the JOSSO authenticator is used to authenticate (vs. to
     * complete identity assertion).
     *
     * @return An AppConfigurationEntry that tells JAAS how to use the JOSSO
     *         authenticator's login module for authentication.
     */
    public AppConfigurationEntry getLoginModuleConfiguration() {
        // Don't pass in any special options.
        // By default, the JOSSO authenticator's login module
        // will authenticate (by checking that the passwords match).
        HashMap options = new HashMap();
        return getConfiguration(options);
    }

    /**
     * Create a JAAS AppConfigurationEntry (which tells JAAS
     * how to create the login module and how to use it) when
     * the JOSSO authenticator is used to complete identity
     * assertion (vs. to authenticate).
     *
     * @return An AppConfigurationEntry that tells JAAS how to use the JOSSO
     *         authenticator's login module for identity assertion.
     */
    public AppConfigurationEntry getAssertionModuleConfiguration() {
        // Pass an option indicating that we're doing identity
        // assertion (vs. authentication) therefore the login module
        // should only check that the user exists (instead of checking
        // the password)
        HashMap options = new HashMap();
        options.put("IdentityAssertion", "true");
        return getConfiguration(options);
    }

    /**
     * Return the principal validator that can validate the
     * principals that the authenticator's login module
     * puts into the subject.
     * <p/>
     * Since the JOSSO authenticator uses the built in
     * WLSUserImpl and WLSGroupImpl principal classes, just
     * returns the built in PrincipalValidatorImpl that knows
     * how to handle these kinds of principals.
     *
     * @return A PrincipalValidator that can validate the
     *         principals that the JOSSO authenticator's login module
     *         puts in the subject.
     */
    public PrincipalValidator getPrincipalValidator() {
        return new PrincipalValidatorImpl();
    }

    /**
     * Returns this providers identity asserter object.
     *
     * @return null since the JOSSO authenticator doesn't
     *         support identity assertion (that is, mapping a token
     *         to a user name).  Do not confuse this with using a
     *         login module in identity assertion mode where the
     *         login module shouldn't try to validate the user.
     */
    public IdentityAsserterV2 getIdentityAsserter() {
        return null;
    }

    /**
     * This implementation returns an array with a GenericServletSSOAgentFilter
     * @see org.josso.servlet.agent.GenericServletSSOAgentFilter
     */
    public Filter[] getServletAuthenticationFilters() {

        return filters;
    }
}

