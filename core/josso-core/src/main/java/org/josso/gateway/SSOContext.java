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
package org.josso.gateway;


import org.josso.SecurityDomain;
import org.josso.Lookup;
import org.josso.gateway.session.SSOSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOContext.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public abstract class SSOContext {

    protected static ThreadLocal<SSOContext> ctx = new ThreadLocal<SSOContext>();

    private static final Log logger = LogFactory.getLog(SSOContext.class);

    protected String scheme;

    protected String userLocation;

    protected SSOSession ssoSession;

    protected SecurityDomain securityDomain;

    protected SSOContext() {
        ctx.set(this);

        if (logger.isDebugEnabled())
            logger.debug("Created context in thread " + Thread.currentThread().getName());
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public SSOSession getSession() {
        return ssoSession;
    }

    /**
     * The following identifiers indicate the location where authentication credentials were activated.
     */
    public String getUserLocation() {
        return userLocation;
    }


    /**
     * This will return the security domain associated with this SSO Context.
     */
    public SecurityDomain getSecurityDomain() {
        return securityDomain;
    }


    public static SSOContext getCurrent() {
        return ctx.get();
    }


}
