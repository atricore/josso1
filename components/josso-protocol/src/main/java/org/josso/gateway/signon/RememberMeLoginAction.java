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

import org.josso.auth.Credential;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.gateway.*;
import org.josso.gateway.Constants;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 10, 2008
 * Time: 8:19:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class RememberMeLoginAction extends LoginAction {

    private static Log logger = LogFactory.getLog(RememberMeLoginAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        // When this action executes, it alwasy attempts a login.
        prepareContext(request);
        return login(mapping, form, request, response);
    }

    @Override
    protected boolean onLoginAuthenticationException(AuthenticationFailureException e, HttpServletRequest request, HttpServletResponse response, Credential[] credentials) throws IOException {

        logger.debug("Removing cookie with 'JOSSO_REMEMBERME_TOKEN' (login auth exception)");

        // Clear the remember me cookie
        Cookie ssoCookie = new Cookie(org.josso.gateway.Constants.JOSSO_REMEMBERME_TOKEN + "_" + SSOContext.getCurrent().getSecurityDomain().getName(), "-");
        ssoCookie.setMaxAge(0);
        ssoCookie.setSecure(true);
        ssoCookie.setPath("/");

        response.addCookie(ssoCookie);

        return super.onLoginAuthenticationException(e, request, response, credentials);

    }

    @Override
    protected boolean onFatalError(Exception e, HttpServletRequest request, HttpServletResponse response) {

        logger.debug("Removing cookie with 'JOSSO_REMEMBERME_TOKEN' (fatal error)");

        // Clear the remember me cookie
        Cookie ssoCookie = new Cookie(Constants.JOSSO_REMEMBERME_TOKEN + "_" + SSOContext.getCurrent().getSecurityDomain().getName(), "-");
        ssoCookie.setMaxAge(0);
        ssoCookie.setSecure(true);
        ssoCookie.setPath("/");

        response.addCookie(ssoCookie);

        return super.onFatalError(e, request, response);
    }

    @Override
    protected Credential[] getCredentials(HttpServletRequest request) throws SSOAuthenticationException {
        String cipherSuite = (String) request.getAttribute
                ("javax.servlet.request.cipher_suite");

        if (cipherSuite == null) {
            logger.error("An SSL Connection is Required to perform Remember Me Authentication");
        }

        Cookie remembermeTokenCookie = getCookie(request, Constants.JOSSO_REMEMBERME_TOKEN + "_" + SSOContext.getCurrent().getSecurityDomain().getName());
        String remembermeToken = remembermeTokenCookie.getValue();

        if (logger.isDebugEnabled())
            logger.debug("Found 'Remember Me' Token ["+remembermeToken+"]");

        if (remembermeToken != null && remembermeToken.length() >= 1) {

            SSOGateway g = getSSOGateway();
            Credential rememberme = g.newCredential(getSchemeName(request),
                    "remembermeToken", remembermeToken );
            Credential[] c = {rememberme};

            return c;
        } else
            logger.error("No Remember Me Token Received");

        return new Credential[0];
    }

    @Override
    protected String getSchemeName(HttpServletRequest request) throws SSOAuthenticationException {
        return "rememberme-authentication";
    }
}
