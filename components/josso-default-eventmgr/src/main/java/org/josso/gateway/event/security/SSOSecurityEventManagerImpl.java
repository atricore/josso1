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
package org.josso.gateway.event.security;

import org.josso.auth.Credential;
import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.SSOEventManagerImpl;

/**
 * This is the default implementation of the SSOSecurityEventManager
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOSecurityEventManagerImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public abstract class SSOSecurityEventManagerImpl extends SSOEventManagerImpl implements SSOSecurityEventManager {

    /**
     * This method creates a new SSOIdentityEvent and invokes the fireSSOEvent method.
     *
     * @see #fireSSOEvent
     */
    public void fireAuthenticationFailureEvent(String remoteHost, String scheme, Credential[] credentials, Throwable error) {
        SSOIdentityEvent event = new SSOIdentityEvent(remoteHost, scheme, credentials, error);
        fireSSOEvent(event);
    }

    /**
     * This method creates a new SSOIdentityEvent and invokes the fireSSOEvent method.
     *
     * @see #fireSSOEvent
     */
    public void fireAuthenticationSuccessEvent(String remoteHost, String scheme, String username, String sessionId) {
        SSOIdentityEvent event = new SSOIdentityEvent(remoteHost, scheme, username, sessionId);
        fireSSOEvent(event);

    }

    /**
     * This method creates a new SSOIdentityEvent and invokes the fireSSOEvent method.
     *
     * @see #fireSSOEvent
     */
    public void fireLogoutSuccessEvent(String remoteHost, String username, String sessionId) {
        SSOIdentityEvent event = new SSOIdentityEvent(remoteHost, username, sessionId);
        fireSSOEvent(event);
    }

    /**
     * This method creates a new SSOIdentityEvent and invokes the fireSSOEvent method.
     *
     * @see #fireSSOEvent
     */
    public void fireLogoutFailureEvent(String remoteHost, String username, String sessionId, Throwable error) {
        SSOIdentityEvent event = new SSOIdentityEvent(remoteHost, username, sessionId, error);
        fireSSOEvent(event);
    }

    /**
     * This method creates a new SSOIdentityEvent and invokes the fireSSOEvent method.
     *
     * @see #fireSSOEvent
     */
    public void fireSessionEvent(String username, String sessionId, String type, Object data) {
        SSOEvent event = new SSOSessionEvent(username, sessionId, type, data);
        fireSSOEvent(event);
    }

    public void fireSessionFailureEvent(String username, String sessionId, String type, Throwable error) {
        SSOEvent event = new SSOSessionEvent(username, sessionId, type, error);
        fireSSOEvent(event);
    }


}
