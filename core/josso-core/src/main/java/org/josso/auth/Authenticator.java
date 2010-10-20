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
package org.josso.auth;

import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.AuthenticationScheme;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * The authenticator validates if credentials are valid proof of user identity.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: Authenticator.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface Authenticator {

    /**
     * The is valid method is used to see if credentials are a valid proof of the user identity.
     * The process populates the received subject with all Principals and Credentials.
     *
     * @param credentials that should proof user identity.
     *                    the state of the authenticated Subject.
     * @return the authenticated subject.
     */
    Subject check(Credential[] credentials, String schemeName) throws SSOAuthenticationException;

    /**
     * Builds the supplied user credentials for the
     * supplied Authentication Scheme.
     */
    Credential newCredential(String schemeName, String name, Object value) throws SSOAuthenticationException;

    /**
     * Retunrs the Principal derived from the given credentials.
     */
    Principal getPrincipal(String schemeName, Credential[] credentials);

    /**
     * Sets the configured authentication schemes available to the authenticator.
     *
     * @param as the authentication schemes.
     */
    void setAuthenticationSchemes(AuthenticationScheme[] as);

    /**
     * @return
     */
    AuthenticationScheme[] getAuthenticationSchemes();

    /**
     *
     * @param name
     * @return
     */
    AuthenticationScheme getAuthenticationScheme(String name);

}
