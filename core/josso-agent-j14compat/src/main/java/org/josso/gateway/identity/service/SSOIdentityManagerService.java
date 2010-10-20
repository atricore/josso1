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

import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;

/**
 * This is the service interface exposed to JOSSO Agents and external JOSSO Identity Service consumers.
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 568 $ $Date: 2008-07-31 15:39:20 -0300 (Thu, 31 Jul 2008) $
 */
public interface SSOIdentityManagerService {

    /**
     * Finds a user based on its name.  The name is a unique identifier of the user in the security domain, probably the user login.
     *
     * @param securityDomain, the security domain associated to the user
     * @param name            the user login name.
     * @throws org.josso.gateway.identity.exceptions.NoSuchUserException
     *          if the user does not exist.
     */
    SSOUser findUser(String requester, String securityDomain, String name)
            throws NoSuchUserException, SSOIdentityException;

    /**
     * Finds the user associated to a sso session
     *
     * @param sessionId the sso session identifier
     * @throws org.josso.gateway.identity.exceptions.NoSuchUserException
     *          if no user is associated to this session id.
     */
    SSOUser findUserInSession(String requester, String sessionId)
            throws NoSuchUserException, SSOIdentityException;

    /**
     * Finds an array of user's roles.
     * Elements in the collection are SSORole instances.
     *
     * @param username
     * @throws org.josso.gateway.identity.exceptions.SSOIdentityException
     *
     */
    SSORole[] findRolesBySSOSessionId(String requester, String username)
            throws SSOIdentityException;

    /**
     * This method validates that the received username matchs an existing user
     *
     * @param username
     * @throws NoSuchUserException  if the user does not exists or is invalid.
     * @throws SSOIdentityException if an error occurs while checking if user exists.
     */
    void userExists(String requester, String securityDomain, String username) throws NoSuchUserException, SSOIdentityException;
}