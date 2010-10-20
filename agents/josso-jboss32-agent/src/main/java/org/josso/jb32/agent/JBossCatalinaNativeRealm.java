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

package org.josso.jb32.agent;

import java.security.Principal;
import java.security.acl.Group;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.naming.Util;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.plugins.JaasSecurityManager;
import org.jboss.web.tomcat.security.JBossSecurityMgrRealm;
import org.jboss.web.tomcat.security.SecurityAssociationValve;
import org.josso.agent.AbstractSSOAgent;
import org.josso.agent.Lookup;
import org.josso.agent.SSOAgentRequest;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.tc50.agent.jaas.CatalinaSSOUser;

/**
 * JBoss Realm proxy that does mainly the following two things :
 *
 * <p>
 * 1. Before invoking the overriden Realm methods, it creates a "java:comp/env/security" JNDI context
 *    needed by the JBossSecurityMgrRealm to retrieve the configured JBoss Security Manager.
 *    The "java:comp/env/security" context is only created by Catalina for built-in authenticators
 *    and web applications contexts. The Context where the Agent Valve is associated to does not have
 *    an ENC at all so we must build one for it.
 * <p>
 * 2. Completely overrides the user authentication method to authenticate directly against the gateway 
 * 	  and to set the current Principal to SSOUser Principal, not the SSO Session Id Principal.
 *
 * <p>
 * All Realm operations that require a SecurityContext were overriden so that there is a chance
 * for our Realm to prepare the "java:comp/env/security" JNDI Context.
 * <p>
 */
public class JBossCatalinaNativeRealm extends JBossSecurityMgrRealm {
    private static final Log logger = LogFactory.getLog(JBossCatalinaNativeRealm.class);

    /** The fixed JOSSO JBoss Security Domain Name */
    private static final String JOSSO_SECURITY_DOMAIN = "java:/jaas/josso";

    private static final String DEFAULT_CACHE_POLICY_PATH = "java:/timedCacheFactory";

    /** The location of the security credential cache policy. This is first treated
     as a ObjectFactory location that is capable of returning CachePolicy instances
     on a per security domain basis by appending a '/security-domain-name' string
     to this name when looking up the CachePolicy for a domain. If this fails then
     the location is treated as a single CachePolicy for all security domains.
     */
    private static String cacheJndiName = DEFAULT_CACHE_POLICY_PATH;

    /** HashMap<UserPrincipal, AuthPrincipal> */
    private HashMap _userPrincipalMap = new HashMap();
    
    /**
     * Checks if the given domain is a SSO security domain.
     * 
     * @param domain the security domain name
     * @return true if this is a SSO security domain.
     */
    protected boolean isSSODomain(String domain) {

        boolean isSSODomain = "josso".equals(domain);

        if (logger.isDebugEnabled())
            logger.debug(" JBoss Security Domain ["+domain+"] is" + (isSSODomain ? "" : " not") + " under SSO Control");

        return isSSODomain; 
    }

