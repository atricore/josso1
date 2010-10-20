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
package org.josso;

import org.josso.auth.Authenticator;
import org.josso.gateway.SSOWebConfiguration;
import org.josso.gateway.SecurityDomainMatcher;
import org.josso.gateway.assertion.AssertionManager;
import org.josso.gateway.audit.SSOAuditManager;
import org.josso.gateway.event.SSOEventManager;
import org.josso.gateway.identity.service.SSOIdentityManager;
import org.josso.gateway.identity.service.SSOIdentityProvider;
import org.josso.gateway.protocol.SSOProtocolManager;
import org.josso.gateway.session.service.SSOSessionManager;
import org.josso.selfservices.password.PasswordManagementService;

import java.util.List;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: SecurityDomain.java 574 2008-08-01 21:14:27Z sgonzalez $
 */

public interface SecurityDomain {

    /**
     * Getter for this domain's Identity Manager instance.
     */
    SSOIdentityManager getIdentityManager();

    /**
     * Setter for this domain's Identity Manager instance.
     */
    void setIdentityManager(SSOIdentityManager im);

    /**
     * Getter for this domain's Session Manager instance.
     */
    SSOSessionManager getSessionManager();

    /**
     * Setter for this domain's Session Manager instance.
     */
    void setSessionManager(SSOSessionManager sm);

    /**
     * Getter for this domain's Authenticator instance.
     */
    Authenticator getAuthenticator();

    /**
     * Setter for this domain's Authenticator instance.
     */
    void setAuthenticator(Authenticator a);

    /**
     * Getter for this domain's Audit Manager Instance
     */
    SSOAuditManager getAuditManager();

    /**
     * Setter for this domain's Audit Manager Instance
     */
    void setAuditManager(SSOAuditManager am);

    /**
     * Getter for this domain's Event Manager instance.
     */
    SSOEventManager getEventManager();

    /**
     * Setter for this domain's Event Manager instance.
     */
    void setEventManager(SSOEventManager em);

    /**
     * Getter for this domain's name
     */
    String getName();

    /**
     * Getter for this domain's type
     */
    String getType();

    /**
     * Setter for this domain's name
     */
    void setName(String name);

    /**
     * Getter for this domain's Protocol Manager Instance
     */
    SSOProtocolManager getProtocolManager();

    /**
     * Setter for this domain's Protocol Manager Instance
     */
    void setProtocolManager(SSOProtocolManager pm);

    SSOWebConfiguration getSSOWebConfiguration();

    void setSSOWebConfiguration(SSOWebConfiguration ssoWebConfiguration);

    AssertionManager getAssertionManager();

    void setAssertionManager(AssertionManager assertionManager);

    SSOIdentityProvider getIdentityProvider();

    void setIdentityProvider(SSOIdentityProvider ssoIdentityProvider);

    List<SecurityDomainMatcher> getMatchers();

    void setMatchers(List<SecurityDomainMatcher> matcher);

    PasswordManagementService getPasswordManager();

    void setPasswordManager(PasswordManagementService pwdSvc);
}
