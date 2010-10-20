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
package org.josso.auth.scheme;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.auth.Credential;
import org.josso.auth.CredentialProvider;
import org.josso.auth.SimplePrincipal;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.josso.auth.util.Crypt;
import org.josso.auth.util.CipherUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

/**
 * Basic authentication scheme, supporting username and password credentials.
 * <p/>
 * Configuration properties supported by this authenticator are :
 * <ul>
 * <li>hashAlgorithm: The message digest algorithm to be used when hashing passwords.
 * If not specified, no hashing is used.
 * This must be an algorithm supported by the java.security.MessageDigest class on your platform.
 * For J2SE 1.4.2 you can check :
 * <a href="http://java.sun.com/j2se/1.4.2/docs/guide/security/CryptoSpec.html#AppB">Java Cryptography Architecture API Specification & Reference - Apendix B : Algorithms</a>
 * </li>
 * <li>hashEncoding: The econding used to store hashed passwords.
 * Supported values are HEX, BASE64.</li>
 * <li>ignorePasswordCase: If true, password case will be igonred. This property is ignored if a hashAlgorithm was specified.
 * Default to false.</li>
 * <li>ignoreUserCase: If ture, username case will be ignored.</li>
 * <li>credential-store: The credential store configured for this authenticator.
 * Check specific stores for specific configuraiton options</li>
 * <li>credential-store-key-adapter: The credential store key adapter configured for this authenticator.
 * Check specific stores for specific configuraiton options</li>
 * </ul>
 * </p>
 * <p/>
 * Sample authenticator configuration for basic authentication (username/password) :
 * </p>
 * <pre>
 *         &lt;authentication-scheme&gt;
 * <p/>
 *         &lt;class&gt;org.josso.auth.scheme.UsernamePasswordAuthScheme&lt;/class&gt;
 *           &lt;hashAlgorithm&gt;MD5&lt;/hashAlgorithm&gt;
 *           &lt;hashEncoding&gt;HEX&lt;/hashEncoding&gt;
 *           &lt;ignorePasswordCase&gt;false&lt;/ignorePasswordCase&gt;
 *           &lt;ignoreUserCase&gt;false&lt;/ignoreUserCase&gt;
 * <p/>
 *           &lt;!-- Configure the proper store here --&gt;
 *           &lt;credential-store&gt;
 *           ...
 *           &lt;/credential-store&gt;
 * <p/>
 *           &lt;credential-store-key-adapter&gt;
 *           ...
 *           &lt;/credential-store-key-adapter&gt;
 * <p/>
 * 	 &lt;/authentication-scheme&gt;
 * <p/>
 * </pre>
 *
 * @org.apache.xbean.XBean element="basic-auth-scheme"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: UsernamePasswordAuthScheme.java 568 2008-07-31 18:39:20Z sgonzalez $
 * @see org.josso.auth.CredentialStore
 * @see org.josso.gateway.identity.service.store.AbstractStore
 * @see UsernamePasswordCredentialProvider
 */

public class UsernamePasswordAuthScheme extends AbstractAuthenticationScheme {


    private static final Log logger = LogFactory.getLog(UsernamePasswordAuthScheme.class);

    private String _hashAlgorithm;

    private String _hashEncoding;

    private String _hashCharset;

    private boolean _ignorePasswordCase;

    private boolean _ignoreUserCase;

    // Some spetial configuration attributes,

    /**
     * This attribute is only used when CRYPT hasing is configued.  The default value is 2.
     */
    private int _saltLenght = 2;

    public UsernamePasswordAuthScheme() {
        this.setName("basic-authentication");
    }

    /**
     * The username recieved as UsernameCredential instance, if any.
     */
    public Principal getPrincipal() {
        return new SimplePrincipal(getUsername(_inputCredentials));
    }

    /**
     * The username recieved as UsernameCredential instance, if any.
     */
    public Principal getPrincipal(Credential[] credentials) {
        return new SimplePrincipal(getUsername(credentials));
    }

    /**
     * Authenticates the user using recieved credentials to proof his identity.
     *
     * @return the Principal if credentials are valid, null otherwise.
     */
    public boolean authenticate() throws SSOAuthenticationException {

        setAuthenticated(false);

        String username = getUsername(_inputCredentials);
        String password = getPassword(_inputCredentials);

        // Check if all credentials are present.
        if (username == null || username.length() == 0 ||
                password == null || password.length() == 0) {

            if (logger.isDebugEnabled()) {
                logger.debug("Username " + (username == null || username.length() == 0 ? " not" : "") + " provided. " +
                        "Password " + (password == null || password.length() == 0 ? " not" : "") + " provided.");
            }

            // We don't support empty values !
            return false;
        }

        String knownUsername = getUsername(getKnownCredentials());
        String expectedPassword = getPassword(getKnownCredentials());

        // We might have to hash the password.
        password = createPasswordHash(password);

        // Validate user identity ...
        if (!validateUsername(username, knownUsername) || !validatePassword(password, expectedPassword)) {
            return false;
        }

        // set valid username so rememberMe token can be created from valid user information
        if (_ignoreUserCase && !username.equals(knownUsername)) {
        	updateUsername(_inputCredentials, knownUsername);
        }
        
        if (logger.isDebugEnabled())
            logger.debug("[authenticate()], Principal authenticated : " + username);

        // We have successfully authenticated this user.
        setAuthenticated(true);
        return true;
    }

