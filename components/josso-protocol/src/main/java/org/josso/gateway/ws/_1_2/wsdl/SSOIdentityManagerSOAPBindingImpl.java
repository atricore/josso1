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

/**
 * SSOIdentityManagerSOAPBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.josso.gateway.ws._1_2.wsdl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.SecurityDomain;
import org.josso.gateway.SSOContext;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.service.SSOSessionManager;
import org.josso.gateway.ws._1_2.protocol.FindRolesBySSOSessionIdRequestType;
import org.josso.gateway.ws._1_2.protocol.FindRolesBySSOSessionIdResponseType;
import org.josso.gateway.ws._1_2.protocol.FindUserInSecurityDomainRequestType;
import org.josso.gateway.ws._1_2.protocol.FindUserInSecurityDomainResponseType;
import org.josso.gateway.ws._1_2.protocol.FindUserInSessionRequestType;
import org.josso.gateway.ws._1_2.protocol.FindUserInSessionResponseType;
import org.josso.gateway.ws._1_2.protocol.InvalidSessionErrorType;
import org.josso.gateway.ws._1_2.protocol.NoSuchUserErrorType;
import org.josso.gateway.ws._1_2.protocol.SSOIdentityManagerErrorType;
import org.josso.gateway.ws._1_2.protocol.SSONameValuePairType;
import org.josso.gateway.ws._1_2.protocol.SSORoleType;
import org.josso.gateway.ws._1_2.protocol.SSOUserType;
import org.josso.gateway.ws._1_2.protocol.UserExistsRequestType;
import org.josso.gateway.ws._1_2.protocol.UserExistsResponseType;

public class SSOIdentityManagerSOAPBindingImpl extends BaseSSOService implements SSOIdentityManager {

    private static final Log logger = LogFactory.getLog(SSOIdentityManagerSOAPBindingImpl.class);

    public FindUserInSessionResponseType findUserInSession(FindUserInSessionRequestType body) throws java.rmi.RemoteException, 
    			InvalidSessionErrorType, NoSuchUserErrorType, SSOIdentityManagerErrorType {

        // ----------------------- <PREPARE SSO CTX>
        String ssoSessionId = body.getSsoSessionId();
        prepareCtx(SSOSessionManager.TOKEN_TYPE, ssoSessionId);
        // ----------------------- <PREPARE SSO CTX>
        SecurityDomain sd = SSOContext.getCurrent().getSecurityDomain();

        if (StringUtils.isBlank(ssoSessionId)) {
        	throw new InvalidSessionErrorType("SSOSessionId is empty!");
        }
        if (sd == null) {
            // Assume that session id was invalid!
            throw new InvalidSessionErrorType(ssoSessionId);
        }

        try {

            // Always validate user session!
            sd.getSessionManager().accessSession(ssoSessionId);

            SSOUser ssoUser = sd.getIdentityManager().findUserInSession(ssoSessionId);

            FindUserInSessionResponseType response = new FindUserInSessionResponseType();
            response.setSSOUser(toSSOUserType(ssoUser));

            return response;
        } catch (NoSuchSessionException e) {
            throw new InvalidSessionErrorType(ssoSessionId);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityManagerErrorType("SSOIdentityManager error : " + e.getMessage());
        }
    }

    public FindUserInSecurityDomainResponseType findUserInSecurityDomain(FindUserInSecurityDomainRequestType body) throws java.rmi.RemoteException, 
    			NoSuchUserErrorType, SSOIdentityManagerErrorType {
        try {
            // ----------------------- <PREPARE SSO CTX>
            String securityDomain = body.getSecurityDomain();
            prepareCtx(securityDomain);
            // ----------------------- <PREPARE SSO CTX>

            String username = body.getUsername();
            if (StringUtils.isBlank(username)) {
            	throw new NoSuchUserException("Username is empty!");
            }
            SSOUser user = SSOContext.getCurrent().getSecurityDomain().getIdentityManager().findUser(username);

            FindUserInSecurityDomainResponseType response = new FindUserInSecurityDomainResponseType();
            response.setSSOUser(toSSOUserType(user));

            return response;

        } catch (NoSuchUserException e) {
            NoSuchUserErrorType wsErr = new NoSuchUserErrorType ();
            wsErr.setUsername(body.getUsername());
            wsErr.setSecurityDomain(body.getSecurityDomain());
            throw wsErr;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityManagerErrorType("SSOIdentityManager error : " + e.getMessage());
        }

    }

    public FindRolesBySSOSessionIdResponseType findRolesBySSOSessionId(FindRolesBySSOSessionIdRequestType body) throws java.rmi.RemoteException, 
    			InvalidSessionErrorType, SSOIdentityManagerErrorType {

        // ----------------------- <PREPARE SSO CTX>
        String ssoSessionId = body.getSsoSessionId();
        prepareCtx(SSOSessionManager.TOKEN_TYPE, ssoSessionId);
        // ----------------------- <PREPARE SSO CTX>
        SecurityDomain sd = SSOContext.getCurrent().getSecurityDomain();
        if (StringUtils.isBlank(ssoSessionId)) {
        	throw new InvalidSessionErrorType("SSOSessionId is empty!");
        }
        if (sd == null) {
            // Assume that session id was invalid!
            throw new InvalidSessionErrorType(ssoSessionId);
        }
        
        try {

            // Always validate user session!
            sd.getSessionManager().accessSession(ssoSessionId);
            
            SSOUser user = sd.getIdentityManager().findUserInSession(ssoSessionId);
            SSORole[] roles = sd.getIdentityManager().findRolesByUsername(user.getName());

            SSORoleType[] roleTypes = adaptRoles(roles);

            FindRolesBySSOSessionIdResponseType response = new FindRolesBySSOSessionIdResponseType();
            response.setRoles(roleTypes);

            return response;
        } catch (NoSuchSessionException e) {
            throw new InvalidSessionErrorType(ssoSessionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityManagerErrorType("SSOIdentityManager error : " + e.getMessage());
        }
    }

    public UserExistsResponseType userExists(UserExistsRequestType body) throws java.rmi.RemoteException, SSOIdentityManagerErrorType {
        try {
            // ----------------------- <PREPARE SSO CTX>
            String securityDomain = body.getSecurityDomain();
            prepareCtx(securityDomain);
            // ----------------------- <PREPARE SSO CTX>

            UserExistsResponseType response = new UserExistsResponseType ();

            try {
            	if (StringUtils.isBlank(body.getUsername())) {
                	throw new NoSuchUserException("Username is empty!");
                }
                SSOContext.getCurrent().getSecurityDomain().getIdentityManager().userExists(body.getUsername());
                response.setUserexists(true);
            } catch (NoSuchUserException e) {
                response.setUserexists(false);
            }

            return response;


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityManagerErrorType("SSOIdentityManager error : " + e.getMessage());
        }
    }

    protected SSOUserType toSSOUserType(SSOUser user) {

        SSOContext cxt = SSOContext.getCurrent();

        SSOUserType userType = new SSOUserType();
        userType.setName(user.getName());
        userType.setSecuritydomain(cxt.getSecurityDomain().getName());

        // Properties
        SSONameValuePairType [] nvpts = adaptNameValuePairs(user.getProperties());

        userType.setProperties(nvpts);

        return userType;

    }

    protected SSONameValuePairType[] adaptNameValuePairs(SSONameValuePair[] nvps) {
        SSONameValuePairType [] nvpts = new SSONameValuePairType [nvps.length];
        for (int i = 0; i < nvps.length; i++) {
            SSONameValuePair nvp = nvps[i];
            SSONameValuePairType nvpt = adaptNameValuePair(nvp);
            nvpts[i] = nvpt;
        }
        return nvpts;

    }

    protected SSONameValuePairType adaptNameValuePair(SSONameValuePair nvp) {
        SSONameValuePairType nvpt = new SSONameValuePairType ();
        nvpt.setName(nvp.getName());
        nvpt.setValue(nvp.getValue());

        return nvpt;
    }

    protected SSORoleType[] adaptRoles(SSORole[] roles) {
        SSORoleType [] roleTypes = new SSORoleType [roles.length];

        for (int i = 0; i < roles.length; i++) {
            SSORole role = roles[i];
            roleTypes[i] = adaptRole(role);
        }

        return roleTypes;

    }

    protected SSORoleType adaptRole(SSORole r) {
        SSORoleType rt = new SSORoleType ();
        rt.setName(r.getName());

        return rt;
    }
    

}
