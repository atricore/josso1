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
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.tc55.agent.jaas.SSOGatewayLoginModule;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * SSOGatewayLogin Module for JBoss.
 * <p>
 * It specialized the SSOGatewayLoginModule by associating an additional
 * group called ("Roles") which contains user roles.
 * The original SSOGatewayLoginModule associates the user and its roles directly
 * as Subject's Principals. This won't work in JBoss since it obtains user roles
 * from a special Group that must be called "Roles".
 * This LoginModule adds this special group, adds the roles as members of it and
 * associates such group to the Subject as built by the SSOGatewayLoginModule.
 * <p>
 * To configure this JAAS Login Module module, add to the
 * $JBOSS_HOME/server/default/conf/login-config.xml file the following entry :
 * <p>
<pre>
&lt;policy&gt;
   &lt;!-- Used by JOSSO Agents for authenticating users against the Gateway --&gt;
   &lt;application-policy name = "josso"&gt;
      &lt;authentication&gt;
         &lt;login-module code = "org.josso.jb32.agent.JBossSSOGatewayLoginModule"
            flag = "required"&gt;
            &lt;module-option name="debug"&gt;true&lt;/module-option&gt;
         &lt;/login-module&gt;
      &lt;/authentication&gt;
   &lt;/application-policy&gt;
   ...
 &lt;/policy&gt;
</pre>
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: JBossSSOGatewayLoginModule.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class JBossSSOGatewayLoginModule extends SSOGatewayLoginModule {

    private static final Log logger = LogFactory.getLog(JBossSSOGatewayLoginModule .class);

    private Subject _savedSubject;

    /** the principal to use when user is not authenticated **/
    protected SSOUser _unauthenticatedIdentity;


    /**
     * Initialize this  LoginModule .
     * Save the received Subject to change it when commit() gets invoked.
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

        _savedSubject = subject;
        super.initialize(subject, callbackHandler, sharedState, options);
        // Check for unauthenticatedIdentity option.
        String name = (String) options.get("unauthenticatedIdentity");
        if( name != null )
        {
           try
           {
              _unauthenticatedIdentity = createIdentity(name);
              logger.debug("Saw unauthenticatedIdentity="+name);
           }
           catch(Exception e)
           {
              logger.warn("Failed to create custom unauthenticatedIdentity", e);
           }
        }
    }

    /**
     * This method supports the unauthenticatedIdentity property used by JBoss.
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

    /*
    * This method is called if the LoginContext's overall authentication succeeded.
    *
    * The Subject saved in the previously executed initialize() method, is modified
    * by adding a new special Group called "Roles" whose members are the SSO user roles.
    * JBoss will fetch user roles by examining such group.
    *
    * @exception LoginException if the commit fails.
    *
    * @return true if this LoginModule's own login and commit
    *        attempts succeeded, or false otherwise.
    */
    public boolean commit() throws LoginException {
        boolean rc = false;
        // HashMap setsMap = new HashMap();

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

    protected SSOUser createIdentity(String username) {
        return new BaseUserImpl(username);
    }

    protected SSORole[] getRoleSets() throws LoginException {
        if (_ssoUserPrincipal == _unauthenticatedIdentity) {
            // Using unauthenticatedIdentity ..
            if(logger.isDebugEnabled())
                logger.debug("Using unauthenticatedIdentity " + _ssoUserPrincipal + ", returning no roles.");

            return new SSORole[0];
        }
        return super.getRoleSets();
    }

}
