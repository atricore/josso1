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

package org.josso.gateway.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.SecurityDomain;
import org.josso.gateway.SSORequestImpl;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSOWebConfiguration;
import org.josso.gateway.protocol.SSOProtocolManager;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.service.SSOSessionManager;
import org.josso.gateway.signon.Constants;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 25, 2008
 * Time: 11:48:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolHandlerFilter implements Filter {
    private static final Log logger = LogFactory.getLog(ProtocolHandlerFilter.class);
    //private static int call;

    //------------------------------------------------------ javax.servlet.Filter implementation section

    public void init(FilterConfig filterConfig) throws ServletException {
        //call = 1;
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {


        try {

            HttpServletRequest hreq = (HttpServletRequest) servletRequest;
            HttpServletResponse hres = (HttpServletResponse) servletResponse;

            // Get our security domain
            SSOContext ctx = Lookup.getInstance().lookupSSOGateway().prepareSSOContext(new SSORequestImpl(hreq));
            SecurityDomain sd = ctx.getSecurityDomain();

            // TODO : Handle this in a more general way.
            // See if P3P configuration is enabled
            // This is required by Microsoft Internet Explorer when embedding JOSSO in a IFRAME
            SSOWebConfiguration cfg = sd.getSSOWebConfiguration();
            if (cfg.isSendP3PHeader()) {

                if (!hres.isCommitted()) {
                    hres.setHeader("P3P", cfg.getP3PHeaderValue());
                    if (logger.isDebugEnabled())
                        logger.debug("Adding P3P Header:" + cfg.getP3PHeaderValue());
                } else {
                    logger.warn("Already commited response, cannot set P3P header");
                }
            }

            // Handle specific protocol requests
            if (!existJossoSession(hreq, sd)) {

                SSOProtocolManager pm = sd.getProtocolManager();
                // We have a Protocol Manager and the request was processed, do not continue with the chain.
                if (pm != null && !pm.dispatchRequest(hreq, hres))
                    return;

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        filterChain.doFilter(servletRequest, servletResponse);

    }

    public void destroy() {
    }

    protected boolean existJossoSession(HttpServletRequest request, SecurityDomain sd) throws Exception {
        String jossoSessionId = getJossoSessionId(request);

        if (jossoSessionId == null)
            return false;

        SSOSessionManager ssoSessionManager = sd.getSessionManager();

        try {
            SSOSession s = ssoSessionManager.getSession(jossoSessionId);
            if (s != null && s.isValid())
                return true;

        } catch (NoSuchSessionException nsse) {

            HttpSession ssn = request.getSession(true);
            // TODO : FIXME This component should not be boud to NTLM!
            /*
            if (ssn.getAttribute(NtlmProtocolHandler.NTLM_PASS_AUTHENTICATION) != null)
                ssn.removeAttribute(NtlmProtocolHandler.NTLM_PASS_AUTHENTICATION); */

            if (ssn.getAttribute( "ntlmHttpPa" ) != null)
                ssn.removeAttribute( "ntlmHttpPa" );
        }

        return false;
    }

    // ----------------------------------------------------- methods
    protected String getJossoSessionId(HttpServletRequest request) {
        Cookie c = getJossoCookie(request);
        if (c != null)
            return c.getValue();

        return null;
    }

    protected Cookie getJossoCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals(Constants.JOSSO_SINGLE_SIGN_ON_COOKIE)) {
                return cookie;
            }
        }
        return null;
    }
}