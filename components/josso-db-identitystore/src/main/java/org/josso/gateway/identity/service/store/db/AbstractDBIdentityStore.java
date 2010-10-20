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
package org.josso.gateway.identity.service.store.db;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.Credential;
import org.josso.auth.CredentialKey;
import org.josso.auth.CredentialProvider;
import org.josso.auth.BaseCredential;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.store.AbstractStore;
import org.josso.gateway.identity.service.store.CertificateUserKey;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.josso.gateway.identity.service.store.UserKey;
import org.josso.gateway.identity.service.store.ExtendedIdentityStore;
import org.josso.selfservices.ChallengeResponseCredential;
import org.w3c.dom.Element;

import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

/**
 * DB implementation of an IdentityStore and CredentialStore.
 * Three querys have to be configured to the store :
 * <p/>
 * - UserQueryString : Used to validate user existence = "SELECT MY_USER FROM MY_USER_TABLE WHERE MY_USR = '?'"
 * - RolesQueryString : Used to retrieve user's roles = "SELECT MY_ROLE FROM MY_USER_ROLES_TABLE WHERE MY_USER = '?'";
 * - CredentialQueryString : Used to retrieve known credentials for a user.
 * This query depends on the configured authentication scheme.  For a user / password based scheme the query could be
 * "SELECT MY_USER AS USERNAME, MY_PWD AS PASSWORD FROM MY_USER_TABLE WHERE MY_USER ='?';
 * The alias is important as is used to map the retrieved value to a specific credential type.
 * <p/>
 * Subclasses have to implement getDBConnection() method, this allows jdbc/datasource based stores.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: AbstractDBIdentityStore.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public abstract class AbstractDBIdentityStore extends AbstractStore implements ExtendedIdentityStore {

    private static final Log logger = LogFactory.getLog(AbstractDBIdentityStore.class);

    private String _userQueryString;
    private String _rolesQueryString;
    private String _credentialsQueryString;
    private String _certificateCredentialsQueryString;
    private String _uidQueryString;
    private String _userPropertiesQueryString;
    private String _resetCredentialDml;
    private String _relayCredentialQueryString;

    // ---------------------------------------------------------------
    // AbstractStore extension.
    // ---------------------------------------------------------------

    public BaseUser loadUser(UserKey key) throws NoSuchUserException, SSOIdentityException {
        Connection c = null;
        try {
            if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            c = getDBConnection();
            IdentityDAO dao = getIdentityDAO(c);
            BaseUser user = dao.selectUser((SimpleUserKey) key);

            // Optionally find user properties.
            if (getUserPropertiesQueryString() != null) {
                SSONameValuePair[] props = dao.selectUserProperties((SimpleUserKey) key);
                user.setProperties(props);
            }

            return user;

        } finally {
            closeDBConnection(c);
        }
    }

    public BaseRole[] findRolesByUserKey(UserKey key) throws SSOIdentityException {
        Connection c = null;
        try {
            if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            c = getDBConnection();
            IdentityDAO dao = getIdentityDAO(c);
            BaseRole[] roles = dao.selectRolesByUserKey((SimpleUserKey) key);
            return roles;

        } finally {
            closeDBConnection(c);
        }
    }

    public Credential[] loadCredentials(CredentialKey key, CredentialProvider cp) throws SSOIdentityException {
        Connection c = null;
        try {
            if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            c = getDBConnection();
            IdentityDAO dao = getIdentityDAO(c, cp);
            Credential[] credentials = dao.selectCredentials((SimpleUserKey) key);
            return credentials;

        } finally {
            closeDBConnection(c);
        }

    }

    public String loadUID(CredentialKey key, CredentialProvider cp)
			throws SSOIdentityException {
    	Connection c = null;
        try {
        	if (!(key instanceof SimpleUserKey)) {
                throw new SSOIdentityException("Unsupported key type : " + key.getClass().getName());
            }

            c = getDBConnection();
            IdentityDAO dao = getIdentityDAO(c, cp);
            return dao.loadUID(key);
        } finally {
            closeDBConnection(c);
        }
	}
    
    // ---------------------------------------------------------------
    // ExtendedIdentityStore implementation.
    // ---------------------------------------------------------------
    public String loadUsernameByRelayCredential ( ChallengeResponseCredential cred ) throws SSOIdentityException {
        Connection c = null;
        try {
            c = getDBConnection();
            IdentityDAO dao = getIdentityDAO(c);
            return dao.resolveUsernameByRelayCredential( cred.getId(), cred.getResponse() );

        } finally {
            closeDBConnection(c);
        }
    }

    public void updateAccountPassword ( UserKey key, Credential newPassword ) throws SSOIdentityException {
        Connection c = null;
        try {
            if (!(newPassword instanceof BaseCredential )) {
                throw new SSOIdentityException("Unsupported Credential type : " + newPassword.getClass().getName());
            }

            if (!(key instanceof SimpleUserKey )) {
                throw new SSOIdentityException("Unsupported UserKey type : " + key.getClass().getName());
            }

            c = getDBConnection();
            IdentityDAO dao = getIdentityDAO(c);
            dao.resetCredential( (SimpleUserKey)key, (BaseCredential)newPassword );

        } finally {
            closeDBConnection(c);
        }
    }

    // -----------------------------------------------------------------------------------
    // Protected utils.
    // -----------------------------------------------------------------------------------

    /**
     * Subclasses must implement getDBConnection() method.
     */
    protected abstract Connection getDBConnection() throws SSOIdentityException;

    protected IdentityDAO getIdentityDAO(Connection c, CredentialProvider cp) {

        return new IdentityDAO(c,
                cp,
                getUserQueryString(),
                getRolesQueryString(),
                getCredentialsQueryString(),
                getUserPropertiesQueryString(),
                getResetCredentialDml(),
                getRelayCredentialQueryString(),
                getCertificateCredentialsQueryString(),
                getUidQueryString());
    }

    protected IdentityDAO getIdentityDAO(Connection c) {

        return new IdentityDAO(c,
                null,
                getUserQueryString(),
                getRolesQueryString(),
                getCredentialsQueryString(),
                getUserPropertiesQueryString(),
                getResetCredentialDml(),
                getRelayCredentialQueryString(),
                getCertificateCredentialsQueryString(),
                getUidQueryString());
        
    }

    /**
     * Close the given db connection.
     *
     * @param dbConnection
     * @throws SSOIdentityException
     */
    protected void closeDBConnection(Connection dbConnection) throws SSOIdentityException {

        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        }
        catch (SQLException se) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error while clossing connection");
            }

            throw new SSOIdentityException(
                    "Error while clossing connection\n" + se.getMessage());
        }
        catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error while clossing connection");
            }

            throw new SSOIdentityException(
                    "Error while clossing connection\n" + e.getMessage());

        }

    }

    // ---------------------------------------------------------------
    // Configuration properties.
    // ---------------------------------------------------------------

    /**
     * The SQL query that returns a user name based on a user key.
     */
    public String getUserQueryString() {
        return _userQueryString;
    }

    /**
     * The SQL query that returns the list of roles associated with a given user.
     */
    public String getRolesQueryString() {
        return _rolesQueryString;
    }

    /**
     * The SQL query that returns the list of known credentials associated with a given user.
     */
    public String getCredentialsQueryString() {
        return _credentialsQueryString;
    }

    /**
     * The SQL query that returns the list of known certificate credentials associated with a given user.
     */
    public String getCertificateCredentialsQueryString() {
        return _certificateCredentialsQueryString;
    }
    
    /**
     * The SQL query that returns the UID.
     */
    public String getUidQueryString() {
        return _uidQueryString;
    }
    
    /**
     * The SQL query that returns the list of properties associated with a given user.
     */
    public String getUserPropertiesQueryString() {
        return _userPropertiesQueryString;
    }

    public void setUserQueryString(String userQueryString) {
        _userQueryString = userQueryString;
    }

    public void setRolesQueryString(String rolesQueryString) {
        _rolesQueryString = rolesQueryString;
    }

    public void setCredentialsQueryString(String credentialsQueryString) {
        _credentialsQueryString = credentialsQueryString;
    }

    public void setCertificateCredentialsQueryString(String certificateCredentialsQueryString) {
    	_certificateCredentialsQueryString = certificateCredentialsQueryString;
    }
    
    public void setUidQueryString(String uidQueryString) {
    	_uidQueryString = uidQueryString;
    }
    
    public void setUserPropertiesQueryString(String userPropertiesQueryString) {
        _userPropertiesQueryString = userPropertiesQueryString;
    }

    public String getResetCredentialDml () {
        return _resetCredentialDml;
    }

    public void setResetCredentialDml ( String resetCredentialDml ) {
        this._resetCredentialDml = resetCredentialDml;
    }

    public String getRelayCredentialQueryString () {
        return _relayCredentialQueryString;
    }

    public void setRelayCredentialQueryString ( String relayCredentialQueryString ) {
        this._relayCredentialQueryString = relayCredentialQueryString;
    }
}
