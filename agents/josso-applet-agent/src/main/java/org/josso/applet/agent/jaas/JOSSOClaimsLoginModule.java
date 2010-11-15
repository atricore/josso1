package org.josso.applet.agent.jaas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.WebserviceGatewayServiceLocator;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.Map;

public class JOSSOClaimsLoginModule implements LoginModule {

    private static final Log logger = LogFactory.getLog(JOSSOClaimsLoginModule.class);

    // initial state
    private Subject _subject;
    private CallbackHandler _callbackHandler;

    // the authentication status
    protected boolean _succeeded;
    protected boolean commitSucceeded;

    // the logged user and his roles.
    protected String _currentSSOSessionId;
    protected String _requester;
    protected SSOUser _ssoUserPrincipal;
    protected SSORole[] _ssoRolePrincipals;

    protected SSOIdentityManagerService _im;
    protected String _endpoint;
    
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {

		this._subject = subject;
		this._callbackHandler = callbackHandler;
	}

	public boolean login() throws LoginException {
		if (_callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                    "to garner authentication information from the user");

        Callback[] callbacks = new Callback[3];

        // Just ask for the session identifier
        callbacks[0] = new NameCallback("ssoSessionId");
        callbacks[1] = new NameCallback("appID");
        callbacks[2] = new NameCallback("endpoint");

        String ssoSessionId;
        
        try {
            _callbackHandler.handle(callbacks);
            ssoSessionId = ((NameCallback) callbacks[0]).getName();
            _requester = ((NameCallback) callbacks[1]).getName();
            _endpoint = ((NameCallback) callbacks[2]).getName();
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                    " not available to garner authentication information " +
                    "from the user");
        }

        logger.debug("Requested authentication to gateway by " + _requester + " using sso session " + ssoSessionId);

        try {

            // If no session is found, ignore this module.
            if (ssoSessionId == null) {
                logger.debug("Session authentication failed : " + ssoSessionId);
                _succeeded = false;
                return false;
            }

            _currentSSOSessionId = ssoSessionId;

            SSOUser ssoUser = getSSOIdentityManager().findUserInSession(_requester, ssoSessionId);

            logger.debug("Session authentication succeeded : " + ssoSessionId);
            _ssoUserPrincipal = ssoUser;
            _succeeded = true;

        } catch (SSOIdentityException e) {
            // Ignore this ... (user does not exist for this session)
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);
            _succeeded = false;
            return false;

        } catch (Exception e) {
            logger.error("Session authentication failed : " + ssoSessionId, e);
            _succeeded = false;
            clearCredentials();
            throw new FailedLoginException("Fatal error authenticating session : " + e);
        }

        return true;
	}

	public boolean commit() throws LoginException {
		if (_succeeded == false) {
            return false;
        } else {

            try {

                // Add the SSOUser as a Principal
                if (!_subject.getPrincipals().contains(_ssoUserPrincipal)) {
                    _subject.getPrincipals().add(_ssoUserPrincipal);
                }

                logger.debug("Added SSOUser Principal to the Subject : " + _ssoUserPrincipal);

                _ssoRolePrincipals = getRoleSets(_requester);

                // Add to the Subject the SSORoles associated with the SSOUser .
                for (int i=0; i < _ssoRolePrincipals .length; i++) {
                    if (_subject.getPrincipals().contains(_ssoRolePrincipals [i]))
                        continue;

                    _subject.getPrincipals().add(_ssoRolePrincipals [i]);
                    logger.debug("Added SSORole Principal to the Subject : " + _ssoRolePrincipals [i]);
                }

                commitSucceeded = true;
                return true;
            } catch (Exception e) {
                logger.error("Session login failed for Principal : " + _ssoUserPrincipal, e);
                throw new LoginException("Session login failed for Principal : " + _ssoUserPrincipal);
            } finally {
                // in any case, clean out state
                clearCredentials();
            }

        }
	}

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

	public boolean logout() throws LoginException {
		_subject.getPrincipals().remove(_ssoUserPrincipal);
        logger.debug("Removed SSOUser Principal from Subject : " + _ssoUserPrincipal);

        // Remove all the SSORole Principals from the Subject.
        for (int i=0; i < _ssoRolePrincipals.length; i++) {
            _subject.getPrincipals().remove(_ssoRolePrincipals[i]);
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
    protected SSORole[] getRoleSets(String requester) throws LoginException {
        try {
            // obtain user roles principals and add it to the subject
            return getSSOIdentityManager().findRolesBySSOSessionId(requester, _currentSSOSessionId);
        } catch(Exception e) {
            logger.error("Session login failed for Principal : " + _ssoUserPrincipal, e);
            throw new LoginException("Session login failed for Principal : " + _ssoUserPrincipal);
        }
    }

    protected SSOIdentityManagerService getSSOIdentityManager() {
        if (_im == null) {
            try {
                WebserviceGatewayServiceLocator wsLocator = new WebserviceGatewayServiceLocator();
                wsLocator.setEndpoint(_endpoint);
                _im = wsLocator.getSSOIdentityManager();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return _im;
    }
}
