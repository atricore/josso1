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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialStoreKeyAdapter;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;

import java.security.Principal;

/**
 * @org.apache.xbean.XBean element="simple-key-adapter"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SimpleIdentityStoreKeyAdapter.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class SimpleIdentityStoreKeyAdapter implements IdentityStoreKeyAdapter, CredentialStoreKeyAdapter {

    private static final Log logger = LogFactory.getLog(SimpleIdentityStoreKeyAdapter.class);

    public UserKey getKeyForUsername(String username) {
        return new SimpleUserKey(username);
    }

    public UserKey getKeyForUser(SSOUser user) {
        return new SimpleUserKey(user.getName());
    }

    public RoleKey getKeyForRole(SSORole role) {
        return new SimpleRoleKey(role.getName());
    }

    public RoleKey getKeyForRolename(String rolename) {
        return new SimpleRoleKey(rolename);
    }

    public CredentialKey getKeyForPrincipal(Principal p) {
        return new SimpleUserKey(p.getName());
    }

}
