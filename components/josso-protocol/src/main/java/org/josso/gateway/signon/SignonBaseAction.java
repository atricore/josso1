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
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionError;
import org.josso.Lookup;
import org.josso.auth.Credential;
import org.josso.auth.scheme.AuthenticationScheme;
import org.josso.auth.scheme.RememberMeAuthScheme;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.gateway.*;
import org.josso.gateway.Constants;
import org.josso.gateway.assertion.AuthenticationAssertion;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.SSOSession;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;

/**
 * This is the base action for all signon actions.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SignonBaseAction.java 612 2008-08-22 12:17:20Z gbrigand $
 */
public abstract class SignonBaseAction extends Action implements org.josso.gateway.signon.Constants {

    private static final Log logger = LogFactory.getLog(SignonBaseAction.class);

    // private static final Log logger = LogFactory.getLog(SignonBaseAction.class);

    /**
     * Gets current sso gateway.
     */
    protected SSOGateway getSSOGateway() {

        SSOGateway g = (SSOGateway) getServlet().getServletContext().getAttribute(KEY_JOSSO_GATEWAY);

        if (g == null) {

            try {
                g = Lookup.getInstance().lookupSSOGateway();
                getServlet().getServletContext().setAttribute(KEY_JOSSO_GATEWAY, g);
            } catch (Exception e) {
                logger.error("Cannot get Gateway instance " + e.getMessage(), e);
            }
        }
        return g;
    }

    /**
     * Gets the received SSO Command. If command is empty (""), returns null.
     */
    protected String getSSOCmd(HttpServletRequest request) {
        String cmd = request.getParameter(PARAM_JOSSO_CMD);
        if ("".equals(cmd))
            cmd = null;
        return cmd;
    }


    /**
     * This method knows how to build a SSO Context based on HTTP state: request, session, etc.
     * Some state is stored as sesion attributes
     *
     * @see #storeSSOParameters(javax.servlet.http.HttpServletRequest)
     */
    protected void prepareContext(HttpServletRequest request) throws SSOException, SSOAuthenticationException {

        // We need to store SSO parameters
        storeSSOParameters(request);

        // Use gateway to select a security domain
        SSOGateway gwy = getSSOGateway();

        // The first thing to do is to create the context and publish the security domain !!!
        MutableSSOContext ctx = (MutableSSOContext) gwy.prepareSSOContext(new SSORequestImpl(request));
        ctx.setUserLocation(request.getRemoteHost());

        // Store current SD name in session
        request.getSession().setAttribute(org.josso.gateway.signon.Constants.KEY_JOSSO_SECURITY_DOMAIN_NAME, ctx.getSecurityDomain().getName());
        if (logger.isDebugEnabled())
            logger.debug("[prepareContext()] Storing security domain name in session [" + KEY_JOSSO_SECURITY_DOMAIN_NAME + "] : " +
                    ctx.getSecurityDomain().getName() + " (" + request.getSession().getId() + ")");

        // SSO Session
        String sessionId = getJossoSessionId(request);
        if (sessionId != null && !"".equals(sessionId)) {
            try {
                // If session is not valid, no current session will be available in context.
                ctx.setCurrentSession(gwy.findSession(sessionId));
            } catch (NoSuchSessionException e) {
                if (logger.isDebugEnabled())
                    logger.debug("NoSuchSessionException : " + sessionId + " " + e.getMessage());
            }
        }

        // TODO : Detect Authentication scheme when user is already logged ...!
        String scheme = getSchemeName(request);
        logger.debug("Using authentication scheme : " + scheme);

        ctx.setScheme(scheme);

    }

    /**
     * This method stores SSO relevant request parameters as http session attributes.
     *
     * @param request
     * @see #clearSSOParameters(javax.servlet.http.HttpServletRequest)
     * @see #PARAM_JOSSO_BACK_TO
     * @see #KEY_JOSSO_BACK_TO
     * @see #PARAM_JOSSO_ON_ERROR
     * @see #KEY_JOSSO_ON_ERROR
     * @see #KEY_JOSSO_SECURITY_DOMAIN_NAME
     */
    protected void storeSSOParameters(HttpServletRequest request) {

        // Get a session
        HttpSession s = request.getSession(true);

        // Store back_to url, if present.
        String back_to = request.getParameter(PARAM_JOSSO_BACK_TO);
        if (back_to != null && !"".equals(back_to)) {
            s.setAttribute(KEY_JOSSO_BACK_TO, back_to);
            if (logger.isDebugEnabled())
                logger.debug("[storeSSOParameters()] Storing back-to url in session [" + KEY_JOSSO_BACK_TO + "] : " + back_to + " (" + s.getId() + ")");
        }

        // Store on_error url if present.
        String on_error = request.getParameter(PARAM_JOSSO_ON_ERROR);
        if (on_error != null && !"".equals(on_error)) {
            s.setAttribute(KEY_JOSSO_ON_ERROR, on_error);
            if (logger.isDebugEnabled())
                logger.debug("[storeSSOParameters()] Storing on-error url in session [" + KEY_JOSSO_ON_ERROR + "] : " + on_error + " (" + s.getId() + ")");
        }

    }

