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
import org.josso.Lookup;
import org.josso.SecurityDomain;
import org.josso.auth.Authenticator;
import org.josso.auth.Credential;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.AssertionManager;
import org.josso.gateway.event.security.SSOSecurityEventManager;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchDomainException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManager;
import org.josso.gateway.identity.service.SSOIdentityProvider;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.SSOSessionManager;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the default SSO Gateway implementation.
 *
 * @org.apache.xbean.XBean element="gateway"
 * description="JOSSO Gateway implementation"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOGatewayImpl.java 620 2008-09-23 13:40:36Z sgonzalez $
 */
public class SSOGatewayImpl implements SSOGateway {

    private static final Log logger = LogFactory.getLog(SSOGatewayImpl.class);

    private boolean _initialized;

    private List<SecurityDomain> securityDomains = new ArrayList<SecurityDomain>();

    private SSOSecurityDomainSelector securityDomainSelector;

    private SecurityDomainRegistry securityDomainRegistry;

    public SSOGatewayImpl() {

    }

    /**
     * Initializes the SSO gateway.
     * Default implementations of optionall components will be used if necessary.
     * <p/>
     * The proper security domain name will be configured for specific components.
     */
    public synchronized void initialize() {

        if (_initialized) {
            throw new IllegalStateException("SSOGateway already initialized");
        }

        // Perform some initialization logic, default implementations of optionall components will be used when necessary

        // ------------------------------------------------------------------------------------------
        // Finishing Gateway seutp !!
        // -------------------------------------------------------------------------------------------

        // Check security domain selector
        if (securityDomainSelector == null) {
            logger.info("Finishing setup, using default SecurityDomainSelector");

            try {
                securityDomainSelector = (SSOSecurityDomainSelector) loadClass("org.josso.gateway.DomainSelectorImpl").newInstance();
            } catch (Exception e) {
                logger.error("Cannot instantiate default security domain selector : " + e.getMessage(), e);
            }

        }

        // Check security domain registry
        if (securityDomainRegistry == null) {
            logger.info("Finishing setup, using default SecurityDomainRegistry");
            try {
                securityDomainRegistry = (SecurityDomainRegistry) loadClass("org.josso.gateway.SecurityDomainRegistryImpl").newInstance();
            } catch (Exception e) {
                logger.error("Cannot instantiate default security domain registry : " + e.getMessage(), e);
            }
        }


        for (int i = 0; i < securityDomains.size(); i++) {
            SecurityDomain sd = securityDomains.get(i);

            String name = sd.getName();

            // Check assertion manager
            if (sd.getIdentityProvider() == null) {
                logger.info("Finishing setup [" + name + "], using default IdentityProvider");
                try {
                    sd.setIdentityProvider((SSOIdentityProvider) loadClass("org.josso.gateway.identity.service.SSOIdentityProviderImpl").newInstance());
                } catch (Exception e) {
                    logger.error("Cannot create default identity provider : " + e.getMessage(), e);
                }
            }

            // Check assertion manager
            if (sd.getAssertionManager() == null) {
                logger.info("Finishing setup [" + name + "], using default AssertionManager");
                try {
                    sd.setAssertionManager((AssertionManager) loadClass("org.josso.gateway.assertion.AssertionManagerImpl").newInstance());
                } catch (Exception e) {
                    logger.error("Cannot create default assertion manager : " + e.getMessage(), e);
                }
            }

            // Check SecurityDomain Matcher
            if (sd.getMatchers().size() == 0) {
                logger.info("Finishing setup [" + name + "], using default SecurityDomainMatcher");
                try {
                    sd.getMatchers().add((SecurityDomainMatcher) loadClass("org.josso.gateway.SimpleSecurityDomainMatcher").newInstance());
                } catch (Exception e) {
                    logger.error("Cannot instantiate default security domain matcher : " + e.getMessage(), e);
                }
            }


        }

        // ------------------------------------------------------------------------------------------
        // Initializing components
        // -------------------------------------------------------------------------------------------

        for (int i = 0; i < securityDomains.size(); i++) {
            SecurityDomain sd = securityDomains.get(i);

            String name = sd.getName();

            securityDomainRegistry.register(sd);

            // This compoment should be initialized first ...
            logger.info("Initializing [" + name + " ] SSOEventManager ...");
            sd.getEventManager().initialize();
            logger.info("Initializing [" + name + " ] SSOEventManager ... DONE");

            logger.info("Initializing [" + name + " ] SSOAssertionManager ...");
            sd.getAssertionManager().setSecurityDomainName(name);
            sd.getAssertionManager().initialize();
            logger.info("Initializing [" + name + " ] SSOAssertionManager ... DONE");

            logger.info("Initializing [" + name + " ] SSOAssertionManager ...");
            sd.getIdentityProvider().initialize();
            logger.info("Initializing [" + name + " ] SSOAssertionManager ... DONE");

            logger.info("Initializing [" + name + " ] SSOIdentityManager ...");
            sd.getIdentityManager().initialize();
            logger.info("Initializing [" + name + " ] SSOIdentityManager ... DONE");

            logger.info("Initializing [" + name + " ] SSOSessionManager ...");
            sd.getSessionManager().setSecurityDomainName(name);
            sd.getSessionManager().initialize();
            logger.info("Initializing [" + name + " ] SSOSessionManager ... DONE");

            logger.info("Initializing [" + name + " ] SSOAuditManager ...");
            sd.getAuditManager().initialize();
            logger.info("Initializing [" + name + " ] SSOAuditManager ... DONE");

            if (sd.getProtocolManager() != null) {
                logger.info("Initializing [" + name + " ] SSOProtocolManager ...");
                sd.getProtocolManager().initialize();
                logger.info("Initializing [" + name + " ] SSOProtocolManager ... DONE");
            }

            for (SecurityDomainMatcher matcher : sd.getMatchers()) {
                logger.info("Initializing [" + name + " ] SecurityDomainMatcher ...");
                matcher.init();
                logger.info("Initializing [" + name + " ] SecurityDomainMatcher ... DONE");
            }

            if (sd.getSSOWebConfiguration().getTrustedHosts().size() == 0) {
                logger.warn("No trusted hosts defined, any 'back_to' value will be accepted!");
            } else {
                for (String trustedHost : sd.getSSOWebConfiguration().getTrustedHosts()) {
                    logger.info("Trusted HOST : [" + trustedHost + "]");
                }
            }
        }

        _initialized = true;
    }

