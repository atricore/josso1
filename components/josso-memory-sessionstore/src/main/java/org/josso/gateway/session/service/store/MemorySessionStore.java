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
package org.josso.gateway.session.service.store;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.BaseSession;

import java.util.*;

/**
 * This is a memory based store that uses a Map
 * This implementation is thread safe.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: MemorySessionStore.java 543 2008-03-18 21:34:58Z sgonzalez $
 *
 * @org.apache.xbean.XBean element="memory-store"
 */

public class MemorySessionStore extends AbstractSessionStore {

    private static final Log logger = LogFactory.getLog(MemorySessionStore.class);

    private Map _sessions;
    private Map _sessionsByUsername;

    public MemorySessionStore() {
        _sessions = new HashMap();
        _sessionsByUsername = new HashMap();
    }

    public int getSize() throws SSOSessionException {
        synchronized (_sessions) {
            return _sessions.size();
        }
    }

    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     */
    public String[] keys() throws SSOSessionException {
        synchronized (_sessions) {
            return (String[]) _sessions.keySet().toArray(new String[_sessions.size()]);
        }
    }

    /**
     * Return an array of all BaseSessions in this store.  If there are not
     * sessions, then return a zero-length array.
     */
    public BaseSession[] loadAll() throws SSOSessionException {
        synchronized (_sessions) {
            return (BaseSession[]) _sessions.values().toArray(new BaseSession[_sessions.size()]);
        }
    }

    /**
     * Load and return the BaseSession associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored BaseSession, return <code>null</code>.
     *
     * @param id BaseSession identifier of the session to load
     */
    public BaseSession load(String id) throws SSOSessionException {
        BaseSession s = null;
        synchronized (_sessions) {
            s = (BaseSession) _sessions.get(id);
        }

        if (logger.isDebugEnabled())
            logger.debug("[load(" + id + ")] Session " + (s == null ? " not" : "") + " found");

        return s;

    }

    /**
     * Load and return the BaseSession associated with the specified username
     * from this Store, without removing it.  If there is no
     * such stored BaseSession, return <code>null</code>.
     *
     * @param name username of the session to load
     */
    public BaseSession[] loadByUsername(String name) throws SSOSessionException {
        BaseSession result[];

        synchronized (_sessions) {
            Set sessions = (Set) _sessionsByUsername.get(name);
            if (sessions == null)
                sessions = new HashSet();
            result = (BaseSession[]) sessions.toArray(new BaseSession[sessions.size()]);
        }

        if (logger.isDebugEnabled())
            logger.debug("[loadByUsername(" + name + ")] Sessions found =  " + result.length);

        return result;

    }

    /**
     * Load and return the BaseSessions whose last access time is less than the received time
     */
    public BaseSession[] loadByLastAccessTime(Date time) throws SSOSessionException {
        List results = new ArrayList();
        synchronized (_sessions) {
            Collection sessions = _sessions.values();
            for (Iterator iterator = sessions.iterator(); iterator.hasNext();) {
                BaseSession session = (BaseSession) iterator.next();
                if (session.getLastAccessTime() < time.getTime()) {
                    results.add(session);
                }
            }
        }

        return (BaseSession[]) results.toArray(new BaseSession[results.size()]);

    }

    public BaseSession[] loadByValid(boolean valid) throws SSOSessionException {
        List results = new ArrayList();
        synchronized (_sessions) {
            Collection sessions = _sessions.values();
            for (Iterator iterator = sessions.iterator(); iterator.hasNext();) {
                BaseSession session = (BaseSession) iterator.next();
                if (session.isValid() == valid) {
                    results.add(session);
                }
            }
        }

        return (BaseSession[]) results.toArray(new BaseSession[results.size()]);
    }


    /**
     * Remove the BaseSession with the specified session identifier from
     * this Store, if present.  If no such BaseSession is present, this method
     * takes no action.
     *
     * @param id BaseSession identifier of the BaseSession to be removed
     */
    public void remove(String id) throws SSOSessionException {
        BaseSession session = null;
        synchronized (_sessions) {
            session = (BaseSession) _sessions.remove(id);
            if (session != null && session.getUsername() != null) {
                Set userSessions = (Set) _sessionsByUsername.get(session.getUsername());
                userSessions.remove(session);
            }

        }

        if (logger.isDebugEnabled())
            logger.debug("[remove(" + id + ")] Session " + (session == null ? " not" : "") + " found");
    }

    /**
     * Remove all Sessions from this Store.
     */
    public void clear() throws SSOSessionException {
        synchronized (_sessions) {
            _sessions.clear();
            _sessionsByUsername.clear();
        }
    }

    /**
     * Save the specified BaseSession into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session BaseSession to be saved
     */
    public void save(BaseSession session) throws SSOSessionException {
        BaseSession oldSession = null;
        synchronized (_sessions) {

            // Replace old session.
            oldSession = (BaseSession) _sessions.put(session.getId(), session);

            // Check if this is an update or an insert :
            if (oldSession != null) {

                // Updating old session :
                String oldUsername = oldSession.getUsername();

                if (oldUsername != null) {
                    // Remove old association
                    Set userSessions = (Set) _sessionsByUsername.get(oldUsername);
                    if (userSessions != null) {
                        userSessions.remove(oldSession);
                        if (logger.isDebugEnabled())
                            logger.debug("Removing old session from reverse map : " + oldSession.getId() + ". user=" + oldUsername);
                    }
                }
            }

            // Add new session to reverse map.
            if (session.getUsername() != null) {
                Set sessions = (Set) _sessionsByUsername.get(session.getUsername());
                if (sessions == null) {

                    if (logger.isDebugEnabled())
                        logger.debug("Building new set for user " + session.getUsername());

                    sessions = new HashSet();
                    _sessionsByUsername.put(session.getUsername(), sessions);
                }

                if (logger.isDebugEnabled())
                    logger.debug("Adding session to reverse map : " + session.getId() + ". user=" + session.getUsername());

                sessions.add(session);

            }
        }

        if (logger.isDebugEnabled())
            logger.debug("[save(BaseSession." + session.getId() + ")] Session " + (oldSession == null ? " inserted" : "") + " updated");

    }

}