    /**
     * Clears SSO relevant attributes from http session.
     *
     * @see #storeSSOParameters(javax.servlet.http.HttpServletRequest)
     */
    protected void clearSSOParameters(HttpServletRequest req) {

        req.getSession().removeAttribute(KEY_JOSSO_BACK_TO);
        if (logger.isDebugEnabled())
            logger.debug("[clearSSOParameters()] Removing " + KEY_JOSSO_BACK_TO + " from session (" + req.getSession().getId() + ")");

        req.getSession().removeAttribute(KEY_JOSSO_ON_ERROR);
        if (logger.isDebugEnabled())
            logger.debug("[clearSSOParameters()] Removing " + KEY_JOSSO_ON_ERROR + " from session (" + req.getSession().getId() + ")");

        req.getSession().removeAttribute(KEY_JOSSO_SECURITY_DOMAIN_NAME);
        if (logger.isDebugEnabled())
            logger.debug("[clearSSOParameters()] Removing " + KEY_JOSSO_SECURITY_DOMAIN_NAME + " from session (" + req.getSession().getId() + ")");
    }


    protected String getBackTo(HttpServletRequest request,
                               SSOSession session,
                               AuthenticationAssertion authAssertion) {

        HttpSession httpSession = request.getSession();
        String back_to = (String) httpSession.getAttribute(KEY_JOSSO_BACK_TO);
        if (back_to == null) {
            try {
                SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

                if (logger.isDebugEnabled())
                    logger.debug("  No 'BACK TO' URL found in session " + httpSession.getId());

                if (logger.isDebugEnabled())
                    logger.debug("  Using configured 'BACK TO' URL : " + cfg.getLoginBackToURL());
                back_to = cfg.getLoginBackToURL();
            } catch (Exception ex) {
                if (logger.isDebugEnabled())
                    logger.debug("  [getBackTo()] cant find SSOWebConfiguration");
            }
        }

        if (back_to == null) {
            // No back to URL received or configured ... use configured success page.
            logger.warn("No 'BACK TO' URL received or configured ... using default forward rule !");

            // Return to controller.
            return null;
        }

        back_to += (back_to.indexOf("?") >= 0 ? "&" : "?") + "josso_assertion_id=" + authAssertion.getId();

        return back_to;
    }

    /**
     * The 'back_to' url used when authentaction failed and the "" command was received
     * @param request
     * @return
     */
    protected String getBackTo(HttpServletRequest request) {

        HttpSession httpSession = request.getSession();
        String back_to = (String) httpSession.getAttribute(KEY_JOSSO_BACK_TO);
        if (back_to == null) {
            try {
                SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

                if (logger.isDebugEnabled())
                    logger.debug("  No 'BACK TO' URL found in session " + httpSession.getId());

                if (logger.isDebugEnabled())
                    logger.debug("  Using configured 'BACK TO' URL : " + cfg.getLoginBackToURL());
                back_to = cfg.getLoginBackToURL();
            } catch (Exception ex) {
                if (logger.isDebugEnabled())
                    logger.debug("  [getBackTo()] cant find SSOWebConfiguration");
            }
        }

        if (back_to == null) {
            // No back to URL received or configured ... use configured success page.
            logger.warn("No 'BACK TO' URL received or configured ... using default forward rule !");

            // Return to controller.
            return null;
        }

        return back_to;
    }


