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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class X509CertificateCredentialProvider implements CredentialProvider {
    private static final Log logger = LogFactory.getLog(X509CertificateCredentialProvider.class);

    /**
     * The name of the credential representing an X.509 Certificate.
     * Used to get a new credential instance based on its name and value.
     * Value : userCertificate
     *
     * @see Credential newCredential(String name, Object value)
     */
    private final static String X509_CERTIFICATE_CREDENTIAL_NAME = "userCertificate";

    public Credential newCredential(String name, Object value) {

        if (name.equalsIgnoreCase(X509_CERTIFICATE_CREDENTIAL_NAME)) {

            if (value instanceof X509Certificate)
                return new X509CertificateCredential(value);
            else if (value instanceof String) {
                X509Certificate cert = buildX509Certificate((String) value);
                return new X509CertificateCredential(cert);
            } else {
                X509Certificate cert = buildX509Certificate((byte[]) value);
                return new X509CertificateCredential(cert);
            }
        }

        // Don't know how to handle this name ...
        if (logger.isDebugEnabled())
            logger.debug("Unknown credential name : " + name);

        return null;
    }

    public Credential newEncodedCredential(String name, Object value) {
        return newCredential(name, value);
    }

    private X509Certificate buildX509Certificate(byte[] binaryCert) {
        X509Certificate cert = null;

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(binaryCert);
            CertificateFactory cf =
                    CertificateFactory.getInstance("X.509");

            cert = (X509Certificate) cf.generateCertificate(bais);

            if (logger.isDebugEnabled())
                logger.debug("Building X.509 certificate result :\n " + cert);

        } catch (CertificateException ce) {
            logger.error("Error instantiating X.509 Certificate", ce);
        }

        return cert;
    }

    private X509Certificate buildX509Certificate(String plainCert) {
        return buildX509Certificate(plainCert.getBytes());
    }

}