    /**
     * Only one password credential supported.
     */
    public Credential[] getPrivateCredentials() {
        Credential c = getPasswordCredential(_inputCredentials);
        if (c == null)
            return new Credential[0];

        Credential[] r = {c};
        return r;

    }

    /**
     * Only one username credential supported.
     */
    public Credential[] getPublicCredentials() {
        Credential c = getUsernameCredential(_inputCredentials);
        if (c == null)
            return new Credential[0];

        Credential[] r = {c};
        return r;
    }

    @Override
    public Credential newEncodedCredential(String name, Object value) {
        try {
            String v = (String) value;
            if (name.equals(UsernamePasswordCredentialProvider.PASSWORD_CREDENTIAL_NAME))
            v = createPasswordHash(v);

            return super.newEncodedCredential(name, v);

        } catch (SSOAuthenticationException e) {
            logger.error("Cannot create encoded credential " + e.getMessage(), e);
            return null;
        }

    }

    // --------------------------------------------------------------------
    // Protected utils
    // --------------------------------------------------------------------

    /**
     * This method validates the input password agaist the expected password.
     *
     * @param inputPassword
     * @param expectedPassword
     */
    protected boolean validatePassword(String inputPassword, String expectedPassword) {

        if (logger.isDebugEnabled())
            logger.debug("Validating passwords [" + inputPassword + "/" + expectedPassword + "]");

        // Validate input and expected passwords.
        if (inputPassword == null && expectedPassword == null)
            return false;

        if (_ignorePasswordCase && _hashAlgorithm == null)
            return inputPassword.equalsIgnoreCase(expectedPassword);
        else
            return inputPassword.equals(expectedPassword);
    }

    /**
     * This method validates the input password agaist the expected password.
     *
     * @param inputUsername
     * @param expectedUsername
     */
    protected boolean validateUsername(String inputUsername, String expectedUsername) {

        if (logger.isDebugEnabled())
            logger.debug("Validating usernames [" + inputUsername + "/" + expectedUsername + "]");

        if (inputUsername == null && expectedUsername == null)
            return false;

        if (_ignoreUserCase)
            return inputUsername.equalsIgnoreCase(expectedUsername);
        else
            return inputUsername.equals(expectedUsername);
    }


    /**
     * This method allows password hashing.
     * In order to work, you need to specify hashAlgorithm and hashEncoding properties.
     * You can optionally set hashCharset property.
     *
     * @return the hashed password.
     */
    protected String createPasswordHash(String password) throws SSOAuthenticationException {

        // If none of this properties are set, do nothing ...
        if (getHashAlgorithm() == null && getHashEncoding() == null) {
            // Nothing to do ...
            return password;
        }

        if (logger.isDebugEnabled())
            logger.debug("Creating password hash for [" + password + "] with algorithm/encoding [" + getHashAlgorithm() + "/" + getHashEncoding() + "]");

        // Check for spetial encryption mechanisms, not supported by the JDK
        if ("CRYPT".equalsIgnoreCase(getHashAlgorithm())) {
            // Get known password
            String knownPassword = getPassword(getKnownCredentials());
            String salt = knownPassword != null && knownPassword.length() > 1 ? knownPassword.substring(0, _saltLenght) : "";

            return Crypt.crypt(salt, password);

        }

        byte[] passBytes;
        String passwordHash = null;

        // convert password to byte data
        try {
            if (_hashCharset == null)
                passBytes = password.getBytes();
            else
                passBytes = password.getBytes(_hashCharset);
        } catch (UnsupportedEncodingException e) {
            logger.error("charset " + _hashCharset + " not found. Using platform default.");
            passBytes = password.getBytes();
        }

        // calculate the hash and apply the encoding.
        try {

            byte[] hash;
            // Hash algorithm is optional
            if (_hashAlgorithm != null)
                hash = getDigest().digest(passBytes);
            else
                hash = passBytes;

            // At this point, hashEncoding is required.
            if ("BASE64".equalsIgnoreCase(_hashEncoding)) {
                passwordHash = CipherUtil.encodeBase64(hash);

            } else if ("HEX".equalsIgnoreCase(_hashEncoding)) {
                passwordHash = CipherUtil.encodeBase16(hash);

            } else if (_hashEncoding == null) {
                logger.error("You must specify a hashEncoding when using hashAlgorithm");

            } else {
                logger.error("Unsupported hash encoding format " + _hashEncoding);

            }

        } catch (Exception e) {
            logger.error("Password hash calculation failed : \n" + e.getMessage() != null ? e.getMessage() : e.toString(), e);
        }

        return passwordHash;

    }


