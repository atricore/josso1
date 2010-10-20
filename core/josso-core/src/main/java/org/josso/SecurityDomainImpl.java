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
 * @org.apache.xbean.XBean element="domain" 
 *
 * Security Domain default implementation.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: SecurityDomainImpl.java 574 2008-08-01 21:14:27Z sgonzalez $
 */

public class SecurityDomainImpl implements SecurityDomain {

    private List<SecurityDomainMatcher> matchers;

    private SSOIdentityManager ssoIdentityManager;
    private SSOSessionManager ssoSessionManager;
    private Authenticator authenticator;
    private SSOAuditManager ssoAuditManager;
    private SSOEventManager ssoEventManager;
    private SSOProtocolManager ssoProtocolManager;

    private SSOWebConfiguration ssoWebConfiguration;
    private AssertionManager assertionManager;
    private SSOIdentityProvider ssoIdentityProvider;
    private PasswordManagementService passwordManager;

    /**
     * The SSO Security Domain Name.
     */
    private String name;

    /**
     * Reserved for future use
     */
    private String type;


    /**
     * @org.apache.xbean.Property alias="identity-manager"
     * @return
     */
    public SSOIdentityManager getIdentityManager() {
        return this.ssoIdentityManager;
    }

    public void setIdentityManager(SSOIdentityManager ssoIdentityManager) {
        this.ssoIdentityManager = ssoIdentityManager;
    }

    /**
     * @org.apache.xbean.Property alias="session-manager"
     */
    public SSOSessionManager getSessionManager() {
        return this.ssoSessionManager;
    }

    public void setSessionManager(SSOSessionManager ssoSessionManager) {
        this.ssoSessionManager = ssoSessionManager;
    }

    /**
     * @org.apache.xbean.Property alias="authenticator"
     */
    public Authenticator getAuthenticator() {
        return this.authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @org.apache.xbean.Property alias="audit-manager"
     */
    public SSOAuditManager getAuditManager() {
        return this.ssoAuditManager;
    }

    public void setAuditManager(SSOAuditManager ssoAuditManager) {
        this.ssoAuditManager = ssoAuditManager;
    }

    /**
     * @org.apache.xbean.Property alias="event-manager"
     */
    public SSOEventManager getEventManager() {
        return ssoEventManager;
    }

    public void setEventManager(SSOEventManager ssoEventManager) {
        this.ssoEventManager = ssoEventManager;
    }

    /**
     * @org.apache.xbean.Property alias="protocol-manager"
     */
    public SSOProtocolManager getProtocolManager() {
        return this.ssoProtocolManager;
    }

    public void setProtocolManager(SSOProtocolManager ssoProtocolManager) {
        this.ssoProtocolManager = ssoProtocolManager;
    }


    /**
     * @org.apache.xbean.Property alias="sso-web-configuration"
     * @return
     */
    public SSOWebConfiguration getSSOWebConfiguration() {
        return ssoWebConfiguration;
    }

    public void setSSOWebConfiguration(SSOWebConfiguration ssoWebConfiguration) {
        this.ssoWebConfiguration = ssoWebConfiguration;
    }

    /**
     * @org.apache.xbean.Property alias="assertion-manager"
     */
    public AssertionManager getAssertionManager() {
        return assertionManager;
    }

    public void setAssertionManager(AssertionManager assertionManager) {
        this.assertionManager = assertionManager;
    }

    /**
     * @org.apache.xbean.Property alias="identity-provider"
     */
    public SSOIdentityProvider getIdentityProvider() {
        return ssoIdentityProvider;
    }

    public void setIdentityProvider(SSOIdentityProvider ssoIdentityProvider) {
        this.ssoIdentityProvider = ssoIdentityProvider;
    }

    /**
     * @org.apache.xbean.Property alias="matchers" nestedType="org.josso.gateway.SecurityDomainMatcher"
     * @return
     */
    public List<SecurityDomainMatcher> getMatchers() {
        return matchers;
    }

    public void setMatchers(List<SecurityDomainMatcher> matchers) {
        this.matchers = matchers;
    }

    public PasswordManagementService getPasswordManager() {
        return this.passwordManager;
    }

    /**
     * @org.apache.xbean.Property alias="password-manager"
     * @return
     */
    public void setPasswordManager(PasswordManagementService pwdSvc) {
        this.passwordManager = pwdSvc;
    }
}
