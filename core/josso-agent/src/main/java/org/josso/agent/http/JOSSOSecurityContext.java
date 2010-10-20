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

package org.josso.agent.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * JOSSO Security context, used only by the Generic Servlet Container Agent.  Other agents will generate container specific
 * security context instances.
 * <p/>
 * Date: Nov 28, 2007
 * Time: 1:03:33 PM
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class JOSSOSecurityContext {

    private static final Log logger = LogFactory.getLog(JOSSOSecurityContext.class);

    /**
     * Current authenticated subject.
     */
    private Subject subject;

    /**
     * Principal representing current SSO user
     */
    private SSOUser ssoUser;

    /**
     * Map of principals representing current user's roles
     */
    private Map roles;

    /**
     * Creates a new security context for the given subject.  The subject must contain at least one SSOUser principal instance.
     */
    public JOSSOSecurityContext(Subject subject) {

        Set principals = subject.getPrincipals();
        roles = new HashMap();

        for (Iterator it = principals.iterator(); it.hasNext();) {

            Principal p = (Principal) it.next();
            if (p instanceof SSOUser) {
                if (ssoUser != null)
                    throw new IllegalArgumentException("Subject cannot contain multiple SSOUser instances");
                this.ssoUser = (SSOUser) p;
            } else if (p instanceof SSORole) {
                SSORole r = (SSORole) p;
                roles.put(r.getName(), r);
            }

        }

        if (ssoUser == null)
            throw new IllegalArgumentException("No SSOUser principal found in subject");

    }

    /**
     * Provides current principal
     */
    public SSOUser getCurrentPrincipal() {
        return ssoUser;
    }

    /**
     * @param role the role name
     * @return true if the subject has a SSORole principal with the given name.
     */
    public boolean isUserInRole(String role) {
        return roles.containsKey(role);
    }


    /**
     * Authenticated subject.
     */
    Subject getSubject() {
        return subject;
    }

}
