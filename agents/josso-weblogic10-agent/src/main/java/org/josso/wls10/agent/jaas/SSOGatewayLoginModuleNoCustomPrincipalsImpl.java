/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2008, Atricore, Inc.
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
 */

package org.josso.wls10.agent.jaas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.*;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.List;
import java.util.Map;
import weblogic.security.principal.WLSUserImpl;
import weblogic.security.principal.WLSGroupImpl;
import weblogic.security.spi.WLSUser;
import weblogic.security.spi.WLSGroup;

/**
 * SSO Gateway Login Module implementation for Weblogic that uses built-in principals instead of
 * JOSSO-specific ones.
 *
 * Date: Nov 20, 2007
 * Time: 6:24:15 PM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class SSOGatewayLoginModuleNoCustomPrincipalsImpl implements LoginModule {

    private static final Log logger = LogFactory.getLog(SSOGatewayLoginModuleNoCustomPrincipalsImpl.class);

    // initial state
    private Subject _subject;
    private CallbackHandler _callbackHandler;

    // the authentication status
    protected boolean _succeeded;
    protected boolean commitSucceeded;

    // the logged user and his roles.
    private String _requester;
    protected String _currentSSOSessionId;
    protected WLSUser _ssoUserPrincipal;
    protected WLSGroup[] _ssoRolePrincipals;

    /**
     * Initialize this  LoginModule
     *
     * @param subject the Subject to be authenticated.
     *
     * @param callbackHandler a CallbackHandler for communicating
     *            with the end user (prompting for user names and
     *            passwords, for example).
     *
     * @param sharedState shared LoginModule state.
     *
     * @param options options specified in the login Configuration
     *        for this particular LoginModule.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {

        this._subject = subject;
        this._callbackHandler = callbackHandler;
    }

    /**
     * Authenticate the user by prompting for the SSO Session Identifier assigned by the SSO Gateway on logon.
     *
     * This method obtains from the gateway, using the provided session identifier, the user associated with
     * such session identifier.
     * Only the NameCallBack is used, since its not a user/password pair but only one value containing the session
     * identifier. Any other callback type is ignored.
     *
     * @return true in all cases since this LoginModule
     *        should not be ignored.
     *
     * @exception javax.security.auth.login.FailedLoginException if the authentication fails.
     *
     * @exception javax.security.auth.login.LoginException if this LoginModule
     *        is unable to perform the authentication.
     */
    public boolean login() throws LoginException {

        if (_callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                    "to garner authentication information from the user");

        Callback[] callbacks = new Callback[2];

        // Just ask for the session identifier
        callbacks[0] = new NameCallback("ssoSessionId");
        callbacks[1] = new PasswordCallback("password", false);
        
        String ssoSessionId;
        String ssoSessionId2 = null;
        try {
            _callbackHandler.handle(callbacks);
            ssoSessionId = ((NameCallback) callbacks[0]).getName();
            if (((PasswordCallback) callbacks[1]).getPassword() != null)
                ssoSessionId2 = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());

            _requester = "";
            // Check for nulls ?
            SSOAgentRequest request = AbstractSSOAgent._currentRequest.get();
            if (request != null)
                _requester = request.getRequester();
            else
                logger.warn("No SSO Agent request found in thread local variable, can't identify requester");

        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                    " not available to garner authentication information " +
                    "from the user");
        }

        logger.debug("Requested authentication to gateway by " + _requester + " using sso session " + ssoSessionId + "/" + ssoSessionId2 );

        try {

            if (ssoSessionId2 != null && !ssoSessionId2.equals(ssoSessionId))
                ssoSessionId = ssoSessionId2;

            // If no session is found, ignore this module.
            if (ssoSessionId == null) {
                if (logger.isDebugEnabled())
                    logger.debug("Session authentication failed : " + ssoSessionId);
                _succeeded = false;
                return false;
            }

            _currentSSOSessionId = ssoSessionId;

            SSOIdentityManagerService im = lookupSsoIdentityManager(_requester);
            SSOUser jossoUser = im.findUserInSession(_requester, ssoSessionId);
            WLSUser wlsUser = new WLSUserImpl (jossoUser.getName());

            if (logger.isDebugEnabled())
                logger.debug("Session authentication succeeded : " + ssoSessionId);

            _ssoUserPrincipal = wlsUser;
            _succeeded = true;

        } catch (SSOIdentityException e) {
            // Ignore this ... (user does not exist for this session)
            logger.debug(e.getMessage());
            _succeeded = false;
            return false;

        } catch (Exception e) {

            logger.error("Session login failed for Principal : " + _ssoUserPrincipal + e.getMessage());

            // Only log if debug is enabled ...
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);

            _succeeded = false;
            clearCredentials();
            throw new FailedLoginException("Fatal error authenticating session : " + _ssoUserPrincipal + " : " + e.getMessage());
        }

        return true;
    }

    /**
     * This method is called if the LoginContext's overall authentication succeeded.
     *
     * Using the SSO user name, saved by the previosuly executed login() operation, obtains from the gateway
     * the roles associated with the user and fills the Subject with the user and role principals.
     * If this LoginModule's own authentication attempted failed, then this method removes any state that was
     * originally saved.
     *
     * @exception javax.security.auth.login.LoginException if the commit fails.
     *
     * @return true if this LoginModule's own login and commit
     *        attempts succeeded, or false otherwise.
     */
    public boolean commit() throws LoginException {
        if (_succeeded == false) {
            return false;
        } else {

            try {

                // Add the SSOUser as a Principal
                if (!_subject.getPrincipals().contains(_ssoUserPrincipal)) {
                    _subject.getPrincipals().add(_ssoUserPrincipal);
                }

                if (logger.isDebugEnabled())
                    logger.debug("Added SSOUser Principal to the Subject : " + _ssoUserPrincipal);

                _ssoRolePrincipals = getRoleSets();

                // Add to the Subject the SSORoles associated with the SSOUser .
                for (int i=0; i < _ssoRolePrincipals .length; i++) {
                    if (_subject.getPrincipals().contains(_ssoRolePrincipals [i]))
                        continue;

                    _subject.getPrincipals().add(_ssoRolePrincipals [i]);

                    if (logger.isDebugEnabled())
                        logger.debug("Added SSORole Principal to the Subject : " + _ssoRolePrincipals [i]);
                }

                commitSucceeded = true;
                return true;

            } catch (Exception e) {
                logger.error("Session commit failed for Principal : " + _ssoUserPrincipal + e.getMessage());
                // Only log if debug is enabled ...
                if (logger.isDebugEnabled())
                    logger.debug(e.getMessage(), e);

                throw new LoginException("Session commit failed for Principal : " + _ssoUserPrincipal + " : " + e.getMessage());
            } finally {
                // in any case, clean out state
                clearCredentials();
            }

        }
    }

    /**
     *  This method is called if the LoginContext's
     * overall authentication failed.
     *
     * @exception javax.security.auth.login.LoginException if the abort fails.
     *
     * @return false if this LoginModule's own login and/or commit attempts
     *        failed, and true otherwise.
     */
    public boolean abort() throws LoginException {
        if (_succeeded == false) {
            return false;
        } else if (_succeeded == true && commitSucceeded == false) {
            // login _succeeded but overall authentication failed
            _succeeded = false;
            clearCredentials();
        } else {
            // overall authentication _succeeded and commit _succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    /**
     * Logout the user.
     *
     * This method removes the SSO User and Role Principals from the Subject that were added by the commit()
     * method.
     *
     * @exception javax.security.auth.login.LoginException if the logout fails.
     *
     * @return true in all cases since this LoginModule
     *          should not be ignored.
     */
    public boolean logout() throws LoginException {
        _subject.getPrincipals().remove(_ssoUserPrincipal);

        if (logger.isDebugEnabled())
            logger.debug("Removed SSOUser Principal from Subject : " + _ssoUserPrincipal);

        // Remove all the SSORole Principals from the Subject.
        for (int i=0; i < _ssoRolePrincipals.length; i++) {
            _subject.getPrincipals().remove(_ssoRolePrincipals[i]);

            if (logger.isDebugEnabled())
                logger.debug("Removed SSORole Principal from Subject : " + _ssoRolePrincipals[i]);
        }

        _succeeded = commitSucceeded;
        clearCredentials();
        return true;
    }

    /**
     * Reset the login module state.
     */
    private void clearCredentials() {
        _ssoUserPrincipal = null;
        _ssoRolePrincipals = null;
        _currentSSOSessionId = null;
    }

    /**
     * Retreives the list of roles associated to current principal
     */
    protected WLSGroup[] getRoleSets() throws LoginException {
        try {
            // obtain user roles principals and add it to the subject
            SSOIdentityManagerService im = lookupSsoIdentityManager(_requester);

            SSORole [] roles = im.findRolesBySSOSessionId(_requester, _currentSSOSessionId);
            WLSGroup [] wlsRoles = new WLSGroupImpl [roles.length];

            for (int i = 0; i < roles.length; i++) {
                SSORole role = roles[i];
                WLSGroup wlsRole = new WLSGroupImpl (role.getName());
                wlsRoles[i] = wlsRole;
            }

            return wlsRoles;

        } catch(Exception e) {
            logger.error("Session login failed for Principal : " + _ssoUserPrincipal, e);
            throw new LoginException("Session login failed for Principal : " + _ssoUserPrincipal);
        }

    }

    protected SSOIdentityManagerService lookupSsoIdentityManager(String requester) throws Exception {

        SSOAgent agent = Lookup.getInstance().lookupSSOAgent();

        if (requester != null) {
            List<SSOPartnerAppConfig> appCfs = agent.getConfiguration().getSsoPartnerApps();
            for (SSOPartnerAppConfig appCfg  : appCfs) {
                if (appCfg.getId().equals(requester)) {
                    if (appCfg.getIdentityManagerService() != null)
                        return appCfg.getIdentityManagerService();
                    break;
                }
            }
        }

        return agent.getSSOIdentityManager();
    }

}