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

package org.josso.tc60.agent.jaas;

import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;

import javax.security.auth.Subject;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * On authentication, Catalina JAAS Realm doesn't return the Principal instances as
 * built by the Login Modules. It returns a Catalina-specific Principal called GenericPrincipal.
 * As a consequence all the additional properties that the SSOUser instance carries are lost.
 * With this class, we'll make Catalina beleive that our SSOUser its actually a GenericPrincipal, so that it
 * doesn't create a new one.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: CatalinaSSOUser.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class CatalinaSSOUser extends GenericPrincipal implements SSOUser {
    private static Log logger = LogFactory.getLog(CatalinaSSOUser.class);

    private static List _userClasses = new ArrayList();
    private static List _roleClasses = new ArrayList();

    static {
        _userClasses.add("org.josso.gateway.identity.service.BaseUserImpl");
        _roleClasses.add("org.josso.gateway.identity.service.BaseRoleImpl");
    }

    private SSOUser _ssoUser;


    /**
     * Construct and return a java.security.Principal instance
     * representing the authenticated user for the specified Subject.  If no
     * such Principal can be constructed, return null.
     *
     * The Principal constructed is *not* GenericPrincipal as in Catalina JAASRealm class,
     * but CatalinaSSOUser which is a SSOUser.
     * The Partner Application can access SSOUser-specific properties that are not available
     * in GenericPrincipal.
     * The JAASRealm superclass invokes this factory method to build the Catalina-specific
     * Principal from the Subject filled by the configured JAASLoginModule.
     *
     * @param subject The Subject representing the logged in user
     */
    public static CatalinaSSOUser newInstance(Realm realm, Subject subject) {
        // Prepare to scan the Principals for this Subject
        String password = null; // Will not be carried forward
        ArrayList roles = new ArrayList();
        SSOUser ssoUser = null;
        String username = null;

        // Scan the Principals for this Subject
        Iterator principals = subject.getPrincipals().iterator();
        while (principals.hasNext()) {
            Principal principal = (Principal) principals.next();
            // No need to look further - that's our own stuff
            if (principal instanceof CatalinaSSOUser) {
                if (logger.isDebugEnabled())
                    logger.debug("Found old CatalinaSSOUser Principal " + principal);
                return (CatalinaSSOUser) principal;
            }
            String principalClass = principal.getClass().getName();

            if (logger.isDebugEnabled())
                logger.debug("Principal: " + principalClass + " " + principal);

            if (_userClasses.contains(principalClass)) {
                // Override the default - which is the original user, accepted by
                // the friendly LoginManager
                username = principal.getName();
            }
            if (_roleClasses.contains(principalClass)) {
                roles.add(principal.getName());
            }
            // Same as Jboss - that's a pretty clean solution
            if ((principal instanceof Group) &&
                    "Roles".equals(principal.getName())) {
                Group grp = (Group) principal;
                Enumeration en = grp.members();
                while (en.hasMoreElements()) {
                    Principal roleP = (Principal) en.nextElement();
                    roles.add(roleP.getName());
                }

            }

            // Save the SSOUser principal so that it can be included in the
            // CatalinaSSOUser Principal
            if (principal instanceof SSOUser) {
                ssoUser = (SSOUser) principal;
            }
        }

        if (ssoUser == null) {
            logger.error("Fatal: Subject does not contain an SSOUser Principal");
            return null;
        }

        // Create the resulting Principal for our authenticated user
        if (username != null) {
            return (new CatalinaSSOUser(ssoUser, realm, username, password, roles));
        } else {
            return (null);
        }
    }

    /**
     * Construct a new Principal, associated with the specified Realm, for the
     * specified username and password.
     *
     * @param realm The Realm that owns this Principal
     * @param name The username of the user represented by this Principal
     * @param password Credentials used to authenticate this user
     */
    private CatalinaSSOUser(SSOUser ssoUser, Realm realm, String name, String password) {

        this(ssoUser, realm, name, password, null);
    }


    /**
     * Construct a new Principal, associated with the specified Realm, for the
     * specified username and password, with the specified role names
     * (as Strings).
     *
     * @param realm The Realm that owns this principal
     * @param name The username of the user represented by this Principal
     * @param password Credentials used to authenticate this user
     * @param roles List of roles (must be Strings) possessed by this user
     */
    private CatalinaSSOUser(SSOUser ssoUser, Realm realm, String name, String password,
                           List roles) {

        super(realm, name, password, roles);
        _ssoUser = ssoUser;

    }

    /**
     * @deprecated this method always return null
     * @return always null
     */
    public String getSessionId() {
        return null;
    }

    public SSONameValuePair[] getProperties() {
        return _ssoUser.getProperties();
    }

    /**
     * Return a String representation of this object, which exposes only
     * information that should be public.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("CatalinaSSOUser[");
        sb.append(this.name);
        sb.append("]");
        return (sb.toString());
    }

}
