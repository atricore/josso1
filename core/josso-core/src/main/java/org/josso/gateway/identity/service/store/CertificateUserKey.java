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

import java.security.cert.X509Certificate;

/**
 * This UserKey uses a String as identifier as well as the Certificate backing it.
 */
public class CertificateUserKey extends SimpleUserKey {

    private X509Certificate _certificate;

    public CertificateUserKey(String id) {
        super(id);
    }

    public CertificateUserKey(String id, X509Certificate certificate) {
        super(id);
        _certificate = certificate;
    }

    public X509Certificate getCertificate() {
    	return _certificate;
    }
    
    /**
     * Compare this CertificateUserKey against another CertificateUserKey.
     *
     * @return true if id and certificate equals another.getId() and another.getCertificate()
     */
    public boolean equals(Object another) {
    	if (!(another instanceof CertificateUserKey)) {
            return false;
        }

        X509Certificate anotherCertificate = ((CertificateUserKey) another).getCertificate();

        boolean equals = super.equals(another);
        if (!equals) {
        	return false;
        }
        
        if (getCertificate() == null) {
            equals = anotherCertificate == null;
        } else {
            equals = getCertificate().equals(anotherCertificate);
        }

        return equals;
    }

    /**
     * Returns the hashcode of the certificate
     */
    public int hashCode() {
    	return (getCertificate() == null ? 0 : getCertificate().hashCode());
    }

    public String toString() {
    	if (getCertificate() != null) {
    		return getCertificate().getSubjectX500Principal().getName() + " / " + 
    				getCertificate().getIssuerX500Principal().getName();
    	} else {
    		return getId();
    	}
    }
}