    /**
     * Return the Principal associated with the specified username and
     * credentials, if there is one; otherwise return null.
     *
     * The method was completely rewritten since the overriden operation,
     * on succesfull authentication, sets as the authenticated Principal
     * a SimplePrincipal instantiated using the provided username.
     * The problem is that in JOSSO the username is a SSO Session Id, not
     * a username. So we need to set the SSOUser returned by the Gateway
     * as the authenticatd Principal.
     * Since the JaasSecurityManager caches the authenticated user using the
     * Principal referring to a JOSSO Session Id, we will need to map, for
     * example when roles are checked against the realm, a user Principal
     * back to its JOSSO Session Identifier Principal. This way the the user
     * and its roles can be retrieved correctly by the JaasSecurityManager.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     * authenticating this username
     */
    public Principal authenticate(String username, String credentials) {

        logger.debug("Begin authenticate, username=" + username);

        Principal principal = null;
        SSOUser ssoUser = null;
        Principal caller = (Principal) SecurityAssociationValve.userPrincipal.get();
        if (caller == null && username == null && credentials == null)
            return null;

        try {
            Context securityCtx = null;
            securityCtx = prepareENC();

            if (securityCtx == null) {
                logger.error("No security context for authenticate(String, String)");
                return null;
            }

            // Get the JBoss security manager from the ENC context
            SubjectSecurityManager securityMgr = (SubjectSecurityManager) securityCtx.lookup("securityMgr");
            if (!isSSODomain(securityMgr.getSecurityDomain())) {
                // This is not a SSO Security domain, let JBoss realm handle this ...
                return super.authenticate(username, credentials);
            }
            
            principal = new SimplePrincipal(username);
            char[] passwordChars = null;
            if (credentials != null)
                passwordChars = credentials.toCharArray();
            
            SSOIdentityManagerService im = Lookup.getInstance().lookupSSOAgent().getSSOIdentityManager();
            
            String requester = "";
			// Check for nulls ?
            SSOAgentRequest request = AbstractSSOAgent._currentRequest.get();
            if (request != null)
            	requester = request.getRequester();
            else
                logger.warn("No SSO Agent request found in thread local variable, can't identify requester");
            
	        ssoUser = im.findUserInSession(requester, username);
			
            if (ssoUser != null) {
                logger.debug("User: " + username + " is authenticated");

                Subject subject = new Subject();
                subject.getPrincipals().add(ssoUser);
                logger.warn("WARN Cannot identify requester!");
	            SSORole[] ssoRolePrincipals = im.findRolesBySSOSessionId(requester, username);
	            Group targetGrp = new BaseRoleImpl("Roles");
	            for (int i=0; i < ssoRolePrincipals.length; i++) {
	                subject.getPrincipals().add(ssoRolePrincipals[i]);
	                targetGrp.addMember(ssoRolePrincipals[i]); // Add user role to "Roles" group
	            }
	            // Add the "Roles" group to the Subject so that JBoss can fetch user roles.
	            subject.getPrincipals().add(targetGrp);
	            
                logger.debug("Authenticated Subject: " + subject);

                // Make the cache aware of the user-session association so that
                // it can handle correctly cache entry lookups.
                //_cachePolicy.attachSessionToUser(principal, ssoUser);

                // Instead of associating the Principal used for authenticating (which is a
                // session id), sets the authenticated principal to the SSOUser part of the
                // Subject returned by the Gateway.
                JBossSecurityAssociationActions.setPrincipalInfo(ssoUser, passwordChars, subject);

                // Get the CallerPrincipal mapping
                RealmMapping rm = (RealmMapping) securityCtx.lookup("realmMapping");
                Principal oldPrincipal = ssoUser;
                principal = rm.getPrincipal(oldPrincipal);
                logger.debug("Mapped from input principal: " + oldPrincipal
                        + " to: " + principal);
                if (!principal.equals(oldPrincipal)) {
                   _userPrincipalMap.put(principal, oldPrincipal);
                }

            } else {
                principal = null;
                logger.debug("User: " + username + " is NOT authenticated");
            }
        } catch (NamingException e) {
            principal = null;
            logger.error("Error during authenticate", e);
        } catch (SSOIdentityException e) {
            // Ignore this ... (user does not exist for this session)
            if (logger.isDebugEnabled()) {
            	logger.debug(e.getMessage());
            }
            principal = null;
        } catch (Exception e) {
        	logger.error("Session authentication failed : " + username, e);
            throw new RuntimeException("Fatal error authenticating session : " + e);
        }
        logger.debug("End authenticate, principal=" + ssoUser);
        return ssoUser;
    }

    /**
     * Return <code>true</code> if the specified Principal has the specified
     * security role, within the context of this Realm; otherwise return
     * <code>false</code>.
     *
     * Since the Principal, in the JaasSecurityManager, has been stored in its cache
     * using the JOSSO Single Sign-On Session Identifier Principal (see isValid method),
     * when roles are checked , the Principal to be submitted to the overriden
     * operation is not the user principal but the JOSSO Session Id Principal.
     *
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     */
    public boolean hasRole(Principal principal, String role) {
        boolean hasRole = false;

        try {
            Context securityCtx = null;
            securityCtx = prepareENC();
            
            if (securityCtx == null) {
                logger.error("No security context for authenticate(String, String)");
                return false;
            }

            logger.debug("hasRole("+principal+","+role+")");

            // Get the JBoss security manager from the ENC context
            SubjectSecurityManager securityMgr = (SubjectSecurityManager) securityCtx.lookup("securityMgr");
            if (!isSSODomain(securityMgr.getSecurityDomain())) {
                // This is not a SSO Security domain, let JBoss realm handle this ...
                return super.hasRole(principal, role);
            }

            Subject activeSubject = securityMgr.getActiveSubject();

            logger.debug("Authenticated Subject: " + activeSubject);

            CatalinaSSOUser ssoUser = CatalinaSSOUser.newInstance(this, activeSubject);
            hasRole = super.hasRole(ssoUser, role);

        } catch (NamingException e) {
            principal = null;
            logger.error("Error during authenticate", e);
        }

        return hasRole;
    }

