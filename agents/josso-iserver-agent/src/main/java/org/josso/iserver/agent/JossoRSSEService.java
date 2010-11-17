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

package org.josso.iserver.agent;

import com.actuate.iportal.security.iPortalSecurityAdapter;
import com.actuate.reportcast.exceptions.AuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.agent.http.HttpSSOAgent;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

public class JossoRSSEService extends iPortalSecurityAdapter {

    private static final String KEY_SESSION_MAP = "org.josso.servlet.agent.sessionMap";
    
    private ServletContext _ctx;
    private HttpSSOAgent _agent;
    //private WebserviceSSOIdentityProvider wsSSOIdP;
    private boolean initlized = false;

    private String username;
    private String passwd;


    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(JossoRSSEService.class);

    private void init(ServletContext context) throws ServletException {
        // Validate and update our current component state
        _ctx = context;
        WebApplicationContext webCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(_ctx);
        _ctx.setAttribute(KEY_SESSION_MAP, new HashMap());

        if (_agent == null) {

            try {

                Lookup lookup = Lookup.getInstance();
                lookup.init("josso-agent-config.xml"); // For spring compatibility ...

                // We need at least an abstract SSO Agent
                _agent = (HttpSSOAgent) lookup.lookupSSOAgent();
                if (logger.isDebugEnabled())
                    _agent.setDebug(1);
                _agent.start();

                // Publish agent in servlet context
                _ctx.setAttribute("org.josso.agent", _agent);

            } catch (Exception e) {
                throw new ServletException("Error starting SSO Agent : " + e.getMessage(), e);
            }
        }
    }

    /** {@inheritDoc} */
    /*
    @Override
    public boolean authenticate(HttpServletRequest req)
            throws AuthenticationException {
        HttpSession currSession = req.getSession(true);

        if (!initlized) {
            try {
                init(currSession.getServletContext());
                initlized = true;
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }
        Cookie jCookie = getJossoCookie(req);
        String jossoSessionId = jCookie.getValue();

        if (jCookie != null || !jCookie.getValue().equals("-")) {
            SSOIdentityManagerService im = null;
            try {
                im = Lookup.getInstance().lookupSSOAgent().getSSOIdentityManager();
                SSOUser ssoUser = im.findUserInSession(jossoSessionId, jossoSessionId);
                if (ssoUser != null) {
                    userName = ssoUser.getName();
                    return true;
                } else{
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    */

    public boolean authenticate(HttpServletRequest req)
            throws AuthenticationException {

        HttpSession currSession = req.getSession(true);
        currSession.setAttribute("authenticationStatus","authenticated") ;

        return true;

    }

    @Override
    public byte[] getExtendedCredentials() {
        return super.getExtendedCredentials();
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public String getRepositoryType() {
        return super.getRepositoryType();
    }

    @Override
    public String getServerUrl() {
        return super.getServerUrl();
    }

    @Override
    public String getUserHomeFolder() {
        return super.getUserHomeFolder();
    }

    @Override
    public String getVolume() {
        return super.getVolume();
    }

    @Override
    public boolean isEnterprise() {
        return super.isEnterprise();
    }


    private Cookie getJossoCookie(HttpServletRequest hreq) {
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

        return cookie;
    }
}