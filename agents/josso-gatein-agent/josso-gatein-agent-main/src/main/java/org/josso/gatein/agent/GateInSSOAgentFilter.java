/*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.josso.gatein.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.servlet.agent.GenericServletSSOAgentFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Heavily based on Gatein's logout filter class.
 *
 * @author <a href="mailto:gbrigandi@atricore.org">Gianluca Brigandi</a>
 */
public class GateInSSOAgentFilter extends GenericServletSSOAgentFilter
{


    private static final Log log = LogFactory.getLog(GateInSSOAgentFilter.class);
    protected String logoutUri;
    private static final String fileEncoding = System.getProperty("file.encoding");

    public void init(FilterConfig config) throws ServletException
    {
        super.init(config);
        this.logoutUri = config.getInitParameter("logoutUri");
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException
    {

        try {
            log.trace("doFilter() -> END");

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            boolean isLogoutInProgress = this.isLogoutInProgress(httpRequest);

            if (isLogoutInProgress)
            {

                if (httpRequest.getSession().getAttribute("SSO_LOGOUT_FLAG") == null)
                {
                    httpRequest.getSession().setAttribute("SSO_LOGOUT_FLAG", Boolean.TRUE);

                    httpResponse.sendRedirect(logoutUri);
                    return;
                }
                else
                {
                    // clear the LOGOUT flag
                    httpRequest.getSession().removeAttribute("SSO_LOGOUT_FLAG");
                }
            }

            if (log.isTraceEnabled())
                log.trace("Gatein Servlet Agent execution END ");

            super.doFilter(request, response, chain);

        } finally {
            log.trace("doFilter() -> END");
        }
    }

    private boolean isLogoutInProgress(HttpServletRequest request) throws UnsupportedEncodingException
    {
        // set character encoding before retrieving request parameters
        if(fileEncoding!=null)
        {
            request.setCharacterEncoding(fileEncoding);
        }
        String action = request.getParameter("portal:action");

        if (action != null && action.equals("Logout"))
        {
            return true;
        }

        return false;
    }

}