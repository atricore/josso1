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

package org.josso.tc50.agent.jaas;

import org.apache.catalina.realm.JAASRealm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.security.auth.Subject;
import java.security.Principal;
import java.security.acl.Group;
import java.util.*;
import java.lang.reflect.Field;

/**
 * Catalina JAASRealm replacement that instantiates CatalinaSSOUser Principal instead of
 * GenericPrincipal.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: CatalinaJAASRealm.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class CatalinaJAASRealm extends JAASRealm {


    private static Log log = LogFactory.getLog(CatalinaJAASRealm.class);

    private boolean requiresRoleMap = false;

    @Override
    public void init() {
        super.init();

        try {

            if (getRoleMapField() != null) {
                requiresRoleMap = true;
                log.debug("Realm requires role mapping (Tomcat 5.0.30 ? )");

            } else  {
                log.debug("Realm does note requires role mapping (Tomcat 5.0.28 ? )");
                requiresRoleMap = false;
            }

        } catch (Exception e) {
            log.warn("Initializing CatalinaJAASRealm : " + e.getMessage(), e);
        }
    }

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

        // We also populate roles map ...

        CatalinaSSOUser p = CatalinaSSOUser.newInstance(this, subject);


        if (requiresRoleMap) {
            // This is a Tomcat 5.0.30 ... !

            try {
                
                List<Principal> roles = new ArrayList<Principal>();

                Iterator principals = subject.getPrincipals().iterator();
                while (principals.hasNext()) {

                    Principal principal = (Principal) principals.next();
                    String principalClass = principal.getClass().getName();


                    if (getRoleClassNames().contains(principalClass)) {
                        log.debug("Adding role : " + principal.getName());
                        roles.add(principal);
                    }

                    // Same as Jboss - that's a pretty clean solution
                    if ((principal instanceof Group) &&
                            "Roles".equals(principal.getName())) {
                        Group grp = (Group) principal;
                        Enumeration en = grp.members();
                        while (en.hasMoreElements()) {
                            Principal roleP = (Principal) en.nextElement();
                            log.debug("Adding role : " + roleP.getName());
                            roles.add(roleP);
                        }

                    }
                }


                // Only in Catalina 5.0.30!
                log.debug("Storing roles in parent roleMap");
                Map m = (Map) getRoleMapField().get(this);
                m.put(p, roles);




            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return p;
            }

        }

        return p;


    }


    protected Field getRoleMapField()  {

        // Check the field in our super class!
        Field[] fields = getClass().getSuperclass().getDeclaredFields();

        for (Field field : fields) {
            log.debug("Field:" + field.getName());
            if (field.getName().equals("roleMap"))
                return field;
        }

        return null;
    }

}
