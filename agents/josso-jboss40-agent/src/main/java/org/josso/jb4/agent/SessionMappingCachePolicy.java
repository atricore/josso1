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

package org.josso.jb4.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.util.CachePolicy;

import java.security.Principal;
import java.util.HashMap;

/**
 * Security Manager JBoss Cache Policy proxy.
 * <p>
 * Its used inside JBoss's JaasSecurityManager class to cache authenticated user entries.
 * This class replaces, in the JaasSecurityManager, the default CachePolicy to allow
 * handling cache entry lookups using SSO Session Identifier Principals as keys.
 *
 * @deprecated No longer needed for JBoss 3.2.6 .
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: SessionMappingCachePolicy.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class SessionMappingCachePolicy implements CachePolicy {
    private static final Log logger = LogFactory.getLog(SessionMappingCachePolicy.class);

    /** HashMap<SSOUserPrincipal, JossoSessionIdPrincipal> */
    private HashMap _userSessionMap = new HashMap();

    /** Proxyed Cache Policy */
    private CachePolicy _cachePolicy;

    /**
     * Constructs a SessionMappingCachePolicy instance which acts as a proxy
     * of the supplied CachePolicy instance.
     *
     * @param cachePolicy the cache policy to be proxyed
     */
    public SessionMappingCachePolicy(CachePolicy cachePolicy ) {
        _cachePolicy = cachePolicy;
    }

    /**
     * Used to associate a Session Principal with a SSOUser Principal.
     * This method is invoked by the JBossCatalinaRealm on successful
     * authentication against the SecurityManager.
     * Everytime an entry is requested given a user Principal key, before
     * calling the proxyed CachePolicy, it will map such key to the
     * session key used for storing cache entries.
     *
     * @param session
     * @param user
     */
    public void attachSessionToUser(Principal session, Principal user ) {
        _userSessionMap.put(user, session);
    }

    /**
     * Returns the object paired with the specified key if it's
     * present in the cache, otherwise must return null. <br>
     * If the supplied key is a user session principal, it will be
     * mapped to the associated user.
     *
     * @param key the key paired with the object
     * @see #peek
     */
    public Object get(Object key) {
    Object targetKey = key;

        logger.debug("Get, Entries = " + _cachePolicy.size());

        synchronized (this) {
            if (_userSessionMap.containsKey(key))
            {
                targetKey = _userSessionMap.get(key);

                logger.debug("Get, mapped user principal '" + key + "'" +
                             " to session principal '" + targetKey + "'"
                            );
            }
        }

        return _cachePolicy.get(targetKey);
    }

    /**
     * Returns the object paired with the specified key if it's
     * present in the cache, otherwise must return null. <br>
     * If the supplied key is a user session principal, it will be
     * mapped to the associated user.
     *
     * @param key the key paired with the object
     * @see #get
     */
    public Object peek(Object key) {
    Object targetKey = key;

        logger.debug("Peek, Entries = " + _cachePolicy.size());

        synchronized (this) {
            if (_userSessionMap.containsKey(key))
            {
                targetKey = _userSessionMap.get(key);

                logger.debug("Peek, mapped user principal '" + key + "'" +
                             " to session principal '" + targetKey + "'"
                            );
            }
        }

        return _cachePolicy.peek(targetKey);
    }

    /**
     * Inserts the specified object into the cache following the
     * implemented policy. <br>
     *
     * @param key the key paired with the object
     * @param object the object to cache
     * @see #remove
     */
    public void insert(Object key, Object object) {
        logger.debug("Insert, key = " + key + ", object = " + object + " Entries = " + _cachePolicy.size());

        _cachePolicy.insert(key, object);
    }

    /**
     * Remove the cached object paired with the specified key. <br>
     * In case the supplied key is a user principal mapped to a session principal, it will
     * be removed.
     *
     * @param key the key paired with the object
     * @see #insert
     */
    public void remove(Object key) {
        logger.debug("Remove, key = " + key +", Entries = " + _cachePolicy.size());

            synchronized (this) {
                if (_userSessionMap.containsKey(key))
                {
                    _userSessionMap.remove(key);
                    logger.debug("Remove, removed session mapping for user '" + key + "'");
                }
            }

        _cachePolicy.remove(key);
    }

    /**
     * Flushes the cached objects from the cache.<br>
     * All user-to-session mapping are removed as well.
     */
    public void flush() {
        logger.debug("Flush Entries = " + _cachePolicy.size());

        _userSessionMap.clear();
        _cachePolicy.flush();
    }

    /**
     * Get the size of the cache.
     */
    public int size() {
        return _cachePolicy.size();
    }

    /**
     * create the service, do expensive operations etc
     */
    public void create() throws Exception {
        _cachePolicy.create();
    }

    /**
     * start the service, create is already called
     */
    public void start() throws Exception {
        _cachePolicy.start();
    }

    /**
     * stop the service
     */
    public void stop() {
        _cachePolicy.stop();
    }

    /**
     * destroy the service, tear down
     */
    public void destroy() {
        _cachePolicy.destroy();
    }
}
