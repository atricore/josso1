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
package org.josso.gateway.identity.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.SecurityDomain;
import org.josso.auth.Authenticator;
import org.josso.auth.Credential;
import org.josso.auth.SimplePrincipal;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.gateway.MutableSSOContext;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSOException;
import org.josso.gateway.assertion.AssertionManager;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.event.security.SSOSecurityEventManager;
import org.josso.gateway.identity.exceptions.IdentityProvisioningException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.SSOSessionManager;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Set;

/**
 * @org.apache.xbean.XBean element="identity-provider"
 *
 * Default SSO Identity Provider implementation
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id$
 */
public class SSOIdentityProviderImpl implements SSOIdentityProvider {

    private static final Log logger = LogFactory.getLog(SSOIdentityProvider.class);

    public void initialize() {

    }

    ///////////////////////////////////////////////////////////////////
    // Exposed operations to remote clients (i.e. WS Client).
    ///////////////////////////////////////////////////////////////////

    /**
     * Request an authentication assertion using simple authentication through the
     * supplied username/password credentials.
     *
     * @param username
     * @param password
     * @return the assertion identifier
     */
    public String assertIdentityWithSimpleAuthentication(String username, String password)
            throws IdentityProvisioningException {

        try {
            Credential cUsername = null;
            Credential cPassword = null;

            // Prepare the context needed for authenticating
            cUsername = newCredential("basic-authentication", "username", username);
            cPassword = newCredential("basic-authentication", "password", password);

            Credential[] c = {cUsername, cPassword};

            // Perform the assertion and open the corresponding SSO session for the user
            // in case successful
            SSOSession userSsoSession = login(c, "basic-authentication");

            // Return the assertion identifier which maps to the new session
            AssertionManager am = Lookup.getInstance().lookupAssertionManager();
            AuthenticationAssertion aa = am.requestAssertion(userSsoSession.getId());

            return aa.getId();

        } catch (SSOAuthenticationException e) {
            throw new IdentityProvisioningException("Failed to assert identity of user : " + username);
        } catch (SSOException e) {
            throw new IdentityProvisioningException("Error asserting identity of user : " + username);
        } catch (Exception e) {
            throw new IdentityProvisioningException("Unknown error asserting identity of user : " + username);
        }

    }

    /**
     * Resolves an authentication assertion given its identifier.
     */
    public String resolveAuthenticationAssertion(String authenticationAssertionId) throws IdentityProvisioningException {

        try {
            AssertionManager am = Lookup.getInstance().lookupAssertionManager();
            AuthenticationAssertion aa = am.consumeAssertion(authenticationAssertionId);

            if (aa == null) {
                throw new AssertionNotValidException(authenticationAssertionId);
            }

            return aa.getSSOSessionId();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IdentityProvisioningException("Error dereferencing authentication assertion : " +
                    authenticationAssertionId, e
            );
        }

    }

    public void globalSignoff(String sessionId) throws IdentityProvisioningException {

        try {

            MutableSSOContext ctx = (MutableSSOContext) SSOContext.getCurrent();
            SecurityDomain domain = ctx.getSecurityDomain();
            SSOSessionManager sm = domain.getSessionManager();
            SSOSession session = sm.getSession(sessionId);

            ctx.setCurrentSession(session);
            ctx.setUserLocation("remote-application");
            ctx.setScheme("basic-authentication");

            logout();

        } catch (SSOException e) {
            throw new IdentityProvisioningException("Error signing off user with sessin : " + sessionId);
        } catch (Exception e) {
            throw new IdentityProvisioningException("Unknown error signing off user with session : " + sessionId);
        }


    }

    ///////////////////////////////////////////////////////////////////
    // Internal operations used only within the gateway application
    ///////////////////////////////////////////////////////////////////