    public boolean isInitialized() {
        return _initialized;
    }

    public void destroy() {
        for (int i = 0; i < securityDomains.size(); i++) {
            SecurityDomain sd = securityDomains.get(i);
            String name = sd.getName();

            logger.info("Destroying [" + name + " ] SSOAssertionManager ...");
            sd.getAssertionManager().destroy();
            logger.info("Destroying [" + name + " ] SSOAssertionManager ... DONE");

            logger.info("Destroying [" + name + " ] SSOSessionManager ...");
            sd.getSessionManager().destroy();
            logger.info("Destroying [" + name + " ] SSOSessionManager ... DONE");
        }

        _initialized = false;
    }


    public SSOContext prepareSSOContext(SSORequest req) throws NoSuchDomainException {
        MutableSSOContext ctx = new MutableSSOContext();
        ctx.setUserLocation(req.getUserLocation());

        // TODO : This properties should be populated later ... or based on ssorequest ?!
        ctx.setScheme("unknown");
        ctx.setSecurityDomain(this.securityDomainSelector.selectDomain(req, securityDomains));

        if (logger.isDebugEnabled())
            logger.debug("SSOContext created from request for SecurityDomain " + (ctx.getSecurityDomain() != null ? ctx.getSecurityDomain().getName() : "null"));

        return ctx;
    }

    public SSOContext prepareSSOContext(String tokenType, String tokenValue) {

        if (tokenType == null || "".equals(tokenType)) {
            throw new IllegalArgumentException("TokenType cannot be null");
        }
        if (tokenValue == null || "".equals(tokenValue)) {
            throw new IllegalArgumentException("TokenValue cannot be null");
        }

        SecurityDomain sd = this.securityDomainRegistry.lookup(tokenType, tokenValue);
        if (sd == null) {
            sd = prepareDefaultSSOContext().getSecurityDomain();
            logger.debug("No security domain found for token : [" + tokenType + "/" + tokenValue + "], using default one : " + sd.getName());
        }

        MutableSSOContext ctx = new MutableSSOContext();
        ctx.setSecurityDomain(sd);

        if (logger.isDebugEnabled())
            logger.debug("SSOContext created from token " + tokenType + "/" + tokenValue + " for SecurityDomain " + (ctx.getSecurityDomain() != null ? ctx.getSecurityDomain().getName() : "null"));

        if (tokenType.equals(SSOSessionManager.TOKEN_TYPE)) {
        	try {
        		SSOSessionManager sm = sd.getSessionManager();
        		SSOSession session = sm.getSession(tokenValue);
				ctx.setCurrentSession(session);
			} catch (Exception e) {
				// ignore all errors
			}
        }
        
        return ctx;
    }

