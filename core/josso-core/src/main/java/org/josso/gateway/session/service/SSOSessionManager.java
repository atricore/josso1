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
package org.josso.gateway.session.service;

import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.exceptions.TooManyOpenSessionsException;
import org.josso.gateway.session.service.store.SessionStore;

import javax.security.auth.Subject;
import java.util.Collection;

/**
 * SSO Session Manager Business interface.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: SSOSessionManager.java 568 2008-07-31 18:39:20Z sgonzalez $
 */
public interface SSOSessionManager extends java.io.Serializable {

    static final String TOKEN_TYPE = SSOSessionManager.class.getName();

    /**
     * The security domain where this SSO Session Manager is configured
     */
    void setSecurityDomainName(String securityDomainName);

    /**
     * Initiates a new session. The session id is returned.
     *
     * @return the new session identifier.
     * @throws TooManyOpenSessionsException if the number of open sessions is exceeded.
     */
    String initiateSession(String username, Subject subject)
            throws SSOSessionException, TooManyOpenSessionsException;

    /**
     * This method accesss the session associated to the received id.
     * This resets the session last access time and updates the access count.
     *
     * @param sessionId the session id previously returned by initiateSession.
     * @throws NoSuchSessionException if the session id is not valid or the session is not valid.
     */
    void accessSession(String sessionId)
            throws NoSuchSessionException, SSOSessionException;

    /**
     * Gets an SSO session based on its id.
     *
     * @param sessionId the session id previously returned by initiateSession.
     * @throws org.josso.gateway.session.exceptions.NoSuchSessionException
     *          if the session id is not related to any sso session.
     */
    SSOSession getSession(String sessionId)
            throws NoSuchSessionException, SSOSessionException;

    /**
     * Gets all SSO sessions.
     */
    Collection getSessions()
            throws SSOSessionException;

    /**
     * Gets an SSO session based on the associated user.
     *
     * @param username the username used when initiating the session.
     * @throws org.josso.gateway.session.exceptions.NoSuchSessionException
     *          if the session id is not related to any sso session.
     */
    Collection getUserSessions(String username)
            throws NoSuchSessionException, SSOSessionException;

    /**
     * Invalidates all open sessions.
     */
    void invalidateAll()
            throws SSOSessionException;

    /**
     * Invalidates a session.
     *
     * @param sessionId the session id previously returned by initiateSession.
     * @throws org.josso.gateway.session.exceptions.NoSuchSessionException
     *          if the session id is not related to any sso session.
     */
    void invalidate(String sessionId)
            throws NoSuchSessionException, SSOSessionException;

    /**
     * Check all sessions and remove those that are not valid from the store.
     * This method is invoked periodically to update sessions state.
     */
    void checkValidSessions();

    /**
     * SessionStore instance is injected before initializing the manager.
     */
    void setSessionStore(SessionStore ss);

    /**
     * SessionIdGenerator instance is injected before initializing the manager.
     */
    void setSessionIdGenerator(SessionIdGenerator g);

    /**
     * Initialize this manager
     */
    void initialize();

    /**
     * Destroy the manager and free resources (running threads).
     */
    void destroy();
    
    /**
     * Returns the total number of registerd sessions
     */
    int getSessionCount() throws SSOSessionException;

}
