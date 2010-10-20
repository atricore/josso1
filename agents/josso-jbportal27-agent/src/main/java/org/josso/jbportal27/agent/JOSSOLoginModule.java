/*
 *   Copyright (c) 2004-2006, Novascope S.A. and the JOSSO team
 *    All rights reserved.
 *    Redistribution and use in source and binary forms, with or
 *    without modification, are permitted provided that the following
 *    conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *
 *    * Neither the name of the JOSSO team nor the names of its
 *      contributors may be used to endorse or promote products derived
 *      from this software without specific prior written permission.
 *
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 *    CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 *    INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 *    BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 *    TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *    ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *    OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *    POSSIBILITY OF SUCH DAMAGE.
 */

package org.josso.jbportal27.agent;

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
import org.josso.tc55.agent.jaas.SSOGatewayLoginModule;

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
 * @version CVS $Id: JBossSSOGatewayLoginModule.java 338 2006-02-09 16:53:07Z sgonzalez $
 */

public class JOSSOLoginModule extends SSOGatewayLoginModule {

    private static final Log logger = LogFactory.getLog(JOSSOLoginModule.class);

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
        Set cour = new java.util.HashSet();
        while (i.hasNext()) {
            Principal p = (Principal)i.next();

            targetGrp.addMember(p); // Add user role to "Roles" group
            
            //super hack to make the Subject work properly with the Portal Authorization Engine
            ((BaseRoleImpl)p).addMember(this.createIdentity(p.getName()));
        }
        // Add the "Roles" group to the Subject so that JBoss can fetch user roles.
        _savedSubject.getPrincipals().removeAll(ssoRolePrincipals);
        _savedSubject.getPrincipals().add(targetGrp);

        /*Set ssoUserPrincipals = _savedSubject.getPrincipals(SSOUser.class);
        Group callerPrincipal = new BaseRoleImpl("CallerPrincipal");
        Iterator j = ssoUserPrincipals.iterator();
        if (j.hasNext()) {
            Principal user = (Principal) j.next();
            callerPrincipal.addMember(user);
        }

        // Add the "CallerPrincipal" group to the Subject so that JBoss can fetch user.
        _savedSubject.getPrincipals().add(callerPrincipal);*/

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