    public SSOContext prepareSSOContext(String securityDomainName) throws NoSuchDomainException {

        if (securityDomainName == null) {
            throw new IllegalArgumentException("SecurityDomanName cannot be null");
        }
        MutableSSOContext ctx = new MutableSSOContext();
        SecurityDomain sd = getSecurityDomain(securityDomainName);
        ctx.setSecurityDomain(sd);

        return ctx;
    }

    public SSOContext prepareDefaultSSOContext() {

        MutableSSOContext ctx = new MutableSSOContext();
        SecurityDomain sd = securityDomains.get(0);
        ctx.setSecurityDomain(sd);

        return ctx;
    }


    public SecurityDomain getSecurityDomain(String name) throws NoSuchDomainException {
        for (int i = 0; i < securityDomains.size(); i++) {
            SecurityDomain securityDomain = securityDomains.get(i);
            if (securityDomain.getName().equals(name))
                return securityDomain;
        }

        throw new NoSuchDomainException(name);
    }


    /**
     * This method logins a user into de SSO infrastructure.
     *
     * @param cred   the user credentials used as user identity proof.
     * @param scheme the authentication scheme name to be used for logging in the user.
     * @throws SSOAuthenticationException if authentication fails.
     * @throws SSOException               if an error occurs.
     */
    public SSOSession login(Credential[] cred, String scheme)
            throws SSOException, SSOAuthenticationException {

        try {
            SSOIdentityProvider ip = Lookup.getInstance().lookupSSOIdentityProvider();

            return ip.login(cred, scheme);

        } catch (AuthenticationFailureException e) {
            throw e;
        } catch (SSOAuthenticationException e) {
            throw e;
        } catch (SSOIdentityException e) {
            throw new SSOException(e.getMessage(), e);
        } catch (SSOSessionException e) {
            throw new SSOException(e.getMessage(), e);
        } catch (Exception e) {
            throw new SSOException(e.getMessage(), e);
        }
    }

    /**
     * Create an authentication assertion based on the supplied credentials. If assertion is successful a new session
     * is created for the subject which can be referenced through the corresponding assertion identifier.
     *
     * @param credentials
     * @param scheme
     * @return
     * @throws SSOException
     * @throws SSOAuthenticationException
     */
    public AuthenticationAssertion assertIdentity(Credential[] credentials, String scheme)
            throws SSOException, SSOAuthenticationException {

        try {
            SSOIdentityProvider ip = Lookup.getInstance().lookupSSOIdentityProvider();
            return ip.assertIdentity(credentials, scheme);
        } catch (AuthenticationFailureException e) {
            throw e;
        } catch (SSOAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new SSOException(e.getMessage(), e);
        }

    }

    /**
     * Create an authentication assertion from a previous existing and valid one.
     *
     * @param sessionId SSO session identifier for the session to be bound to the new assertion.
     * @return
     * @throws SSOException
     */
    public AuthenticationAssertion assertIdentity(String sessionId)
            throws SSOException {

        try {
        	if (sessionId == null || sessionId.equals("")) {
        		throw new SSOException("Session ID is empty!");
        	}
            SSOIdentityProvider ip = Lookup.getInstance().lookupSSOIdentityProvider();
            return ip.assertIdentity(sessionId);
        } catch (Exception e) {
            throw new SSOException(e.getMessage(), e);
        }

    }

    /**
     * Logouts a user from the SSO infrastructure.
     *
     * @throws SSOException
     */
    public void logout() throws SSOException {

        try {

            SSOIdentityProvider ip = Lookup.getInstance().lookupSSOIdentityProvider();
            ip.logout();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOException(e.getMessage(), e);
        }
    }


