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
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.scheme.AuthenticationScheme;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRole;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUser;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.gateway.identity.service.store.CertificateUserKey;
import org.josso.gateway.identity.service.store.SimpleUserKey;
import org.w3c.dom.Element;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;

/**
 * JDBC Identity DAO, used by AbstractDBIdentityStore.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: IdentityDAO.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class IdentityDAO {

    private static final Log logger = LogFactory.getLog(IdentityDAO.class);

    private Connection _conn;
    private CredentialProvider _cp;
    private String _userQueryString;
    private String _rolesQueryString;
    private String _credentialsQueryString;
    private String _certificateCredentialsQueryString;
    private String _uidQueryString;
    private String _userPropertiesQueryString;
    private int _userPropertiesQueryVariables = 0; // This will be calcultated when setting _userPropertiesQueryString
    private String _resetCredentialDml;
    private String _relayCredentialQueryString;

    public IdentityDAO(Connection conn,
                       CredentialProvider cp,
                       String userQueryString,
                       String rolesQueryString,
                       String credentialsQueryString,
                       String userPropertiesQueryString,
                       String resetCredentialDml,
                       String relayCredentialQueryString,
                       String certificateCredentialsQueryString,
                       String uidQueryString) {

        _conn = conn;
        _cp = cp;
        _userQueryString = userQueryString;
        _rolesQueryString = rolesQueryString;
        _credentialsQueryString = credentialsQueryString;
        _certificateCredentialsQueryString = certificateCredentialsQueryString;
        _uidQueryString = uidQueryString;
        _resetCredentialDml = resetCredentialDml;
        _relayCredentialQueryString = relayCredentialQueryString;

        // User properties query :
        if (userPropertiesQueryString != null) {
            _userPropertiesQueryString = userPropertiesQueryString;
            _userPropertiesQueryVariables = countQueryVariables(_userPropertiesQueryString);
        }
    }

    public BaseUser selectUser(SimpleUserKey key) throws SSOIdentityException {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {

            stmt = createPreparedStatement(_userQueryString);
            stmt.setString(1, key.getId());
            result = stmt.executeQuery();

            BaseUser user = fetchUser(result);
            if (user == null)
                throw new SSOIdentityException("Can't find user for : " + key);

            return user;
        } catch (SQLException sqlE) {
            logger.error("SQLException while listing user", sqlE);
            throw new SSOIdentityException("During user listing: " + sqlE.getMessage());
        } catch (IOException ioE) {
            logger.error("IOException while listing user", ioE);
            throw new SSOIdentityException("During user listing: " + ioE.getMessage());
        } catch (Exception e) {
            logger.error("Exception while listing user", e);
            throw new SSOIdentityException("During user listing: " + e.getMessage());
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
        }

    }

    public BaseRole[] selectRolesByUserKey(SimpleUserKey key) throws SSOIdentityException {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {

            stmt = createPreparedStatement(_rolesQueryString);
            stmt.setString(1, key.getId());
            result = stmt.executeQuery();

            BaseRole[] roles = fetchRoles(result);

            return roles;
        } catch (SQLException sqlE) {
            logger.error("SQLException while listing roles", sqlE);
            throw new SSOIdentityException("During roles listing: " + sqlE.getMessage());

        } catch (IOException ioE) {
            logger.error("IOException while listing roles", ioE);
            throw new SSOIdentityException("During roles listing: " + ioE.getMessage());

        } catch (Exception e) {
            logger.error("Exception while listing roles", e);
            throw new SSOIdentityException("During roles listing: " + e.getMessage());

        } finally {
            closeResultSet(result);
            closeStatement(stmt);
        }
    }

    public Credential[] selectCredentials(SimpleUserKey key) throws SSOIdentityException {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {

        	String credentialsQueryString = _credentialsQueryString;
        	String schemeName = null;
            if (_cp instanceof AuthenticationScheme) {
            	schemeName = ((AuthenticationScheme) _cp).getName();
            }
            if ("strong-authentication".equals(schemeName)) {
            	credentialsQueryString = _certificateCredentialsQueryString;
            }
            
            if (logger.isDebugEnabled())
                logger.debug("[selectCredemtiasl()]]: key=" + key.getId());
            stmt = createPreparedStatement(credentialsQueryString);
            stmt.setString(1, key.getId());
            result = stmt.executeQuery();

            Credential[] creds = fetchCredentials(result);

            return creds;
        } catch (SQLException sqlE) {
            logger.error("SQLException while listing credentials", sqlE);
            throw new SSOIdentityException("During credentials listing: " + sqlE.getMessage());

        } catch (IOException ioE) {
            logger.error("IOException while listing credentials", ioE);
            throw new SSOIdentityException("During credentials listing: " + ioE.getMessage());

        } catch (Exception e) {
            logger.error("Exception while listing credentials", e);
            throw new SSOIdentityException("During credentials listing: " + e.getMessage());

        } finally {
            closeResultSet(result);
            closeStatement(stmt);
        }

    }

    public String loadUID(CredentialKey key) throws SSOIdentityException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        String uid = null;

        try {

        	if (key instanceof CertificateUserKey) {
    			X509Certificate certificate = ((CertificateUserKey)key).getCertificate();
    			if (certificate != null) {
    				stmt = createPreparedStatement(_uidQueryString);
		            stmt.setBytes(1, certificate.getEncoded());
		            // Use key id to narrow search scope?
		            //stmt.setString(2, ((CertificateUserKey)key).getId());
		            result = stmt.executeQuery();
		            while (result.next()) {
		                uid = result.getString(1);
		                break;
		            }
    			}
    		} else if (key instanceof SimpleUserKey) {
    			uid = ((SimpleUserKey)key).getId();
    		}

        } catch (SQLException sqlE) {
            logger.error("SQLException while finding UID", sqlE);
            throw new SSOIdentityException("During UID lookup: " + sqlE.getMessage());

        } catch (Exception e) {
            logger.error("Exception while finding UID", e);
            throw new SSOIdentityException("During UID lookup: " + e.getMessage());

        } finally {
            closeResultSet(result);
            closeStatement(stmt);
        }

        return uid;
    }
    
    /**
     * This will execute the configured query to get all user properties.
     * Because we sugested the use of 'UNION' key words to retrieve properties from multiple columns/tables,
     * we probably must send multiple times the username value to "avoid not all variables bound" error.
     * We could avoid this by using JDBC 3.0 drivers in the future.
     *
     * @param key
     * @throws SSOIdentityException
     */

    public SSONameValuePair[] selectUserProperties(SimpleUserKey key) throws SSOIdentityException {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {

            if (logger.isDebugEnabled())
                logger.debug("[selectUserProperties()]]: key=" + key.getId());
            stmt = createPreparedStatement(_userPropertiesQueryString);

            // We don't jave JDBC 3.0 drivers, so ... bind all variables manually
            for (int i = 1; i <= _userPropertiesQueryVariables; i++) {
                stmt.setString(i, key.getId());
            }

            result = stmt.executeQuery();

            SSONameValuePair[] props = fetchSSONameValuePairs(result);

            return props;
        } catch (SQLException sqlE) {
            logger.error("SQLException while listing user properties", sqlE);
            throw new SSOIdentityException("During user properties listing: " + sqlE.getMessage());

        } catch (IOException ioE) {
            logger.error("IOException while listing user properties", ioE);
            throw new SSOIdentityException("During user properties listing: " + ioE.getMessage());

        } catch (Exception e) {
            logger.error("Exception while listing user properties", e);
            throw new SSOIdentityException("During user properties listing: " + e.getMessage());

        } finally {
            closeResultSet(result);
            closeStatement(stmt);
        }
    }

    public void resetCredential ( SimpleUserKey key, BaseCredential newPassword) throws SSOIdentityException {
        PreparedStatement stmt = null;
        try {
            if (logger.isDebugEnabled())
                logger.debug("[resetCredential()]]: key=" + key.getId());

            stmt = createPreparedStatement( _resetCredentialDml );
            stmt.setString( 1, newPassword.getValue().toString() );
            stmt.setString( 2, key.getId() );
            stmt.execute();
            _conn.commit();

        } catch (SQLException e) {
            logger.error("SQLException while updating user credential", e);
            throw new SSOIdentityException("During user update credential: " + e.getMessage());
        } finally {
            closeStatement( stmt );
        }
    }

    public String resolveUsernameByRelayCredential(String name, String value) throws SSOIdentityException {
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            if (logger.isDebugEnabled())
                logger.debug("[resolveUsernameByRelayCredential(name, value)]]: name=" + name + " value=" + value);

            if( _relayCredentialQueryString.contains( "#?#" )){
                stmt = createPreparedStatement( _relayCredentialQueryString.replace( "#?#", name ));
                stmt.setString( 1, value );
            } else {
                stmt = createPreparedStatement( _relayCredentialQueryString);
                stmt.setString( 1, name );
                stmt.setString( 2, value );
            }
            result = stmt.executeQuery();

            String username = result.next() ? result.getString( 1 ) : null;
            if( result.next() ){
                throw new SSOIdentityException( "Statement " + stmt + " returned more than one row" );
            }
            return username; 

        } catch (SQLException sqlE) {
            logger.error("SQLException while loading user with relay credential", sqlE);
            throw new SSOIdentityException("During load user with relay credential: " + sqlE.getMessage());

        } catch (Exception e) {
            logger.error("Exception while loading user with relay credential", e);
            throw new SSOIdentityException("During load user with relay credential: " + e.getMessage());

        } finally {
            closeResultSet(result);
            closeStatement(stmt);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Protected DB utils.
    // ------------------------------------------------------------------------------------------

    /**
     * Builds an array of credentials based on a ResultSet
     * Column names are used to build a credential.
     */
    protected Credential[] fetchCredentials(ResultSet rs)
            throws SQLException, IOException, SSOAuthenticationException {

    	List creds = new ArrayList();
        while (rs.next()) {

            ResultSetMetaData md = rs.getMetaData();

            // Each column is a credential, the column name is used as credential name ...
            for (int i = 1; i <= md.getColumnCount(); i++) {
                String cName = md.getColumnLabel(i);
                Object credentialObject = rs.getObject(i);
                String credentialValue = null;
                
                // if the attribute value is an array, cast it to byte[] and then convert to
                // String using proper encoding
                if (credentialObject.getClass().isArray()) {

                    try {
                        // Try to create a UTF-8 String, we use java.nio to handle errors in a better way.
                        // If the byte[] cannot be converted to UTF-8, we're using the credentialObject as is.
                        byte[] credentialData = (byte[]) credentialObject;
                        ByteBuffer in = ByteBuffer.allocate(credentialData.length);
                        in.put(credentialData);
                        in.flip();

                        Charset charset = Charset.forName("UTF-8");
                        CharsetDecoder decoder = charset.newDecoder();
                        CharBuffer charBuffer = decoder.decode(in);

                        credentialValue = charBuffer.toString();

                    } catch (CharacterCodingException e) {
                        if (logger.isDebugEnabled())
                            logger.debug("Can't convert credential value to String using UTF-8");
                    }

                } else if (credentialObject instanceof String) {
                    // The credential value must be a String ...
                    credentialValue = (String) credentialObject;
                }
                
                Credential c = null;
                if (credentialValue != null) {
                	c = _cp.newCredential(cName, credentialValue);
                } else {
                	c = _cp.newCredential(cName, credentialObject);
                }
                
                if (c != null) {
                	creds.add(c);
                }
            }

        }

        return (Credential[]) creds.toArray(new Credential[creds.size()]);
    }

    /**
     * Builds an array of name-value pairs on a ResultSet
     * The resultset must have two columns, the first one contains names and the second one values.
     */
    protected SSONameValuePair[] fetchSSONameValuePairs(ResultSet rs)
            throws SQLException, IOException, SSOAuthenticationException {
        List props = new ArrayList();

        while (rs.next()) {
            // First column is a name and second is a value.
            String cName = rs.getString(1);
            String cValue = rs.getString(2);
            SSONameValuePair prop = new SSONameValuePair(cName, cValue);
            props.add(prop);
        }

        return (SSONameValuePair[]) props.toArray(new SSONameValuePair[props.size()]);
    }


    /**
     * Builds a user based on a result set.
     * ResultSet must have one and only one record.
     */
    protected BaseRole[] fetchRoles(ResultSet rs)
            throws SQLException, IOException {

        List roles = new ArrayList();

        while (rs.next()) {

            BaseRole role = new BaseRoleImpl();
            String rolename = rs.getString(1);
            role.setName(rolename);

            roles.add(role);

        }

        return (BaseRole[]) roles.toArray(new BaseRole[roles.size()]);
    }

    /**
     * Builds a user based on a result set.
     */
    protected BaseUser fetchUser(ResultSet rs)
            throws SQLException, IOException {

        if (rs.next()) {
            BaseUser user = new BaseUserImpl();
            String username = rs.getString(1);
            user.setName(username);

            return user;
        }

        return null;
    }


    /**
     * Creates a new prepared statement for the received query string.
     *
     * @param query
     * @throws SQLException
     */
    private PreparedStatement createPreparedStatement(String query)
            throws SQLException {

        if (logger.isDebugEnabled())
            logger.debug("[createPreparedStatement()] : " + "(" + query + ")");

        PreparedStatement stmt =
                _conn.prepareStatement(query + " ");

        return stmt;
    }

    protected void closeStatement(PreparedStatement stmt)
            throws SSOIdentityException {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException se) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error clossing statement");
            }

            throw new SSOIdentityException("Error while clossing statement: \n " + se.getMessage());

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error clossing statement");
            }

            // throw new SSOIdentityException("Error while clossing statement: \n " + e.getMessage());
        }
    }


    protected void closeResultSet(ResultSet result)
            throws SSOIdentityException {
        try {
            if (result != null) {
                result.close();
            }
        } catch (SQLException se) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error while clossing result set");
            }

            throw new SSOIdentityException("SQL Exception while closing\n" + se.getMessage());
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error while clossing result set");
            }

            // throw new SSOIdentityException("Exception while closing Result Set\n" + e.getMessage());

        }
    }

    /**
     * This util counts the number of times that the '?' char appears in the received query string.
     */
    protected int countQueryVariables(String qry) {
        StringTokenizer st = new StringTokenizer(qry, "?", true);
        int count = 0;
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            if ("?".equals(tk)) {
                count++;
            }
        }
        return count;
    }


}
