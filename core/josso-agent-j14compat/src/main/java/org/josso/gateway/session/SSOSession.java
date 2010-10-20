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
package org.josso.gateway.session;

import java.io.Serializable;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOSession.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface SSOSession extends Serializable {

    /**
     * The SessionEvent event type when a session is created.
     */
    public static final String SESSION_CREATED_EVENT = "createSession";


    /**
     * The SessionEvent event type when a session is destroyed.
     */
    public static final String SESSION_DESTROYED_EVENT = "destroySession";

    /**
     * Gets the sso session identifier.
     */
    String getId();

    /**
     * Indicates if this is a valid session.
     */
    boolean isValid();

    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the SSO Service will invalidate the session.  A negative
     * time indicates that the session should never time out.
     *
     * @param interval The new maximum interval in seconds
     */
    void setMaxInactiveInterval(int interval);

    /**
     * Set the maximum time interval, in seconds, between client requests
     * before the SSO Service will invalidate the session.  A negative
     * time indicates that the session should never time out.
     */
    int getMaxInactiveInterval();

    /**
     * Gets this session creation time in milliseconds.
     */
    long getCreationTime();

    /**
     * Sends a session event.
     */
    void fireSessionEvent(String type, Object data);

    /**
     * Gets this session last access time in milliseconds.
     */
    long getLastAccessTime();

    /**
     * Gets this session access count.
     */
    long getAccessCount();

    /**
     * Getter for the username associated to this session.
     */
    String getUsername();
}