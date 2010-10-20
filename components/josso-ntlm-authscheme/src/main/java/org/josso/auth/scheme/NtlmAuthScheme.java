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
import org.josso.auth.SimplePrincipal;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.gateway.protocol.handler.NtlmProtocolHandler;
import org.josso.gateway.protocol.handler.ProtocolHandler;
import org.josso.gateway.protocol.SSOProtocolManager;
import org.josso.gateway.MutableSSOContext;
import org.josso.gateway.SSOContext;

import java.security.Principal;

/**
 * Extends basic athentication scheme with domain credential to support NTLM integration
 * <p/>
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 15, 2008
 * Time: 11:02:47 AM
 *
 * @org.apache.xbean.XBean element="ntlm-auth-scheme"
 */
public class NtlmAuthScheme extends AbstractAuthenticationScheme {
    private static final Log logger = LogFactory.getLog(NtlmAuthScheme.class);

    public NtlmAuthScheme() {
        this.setName("ntlm-authentication");
    }
    //------------------------------------------- AbstractAuthenticationScheme Methods
    protected CredentialProvider doMakeCredentialProvider() {
        return new NtlmCredentialProvider();
    }

    //------------------------------------------- AuthenticationScheme Methods
    public boolean authenticate() throws AuthenticationFailureException {
        boolean authenticationSucceded = false;

        MutableSSOContext ctx = (MutableSSOContext) SSOContext.getCurrent();

        try {
            authenticationSucceded = ctx.getSecurityDomain().getProtocolManager().authenticate(_inputCredentials);
            setAuthenticated(authenticationSucceded);
        } catch (AuthenticationFailureException afe) {
            throw afe;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        return authenticationSucceded;
    }

    public Principal getPrincipal() {
        return new SimplePrincipal(NtlmCredentialProvider.retreiveCredentialName(NtlmCredentialProvider.PASSWORD_AUTHENTICATION_CREDENTIAL, _inputCredentials));
    }

    public Principal getPrincipal(Credential[] credentials) {
        return new SimplePrincipal(NtlmCredentialProvider.retreiveCredentialName(NtlmCredentialProvider.PASSWORD_AUTHENTICATION_CREDENTIAL, credentials));
    }

    public Credential[] getPrivateCredentials() {
        Credential c = NtlmCredentialProvider.retreiveCredential(NtlmCredentialProvider.PASSWORD_AUTHENTICATION_CREDENTIAL, _inputCredentials);
        if (c == null)
            return new Credential[0];

        return new Credential[]{c};
    }

    public Credential[] getPublicCredentials() {
        return this.getPrivateCredentials();
    }

}