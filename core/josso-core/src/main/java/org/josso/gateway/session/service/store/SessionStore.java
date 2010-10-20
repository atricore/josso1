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

import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.BaseSession;

import java.util.Date;

/**
 * Represents a resource to store sessions.
 * Implementations define the specific persistence mechanism to store sessions.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SessionStore.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface SessionStore {

    /**
     * Return the number of Sessions present in this Store.
     */
    int getSize() throws SSOSessionException;


    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     */
    String[] keys() throws SSOSessionException;

    /**
     * Return an array of all BaseSessions in this store.  If there are no
     * sessions, then return a zero-length array.
     */
    BaseSession[] loadAll() throws SSOSessionException;

    /**
     * Load and return the BaseSession associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored BaseSession, return <code>null</code>.
     *
     * @param id BaseSession identifier of the session to load
     */
    BaseSession load(String id)
            throws SSOSessionException;

    /**
     * Load and return the BaseSession associated with the specified username
     * from this Store, without removing it.  If there is no
     * such stored BaseSession, return <code>null</code>.
     *
     * @param name username of the session to load
     */
    BaseSession[] loadByUsername(String name)
            throws SSOSessionException;

    /**
     * Load and return the BaseSessions whose last access time is less than the received time
     */
    BaseSession[] loadByLastAccessTime(Date time) throws SSOSessionException;

    /**
     * Load and return the BaseSessions whose valid property is equals to the valid argument.
     */
    BaseSession[] loadByValid(boolean valid) throws SSOSessionException;

    /**
     * Remove the BaseSession with the specified session identifier from
     * this Store, if present.  If no such BaseSession is present, this method
     * takes no action.
     *
     * @param id BaseSession identifier of the BaseSession to be removed
     */
    void remove(String id) throws SSOSessionException;


    /**
     * Remove all Sessions from this Store.
     */
    void clear() throws SSOSessionException;

    /**
     * Save the specified BaseSession into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session BaseSession to be saved
     */
    void save(BaseSession session) throws SSOSessionException;


}
