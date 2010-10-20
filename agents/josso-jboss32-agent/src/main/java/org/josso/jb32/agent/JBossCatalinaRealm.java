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

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.naming.Util;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;
import org.jboss.security.plugins.JaasSecurityManager;
import org.jboss.util.CachePolicy;
import org.jboss.web.tomcat.security.JBossSecurityMgrRealm;
import org.jboss.web.tomcat.security.SecurityAssociationValve;
import org.josso.gateway.identity.SSOUser;
import org.josso.tc50.agent.jaas.CatalinaSSOUser;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
 * 2. Completely overrides the user authentication method so that the current Principal is not
 *    the SSO Session Id Principal but the SSOUser Principal.
 *
 * <p>
 * All Realm operations that require a SecurityContext were overriden so that there is a chance
 * for our Realm to prepare the "java:comp/env/security" JNDI Context.
 * <p>
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: JBossCatalinaRealm.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class JBossCatalinaRealm extends JBossSecurityMgrRealm {
    private static final Log logger = LogFactory.getLog(JBossCatalinaRealm.class);

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

    /** HashMap<SSOUserPrincipal, JossoSessionIdPrincipal> */
    /* private HashMap _userSessionMap = new HashMap();*/
    private SessionMappingCachePolicy _cachePolicy;

    /**
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
     * a username. So we need to set the SSOUser returned by the JAAS Gateway
     * Login Module as the authenticatd Principal.
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

            Subject subject = new Subject();
            if (securityMgr.isValid(principal, passwordChars, subject)) {
                logger.debug("User: " + username + " is authenticated");

                // Get the authorized subject set by the isValid() call on succesful
                // authentication.
                // Subject activeSubject = securityMgr.getActiveSubject();

                // logger.debug("Authenticated Subject: " + activeSubject);

                logger.debug("Authenticated Subject: " + subject);

                Set principals = subject.getPrincipals(SSOUser.class);
                Iterator i = principals.iterator();
                while (i.hasNext()) {
                    ssoUser = (SSOUser) i.next();
                    break;
                }

                // Make the cache aware of the user-session association so that
                // it can handle correctly cache entry lookups.
                //_cachePolicy.attachSessionToUser(principal, ssoUser);

                // Instead of associating the Principal used for authenticating (which is a
                // session id), sets the authenticated principal to the SSOUser part of the
                // Subject returned by the Gateway.
                JBossSecurityAssociationActions.setPrincipalInfo(ssoUser, passwordChars, subject);

                // Get the CallerPrincipal mapping
                RealmMapping realmMapping = (RealmMapping) securityCtx.lookup("realmMapping");
                Principal oldPrincipal = ssoUser;
                principal = realmMapping.getPrincipal(oldPrincipal);
                logger.debug("Mapped from input principal: " + oldPrincipal
                      + "to: " + principal);
                if (principal.equals(oldPrincipal) == false)
                {
                   _userPrincipalMap.put(principal, oldPrincipal);
                }

            } else {
                principal = null;
                logger.debug("User: " + username + " is NOT authenticated");
            }
        } catch (NamingException e) {
            principal = null;
            logger.error("Error during authenticate", e);
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
        } catch (NamingException ne) {
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
            logger.debug("Creating ENC using ClassLoader: " + loader);
            ClassLoader parent = loader.getParent();
            while (parent != null) {
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
            logger.debug("Linking security/securityMgr to JNDI name: " + JOSSO_SECURITY_DOMAIN);
            Util.bind(envCtx, "security/securityMgr", new LinkRef(JOSSO_SECURITY_DOMAIN));
            Util.bind(envCtx, "security/realmMapping", new LinkRef(JOSSO_SECURITY_DOMAIN));
            Util.bind(envCtx, "security/security-domain", new LinkRef(JOSSO_SECURITY_DOMAIN));
            Util.bind(envCtx, "security/subject", new LinkRef(JOSSO_SECURITY_DOMAIN + "/subject"));
        }

        logger.debug("JBossCatalinaRealm.prepareENC, End");

        return (Context) iniCtx.lookup("java:comp/env/security");
    }


    /** Lookup the authentication CachePolicy object for a security domain. This
     method first treats the cacheJndiName as a ObjectFactory location that is
     capable of returning CachePolicy instances on a per security domain basis
     by appending a '/security-domain-name' string to the cacheJndiName when
     looking up the CachePolicy for a domain. If this fails then the cacheJndiName
     location is treated as a single CachePolicy for all security domains.
     @deprecated No longer used for JBoss 3.2.6 support
     */
    private static CachePolicy lookupCachePolicy(String securityDomain) {
        CachePolicy authCache = null;
        String domainCachePath = cacheJndiName + '/' + securityDomain;
        try {
            InitialContext iniCtx = new InitialContext();
            authCache = (CachePolicy) iniCtx.lookup(domainCachePath);
        } catch (Exception e) {
            // Failed, treat the cacheJndiName name as a global CachePolicy binding
            try {
                InitialContext iniCtx = new InitialContext();
                authCache = (CachePolicy) iniCtx.lookup(cacheJndiName);
            } catch (Exception e2) {
                logger.warn("Failed to locate auth CachePolicy at: " + cacheJndiName
                        + " for securityDomain=" + securityDomain);
            }
        }
        return authCache;
    }

    /** Use reflection to attempt to set the authentication cache on the
     * securityMgr argument.
     *
     * This is done this way to avoid dependency with JaasSecurityManager.
     *
     * @deprecated No longer used for JBoss 3.2.6 support
     * @param securityMgr the security manager
     * @param cachePolicy the cache policy implementation
     */
    private static void setSecurityDomainCache(AuthenticationManager securityMgr,
                                               CachePolicy cachePolicy) {
        try {
            Class[] setCachePolicyTypes = {CachePolicy.class};
            Method m = securityMgr.getClass().getMethod("setCachePolicy", setCachePolicyTypes);
            Object[] setCachePolicyArgs = {cachePolicy};
            m.invoke(securityMgr, setCachePolicyArgs);
            logger.debug("setCachePolicy, c=" + setCachePolicyArgs[0]);
        } catch (Exception e2) {   // No cache policy support, this is ok
            logger.debug("setCachePolicy failed", e2);
        }
    }

}
