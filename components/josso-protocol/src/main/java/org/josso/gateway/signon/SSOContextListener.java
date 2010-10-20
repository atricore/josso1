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
import org.josso.Lookup;
import org.josso.gateway.SSOGateway;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This Servlet Context Listener is used to start JOSSO on servlet init.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOContextListener.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public class SSOContextListener implements ServletContextListener, Constants {

    private static final Log logger = LogFactory.getLog(SSOContextListener.class);

    public void contextInitialized(ServletContextEvent event) {
        try {
            // This will initialize all needed components

            Lookup lookup = Lookup.getInstance();
            lookup.init("josso-gateway-config.xml");
            lookup.lookupSSOGateway();

            // Build and initialize the SSO Gateway
            ServletContext ctx = event.getServletContext();
            SSOGateway g = (SSOGateway) ctx.getAttribute(KEY_JOSSO_GATEWAY);
            if (g == null) {
                g = Lookup.getInstance().lookupSSOGateway();
                ctx.setAttribute(KEY_JOSSO_GATEWAY, g);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void contextDestroyed(ServletContextEvent event) {
        SSOGateway g = (SSOGateway) event.getServletContext().getAttribute(KEY_JOSSO_GATEWAY);
        if (g != null) {
            g.destroy();
        }

    }

}
