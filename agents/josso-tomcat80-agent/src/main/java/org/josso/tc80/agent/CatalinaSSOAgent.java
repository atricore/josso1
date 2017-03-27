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

package org.josso.tc80.agent;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.http.HttpSSOAgent;

import java.security.Principal;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: CatalinaSSOAgent.java 974 2009-01-14 00:39:45Z sgonzalez $
 * @org.apache.xbean.XBean element="agent"
 * <p>
 * Catalina SSO Agent Implementation that authenticates using the configured Catalina Realm's
 * Gateway SSO Login module.
 */
public class CatalinaSSOAgent extends HttpSSOAgent {

    private static final Log LOG = LogFactory.getLog(CatalinaSSOAgent.class);

    private Container _container;

    public CatalinaSSOAgent() {
        super();
    }


    public CatalinaSSOAgent(Container container) {
        super();
        _container = container;

    }

    public void start() {
        super.start();
        // Add context config as partner app ...
        if (_container instanceof Context) {
            Context context = (Context) _container;
            _cfg.addSSOPartnerApp(context.getPublicId(), null, context.getPath(), null, null);
        }
    }

    /**
     * Sets the Catalina Context to be used by the authenticator.
     *
     * @param container
     */
    public void setCatalinaContainer(Container container) {
        _container = container;

    }

    /**
     * Authenticates the Single Sign-on Session by calling the
     * configured Realm for the Catalina Context. The configured Realm
     * should be the JAAS one so that the GatewayLoginModule can act
     * and validate de given SSO Session Identifier in the Gateway.
     *
     * @param request
     * @return the authenticated principal.
     */
    protected Principal authenticate(SSOAgentRequest request) {
        CatalinaSSOAgentRequest r = (CatalinaSSOAgentRequest) request;
        Context c = r.getContext();

        // Invoke authentication
        Realm realm = c.getRealm();

        if (debug > 0)
            log("Using realm : " + realm.getClass().getName() + " SSOSID : " + r.getSessionId());

        Principal p = realm.authenticate(r.getSessionId(), r.getSessionId());

        if (debug > 0)
            log("Received principal : " + p + "[" + (p != null ? p.getClass().getName() : "<null>") + "]");

        return p;
    }

    protected void log(String message) {
        if (LOG.isDebugEnabled())
            LOG.debug(message);
    }

    protected void log(String message, Throwable throwable) {
        if (LOG.isDebugEnabled())
            LOG.debug(message, throwable);
    }

    /**
     * Return a String rendering of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("CatalinaSSOAgent[");
        sb.append(_container != null ? _container.getName() : "");
        sb.append("]");
        return (sb.toString());

    }

}
