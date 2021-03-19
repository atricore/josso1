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

package org.josso.wls10.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.agent.LocalSession;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SSOPartnerAppConfig;
import org.josso.gateway.session.exceptions.FatalSSOSessionException;
import org.josso.servlet.agent.GenericServletLocalSession;
import org.josso.servlet.agent.GenericServletSSOAgentRequest;
import weblogic.servlet.security.ServletAuthentication;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.*;
import javax.servlet.*;
import java.io.IOException;

/**
 * Servlet Filter that will assert container-authenticated SSO sessions. This filter must be enabled for
 * keeping JOSSO intercepting authenticated requests.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 */
public class WLSSessionEnforcementServletFilter implements Filter {

    private static final Log log = LogFactory.getLog(org.josso.wls10.agent.WLSSessionEnforcementServletFilter.class);

    /**
     * One agent instance for all applications.
     */
    private WLSSSOAgent _agent;

    /**
     * Logger
     */
    public WLSSessionEnforcementServletFilter() {

    }

    public void init(FilterConfig filterConfig) throws ServletException {
        // Validate and update our current component state
        ServletContext ctx = filterConfig.getServletContext();

        if (_agent == null) {

            try {

                Lookup lookup = Lookup.getInstance();
                lookup.init("josso-agent-config.xml"); // For spring compatibility ...

                // We need at least an abstract SSO Agent
                _agent = (WLSSSOAgent) lookup.lookupSSOAgent();
                _agent.start();

                // Enable debug if we use debug ....
                if (log.isDebugEnabled()) _agent.setDebug(1);

            } catch (Exception e) {
                throw new ServletException("Error starting SSO Agent : " + e.getMessage(), e);
            }

        }

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {


        HttpServletRequest hreq =
                (HttpServletRequest)request;

        HttpServletResponse hres =
                (HttpServletResponse) response;

        HttpSession session = hreq.getSession(true);

        if (log.isDebugEnabled())
            log.debug("Processing : " + hreq.getContextPath());

        String contextPath = hreq.getContextPath();
        String vhost = hreq.getServerName();
        SSOPartnerAppConfig cfg = _agent.getPartnerAppConfig(vhost, contextPath);

        // ------------------------------------------------------------------
        // Check for the single sign on cookie
        // ------------------------------------------------------------------
        if (log.isDebugEnabled())
            log.debug("Checking for SSO cookie");
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }
        if (cookie != null && !cookie.getValue().equals("-")) {
            String jossoSessionId = cookie.getValue();

            if (log.isDebugEnabled())
                log.debug("asserting SSO session for : " + jossoSessionId);

            SSOAgentRequest sessionAssertionRequest;

            sessionAssertionRequest = doMakeSSOAgentRequest(cfg.getId(),
                    SSOAgentRequest.ACTION_ASSERT_SESSION,
                    jossoSessionId,
                    null,
                    null,
                    hreq,
                    hres
            );


            // TODO: Agents should be able to pass back responses corresponding to the submitted request.
            try {
                _agent.processRequest(sessionAssertionRequest);
                if (log.isDebugEnabled())
                    log.debug("asserted successfully SSO session for : " + jossoSessionId);
            } catch (FatalSSOSessionException e) {
                if (log.isDebugEnabled())
                    log.debug("error asserting SSO session : " + jossoSessionId);

                String requestedResourceUrl;

                // Clear previous COOKIE ...
                String ssoCookie = _agent.newJossoCookieHeader(hreq.getContextPath(), "-", hreq.isSecure());
                hres.addHeader("Set-Cookie", ssoCookie);
                session.invalidate();
                requestedResourceUrl = _agent.buildBackToURL(hreq, "");
                hres.sendRedirect(hres.encodeRedirectURL(requestedResourceUrl));
                return ;
            }

        }

        filterChain.doFilter(hreq, hres);

    }

    public void destroy() {
        // Validate and update our current component state
        if (_agent != null) {
            _agent.stop();
            _agent = null;
        }

    }


    /**
     * Creates a new request
     */
    protected SSOAgentRequest doMakeSSOAgentRequest(String requester, int action, String sessionId, LocalSession session, String assertionId,
                                                    HttpServletRequest hreq, HttpServletResponse hres) {
        GenericServletSSOAgentRequest r = new GenericServletSSOAgentRequest(requester, action, sessionId, session, assertionId);
        r.setRequest(hreq);
        r.setResponse(hres);

        return r;

    }


}
