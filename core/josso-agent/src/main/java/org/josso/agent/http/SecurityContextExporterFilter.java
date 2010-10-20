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
import java.io.IOException;
import java.util.*;

/**
 * JOSSO Servlet Filter for exposing the security context as propagated by the agent.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 */
public class SecurityContextExporterFilter implements Filter {

    public static final String SECURITY_CONTEXT_BINDING = "org.josso.agent.http.securitycontext.binding";
    public static final String SECURITY_CONTEXT_CONTENT = "org.josso.agent.http.securitycontext.content";

    public static final String HTTP_HEADERS_BINDING = "HTTP_HEADERS_BINDING";

    public static final String HTTP_REQ_ATTRS_BINDING = "HTTP_REQ_ATTRS_BINDING";

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        MutableHttpServletRequestImpl hreq =
                new MutableHttpServletRequestImpl((HttpServletRequest) request);

        String binding = (String) hreq.getAttribute(SECURITY_CONTEXT_BINDING);
        HashMap content = (HashMap) hreq.getAttribute(SECURITY_CONTEXT_CONTENT);

        if (content != null) {
            if (binding.equals(HTTP_HEADERS_BINDING)) {
                Set headerKeys = content.keySet();
                for (Iterator iterator = headerKeys.iterator(); iterator.hasNext();) {
                    String name = (String) iterator.next();

                    List values = (List) content.get(name);

                    for (Iterator iterator1 = values.iterator(); iterator1.hasNext();) {
                        Object value = (String) iterator1.next();
                        hreq.addHeader(name, (String) value);

                    }
                }

            } else if (binding.equals(HTTP_REQ_ATTRS_BINDING)) {

                Set attrKeys = content.keySet();
                for (Iterator iterator = attrKeys.iterator(); iterator.hasNext();) {
                    String attrKey = (String) iterator.next();
                    String attrValue = (String) content.get(attrKey);

                    hreq.setAttribute(attrKey, attrValue);

                }

            }
        }

        filterChain.doFilter(hreq, response);
    }

    public void destroy() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
