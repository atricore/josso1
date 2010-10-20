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

import org.josso.gateway.identity.exceptions.IdentityProvisioningException;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;

/**
 * This is the service interface exposed to JOSSO Agents and external JOSSO Identity Service consumers.
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 568 $ $Date: 2008-07-31 15:39:20 -0300 (Thu, 31 Jul 2008) $
 */
public interface SSOIdentityProviderService {

    /**
     * Request an authentication assertion using simple authentication through the
     * supplied username/password credentials.
     *
     * @param username
     * @param password
     * @return the assertion identifier
     */
    String assertIdentityWithSimpleAuthentication(String requester, String securityDomain, String username, String password) throws IdentityProvisioningException;

    /**
     * Resolves an authentication assertion given its identifier.
     */
    String resolveAuthenticationAssertion(String requester, String authenticationAssertionId) throws AssertionNotValidException, IdentityProvisioningException;

    /**
     * Performs a global signoff.
     *
     * @param sessionId
     */
    void globalSignoff(String requester, String sessionId) throws IdentityProvisioningException;
}