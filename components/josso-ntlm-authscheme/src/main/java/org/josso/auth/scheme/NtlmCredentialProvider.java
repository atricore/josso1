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
import org.josso.auth.Credential;
import org.josso.auth.CredentialProvider;

/**
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 16, 2008
 * Time: 3:41:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class NtlmCredentialProvider implements CredentialProvider {
    private static final Log logger = LogFactory.getLog(NtlmCredentialProvider.class);

    public static final String DOMAIN_CONTROLLER_CREDENTIAL = "UniAddress";

    public static final String PASSWORD_AUTHENTICATION_CREDENTIAL = "NtlmPasswordAuthentication";

    public Credential newCredential(String name, Object value) {
        if (name.equals(DOMAIN_CONTROLLER_CREDENTIAL))
            return new NtlmDomainControllerCredential(value);
        else if (name.equals(PASSWORD_AUTHENTICATION_CREDENTIAL)) {
            return new NtlmPasswordAuthenticationCredential(value);
        }

        // Don't know how to handle this name ...
        if (logger.isDebugEnabled())
            logger.debug("Unknown credential name: " + name);

        return null;
    }

    public Credential newEncodedCredential(String name, Object value) {
        return newCredential(name, value);
    }

    public static Credential retreiveCredential(String name, Credential[] credentials) {
        for (Credential c : credentials) {
            if (c instanceof NtlmPasswordAuthenticationCredential && name.equals(PASSWORD_AUTHENTICATION_CREDENTIAL))
                return c;
            if (c instanceof NtlmDomainControllerCredential && name.equals(DOMAIN_CONTROLLER_CREDENTIAL))
                return c;
        }

        return null;
    }

    public static String retreiveCredentialName(String name, Credential[] credentials) {
        Credential c = retreiveCredential(name, credentials);

        return c == null ? "" : c.toString();
    }
}