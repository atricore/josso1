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

package org.josso.servlet.agent;

import org.josso.agent.LocalSession;
import org.josso.agent.LocalSessionEvent;
import org.josso.agent.LocalSessionListener;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Date: Nov 27, 2007
 * Time: 11:35:39 AM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class LocalSessionImpl implements LocalSession {

    /**
     * The session event listeners for this Session.
     */
    private transient ArrayList _listeners = new ArrayList();
    private long _creationTime;


    private String _id;

    private long _lastAccessedTime;

    private int _maxInactiveInterval;
    
    private Object _wrapped;

    public LocalSessionImpl() {
    }

    public long getCreationTime() {
        return _creationTime;
    }

    public String getId() {
        return _id;
    }

    public long getLastAccessedTime() {
        return _lastAccessedTime;
    }

    public void setMaxInactiveInterval(int i) {
        _maxInactiveInterval = i;
    }

    public int getMaxInactiveInterval() {
        return _maxInactiveInterval;
    }

    public void expire() {

        Iterator i = _listeners.iterator();
        while (i.hasNext()) {
            LocalSessionListener listener = (LocalSessionListener) i.next();

            listener.localSessionEvent(new LocalSessionEvent(this, LocalSession.LOCAL_SESSION_DESTROYED_EVENT, null));
        }
    }

    public void addSessionListener(LocalSessionListener sessionListener) {
        _listeners.add(sessionListener);
    }

    public void removeSessionListener(LocalSessionListener sessionListener) {
        _listeners.remove(sessionListener);
    }

    public void exipre() {
        ((HttpSession)_wrapped).invalidate();
    }

    public void setWrapped(Object wrapped) {
        _wrapped = wrapped;
    }

    public Object getWrapped() {
        return _wrapped;
    }
}


