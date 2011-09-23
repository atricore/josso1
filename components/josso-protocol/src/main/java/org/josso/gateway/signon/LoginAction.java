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


import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.*;
import org.josso.Lookup;
import org.josso.SecurityDomain;
import org.josso.auth.Credential;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.SSOWebConfiguration;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.session.SSOSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Base login action extended by concrete actions associated to specific authentication schemes.
 * This actions controls the auth. process and invokes subclasses methods (template method).
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: LoginAction.java 612 2008-08-22 12:17:20Z gbrigand $
 * @revision 07/05/2008 ajadzinsky
 */

public abstract class LoginAction extends SignonBaseAction {

    public static final String JOSSO_CMD_LOGIN = "login";

    private static final Log logger = LogFactory.getLog(LoginAction.class);

    /**
     * Executes the proper login method based on sso_command request parameter.
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        if (logger.isDebugEnabled())
            logger.debug("JOSSO Command : [cmd="+getSSOCmd(request)+"]");

        /**
         * SSO Context needs to resolve security domain
         */
        prepareContext(request);

        // Get current SSO Command ...
        String cmd = getSSOCmd(request);

        // Validate BACK TO URL to avoid XSR exploit
        String backTo = getBackTo(request);
        if (backTo != null) {

            backTo = backTo.toLowerCase();

            SecurityDomain domain = SSOContext.getCurrent().getSecurityDomain();
            SSOWebConfiguration cfg = domain.getSSOWebConfiguration();

            boolean trusted = false;
            if (cfg.getTrustedHosts().size() > 0) {
            	String backToHost = null;
            	if (backTo.startsWith("http://") || backTo.startsWith("https://")) {
            		try {
		            	URL backToUrl = new URL(backTo);
		            	backToHost = backToUrl.getHost();
		            	backToHost = backToHost.substring(backToHost.lastIndexOf("@")+1);
            		} catch (MalformedURLException e) {
            			if (logger.isDebugEnabled())
            	            logger.debug("BackTo URL is malformed : [backTo=" + backTo + "]");
        			}
            	}
            	for (String trustedHost : cfg.getTrustedHosts()) {
    	    		if (StringUtils.isNotBlank(trustedHost) && trustedHost.equals(backToHost)) {
    					trusted = true;
    					break;
    				}
                }
            }

            if (!trusted && cfg.getTrustedHosts().size() > 0) {

                logger.warn("Attempt to use untrusted host in back_to URL " + backTo);
                
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                // Add non-cache headers


                return null;
            }
        }


        if (canRelay(request))
            return relay(mapping, form, request, response);

        // If no command was specified, "ask-for-login" is the default value.
        if (cmd == null) {
            return askForLogin(mapping, form, request, response);
        }