    protected Cookie getJossoCookie(HttpServletRequest request, String securityDomainName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(JOSSO_SINGLE_SIGN_ON_COOKIE + "_" + securityDomainName)) {
                return cookie;
            }
        }
        return null;

    }

    protected Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        return null;

    }



    /**
     * Gets the josso session id value
     * <p/>
     * participantparam request
     *
     * @return null, if JOSSO_SINGLE_SIGN_ON_COOKIE is not found in reqeust.
     */
    protected String getJossoSessionId(HttpServletRequest request) {
        SSOContext ctx = SSOContext.getCurrent();
        String jossoSessionId = null;

        try {
            SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

            if (cfg.isSessionTokenOnClient()) {
                Cookie c = getJossoCookie(request, ctx.getSecurityDomain().getName());
                if (c != null)
                    jossoSessionId = c.getValue();
            } else {
                HttpSession session = request.getSession();
                return (String) session.getAttribute(JOSSO_SINGLE_SIGN_ON_COOKIE + "_" + ctx.getSecurityDomain().getName());
            }
        } catch (Exception ex) {
            if (logger.isDebugEnabled())
                logger.debug("  [getJossoSessionId()] cant find SSOWebConfiguration");
        }

        return jossoSessionId;
    }

    /**
     * Stores session id
     *
     * @param request http request
     * @param session SSO session instance
     */
    protected void storeSSOInformation(HttpServletRequest request, HttpServletResponse response, SSOSession session) {
        MutableSSOContext ctx = (MutableSSOContext) SSOContext.getCurrent();
        ctx.setCurrentSession(session);

        try {
            SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

            if (cfg.isSessionTokenOnClient()) {
                logger.debug("Storing SSO Session ID on clinet");
                Cookie ssoCookie = newJossoCookie(
                        request.getContextPath(),
                        JOSSO_SINGLE_SIGN_ON_COOKIE + "_" + ctx.getSecurityDomain().getName(),
                        session.getId());
                response.addCookie(ssoCookie);
            } else {
                logger.debug("Storing SSO Session ID on server");
                HttpSession hsession = request.getSession();
                hsession.setAttribute(JOSSO_SINGLE_SIGN_ON_COOKIE + "_" + ctx.getSecurityDomain().getName(), session.getId());
            }

            logger.debug("Remember Me:" + request.getParameter(org.josso.gateway.signon.Constants.PARAM_JOSSO_REMEMBERME));
            logger.debug("Command:" + request.getParameter(org.josso.gateway.signon.Constants.PARAM_JOSSO_CMD));

            // Remember user authentication.
            if (cfg.isRememberMeEnabled() && request.getParameter(org.josso.gateway.signon.Constants.PARAM_JOSSO_REMEMBERME) != null) {

                // Storing remember me information (always on client)
                logger.debug("Storing SSO Rememberme Token on Client");

                String cipherSuite = (String) request.getAttribute
                    ("javax.servlet.request.cipher_suite");

                if (cipherSuite == null)
                    logger.error("SSL Required for 'remember me' feature");

                // We need this auth scheme to build the proper token
                // TODO : Check this when implementing the "Password Recovery" becauase it's a similar case.  We will have to acces the password value from the store
                RememberMeAuthScheme scheme = (RememberMeAuthScheme) ctx.getSecurityDomain().getAuthenticator().getAuthenticationScheme("rememberme-authentication");
                String token = scheme.getRemembermeTokenForUser(session.getUsername());

                // This will provide the credential string value ...
                Cookie rememberMeCookie = new Cookie(JOSSO_REMEMBERME_TOKEN + "_" + ctx.getSecurityDomain().getName(), token);

                // If max age was not specified, assume a year.
                rememberMeCookie.setMaxAge(60 * (cfg.getRememberMeMaxAge() > 0 ? cfg.getRememberMeMaxAge() : 60 * 24 *365)); // The cookie will live for a year ...

                rememberMeCookie.setPath("/");
                if (cfg.isSessionTokenSecure()) {
                    rememberMeCookie.setSecure(true);
                } else {
                    logger.error("Remember Me funcion requires SSL Transport!");
                }


                // Store cookie in response
                response.addCookie(rememberMeCookie);


            }

        } catch (Exception ex) {
            logger.error("Error while storing SSO Information : " + ex.getMessage(), ex);
        }

    }

    protected void removeJossoSessionId(HttpServletRequest request, HttpServletResponse response) {
        SSOContext ctx = SSOContext.getCurrent();

        try {
            SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

            if (cfg.isSessionTokenOnClient()) {
                Cookie ssoCookie = newJossoCookie(
                        request.getContextPath(),
                        JOSSO_SINGLE_SIGN_ON_COOKIE + "_" + ctx.getSecurityDomain().getName(),
                        "-");
                ssoCookie.setMaxAge(0);
                response.addCookie(ssoCookie);
            } else {
                HttpSession session = request.getSession();
                session.removeAttribute(JOSSO_SINGLE_SIGN_ON_COOKIE + "_" + ctx.getSecurityDomain().getName());
            }

            if (cfg.isRememberMeEnabled()) {

                // Clear the remember me cookie
                Cookie rememberMeCookie = new Cookie(Constants.JOSSO_REMEMBERME_TOKEN + "_" + SSOContext.getCurrent().getSecurityDomain().getName(), "-");
                rememberMeCookie.setMaxAge(0);
                rememberMeCookie.setSecure(cfg.isSessionTokenSecure());
                rememberMeCookie.setPath("/");

                response.addCookie(rememberMeCookie);
            }
        } catch (Exception ex) {
            if (logger.isDebugEnabled())
                logger.debug("  [removeJossoSessionId()] cant find SSOWebConfiguration");
        }
    }


    protected Cookie newJossoCookie(String path, String name, String value) throws Exception {
        SSOWebConfiguration cfg = Lookup.getInstance().lookupSSOWebConfiguration();

        Cookie ssoCookie = new Cookie(name, value);
        ssoCookie.setMaxAge(-1);

        if (cfg.isSessionTokenSecure()) {
            ssoCookie.setSecure(true);
        }

        ssoCookie.setPath(path);

        return ssoCookie;


//        if (cfg.getSessionTokenScope() != null) {
//            ssoCookie.setDomain(cfg.getSessionTokenScope());
//        }

    }


    /**
     * Subclasses should provide proper credentials based on specific authentication schemes.
     */
    protected Credential[] getCredentials(HttpServletRequest request) throws SSOAuthenticationException {
        return new Credential[0];
    }

    protected String getSchemeName(HttpServletRequest request) throws SSOAuthenticationException {
        return "";
    }
}
