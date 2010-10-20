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

import java.security.cert.X509Certificate;

import org.josso.gateway.identity.exceptions.SSOIdentityException;

/**
 * This is a resource that provides credential storage functionallity.  It's used to decouple
 * authentication schemes from credential persistence mechanisms.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: CredentialStore.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface CredentialStore {

    /**
     * Load credentials from store.
     *
     * @param key the key used to retrieve credentials from store.
     * @throws SSOIdentityException
     */
    Credential[] loadCredentials(CredentialKey key, CredentialProvider cp) throws SSOIdentityException; // This is the wrong exception !

    /**
     * Load user UID (username) from store.
     *
     * @param key the key used to load UID from store.
     * @param cp credential provider
     * @throws SSOIdentityException
     */
    String loadUID(CredentialKey key, CredentialProvider cp) throws SSOIdentityException;
}
