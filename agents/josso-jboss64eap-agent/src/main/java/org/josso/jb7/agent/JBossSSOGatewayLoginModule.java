package org.josso.jb7.agent;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.jaspi.agent.SSOGatewayLoginModule;

/**
 * SSOGatewayLogin Module for JBoss.
 *
 * Adds support for the unauthenticatedIdentity property used by JBoss.
 */
public class JBossSSOGatewayLoginModule extends SSOGatewayLoginModule {

    private static final Log logger = LogFactory.getLog(JBossSSOGatewayLoginModule.class);

    /** the principal to use when user is not authenticated **/
    protected SSOUser _unauthenticatedIdentity;

    private Subject _savedSubject;

    /**
     * This method supports the unauthenticatedIdentity property used by JBoss.
     *
     * @see org.josso.jaspi.agent.SSOGatewayLoginModule#initialize(Subject, CallbackHandler, Map, Map)
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        _savedSubject = subject;
        super.initialize(subject, callbackHandler, sharedState, options);
        // Check for unauthenticatedIdentity option.
        String name = (String) options.get("unauthenticatedIdentity");
        if (name != null) {
            try {
                _unauthenticatedIdentity = createIdentity(name);
                logger.debug("Saw unauthenticatedIdentity="+name);
            } catch(Exception e) {
                logger.warn("Failed to create custom unauthenticatedIdentity", e);
            }
        }
    }

    /**
     * This method supports the unauthenticatedIdentity property used by JBoss.
     *
     * @see org.josso.jaspi.agent.SSOGatewayLoginModule#login()
     */
    public boolean login() throws LoginException {
        if (!super.login()) {
            // We have an unauthenticated user, use configured Principal
            if (_unauthenticatedIdentity != null) {
                logger.debug("Authenticated as unauthenticatedIdentity : " + _unauthenticatedIdentity);
                _ssoUserPrincipal = _unauthenticatedIdentity;
                _succeeded = true;
                return true;
            }
            return false;
        }

        return true;
    }

    /**
     * The Subject saved in the previously executed initialize() method, is modified
     * by adding a new special Group called "Roles" whose members are the SSO user roles.
     * JBoss will fetch user roles by examining such group.
     *
     * @see org.josso.jaspi.agent.SSOGatewayLoginModule#logout()
     */
    public boolean commit() throws LoginException {
        boolean rc = false;

        rc = super.commit();

        Set ssoRolePrincipals = _savedSubject.getPrincipals(SSORole.class);
        Group targetGrp = new BaseRoleImpl("Roles");
        Iterator i = ssoRolePrincipals.iterator();
        while (i.hasNext()) {
            Principal p = (Principal)i.next();
            targetGrp.addMember(p); // Add user role to "Roles" group
        }
        // Add the "Roles" group to the Subject so that JBoss can fetch user roles.
        _savedSubject.getPrincipals().add(targetGrp);

        Set ssoUserPrincipals = _savedSubject.getPrincipals(SSOUser.class);
        Group callerPrincipal = new BaseRoleImpl("CallerPrincipal");
        Iterator j = ssoUserPrincipals.iterator();
        if (j.hasNext()) {
            Principal user = (Principal) j.next();
            callerPrincipal.addMember(user);
        }

        // Add the "CallerPrincipal" group to the Subject so that JBoss can fetch user.
        _savedSubject.getPrincipals().add(callerPrincipal);

        return rc;
    }

    /**
     * Retreives the list of roles associated to current principal.
     * This method supports the unauthenticatedIdentity property used by JBoss.
     */
    protected SSORole[] getRoleSets() throws LoginException {
        if (_ssoUserPrincipal == _unauthenticatedIdentity) {
            // Using unauthenticatedIdentity ...
            if(logger.isDebugEnabled()) {
                logger.debug("Using unauthenticatedIdentity " + _ssoUserPrincipal + ", returning no roles.");
            }
            return new SSORole[0];
        }
        return super.getRoleSets();
    }

    /**
     * Creates BaseUser implementation with the given name.
     *
     * @param username username
     * @return BaseUserImpl
     */
    protected SSOUser createIdentity(String username) {
        return new BaseUserImpl(username);
    }
}
