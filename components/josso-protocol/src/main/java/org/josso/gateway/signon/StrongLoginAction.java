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
package org.josso.gateway.signon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.Credential;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.gateway.SSOGateway;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;

/**
 * Strong Authentication Struts Action which instantiates the Credentials using the
 * X.509 Certificate provided in the Http Request.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: StrongLoginAction.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class StrongLoginAction extends LoginAction {

    private static final Log logger = LogFactory.getLog(StrongLoginAction.class);

    /**
     * Obtain the X.509 Certificate from the Request.
     * <p/>
     * In order for strong authentication to work, the SSL connection must be established
     * in client authentication mode, so that client certificates are sent to the server.
     *
     * @param request
     * @throws SSOAuthenticationException
     */
    protected Credential[] getCredentials(HttpServletRequest request) throws SSOAuthenticationException {

        String cipherSuite = (String) request.getAttribute
                ("javax.servlet.request.cipher_suite");

        if (cipherSuite != null) {
            X509Certificate certChain[] =

                    (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

            if (certChain != null && certChain.length >= 1) {
                SSOGateway g = getSSOGateway();
                Credential x509_certificate = g.newCredential(getSchemeName(request),
                        "userCertificate",
                        certChain[0]
                );
                Credential[] c = {x509_certificate};

                return c;
            } else
                logger.error("No X.509 Certificate Received");
        } else
            logger.error("An SSL Connection is Required to perform Strong Authentication");

        return new Credential[0];
    }

    @Override
    protected String getSchemeName(HttpServletRequest request) throws SSOAuthenticationException {
        return "strong-authentication";
    }
}
