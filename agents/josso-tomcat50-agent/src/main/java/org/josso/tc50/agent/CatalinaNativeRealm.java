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

package org.josso.tc50.agent;

import java.lang.reflect.Field;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.catalina.realm.RealmBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.AbstractSSOAgent;
import org.josso.agent.Lookup;
import org.josso.agent.SSOAgent;
import org.josso.agent.SSOAgentRequest;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.tc50.agent.jaas.CatalinaSSOUser;

/**
 * Catalina Realm replacement that will authenticate users 
 * directly against the gateway.
 */
public class CatalinaNativeRealm extends RealmBase {
    private static Log log = LogFactory.getLog(CatalinaNativeRealm.class);

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "CatalinaNativeRealm";
    
    private boolean requiresRoleMap = false;
    
    private static List _roleClasses = new ArrayList();

    static {
        _roleClasses.add("org.josso.gateway.identity.service.BaseRoleImpl");
    }
    
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

	@Override
	public Principal authenticate(String username, String credentials) {
		try {


			String requester = "";
			// Check for nulls ?

            SSOAgentRequest request = AbstractSSOAgent._currentRequest.get();
            SSOAgent agent = Lookup.getInstance().lookupSSOAgent();
            SSOIdentityManagerService im = request.getConfig(agent).getIdentityManagerService();
            if (im == null) {
                im = agent.getSSOIdentityManager();
            }

            if (request == null)
                log.warn("No SSO Agent request found in thread local variable, can't identify requester");

            requester = request.getRequester();
            SSOUser ssoUser = im.findUserInSession(requester, username);
			
	        Principal principal = null;
	        
	        if (ssoUser != null) {
	        	Subject subject = new Subject();
	        	subject.getPrincipals().add(ssoUser);
	            SSORole[] ssoRolePrincipals = im.findRolesBySSOSessionId(requester, username);
	            for (int i=0; i < ssoRolePrincipals.length; i++) {
	                subject.getPrincipals().add(ssoRolePrincipals[i]);
	            }
	            // Return the appropriate Principal for this authenticated Subject
	            principal = createPrincipal(username, subject);
	        }
			
	        return principal;
		} catch (SSOIdentityException e) {
            // Ignore this ... (user does not exist for this session)
            if (log.isDebugEnabled()) {
            	log.debug(e.getMessage());
            }
            return null;
        } catch (Exception e) {
        	log.error("Session authentication failed : " + username, e);
            throw new RuntimeException("Fatal error authenticating session : " + e);
        }
	}

	/**
     * Construct and return a java.security.Principal instance
     * representing the authenticated user for the specified Subject. If no
     * such Principal can be constructed, return null.
     *
     * The Principal constructed is CatalinaSSOUser which is a SSOUser.
     * The Partner Application can access SSOUser-specific properties that are not available
     * in GenericPrincipal.
     *
     * @param subject The Subject representing the logged in user
     */
    protected Principal createPrincipal(String username, Subject subject) {
    	CatalinaSSOUser p = CatalinaSSOUser.newInstance(this, subject);
        
        if (requiresRoleMap) {
            // This is a Tomcat 5.0.30 ... !

            try {
                
                List<Principal> roles = new ArrayList<Principal>();

                Iterator principals = subject.getPrincipals().iterator();
                while (principals.hasNext()) {

                    Principal principal = (Principal) principals.next();
                    String principalClass = principal.getClass().getName();

                    if (_roleClasses.contains(principalClass)) {
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

	@Override
	protected String getName() {
		return name;
	}

	@Override
	protected String getPassword(String username) {
		return null;
	}

	@Override
	protected Principal getPrincipal(String username) {
		return authenticate(username, username);
	}
	
	protected Field getRoleMapField() {

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
