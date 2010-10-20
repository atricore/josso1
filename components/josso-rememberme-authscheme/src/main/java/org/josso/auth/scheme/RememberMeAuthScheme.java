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

import org.josso.auth.CredentialProvider;
import org.josso.auth.Credential;
import org.josso.auth.SimplePrincipal;
import org.josso.auth.util.CipherUtil;
import org.josso.auth.exceptions.SSOAuthenticationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * This authentication scheme uses symetric encrypton to compare a received token with
 * a username stored in the configured identity store..
 *
 * Subclasses may provide different mechanisms to store / retrieve tokens instead of enrcyption
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 10, 2008
 * Time: 2:36:29 PM
 * @org.apache.xbean.XBean element="rememberme-auth-scheme"
 */
public class RememberMeAuthScheme extends AbstractAuthenticationScheme {

    public static final String USERNAME_CREDENTIAL_NAME="username";

    public static final String REMEMBER_ME_TOKEN_CREDENTIAL_NAME="remembermeToken";

    private static final Log logger = LogFactory.getLog(RememberMeAuthScheme.class);

    private String  base64Key;

    public RememberMeAuthScheme() {
        this.setName("rememberme-authentication");

        Properties authProps = new Properties();

        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream("/josso-auth.properties");
            if (is == null)
                throw new IOException("Cannot find resource /josso-auth.properties.  Make sure this file is installed with JOSSO Gateway!");

            authProps.load(is);

            this.base64Key = authProps.getProperty("josso.rememberme.authscheme.key");

            // Just check that the key is not the one provided with the archetype ...
            if (this.base64Key.equals("5FvzKCtKKjeqakdm4c89WA\\=\\="))
                logger.warn("Please, replace josso-auth.properties key! Do not use the one provided with the Gateway Archetype!");
            
        } catch (IOException e) {
            logger.error("Cannot load auth properties : " + e.getMessage(), e);
        } finally  {
            if (is != null) try { is.close(); } catch (IOException e) { /**/}
        }

    }

    public String getName() {
        return _name;
    }

    protected CredentialProvider doMakeCredentialProvider() {
        return this;
    }

    @Override
    public Credential newCredential(String name, Object value) {

        if (name.equalsIgnoreCase(REMEMBER_ME_TOKEN_CREDENTIAL_NAME)) {
            return new RememberMeCredential(value);
        }

        if (name.equalsIgnoreCase(USERNAME_CREDENTIAL_NAME)) {
            return new UsernameCredential(value);
        }

        // Don't know how to handle this name ...
        if (logger.isDebugEnabled())
            logger.debug("Unknown credential name : " + name);

        return null;

    }

    public boolean authenticate() throws SSOAuthenticationException {
        setAuthenticated(false);

        String remembermeToken = getRemembermeToken(_inputCredentials);
        // Check if all credentials are present.
        if (remembermeToken == null || remembermeToken.length() == 0) {

            if (logger.isDebugEnabled()) {
                logger.debug("RememberMe Token" + (remembermeToken == null || remembermeToken.length() == 0 ? " not" : "") + " provided. ");
            }

            // We don't support empty values !
            return false;
        }

        String username = getUsername(_inputCredentials);
        if (username == null || username.length() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Username not " + (remembermeToken == null || remembermeToken.length() == 0 ? " not" : "") + " provided. ");
            }

            // We don't support empty values !
            return false;

        }

        // Get known gredentials
        Credential[] knownCredentials = getKnownCredentials();
        String knownUsername = getUsername(knownCredentials);
        String knownRemembermeToken = getRemembermeToken(knownCredentials);

        // Validate user identity ...
        if (!validateUsername(username, knownUsername) || !validateRememberMeToken(remembermeToken, knownRemembermeToken)) {
            return false;
        }

        if (logger.isDebugEnabled())
            logger.debug("[authenticate()], Rememberme Token : " + remembermeToken);

        // We have successfully authenticated this user.
        setAuthenticated(true);
        return true;

    }

    /**
     * This implementation will retrieve some credentials from the identity store but will also use input some credentials as known credentials
     *
     * @throws SSOAuthenticationException
     */
    @Override
    protected Credential[] getKnownCredentials() throws SSOAuthenticationException {
        // Load username credential from store
        Credential[] creds = super.getKnownCredentials();

        // Add remember me token to credential array
        Credential[] newCreds = new Credential[creds.length+1];

        for (int i = 0; i < creds.length; i++) {
            Credential cred = creds[i];
            newCreds[i] = creds[i];
        }

        newCreds[newCreds.length - 1] = getRememberMeCredential(_inputCredentials);

        return newCreds;

    }

    public Principal getPrincipal() {
        return new SimplePrincipal(getUsername(_inputCredentials));
    }

    public Principal getPrincipal(Credential[] credentials) {
        return new SimplePrincipal(getUsername(credentials));
    }

    public Credential[] getPrivateCredentials() {
        Credential c = getRememberMeCredential(_inputCredentials);

        if (c == null)
            return new Credential[0];

        Credential[] r = {c};
        return r;    }

    public Credential[] getPublicCredentials() {
        Credential c = getRememberMeCredential(_inputCredentials);

        if (c == null)
            return new Credential[0];

        Credential[] r = {c};
        return r;

    }

    /**
     * This implementation will
     * @param userCredentials
     * @param s
     */
    @Override
    public void initialize(Credential[] userCredentials, Subject s) {
        super.initialize(userCredentials, s);

        RememberMeCredential rememberMe = getRememberMeCredential(userCredentials);
        if (rememberMe == null) {
            logger.warn("No remember me credential recevied");
            return;
        }

        // Add a new input credential with the username associated to the remember me token
        String remembermeToken = (String) rememberMe.getValue();
        String username = getUsernameForToken(remembermeToken);
        if (username == null) {
            logger.debug("Username not provided, skiping UsernameCredential injection");
            return;
        }

        // Now, inject username credential
        Credential usernameCred = doMakeCredentialProvider().newCredential(USERNAME_CREDENTIAL_NAME, username);
        this._inputCredentials = new Credential[_inputCredentials.length+1];
        for (int i = 0; i < userCredentials.length; i++) {
            Credential userCredential = userCredentials[i];
            _inputCredentials[i] = userCredential;
        }

        _inputCredentials[_inputCredentials.length - 1] = usernameCred;

    }


    /**
     * This will decrypt a remember me token into a username
     *
     * If the token is not symmetric, this should return null.
     * @param remembermeToken
     * @return
     */
    public String getUsernameForToken(String remembermeToken) {

        try {
        	String msg = CipherUtil.decryptAES(URLDecoder.decode(remembermeToken, "UTF-8"), base64Key);
            return msg.substring("josso:".length());
        } catch (UnsupportedEncodingException e) {
            logger.debug(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            logger.debug(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.debug(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            logger.debug(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            logger.debug(e.getMessage(), e);
        } catch (BadPaddingException e) {
            logger.debug(e.getMessage(), e);
        }

        return null;

    }

    /**
     * This will encrypt a username into a remember me token
     * @param username
     * @return
     */
    public String getRemembermeTokenForUser(String username) {
        try {
        	String token = CipherUtil.encryptAES("josso:" + username, base64Key);
            return URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            logger.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            logger.error(e.getMessage(), e);
        } catch (BadPaddingException e) {
            logger.error(e.getMessage(), e);
        }

        return "";
    }



    // ----------------------------------------------------------

    protected boolean validateUsername(String username, String knownUsername) {
        return username != null && knownUsername != null && username.length() > 0 &&
                knownUsername.length() > 0 && username.equals(knownUsername);
    }

    protected boolean validateRememberMeToken(String rememberMeToken, String knownRememberMeToken) {
        return rememberMeToken != null && knownRememberMeToken != null && rememberMeToken.length() > 0 &&
                knownRememberMeToken.length() > 0 && rememberMeToken.equals(knownRememberMeToken);
    }

    /**
     * Finds the credential instance that is a RememberMeCredential in the given set
     */
    protected RememberMeCredential getRememberMeCredential(Credential[] credentials) {

        for (int i = 0; i < credentials.length; i++) {
            if (credentials[i] instanceof RememberMeCredential) {
                return (RememberMeCredential) credentials[i];
            }
        }
        return null;
    }

    /**
     * Finds the credential instance that is a UsernameCredential in the given set
     */
    protected UsernameCredential getUsernameCredential(Credential[] credentials) {
        for (int i = 0; i < credentials.length; i++) {
            if (credentials[i] instanceof UsernameCredential) {
                return (UsernameCredential) credentials[i];
            }
        }
        return null;
    }

    /**
     * Gets a username based on a credential set
     *
     * @see #getUsernameCredential(org.josso.auth.Credential[])
     */
    protected String getUsername(Credential[] creds) {
        UsernameCredential cred = getUsernameCredential(creds);
        if (cred == null)
            return null;

        return cred.getValue().toString();
    }


    /**
     * Gets a remember me token based on a credential set
     * @see #getRememberMeCredential(org.josso.auth.Credential[])
     */
    protected String getRemembermeToken(Credential[] creds) {
        RememberMeCredential cred = getRememberMeCredential(creds);
        if (cred == null)
            return null;
        return cred.getValue().toString();

    }

}
