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

package org.josso.tc55.agent.jaas;

import org.apache.catalina.realm.JAASRealm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Catalina JAASRealm replacement that instantiates CatalinaSSOUser Principal instead of
 * GenericPrincipal.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: CatalinaJAASRealm.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class CatalinaJAASRealm extends JAASRealm {
    private static Log log = LogFactory.getLog(CatalinaJAASRealm.class);

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
    protected Principal createPrincipal(String username, Subject subject) {
        return CatalinaSSOUser.newInstance(this, subject);
    }

}
