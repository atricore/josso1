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
import org.josso.gateway.session.SSOSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is an SSO Context that can be modified.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: MutableSSOContext.java 568 2008-07-31 18:39:20Z sgonzalez $
 */
public class MutableSSOContext extends SSOContext {

    private static final Log logger = LogFactory.getLog(MutableSSOContext.class);

    /**
     * Only the gateway can create a Mutable SSO Context
     */
    MutableSSOContext() {
        super();
    }

    public void setCurrentSession(SSOSession currentSession) {
        this.ssoSession = currentSession;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public void setSecurityDomain(SecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }

}
