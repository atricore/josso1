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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generic utils to be used by partner application developres to trigger authentication process and obtain current security context.
 * <p/>
 * Date: Nov 29, 2007
 * Time: 4:41:53 PM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class WebAccessControlUtil {

    public static final Log log = LogFactory.getLog(WebAccessControlUtil.class);

    //public static final String KEY_JOSSO_SAVED_REQUEST_URI = "org.josso.servlet.agent.savedRequest";
    
    public static final String KEY_JOSSO_SAVED_REQUEST_URI = "JOSSO_SAVED_REQUEST";

    /**
     * Attribute key used to store current security context instance.
     */
    public static final String KEY_JOSSO_SECURITY_CONTEXT = "org.josso.servlet.agent.JOSSOSecurityContext";


    /**
     * This method will redirect the user the the login page configured in the JOSSO Gateway.
     */
    public static void askForLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {

        StringBuffer sb = new StringBuffer(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append('?');
            sb.append(request.getQueryString());
        }

        HttpSSOAgent agent = (HttpSSOAgent) Lookup.getInstance().lookupSSOAgent();
        
        if (log.isDebugEnabled())
            log.debug("Storing original request : " + sb.toString());

        agent.setAttribute(request, response, KEY_JOSSO_SAVED_REQUEST_URI, sb.toString());
        
        response.sendRedirect(request.getContextPath() + agent.getJOSSOLoginUri());
        
    }

    /**
     * This method provides access to JOSSO securit context, if no context is present is because user is not authenticated.
     *
     * @param request
     * @return
     */
    public static JOSSOSecurityContext getSecurityContext(HttpServletRequest request) {
        return (JOSSOSecurityContext) request.getSession().getAttribute(
                WebAccessControlUtil.KEY_JOSSO_SECURITY_CONTEXT);
    }
}
