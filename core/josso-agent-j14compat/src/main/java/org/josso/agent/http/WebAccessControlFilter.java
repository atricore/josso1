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

package org.josso.agent.http;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JOSSO Servlet Filter for for performing simple access control based on its custom security context.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 */
public class WebAccessControlFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        MutableHttpServletRequestImpl hreq =
                new MutableHttpServletRequestImpl((HttpServletRequest) request);

        // Obtain a JOSSO security context instance, if none is found is because user has not been authenticated.
        JOSSOSecurityContext ctx = WebAccessControlUtil.getSecurityContext((HttpServletRequest) request);
        if (ctx == null) {
            // User has not been authenticated, ask him to login, this will trigger the login process,  storing current URL and
            // Redirecting user to JOSSO Gateway Login page :
            try {
                WebAccessControlUtil.askForLogin((HttpServletRequest) request, (HttpServletResponse) response);
            } catch (Exception e) {
                throw new ServletException(e);
            }
            return;
        }

        filterChain.doFilter(hreq, response);
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}