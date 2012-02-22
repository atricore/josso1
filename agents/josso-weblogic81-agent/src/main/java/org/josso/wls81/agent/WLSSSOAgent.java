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
 */

package org.josso.wls81.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.Constants;
import org.josso.servlet.agent.GenericServletSSOAgentRequest;
import weblogic.servlet.security.ServletAuthentication;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;

/**
 * Weblogic SSO Agent implementation, it will create Weblogic security context.
 *
 * Date: Nov 27, 2007
 * Time: 11:08:18 AM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class WLSSSOAgent extends HttpSSOAgent {

    private static final String JOSSO_LOGIN_URI = "/josso-wls/josso_login.jsp";

    private static final String JOSSO_USER_LOGIN_URI = "/josso-wls/josso_user_login.jsp";
    
    private static final String JOSSO_SECURITY_CHECK_URI = "/josso-wls/josso_security_check.jsp";

    private static final String JOSSO_LOGOUT_URI = "/josso-wls/josso_logout.jsp";

    private static final String JOSSO_AUTHENTICATION_URI = "/josso-wls/josso_authentication.jsp";

    private static Log logger = LogFactory.getLog(WLSSSOAgent.class);

    public void start() {
        super.start();
    }

    /**
     * This method builds a login URL based on a HttpServletRequest.  The url contains all necessary parameters
     * required by the front-channel part of the SSO protocol.
     */
    public String buildLoginUrl(HttpServletRequest hreq) {
        String loginUrl =  getGatewayLoginUrl();
        String onErrorUrl = getGatewayLoginErrorUrl();

        String backto = buildBackToURL(hreq, "/josso-wls/josso_security_check.jsp");

        loginUrl = loginUrl + "?josso_back_to=" + backto;

        // Add login URL parameters
        loginUrl += buildLoginUrlParams(hreq);

        return loginUrl;
    }

    /**
     * By default we do require to authenticate all requests.
     */
    protected boolean isAuthenticationAlwaysRequired() {
        return true;
    }

    protected Principal authenticate(SSOAgentRequest request) {

        if (logger.isDebugEnabled())
            logger.debug("Authenticating SSO Agent request ... ");

        try {
            GenericServletSSOAgentRequest r = (GenericServletSSOAgentRequest) request;
            String ssoSessionId = r.getSessionId();

            int result = ServletAuthentication.login(ssoSessionId, ssoSessionId, r.getRequest());

            if (logger.isDebugEnabled())
                logger.debug("Authenticating SSO Agent request : " + result);

            if (result == ServletAuthentication.AUTHENTICATED) {

                Principal p = this.getSSOIdentityManager().findUserInSession(request.getRequester(), ssoSessionId);

                if (logger.isDebugEnabled())
                    logger.debug("WLS Principal is " + p.getName());

                return p;
            }

        } catch (LoginException e) {
            logger.warn(e.getMessage());

            if (logger.isDebugEnabled())
                logger.debug(e, e);
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (logger.isDebugEnabled())
                logger.debug(e, e);
            return null;
        }

        return null;
        
    }

    /**
     * Log messages to common logging infrastructrue
     * @param message
     */
    protected void log(String message) {
        // TODO : Can we use a BEA Logger ?!
        logger.info(message);
    }

    /**
     * Log messages to common logging infrastructrue
     * @param message
     */
    protected void log(String message, Throwable throwable) {
        logger.info(message, throwable);
    }


    public String getJossoLoginUri() {
        return JOSSO_LOGIN_URI;
    }

    public String getJossoUserLoginUri() {
        return JOSSO_USER_LOGIN_URI;
    }
    
    public String getJossoSecurityCheckUri() {
        return JOSSO_SECURITY_CHECK_URI;
    }

    public String getJossoLogoutUri() {
        return JOSSO_LOGOUT_URI;
    }

    public String getJossoAuthenticationUri() {
        return JOSSO_AUTHENTICATION_URI;
    }
}
