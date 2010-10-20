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
package org.josso.gateway.identity.service;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.gateway.identity.service.store.ExtendedIdentityStore;
import org.josso.selfservices.ChallengeResponseCredential;
import org.josso.auth.Credential;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.store.*;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;
import org.josso.gateway.session.service.BaseSession;
import org.josso.gateway.session.service.SSOSessionManager;

/**
 * @org.apache.xbean.XBean element="identity-manager"
 *
 * This is the default implementation of an SSOIdentityManager.
 * This implementation keeps track of user and session associations in memory.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOIdentityManagerImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class SSOIdentityManagerImpl implements SSOIdentityManager {

    private static final Log logger = LogFactory.getLog(SSOIdentityManagerImpl.class);

    // Identity store used by the manager.
    private IdentityStore _store;
    private IdentityStoreKeyAdapter _keyAdapter;
    private SSOSessionManager _sessionManager;

    /**
     *
     */
    public SSOIdentityManagerImpl() {
    }

    /**
     * Finds a user based on its name.
     *
     * @param name the user login name, wich is unique for a domain.
     * @throws NoSuchUserException if the user does not exist for the domain.
     */
    public SSOUser findUser(String name)
            throws NoSuchUserException, SSOIdentityException {

        // Find user in store
        UserKey key = getIdentityStoreKeyAdapter().getKeyForUsername(name);
        BaseUser user = getIdentityStore().loadUser(key);
        if (user == null)
            throw new NoSuchUserException(key);

        // Done ... user found.
        return user;
    }

    /**
     * Finds the user associated to a sso session
     *
     * @param sessionId the sso session identifier
     * @throws SSOIdentityException if no user is associated to this session id.
     */
    public SSOUser findUserInSession(String sessionId)
            throws SSOIdentityException {

        BaseUser user = null;
        UserKey key = null;

        try {
            BaseSession s = (BaseSession) getSessionManager().getSession(sessionId);
            key = new SimpleUserKey(s.getUsername());
            user = getIdentityStore().loadUser(key);

            if (logger.isDebugEnabled())
                logger.debug("[findUserInSession(" + sessionId + ")] Found :  " + user);

            return user;

        } catch (NoSuchSessionException e) {
            throw new SSOIdentityException("Invalid session : " + sessionId);

        } catch (SSOSessionException e) {
            throw new SSOIdentityException(e.getMessage(), e);
        }

    }


    /**
     * Finds a collection of user's roles.
     * Elements in the collection are SSORole instances.
     *
     * @param username
     * @throws SSOIdentityException
     */
    public SSORole[] findRolesByUsername(String username)
            throws SSOIdentityException {

        UserKey key = getIdentityStoreKeyAdapter().getKeyForUsername(username);
        return getIdentityStore().findRolesByUserKey(key);
    }

    /**
     * Checks if current user exists in this manager.
     *
     * @throws NoSuchUserException  if the user does not exists.
     * @throws SSOIdentityException if an error occurs
     */
    public void userExists(String username) throws NoSuchUserException, SSOIdentityException {
        UserKey key = getIdentityStoreKeyAdapter().getKeyForUsername(username);
        if (!getIdentityStore().userExists(key))
            throw new NoSuchUserException(key);
    }

    public void updateAccountPassword(SSOUser user, Credential password) throws NoSuchUserException, SSOIdentityException {
        if (this._store instanceof ExtendedIdentityStore) {
            UserKey key = getIdentityStoreKeyAdapter().getKeyForUsername(user.getName());
            ExtendedIdentityStore eStore = (ExtendedIdentityStore) _store;
            eStore.updateAccountPassword(key, password);
        } else {
            throw new UnsupportedOperationException("The configured identity store implementatino does not support account update.");
        }

    }


    public String findUsernameByRelayCredential(ChallengeResponseCredential relayCredential) throws SSOIdentityException {
        if (this._store instanceof ExtendedIdentityStore) {

            ExtendedIdentityStore eStore = (ExtendedIdentityStore) _store;
            return eStore.loadUsernameByRelayCredential(relayCredential);
        } else {
            throw new UnsupportedOperationException("The configured identity store implementatino does not support account update.");
        }

    }



    // --------------------------------------------------------------------
    // Public utils
    // --------------------------------------------------------------------

    /**
     * Used to set the store for this manager.
     *
     * @param s
     */
    public void setIdentityStore(IdentityStore s) {
        _store = s;
    }

    public void setIdentityStoreKeyAdapter(IdentityStoreKeyAdapter a) {
        _keyAdapter = a;
    }

    public void initialize() {

    }

    // --------------------------------------------------------------------
    // Protected utils
    // --------------------------------------------------------------------

    protected IdentityStore getIdentityStore() {
        return _store;
    }

    protected IdentityStoreKeyAdapter getIdentityStoreKeyAdapter() {
        return _keyAdapter;
    }

    protected SSOSessionManager getSessionManager() {

        if (_sessionManager == null) {

            try {
                _sessionManager = Lookup.getInstance().lookupSecurityDomain().getSessionManager();
            } catch (Exception e) {
                logger.error("Can't find Session Manager : \n" + e.getMessage() != null ? e.getMessage() : e.toString(), e);
            }
        }

        return _sessionManager;
    }


}