        // All other commands mean "perform-login"
        return login(mapping, form, request, response);


    }

    /**
     * Ask the user for login information.
     */
    protected ActionForward askForLogin(ActionMapping mapping,
                                        ActionForm form,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {

        try {

            // Ask user for login information.
            SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

            String loginUrl = cfg.getCustomLoginURL();
            String backTo = getBackTo(request);

            if (loginUrl != null) {

            	if (backTo != null) {
            		loginUrl += (loginUrl.indexOf("?") >= 0 ? "&" : "?") + "josso_back_to=" + backTo;
            	}
            	
                // The authentication interface is not the default ...
                if (logger.isDebugEnabled())
                    logger.debug("Redirecting to custom login : " + loginUrl);

                response.sendRedirect(response.encodeRedirectURL(loginUrl));
                return null; // No action forward needed, we

            }

            return mapping.findForward("login-page");

        } catch (Exception e) {
            if (this.onFatalError(e, request, response))
                return null;

            return mapping.findForward("error");
        }
    }


    /**
     * Logins the user in the SSO infrastructure
     */
    protected ActionForward login(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        try {

            SSOGateway gwy = getSSOGateway();

            Credential[] c = getCredentials(request);

            try {
                // 1 - Handle Outbound relaying by generating an assertion for the authentication request
                SSOContext ctx = SSOContext.getCurrent();
                AuthenticationAssertion authAssertion = gwy.assertIdentity(c, ctx.getScheme());

                String sessionId = authAssertion.getSSOSessionId();
                SSOSession session = gwy.findSession(sessionId);

                // Cookie ssoCookie = newJossoCookie(request.getContextPath(), session.getProcessId());
                // response.addCookie(ssoCookie);

                storeSSOInformation(request, response, session);

                if (logger.isDebugEnabled())
                    logger.debug("[login()], authentication successfull.");


                // 2 - Restore BACK TO URL ...
                String back_to = this.getBackTo(request, session, authAssertion);
                if (back_to == null) {

                    // Return to controller, if we do not have a back-to url, add more information to the context ;)
                    SSOUser user = gwy.findUserInSession(sessionId);
                    SSORole[] roles = gwy.findRolesByUsername(user.getName());

                    // so that pages can access this bean
                    request.setAttribute(KEY_JOSSO_SESSION, session);
                    request.setAttribute(KEY_JOSSO_USER, user);
                    request.setAttribute(KEY_JOSSO_USER_ROLES, roles);

                    return mapping.findForward("login-result");
                }


                // If authentication succeds, remove al SSO session data.
                this.clearSSOParameters(request);

                // We're going back to the partner app.
                if (logger.isDebugEnabled())
                    logger.debug("[login()], Redirecting user to : " + back_to);

                response.sendRedirect(response.encodeRedirectURL(back_to));

                return null; // No forward is needed, we perfomed a 'sendRedirect'.

            } catch (AuthenticationFailureException e) {

                if (logger.isDebugEnabled())
                    logger.debug("[AuthenticationFailureException] " + e.getMessage(), e);

                // logs the error
                ActionErrors errors = new ActionErrors();
                errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("sso.login.failed"));
                saveErrors(request, errors);

                // Invalid login attempt, redirect to ON ERROR URL, if any.
                boolean ok = this.onLoginAuthenticationException(e, request, response, c);
                if (ok) {
                    return null; // No forward is needed, we perfomed a 'sendRedirect'.
                }

                SSOWebConfiguration cfg = SSOContext.getCurrent().getSecurityDomain().getSSOWebConfiguration();
                if (cfg.isBasicAuthenticationEnabled()) {
                	return mapping.findForward("login-page");
                } else {
                	response.setHeader("Cache-Control", "no-cache");
                    response.setHeader("Pragma", "no-cache");
                    response.setHeader("Expires", "0");
                	response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                	return null;
                }
            }

        } catch (Exception e) {
            if (this.onFatalError(e, request, response))
                return null;

            return mapping.findForward("error");
        }
    }

    /**
     * @param e           is the <AuthenticationFailureException> Exception to br handled
     * @param request     is the <HttpServletRequest> context
     * @param response    is the <HttpServletResponse> context
     * @param credentials contains the <Crdential> used to perform de authentication
     * @return false will execute the default mapping.findForward otherwise no action will be taken
     */
    protected boolean onLoginAuthenticationException(AuthenticationFailureException e,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     Credential[] credentials) throws IOException {

        String cmd = getSSOCmd(request);
        if (cmd != null && cmd.equals("login_optional")) {

            // Go back to agent ...
            String back_to = getBackTo(request);

            // We're going back to the partner app.
            if (logger.isDebugEnabled())
                logger.debug("[login()], Login Optional failed, redirecting user to : " + back_to);

            response.sendRedirect(response.encodeRedirectURL(back_to));
            return true; // We handled the redirect
        }


        String on_error = (String) request.getSession(true).getAttribute(KEY_JOSSO_ON_ERROR);

        if (on_error == null) {
            // Check for a configured custom login url
            try {
                SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();
                if (cfg.isBasicAuthenticationEnabled()) {
                	on_error = cfg.getCustomLoginURL();
                }
            } catch (Exception ex) {
                logger.error(e.getMessage(), e);
            }
        }

        if (on_error != null) {

            // TODO : Improve error information handling, this could be managed with an outbound mechanism, like assertions.

            // Add error type and received username to ERROR URL.
            SSOGateway g = getSSOGateway();
            on_error += (on_error.indexOf("?") >= 0 ? "&" : "?") + "josso_error_type=" + e.getErrorType();
            try {
                SSOContext ctx = SSOContext.getCurrent();
                on_error += "&josso_username=" + g.getPrincipalName(ctx.getScheme(), credentials);
            } catch (Exception ex) {
                if (logger.isDebugEnabled())
                    logger.error("  [onLoginAuthenticationException()] cant find PrincipalName");
            }

            response.sendRedirect(response.encodeRedirectURL(on_error));
            if (logger.isDebugEnabled())
                logger.debug("[login()], authentication failure. Redirecting user to : " + on_error);

            return true;
        }

        return false;
    }

    /**
     * Relay using a previously opened and valid SSO session.
     */
    protected ActionForward relay(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        try {

            SSOGateway g = getSSOGateway();

            // 1 - Recover session and create a new assertion.
            SSOSession session = SSOContext.getCurrent().getSession();
            AuthenticationAssertion authAssertion = g.assertIdentity(session.getId());

            if (logger.isDebugEnabled())
                logger.debug("[relay()], authentication successfull.");

            // 2 - Restore BACK TO URL ...
            String back_to = this.getBackTo(request, session, authAssertion);
            if (back_to == null) {
                // Return to controller.
                return mapping.findForward("login-result");
            }

            this.clearSSOParameters(request);

            // We're going back to the partner app.
            if (logger.isDebugEnabled())
                logger.debug("[relay()], Redirecting user to : " + back_to);

            response.sendRedirect(response.encodeRedirectURL(back_to));

            return null; // No forward is needed, we perfomed a 'sendRedirect'.

        } catch (Exception e) {
            if (this.onFatalError(e, request, response))
                return null;

            return mapping.findForward("error");
        }
    }

    protected boolean onFatalError(Exception e,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        // Fatal error ...
        logger.error(e.getMessage(), e);
        ActionErrors errors = new ActionErrors();
        errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("sso.error", e.getMessage() != null ? e.getMessage() : e.toString()));
        saveErrors(request, errors);
        return false;
    }

    /**
     * Check if the request can be relayed to the requesting party without having to reauthenticate.
     *
     * @param request
     * @return true if a relay can be achieved or false in case a new authentication assertion must be issued.
     */
    protected boolean canRelay(HttpServletRequest request) {

        SSOSession s = SSOContext.getCurrent().getSession();
        return s!= null && s.isValid();

        /*
        boolean canRelay = false;


        try {



            String jossoSessionId = getJossoSessionId(request);
            if  ( jossoSessionId != null ) {
                SSOSessionManager ssoSessionManager = Lookup.getInstance().lookupSecurityDomain().getSessionManager();
                SSOSession s = ssoSessionManager.getSession( jossoSessionId);
                if (s != null)
                    canRelay = true;
            }

        } catch (NoSuchSessionException e) {
            // Ingore this error ....  we probably got an old SESSION id ...
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return canRelay;
        */
    }

}
