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
package org.josso.auth;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.exceptions.AuthenticationFailureException;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.AuthenticationScheme;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.List;
import java.util.ArrayList;

/**
 * This is the default authenticator implementation.
 *
 * @org.apache.xbean.XBean element="authenticator"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: AuthenticatorImpl.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public class AuthenticatorImpl implements Authenticator {

    private static final Log logger = LogFactory.getLog(AuthenticatorImpl.class);

    private long _authCount;
    private long _authFailures;

    // Prototype instance for authentication scheme.
    private List<AuthenticationScheme> _as;

    /**
     * Validates user identity.  Populates the Subject with Principal and Credential information.
     *
     * @param credentials the credentials to be checked
     * @param schemeName  the authentication scheme to be used to check the supplied credentials.
     */
    public Subject check(Credential[] credentials, String schemeName)
            throws SSOAuthenticationException {

        // Initialize the AuthenticationScheme
        Subject s = new Subject();
        AuthenticationScheme scheme = getScheme(schemeName);
        scheme.initialize(credentials, s);

        if (scheme.authenticate()) {
            scheme.confirm();
            _authCount++;
        } else {
            scheme.cancel();
            _authFailures++;

            throw new AuthenticationFailureException(scheme.getPrincipal().getName());
        }

        return s;
    }

    public Credential newCredential(String schemeName, String name, Object value) throws SSOAuthenticationException {
        return getScheme(schemeName).newCredential(name, value);
    }

    public Principal getPrincipal(String schemeName, Credential[] credentials) {
        return getScheme(schemeName).getPrincipal(credentials);
    }

    /**
     * A prototype instance of the used authentication scheme is injected.
     * This isntance will be cloned for each authentication process.
     */
    public void setAuthenticationSchemes(AuthenticationScheme[] as) {
        _as = new ArrayList<AuthenticationScheme>();
        for (int i = 0; i < as.length; i++) {
            AuthenticationScheme a = as[i];
            logger.info("[setAuthenticationScheme()] : " + a.getName() + "," + a.getClass().getName());
            _as.add(a);
        }

    }

    public AuthenticationScheme[] getAuthenticationSchemes() {
        return this._as.toArray(new AuthenticationScheme[_as.size()]);
    }

    public AuthenticationScheme getAuthenticationScheme(String name) {
        return this.getScheme(name);
    }

    /**
     * @org.apache.xbean.Property alias="schemes" nestedType="org.josso.auth.AuthenticationScheme"
     * @return
     */
    public List<AuthenticationScheme> getSchemes() {
        return _as;
    }

    public void setSchemes(List<AuthenticationScheme> schemes) {
        this._as = schemes;
    }



    public long getAuthCount() {
        return _authCount;
    }

    public long getAuthFailures() {
        return _authFailures;
    }

    public List<String> getSchemeNames() {
        List<String> names = new ArrayList<String>(_as.size());
        for (AuthenticationScheme s: _as) {
            names.add(s.getName());
        }
        return names;

    }

    // --------------------------------------------------------------
    // Protected utils
    // --------------------------------------------------------------

    /**
     * This method clones the configured authentication scheme because
     * authentication schemes are not thread safe.  It's a "prototype" pattern.
     *
     * @param schemeName the name of the authentication scheme to instantiate.
     * @return the cloned AuthenticationScheme
     */
    protected AuthenticationScheme getScheme(String schemeName) {
        for (int i = 0; i < _as.size(); i++) {
            AuthenticationScheme a = _as.get(i);

            if (logger.isDebugEnabled())
                logger.debug("getScheme() : checking " + a.getName());

            if (a.getName().equals(schemeName))
                return (AuthenticationScheme) a.clone();
        }

        logger.warn("Authentication scheme ["+schemeName+"] not registered!");
        return null;
    }

}
