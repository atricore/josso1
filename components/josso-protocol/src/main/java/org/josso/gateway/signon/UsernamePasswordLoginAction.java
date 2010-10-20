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
import org.josso.gateway.SSOGateway;

import javax.servlet.http.HttpServletRequest;

/**
 * This is the login action used with username and password authentication scheme.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: UsernamePasswordLoginAction.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class UsernamePasswordLoginAction extends LoginAction {
    /**
     * Request parameter containing username.
     * Value : sso_username
     */
    public static final String PARAM_JOSSO_USERNAME = "josso_username";

    /**
     * Request parameter containing user password.
     * Value : sso_password
     */
    public static final String PARAM_JOSSO_PASSWORD = "josso_password";

    // private static final Log logger = LogFactory.getLog(UsernamePasswordLoginAction.class);

    /**
     * Creates credentials for username and password, using configuration.
     */
    protected Credential[] getCredentials(HttpServletRequest request) throws SSOAuthenticationException {

        SSOGateway g = getSSOGateway();
        Credential username = g.newCredential(getSchemeName(request), "username", request.getParameter(PARAM_JOSSO_USERNAME));
        Credential password = g.newCredential(getSchemeName(request), "password", request.getParameter(PARAM_JOSSO_PASSWORD));

        Credential[] c = {username, password};

        return c;
    }

    @Override
    protected String getSchemeName(HttpServletRequest request) throws SSOAuthenticationException {
        return "basic-authentication";
    }
}
