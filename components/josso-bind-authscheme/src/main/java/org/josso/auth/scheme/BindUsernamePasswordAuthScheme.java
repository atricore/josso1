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
package org.josso.auth.scheme;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.BindableCredentialStore;
import org.josso.auth.CredentialStore;
import org.josso.auth.exceptions.SSOAuthenticationException;

/**
 * Basic authentication scheme, supporting username and password credentials.
 * <p/>
 * <p>
 * This implementation relays on the configured CredentialStore to authenticate users.
 * The configured store must be instance of BindableCredentialStore. If the bind operation provided by the store succeeds,
 * the user is authenticated.
 * </p>
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BindUsernamePasswordAuthScheme.java 543 2008-03-18 21:34:58Z sgonzalez $
 * @see org.josso.auth.CredentialStore
 * @see org.josso.auth.BindableCredentialStore
 * @see org.josso.gateway.identity.service.store.AbstractStore
 *
 * @org.apache.xbean.XBean element="bind-auth-scheme"
 */

public class BindUsernamePasswordAuthScheme extends UsernamePasswordAuthScheme {

    private static final Log logger = LogFactory.getLog(BindUsernamePasswordAuthScheme.class);

    public BindUsernamePasswordAuthScheme() {
        this.setName("bind-authentication");
    }

    /**
     * Authenticates the user using recieved credentials to proof his identity.
     *
     * @return the Principal if credentials are valid, null otherwise.
     */
    public boolean authenticate() throws SSOAuthenticationException {

        setAuthenticated(false);

        String username = getUsername(_inputCredentials);
        String password = getPassword(_inputCredentials);

        // Check if all credentials are present.
        if (username == null || username.length() == 0 ||
                password == null || password.length() == 0) {

            if (logger.isDebugEnabled()) {
                logger.debug("Username " + (username == null || username.length() == 0 ? " not" : "") + " provided. " +
                        "Password " + (password == null || password.length() == 0 ? " not" : "") + " provided.");
            }

            // We don't support empty values !
            return false;
        }

        // hash the password if needed.
        password = createPasswordHash(password);

        // Authenticate the user against the configured store via a bind
        // The configured store could be using a LDAP server , a DB, etc.
        if (((BindableCredentialStore) _credentialStore).bind(username, password)) {

            if (logger.isDebugEnabled())
                logger.debug("[authenticate()], Principal authenticated : " + username);

            // We have successfully authenticated this user.
            setAuthenticated(true);
            return true;
        }

        return false;
    }

    public void setCredentialStore(CredentialStore c) {
        if (c instanceof BindableCredentialStore) {
            super.setCredentialStore(c);
        } else {
            throw new RuntimeException("Invalid credential store type, it must be instace of " + BindableCredentialStore.class.getName());
        }

    }

}
