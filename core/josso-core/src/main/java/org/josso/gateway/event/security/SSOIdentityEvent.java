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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.auth.Credential;
import org.josso.gateway.event.BaseSSOEvent;

import java.security.Principal;

/**
 * Represents an authorizantion event, like authorization success or authorization failure.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOIdentityEvent.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SSOIdentityEvent extends BaseSSOEvent {

    private static final Log logger = LogFactory.getLog(SSOIdentityEvent.class);

    /**
     * The AuthenticationEvent event type when a user is successfully authenticated.
     */
    public static final String LOGIN_SUCCESS_EVENT = "authenticationSuccess";

    /**
     * The AuthenticationEvent event type when a user authentication fails.
     */
    public static final String LOGIN_FAILED_EVENT = "authenticationFailed";


    /**
     * The AuthenticationEvent event type when a user logout fails.
     */
    public static final String LOGOUT_FAILED_EVENT = "logoutFailed";

    /**
     * The AuthenticationEvent event type when a user logout success.
     */
    public static final String LOGOUT_SUCCESS_EVENT = "logoutSuccess";


    private String username;
    private String remoteHost;
    private String scheme;
    private String sessionId;

    /**
     * Constructs an LOGIN_SUCCESS_EVENT event
     *
     * @param username  the authenticated user
     * @param sessionId the session associated with the new authenticated user.
     */
    public SSOIdentityEvent(String remoteHost, String scheme, String username, String sessionId) {
        super(LOGIN_SUCCESS_EVENT, username);
        this.sessionId = sessionId;
        this.scheme = scheme;
        this.remoteHost = remoteHost;
        this.username = username;

    }

    /**
     * Constructs an LOGIN_FAILED_EVENT event
     *
     * @param credentials that failed when attemting to authenticate a user.
     */
    public SSOIdentityEvent(String remoteHost, String scheme, Credential[] credentials, Throwable error) {
        super(LOGIN_FAILED_EVENT, credentials, error);
        this.remoteHost = remoteHost;
        this.scheme = scheme;
        // Try to guess provided username ... !
        try {
            Principal p = Lookup.getInstance().lookupSecurityDomain().getAuthenticator().getPrincipal(scheme, credentials);
            this.username = p.getName();
        } catch (Exception e) {
            logger.warn("Cannot derive principal name based on credentials ...");
        }

    }

    /**
     * Constructs an LOGOUT_SUCCESS_EVENT event
     *
     * @param username  the authenticated user
     * @param sessionId the session associated with the new authenticated user.
     */
    public SSOIdentityEvent(String remoteHost, String username, String sessionId) {
        super(LOGOUT_SUCCESS_EVENT, username);
        this.sessionId = sessionId;
        this.remoteHost = remoteHost;
        this.username = username;
    }

    /**
     * Constructs an LOGOUT_SUCCESS_EVENT event
     *
     * @param username  the authenticated user
     * @param sessionId the session associated with the new authenticated user.
     */
    public SSOIdentityEvent(String remoteHost, String username, String sessionId, Throwable error) {
        super(LOGOUT_FAILED_EVENT, username, error);
        this.sessionId = sessionId;
        this.remoteHost = remoteHost;
        this.username = username;
    }


    public String getUsername() {
        return username;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getScheme() {
        return scheme;
    }

    /**
     * Return a string representation of this event.
     */
    public String toString() {
        return ("SSOIdentityEvent['" + getType() + "']");
    }


}
