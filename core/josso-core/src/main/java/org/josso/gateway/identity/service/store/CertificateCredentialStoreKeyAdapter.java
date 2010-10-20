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

import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.CertificatePrincipal;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialStoreKeyAdapter;

/**
 * @org.apache.xbean.XBean element="certificate-key-adapter"
 */
public class CertificateCredentialStoreKeyAdapter implements CredentialStoreKeyAdapter {

    private static final Log logger = LogFactory.getLog(CertificateCredentialStoreKeyAdapter.class);

    public CredentialKey getKeyForPrincipal(Principal p) {
    	if (p instanceof CertificatePrincipal) {
    		return new CertificateUserKey(p.getName(), ((CertificatePrincipal) p).getCertificate());
    	} else {
    		return new CertificateUserKey(p.getName());
    	}
    }

}