    public Credential newCredential(String schemeName, String name, Object value) throws SSOAuthenticationException {
        try {

            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            Authenticator au = domain.getAuthenticator();

            return au.newCredential(schemeName, name, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String getPrincipalName(String schemeName, Credential[] creds) throws SSOAuthenticationException {
        try {

            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            Authenticator au = domain.getAuthenticator();

            Principal p = au.getPrincipal(schemeName, creds);
            if (p != null)
                return p.getName();


        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
        return null;
    }


    /**
     * Finds a user associated to the given session.
     *
     * @param sessionId
     * @throws SSOException
     */
    public SSOUser findUserInSession(String sessionId)
            throws SSOException {
        try {

        	if (sessionId == null || sessionId.equals("")) {
        		throw new NoSuchSessionException("Session ID is empty!");
        	}
            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            SSOIdentityManager im = domain.getIdentityManager();
            return im.findUserInSession(sessionId);

        } catch (NoSuchSessionException e) {
            // Session is not valid ... (we could signal it with a specific exception)
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);

            throw new SSOException(e.getMessage(), e);

        } catch (SSOIdentityException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);

            throw new SSOException(e.getMessage(), e);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

            throw new SSOException(e.getMessage(), e);
        }
    }

    public SSORole[] findRolesByUsername(String username) throws SSOException {
        try {
        	
        	if (username == null || username.equals("")) {
        		throw new SSOIdentityException("Username is empty!");
        	}
            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            SSOIdentityManager im = domain.getIdentityManager();
            return im.findRolesByUsername(username);

        } catch (SSOIdentityException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);

            throw new SSOException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOException(e.getMessage(), e);
        }
    }

    public SSOSession findSession(String jossoSessionId) throws SSOException, NoSuchSessionException {
        try {
        	if (jossoSessionId == null || jossoSessionId.equals("")) {
        		throw new NoSuchSessionException("Session ID is empty!");
        	}
            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            SSOSessionManager sm = domain.getSessionManager();
            return sm.getSession(jossoSessionId);
        } catch (NoSuchSessionException e) {
            throw e;

        } catch (SSOIdentityException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);

            throw new SSOException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOException(e.getMessage(), e);
        }
    }

    protected void notifyLoginFailed(SSOContext ctx, Credential[] credentials, String scheme, Throwable error) {
        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireAuthenticationFailureEvent(ctx.getUserLocation(), scheme, credentials, error);

        } catch (Exception e) {
            logger.error("Can't notify login failure : " + e.getMessage(), e);
        }
    }

    protected void notifyLoginSuccess(SSOContext ctx, String username, SSOSession session, String scheme) {
        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireAuthenticationSuccessEvent(ctx.getUserLocation(), scheme, username, session.getId());

        } catch (Exception e) {
            logger.error("Can't notify login success : " + e.getMessage(), e);
        }
    }

    private void notifyLogoutFail(SSOContext ctx, Throwable error) {
        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireLogoutFailureEvent(ctx.getUserLocation(), ctx.getSession().getUsername(), ctx.getSession().getId(), error);

        } catch (Exception e) {
            logger.error("Can't notify login success : " + e.getMessage(), e);
        }
    }

    protected void notifyLogoutSuccess(SSOContext ctx) {
        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireLogoutSuccessEvent(ctx.getUserLocation(), ctx.getSession().getUsername(), ctx.getSession().getId());

        } catch (Exception e) {
            logger.error("Can't notify login success : " + e.getMessage(), e);
        }

    }


    public SSOSecurityDomainSelector getSecurityDomainSelector() {
        return securityDomainSelector;
    }

    /**
     * @org.apache.xbean.Property alias="sso-domain-selector"
     *
     * @param securityDomainSelector
     */
    public void setSecurityDomainSelector(SSOSecurityDomainSelector securityDomainSelector) {
        this.securityDomainSelector = securityDomainSelector;
    }

    /**
     * @org.apache.xbean.Property alias="sso-domains-registry"
     *
     * @return
     */
    public SecurityDomainRegistry getSecurityDomainRegistry() {
        return securityDomainRegistry;
    }

    public void setSecurityDomainRegistry(SecurityDomainRegistry securityDomainRegistry) {
        this.securityDomainRegistry = securityDomainRegistry;
    }

    public void setSecurityDomains(List<SecurityDomain> sds) {
        securityDomains = sds;

        if (_initialized) {
            for (SecurityDomain sd : sds) {
                this.securityDomainRegistry.register(sd);
            }
        }

    }

    /**
     * @org.apache.xbean.Property alias="domains" nestedType="org.josso.SecurityDomain"
     */
    public List<SecurityDomain> getSecurityDomains() {
        return securityDomains;
    }

    protected Class loadClass(String fqcn) throws ClassNotFoundException {

        Class c = null;

        try {
            c = this.getClass().getClassLoader().loadClass(fqcn);
            return c;
        } catch (ClassNotFoundException e) {

        }

        try {
            c = Thread.currentThread().getContextClassLoader().loadClass(fqcn);
            return c;
        } catch (ClassNotFoundException e) {

        }

        try {
            Class.forName(fqcn);
            return c;
        } catch (ClassNotFoundException e) {

        }


        throw new ClassNotFoundException(fqcn);
    }

}