    /**
     * Only invoke this if algorithm is set.
     *
     * @throws SSOAuthenticationException
     */
    protected MessageDigest getDigest() throws SSOAuthenticationException {

        MessageDigest _digest = null;
        if (_hashAlgorithm != null) {

            try {
                _digest = MessageDigest.getInstance(_hashAlgorithm);
                logger.debug("Using hash algorithm/encoding : " + _hashAlgorithm + "/" + _hashEncoding);
            } catch (NoSuchAlgorithmException e) {
                logger.error("Algorithm not supported : " + _hashAlgorithm, e);
                throw new SSOAuthenticationException(e.getMessage(), e);
            }
        }

        return _digest;

    }

    /**
     * Gets the username from the received credentials.
     *
     * @param credentials
     */
    protected String getUsername(Credential[] credentials) {
        UsernameCredential c = getUsernameCredential(credentials);
        if (c == null)
            return null;

        return (String) c.getValue();
    }

    /**
     * Gets the password from the recevied credentials.
     *
     * @param credentials
     */
    protected String getPassword(Credential[] credentials) {
        PasswordCredential p = getPasswordCredential(credentials);
        if (p == null)
            return null;
        return (String) p.getValue();
    }

    /**
     * Updates the username of the received credentials.
     *
     * @param credentials
     */
    protected void updateUsername(Credential[] credentials, String username) {
        UsernameCredential c = getUsernameCredential(credentials);
        if (c != null) {
        	c.setValue(username);
        }
    }
    
    /**
     * Gets the credential that represents a password.
     *
     * @param credentials
     */
    protected PasswordCredential getPasswordCredential(Credential[] credentials) {
        for (int i = 0; i < credentials.length; i++) {
            if (credentials[i] instanceof PasswordCredential) {
                return (PasswordCredential) credentials[i];
            }
        }
        return null;
    }

    /**
     * Gets the credential that represents a Username.
     */
    protected UsernameCredential getUsernameCredential(Credential[] credentials) {

        for (int i = 0; i < credentials.length; i++) {
            if (credentials[i] instanceof UsernameCredential) {
                return (UsernameCredential) credentials[i];
            }
        }
        return null;
    }


    protected CredentialProvider doMakeCredentialProvider() {
        return new UsernamePasswordCredentialProvider();
    }

    public String getHashAlgorithm() {
        return _hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        if (hashAlgorithm != null && hashAlgorithm.equals(""))
            hashAlgorithm = null;
        _hashAlgorithm = hashAlgorithm;
    }

    /**
     * Getter for the encoding used for password hashing.
     * Supported values : HEX, BASE64
     */
    public String getHashEncoding() {
        return _hashEncoding;
    }

    /**
     * Setter for the encoding used for password hashing.
     * Supported values : HEX, BASE64
     */
    public void setHashEncoding(String hashEnconding) {
        if (hashEnconding != null && hashEnconding.equals(""))
            hashEnconding = null;
        _hashEncoding = hashEnconding;
    }

    public String getHashCharset() {
        return _hashCharset;
    }


    public void setHashCharset(String hashCharset) {
        _hashCharset = hashCharset;
    }

    public void setSaltLenght(String saltLenght) {
        setSaltLength(Integer.valueOf(saltLenght).intValue());
    }

    /**
     * Only used when CRYPT is configured, default value is 2.
     */
    public int getSaltLength() {
        return _saltLenght;
    }

    public void setSaltLength(int sl) {
        _saltLenght = sl;
    }


    /**
     * Values : true , false,
     */
    public void setIgnorePasswordCase(String ignorePasswordCase) {
        _ignorePasswordCase = Boolean.valueOf(ignorePasswordCase).booleanValue();
    }

    /**
     * Values : true , false,
     */
    public void setIgnoreUserCase(String ignoreUserCase) {
        _ignoreUserCase = Boolean.valueOf(ignoreUserCase).booleanValue();
    }

    public Object clone() {

        UsernamePasswordAuthScheme s = (UsernamePasswordAuthScheme) super.clone();

        s.setHashAlgorithm(_hashAlgorithm);
        s.setHashCharset(_hashCharset);
        s.setHashEncoding(_hashEncoding);
        s.setIgnorePasswordCase(_ignorePasswordCase + "");
        s.setIgnoreUserCase(_ignoreUserCase + "");
        s.setName(_name);

        return s;
    }

}
