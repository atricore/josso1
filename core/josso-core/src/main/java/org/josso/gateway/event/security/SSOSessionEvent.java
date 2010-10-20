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


import org.josso.gateway.event.BaseSSOEvent;

/**
 * Event for notifying chages releated to an SSO Session.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOSessionEvent.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class SSOSessionEvent extends BaseSSOEvent {

    /**
     * The data associated with this event.
     */
    private Object data = null;

    private String username;

    /**
     * Construct a new SessionEvent
     *
     * @param sessionId SSO Session identifier on which this event occurred
     * @param type
     * @param data
     */
    public SSOSessionEvent(String username, String sessionId, String type, Object data) {
        super(type, sessionId);
        this.data = data;
        this.username = username;
    }

    public SSOSessionEvent(String username, String sessionId, String type, Throwable error) {
        super(type, sessionId, error);
        this.username = username;
    }


    /**
     * Return the event this._data of this event.
     */
    public Object getData() {
        return (this.data);
    }

    /**
     * Return the Session on which this event occurred.
     */
    public String getSessionId() {
        return (String) getSource();
    }

    /**
     * Returns the username associated to this event.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Return a string representation of this event.
     */
    public String toString() {
        return ("SSOSessionEvent['" + getSessionId() + "','" +
                getType() + "']");
    }

}


