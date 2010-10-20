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
package org.josso.gateway.identity.service.store;

import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;

/**
 * This interface is used by the IdentityManager to transform user information into specific keys.
 * Different persistence mechanims may provide specific adapters to build compatible keys,
 * like JNDI (LDAP) distinguish names, etc.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: IdentityStoreKeyAdapter.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface IdentityStoreKeyAdapter {

    /**
     * Gets the key associated to the given SSO user.
     */
    UserKey getKeyForUser(SSOUser user);

    /**
     * Gets the key associated to the given username.
     */
    UserKey getKeyForUsername(String username);

    /**
     * Gets the key associated to the given SSO Role
     */
    RoleKey getKeyForRole(SSORole role);

    /**
     * Gets the key associated to the given rolename
     */
    RoleKey getKeyForRolename(String rolename);

}
