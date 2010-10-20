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

import org.josso.auth.Credential;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSOException;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.identity.exceptions.IdentityProvisioningException;
import org.josso.gateway.session.SSOSession;


/**
 * SSO Identity Provider Business interface.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id$
 */
public interface SSOIdentityProvider extends java.io.Serializable {

    void initialize();


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
    String assertIdentityWithSimpleAuthentication(String username, String password)
            throws IdentityProvisioningException;

    /**
     * Resolves an authentication assertion given its identifier.
     */
    String resolveAuthenticationAssertion(String authenticationAssertionId)
            throws AssertionNotValidException, IdentityProvisioningException;


    /**
     * Performs a global signoff.
     *
     * @param sessionId
     */
    void globalSignoff(String sessionId)
            throws IdentityProvisioningException;


    ///////////////////////////////////////////////////////////////////
    // Internal operations used only within the gateway application
    ///////////////////////////////////////////////////////////////////

    /**
     * Login a user into the SSO infrastructure.
     *
     * @param credentials that proof user identity.
     * @param scheme      the authentication scheme name to be used for
     *                    logging in the user.
     * @return the user information after login.
     * @throws org.josso.gateway.SSOException if an error occurs.
     * @throws org.josso.auth.exceptions.SSOAuthenticationException
     *                                        if user identity cannot be confirmed.
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
     * Logouts a user from the SSO infrastructure.
     *
     * @throws SSOException
     */
    void logout() throws SSOException;
}
