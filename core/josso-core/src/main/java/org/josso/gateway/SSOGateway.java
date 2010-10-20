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

import org.josso.SecurityDomain;
import org.josso.auth.Credential;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchDomainException;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;

/**
 * SSO Gateway service interface.
 * <br>
 * <br><strong>Important !</strong> Since JOSSO 1.8 you have to call prepareContext before invoking Gateway operations.
 * <br
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOGateway.java 602 2008-08-20 23:58:11Z gbrigand $
 * @see #prepareSSOContext(SSORequest)
 * @see #prepareSSOContext(String, String)
 * @see #prepareSSOContext(String)
 */
public interface SSOGateway {

    // ---------------------------------------------------------------------------------------------------------
    // TODO !!! Improve SSOGateway service .
    //
    // Now the Gateway is kind of "statefull", you need to call prepareSSOContext before start calling operations.
    // SSOContext is thread local.
    //
    // Transform SSOGateway interface, using SSORequest / SSOResponse types and subtypes to avoid calling prepareSSOContext
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Login a user into the SSO infrastructure.
     *
     * @param credentials that proof user identity.
     * @param scheme      the authentication scheme name to be used for
     *                    logging in the user.
     * @return the user information after login.
     * @throws SSOException               if an error occurs.
     * @throws SSOAuthenticationException if user identity cannot be confirmed.
     */
    SSOSession login(Credential[] credentials, String scheme)
            throws SSOException, SSOAuthenticationException;

    /**
     * Create an authentication assertion based on the supplied credentials. If assertion is successful a new session
     * is created for the subject which can be referenced through the corresponding assertion identifier.
     *
     * @param credentials that proof user identity.
     * @param scheme      the authentication scheme name to be used for
     *                    logging in the user.
     * @return the user information after login.
     * @throws SSOException               if an error occurs.
     * @throws SSOAuthenticationException if user identity cannot be confirmed.
     */
    AuthenticationAssertion assertIdentity(Credential[] credentials, String scheme)
            throws SSOException, SSOAuthenticationException;


    /**
     * Create an authentication assertion from a previous existing and valid one.
     *
     * @param sessionId SSO session identifier for the session to be bound to the new assertion.
     * @return
     * @throws SSOException
     */
    AuthenticationAssertion assertIdentity(String sessionId)
            throws SSOException;

    /**
     * Builds the supplied user credentials for the
     * supplied Authentication Scheme.
     */
    Credential newCredential(String schemeName, String name, Object value)
            throws SSOAuthenticationException;

    /**
     * Obtains the principal name from the given credentials using the
     * supplied Authentication Scheme.
     */
    String getPrincipalName(String schemeName, Credential[] creds)
            throws SSOAuthenticationException;

    /**
     * Logout a user from the SSO infrastructure, user is associated with current SSOContext instance
     *
     * @throws SSOException if an error occurs.
     */
    void logout() throws SSOException;

    /**
     * Finds a user based on session id, the user has to be logged in the SSO infrastructure.
     *
     * @param sessionId
     * @throws SSOException if user was not logged in the SSO.
     */
    SSOUser findUserInSession(String sessionId)
            throws SSOException;

    /**
     * List user's roles base on user's name.
     *
     * @param username
     * @throws SSOException
     */
    SSORole[] findRolesByUsername(String username)
            throws SSOException;

    /**
     * Finds a session given its id.
     */
    SSOSession findSession(String jossoSessionId)
            throws SSOException, NoSuchSessionException;

    /**
     * Initializes this gateway.
     */
    void initialize();

    /**
     * Destroys this instance, free all resources.
     */
    void destroy();

    /**
     * @return true if the gateway was already initialized.
     */
    boolean isInitialized();

    /**
     * Prepares SSOContext for this request.
     *
     * @param ssoRequest the SSORequest instance use to populate SSOContext information.
     * @return
     * @throws NoSuchDomainException
     */
    SSOContext prepareSSOContext(SSORequest ssoRequest) throws NoSuchDomainException;

    /**
     * Prepares SSOContext for the given security token (ssosessionid or authenticationassertionid)
     *
     * @return
     * @throws NoSuchDomainException
     */
    SSOContext prepareSSOContext(String tokenType, String tokenValue);

    /**
     * Prepares SSOContext for the given security domain name.
     *
     * @return
     * @throws NoSuchDomainException
     */
    SSOContext prepareSSOContext(String securityDomainName) throws NoSuchDomainException;

    SSOContext prepareDefaultSSOContext();

    SecurityDomain getSecurityDomain(String name) throws NoSuchDomainException;

    SecurityDomainRegistry getSecurityDomainRegistry();

}
