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
import org.josso.gateway.event.SSOEventManager;

/**
 * Envent Manager that also sends SSO Security events (login, logout, authentication, session creation, etc)
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOSecurityEventManager.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public interface SSOSecurityEventManager extends SSOEventManager {

    void fireAuthenticationFailureEvent(String userLocation, String scheme, Credential[] credentials, Throwable error);

    void fireAuthenticationSuccessEvent(String userLocation, String scheme, String username, String sessionId);

    void fireLogoutSuccessEvent(String userLocation, String username, String sessionId);

    void fireLogoutFailureEvent(String userLocation, String username, String sessionId, Throwable error);

    void fireSessionEvent(String username, String sessionId, String type, Object data);

    void fireSessionFailureEvent(String username, String sessionId, String type, Throwable error);

}
