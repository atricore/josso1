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

package org.josso.jb5.agent;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.web.tomcat.security.JBossWebRealm;

/**
 * JBoss Realm that overrides hasRole method 
 * and creates CatalinaSSOUser (GenericPrincipal)
 * from the active subject (for SSO security domain) 
 * so roles can be processed by base tomcat/jboss realms.
 */
public class JBossCatalinaRealm extends JBossWebRealm {

	private static final Log logger = LogFactory
			.getLog(JBossCatalinaRealm.class);

	/**
	 * Return <code>true</code> if the specified Principal has the specified
	 * security role, within the context of this Realm; otherwise return
	 * <code>false</code>.
	 * 
	 * For SSO security domain it creates a GenericPrincipal from 
	 * the active authenticated subject before checking roles.
	 * 
	 * @param principal Principal for whom the role is to be checked
	 * @param role Security role to be checked
	 */
	public boolean hasRole(Principal principal, String role) {
		boolean hasRole = false;
		
		logger.debug("hasRole(" + principal + "," + role + ")");

		try {
			SecurityContext sc = JBossSecurityAssociationActions.getSecurityContext();
			if (!isSSODomain(sc.getSecurityDomain())) {
				// This is not a SSO Security domain, let JBoss realm handle this ...
				return super.hasRole(principal, role);
			}

			//Subject callerSubject = JBossSecurityAssociationActions.getSubject();
			Subject activeSubject = (Subject) PolicyContext
					.getContext(SecurityConstants.SUBJECT_CONTEXT_KEY);

			logger.debug("Authenticated Subject: " + activeSubject);

			CatalinaSSOUser ssoUser = CatalinaSSOUser.newInstance(this,	activeSubject);
			hasRole = super.hasRole(ssoUser, role);

		} catch (NullPointerException npe) {
			// Just in case ...
			if (logger.isDebugEnabled())
				logger.debug(npe);

			hasRole = super.hasRole(principal, role);

		} catch (PolicyContextException e) {
			logger.error(e, e);
		}

		return hasRole;
	}

	/**
	 * Checks if the given domain is a SSO security domain.
	 * 
	 * @param domain the security domain name to check
	 * @return true if this is a SSO security domain.
	 */
	protected boolean isSSODomain(String domain) {
		boolean isSSODomain = "josso".equals(domain);
		if (logger.isDebugEnabled()) {
			logger.debug(" JBoss Security Domain [" + domain + "] is"
					+ (isSSODomain ? "" : " not") + " under SSO Control");
		}
		return isSSODomain;
	}
}
