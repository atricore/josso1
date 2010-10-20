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
package org.josso.agent;

import java.util.EventObject;

/**
 * General event for notifying listeners of significant changes on a LocalSession.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: LocalSessionEvent.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class LocalSessionEvent
        extends EventObject {

    /**
     * The event _data associated with this event.
     */
    private Object _data = null;

    /**
     * The Session on which this event occurred.
     */
    private LocalSession _localSession = null;

    /**
     * The event _type this instance represents.
     */
    private String _type = null;

    /**
     * Construct a new SessionEvent with the specified parameters.
     *
     * @param localSession Local Session on which this event occurred
     * @param type         Event _type
     * @param data         Event _data
     */
    public LocalSessionEvent(LocalSession localSession, String type, Object data) {
        super(localSession);
        this._localSession = localSession;
        this._type = type;
        this._data = data;
    }

    /**
     * Return the event _data of this event.
     */
    public Object getData() {
        return (this._data);
    }

    /**
     * Return the Session on which this event occurred.
     */
    public LocalSession getLocalSession() {
        return (this._localSession);
    }

    /**
     * Return the event _type of this event.
     */
    public String getType() {
        return (this._type);
    }

    /**
     * Return a string representation of this event.
     */
    public String toString() {
        return ("LocalSessionEvent['" + getLocalSession() + "','" +
                getType() + "']");
    }

}