    /**
     * This method logins a user into de SSO infrastructure.
     *
     * @param cred   the user credentials used as user identity proof.
     * @param scheme the authentication scheme name to be used for logging in the user.
     * @throws AuthenticationFailureException if authentication fails.
     * @throws SSOException                   if an error occurs.
     */
    public SSOSession login(Credential[] cred, String scheme)
            throws SSOException, SSOAuthenticationException {

        SSOContext ctx = SSOContext.getCurrent();

        try {


            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();

            // Configure this ...!
            SSOIdentityManager im = domain.getIdentityManager();
            SSOSessionManager sm = domain.getSessionManager();
            Authenticator au = domain.getAuthenticator();

            // 1. Invalidate current session
            SSOSession currentSession = ctx.getSession();
            if (currentSession != null) {
                try {
                    if (logger.isDebugEnabled())
                        logger.debug("Invalidating existing session : " + currentSession.getId());
                    sm.invalidate(currentSession.getId());
                } catch (Exception e) {
                    logger.warn("Can't ivalidate current session : " + currentSession.getId() + "\n" + e.getMessage(), e);
                }
            }

            // 2. Authenticate using credentials :
            Subject s = au.check(cred, scheme);
            Set principals = s.getPrincipals(SimplePrincipal.class);
            if (principals.size() != 1) {
                // The Set should NEVER be empty or have more than one Principal ...
                // In the future, we could have more than one principal if authenticated with multiple schemes.
                throw new SSOException("Assertion failed : principals.size() != 1");
            }

            // 3. Find SSO User, authentication was successfull and we have only one principal
            // Check the username with the IdentityManager, just to be sure it's a valid user:
            Principal p = (Principal) principals.iterator().next();
            im.userExists(p.getName());

            // 4. Create a new sso session :
            String ssoSessionId = sm.initiateSession(p.getName(), s);
            SSOSession session = sm.getSession(ssoSessionId);

            notifyLoginSuccess(session.getUsername(), session, scheme);

            return session;

        } catch (AuthenticationFailureException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);
            // Re-throw current exception ...
            notifyLoginFailed(cred, scheme, e);
            throw e;


        } catch (SSOAuthenticationException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);
            // Re-throw current exception ...
            notifyLoginFailed(cred, scheme, e);
            throw e;

        } catch (SSOIdentityException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);
            notifyLoginFailed(cred, scheme, e);
            throw new SSOException(e.getMessage(), e);

        } catch (SSOSessionException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);
            notifyLoginFailed(cred, scheme, e);
            throw new SSOException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            notifyLoginFailed(cred, scheme, e);
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
     * @throws AuthenticationFailureException if authentication fails
     * @throws SSOException
     * @throws SSOAuthenticationException
     */
    public AuthenticationAssertion assertIdentity(Credential[] credentials, String scheme) throws SSOException, SSOAuthenticationException {

        SSOContext ctx = SSOContext.getCurrent();

        try {
            SSOSession session;

            session = login(credentials, scheme);
            AssertionManager assertionManager = Lookup.getInstance().lookupAssertionManager();
            return assertionManager.requestAssertion(session.getId());
        } catch (AuthenticationFailureException e) {
            throw e;
        } catch (SSOAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // TODO : Notify assertion failed event
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
            SSOSession session;

            SSOSessionManager sm = Lookup.getInstance().lookupSecurityDomain().getSessionManager();
            AssertionManager assertionManager = Lookup.getInstance().lookupAssertionManager();

            session = sm.getSession(sessionId);

            return assertionManager.requestAssertion(session.getId());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // TODO: Notify assertion failed event
            throw new SSOException(e.getMessage(), e);
        }

    }

    /**
     * Logouts a user from the SSO infrastructure.
     *
     * @throws SSOException
     */
    public void logout() throws SSOException {

        SSOContext ctx = SSOContext.getCurrent();

        SSOSession session = ctx.getSession();
        if (session == null) return;

        String ssoSessionId = session.getId();

        try {
            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            SSOSessionManager sm = domain.getSessionManager();
            sm.invalidate(ssoSessionId);
            notifyLogoutSuccess(session);
        } catch (NoSuchSessionException e) {
            // Ignore this ....
            if (logger.isDebugEnabled())
                logger.debug("[logout()] Session is not valid : " + ssoSessionId);

        } catch (SSOSessionException e) {
            logger.error(e.getMessage(), e);
            notifyLogoutFail(e);
            throw new SSOException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            notifyLogoutFail(e);
            throw new SSOException(e.getMessage(), e);
        }
    }

    protected void notifyLoginFailed(Credential[] credentials, String scheme, Throwable error) {

        SSOContext ctx = SSOContext.getCurrent();

        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireAuthenticationFailureEvent(ctx.getUserLocation(), scheme, credentials, error);

        } catch (Exception e) {
            logger.error("Can't notify login failure : " + e.getMessage(), e);
        }
    }

    protected void notifyLoginSuccess(String username, SSOSession session, String scheme) {

        SSOContext ctx = SSOContext.getCurrent();

        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireAuthenticationSuccessEvent(ctx.getUserLocation(), scheme, username, session.getId());

        } catch (Exception e) {
            logger.error("Can't notify login success : " + e.getMessage(), e);
        }
    }

    private void notifyLogoutFail(Throwable error) {

        SSOContext ctx = SSOContext.getCurrent();

        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireLogoutFailureEvent(ctx.getUserLocation(), ctx.getSession().getUsername(), ctx.getSession().getId(), error);

        } catch (Exception e) {
            logger.error("Can't notify logout failure : " + e.getMessage(), e);
        }
    }

    protected void notifyLogoutSuccess(SSOSession session) {

        SSOContext ctx = SSOContext.getCurrent();

        try {
            // We expect a spetial Event Manager ...
            SSOSecurityEventManager em = (SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager();
            em.fireLogoutSuccessEvent(ctx.getUserLocation(), session.getUsername(), session.getId());

        } catch (Exception e) {
            logger.error("Can't notify logout success : " + e.getMessage(), e);
        }

    }


    protected Credential newCredential(String schemeName, String name, Object value) throws SSOAuthenticationException {
        try {

            SecurityDomain domain = Lookup.getInstance().lookupSecurityDomain();
            Authenticator au = domain.getAuthenticator();

            return au.newCredential(schemeName, name, value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


}
