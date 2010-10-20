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
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.NtlmCredentialProvider;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.MutableSSOContext;
import org.josso.gateway.SSOContext;
import org.josso.gateway.protocol.handler.NtlmProtocolHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;


/**
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 14, 2008
 * Time: 3:26:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class NtlmLoginAction extends LoginAction {
    private static final Log logger = LogFactory.getLog(NtlmLoginAction.class);

    /**
     * Creates credentials for username and password, using configuration.
     */
    protected Credential[] getCredentials(HttpServletRequest request) throws SSOAuthenticationException {
        SSOGateway g = getSSOGateway();
        Credential dc = g.newCredential(getSchemeName(request), NtlmCredentialProvider.DOMAIN_CONTROLLER_CREDENTIAL, request.getSession().getAttribute(NtlmProtocolHandler.NTLM_DOMAIN_CONTROLLER));
        Credential pa = g.newCredential(getSchemeName(request), NtlmCredentialProvider.PASSWORD_AUTHENTICATION_CREDENTIAL, request.getSession().getAttribute(NtlmProtocolHandler.NTLM_PASS_AUTHENTICATION));

        return new Credential[]{dc, pa};
    }

    @Override
    protected String getSchemeName(HttpServletRequest request) throws SSOAuthenticationException {
        return "ntlm-authentication";
    }

    /**
     * On authentication error restarts the negotiation
     *
     * @param e
     * @param request
     * @param response
     * @param credentials
     * @return
     * @throws IOException
     */
    protected boolean onLoginAuthenticationException(AuthenticationFailureException e, HttpServletRequest request, HttpServletResponse response, Credential[] credentials) throws IOException {
        HttpSession ssn = request.getSession(true);

        MutableSSOContext ctx = (MutableSSOContext) SSOContext.getCurrent();

        if (ssn.getAttribute(NtlmProtocolHandler.NTLM_PASS_AUTHENTICATION) != null)
            ssn.removeAttribute(NtlmProtocolHandler.NTLM_PASS_AUTHENTICATION);

        try {
            request.setAttribute(NtlmProtocolHandler.NTLM_ERROR_FLAG, "ERROR");
            ctx.getSecurityDomain().getProtocolManager().dispatchRequest(request, response);

            return true;
        } catch (Exception ex) {
            logger.error("  [onLoginAuthenticationException()] " + ex.getMessage(), ex);
        }

        return false;
    }
}