    /**
     * Return the Principal associated with the specified chain of X509
     * client certificates.  If there is none, return <code>null</code>.
     *
     * Before invoking the overriden operation it creates the security JNDI context
     * in case one was not found.
     *
     * @param certs Array of client certificates, with the first one in
     * the array being the certificate of the client itself.
     */
    public Principal authenticate(X509Certificate[] certs) {
        logger.debug("authenticate(X509Certificate[]), Begin");

        try {
            prepareENC();
            return super.authenticate(certs);
        } catch (Exception ne) {
            // Error creating ENC Context
            logger.error("Cannot create ENC Context");
        }

        logger.debug("authenticate(), Emd");
        return null;
    }

    /** This creates a java:comp/env/security context that contains a
     securityMgr binding pointing to an AuthenticationManager implementation
     and a realmMapping binding pointing to a RealmMapping implementation.
     */
    protected Context prepareENC()
            throws NamingException {

        if (logger.isDebugEnabled())
            logger.debug("JBossCatalinaRealm.prepareENC, Start");

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        InitialContext iniCtx = new InitialContext();

        boolean securityContextExists = false;
        boolean isJaasSecurityManager = false;
        try {
            Context envCtx = (Context) iniCtx.lookup("java:comp/env");
            Context securityCtx = (Context) envCtx.lookup("security");
            securityContextExists = true;

            AuthenticationManager securityMgr = (AuthenticationManager)
                    securityCtx.lookup("securityMgr");

            // If the Security Manager set in the web application ENC is not
            // a JaasSecurityManager, unbind the Security context and rebind it
            // with the JaasSecurityManager associated with the JOSSO Security Domain.
            // Note: the jboss-web.xml file of the partner application MUST not have an
            // entry referring to a security domain.
            if (!(securityMgr instanceof JaasSecurityManager)) {
                Util.unbind(envCtx, "security");
            } else
                isJaasSecurityManager = true;
        } catch (NamingException e) {
            // No Security Context found
        }

        // If we do not have a SecurityContext create it
        Context envCtx = null;
        if (!securityContextExists) {
            Thread currentThread = Thread.currentThread();
            if (logger.isDebugEnabled())
                logger.debug("Creating ENC using ClassLoader: " + loader);
            ClassLoader parent = loader.getParent();
            while (parent != null) {

                if (logger.isDebugEnabled())
                    logger.debug(".." + parent);

                parent = parent.getParent();
            }

            envCtx = (Context) iniCtx.lookup("java:comp");
            envCtx = envCtx.createSubcontext("env");
        } else
            envCtx = (Context) iniCtx.lookup("java:comp/env");

        // If the Security Manager binded is not a JaasSecurityManager, rebind using
        // the Security Manager associated with the JOSSO Security Domain.
        if (!isJaasSecurityManager) {
            // Prepare the Security JNDI subcontext
            if (logger.isDebugEnabled())
                logger.debug("Linking security/securityMgr to JNDI name: " + JOSSO_SECURITY_DOMAIN);

            Util.bind(envCtx, "security/securityMgr", new LinkRef(JOSSO_SECURITY_DOMAIN));
            Util.bind(envCtx, "security/realmMapping", new LinkRef(JOSSO_SECURITY_DOMAIN));
            Util.bind(envCtx, "security/security-domain", new LinkRef(JOSSO_SECURITY_DOMAIN));
            Util.bind(envCtx, "security/subject", new LinkRef(JOSSO_SECURITY_DOMAIN + "/subject"));
        }

        if (logger.isDebugEnabled())
            logger.debug("JBossCatalinaRealm.prepareENC, End");

        return (Context) iniCtx.lookup("java:comp/env/security");
    }

}
