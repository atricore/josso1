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
 * SSOIdentityProviderSOAPBindingImpl.java
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
import org.josso.gateway.assertion.AssertionManager;
import org.josso.gateway.assertion.exceptions.AssertionNotValidException;
import org.josso.gateway.session.service.SSOSessionManager;
import org.josso.gateway.ws._1_2.protocol.AssertIdentityWithSimpleAuthenticationRequestType;
import org.josso.gateway.ws._1_2.protocol.AssertIdentityWithSimpleAuthenticationResponseType;
import org.josso.gateway.ws._1_2.protocol.AssertionNotValidErrorType;
import org.josso.gateway.ws._1_2.protocol.GlobalSignoffRequestType;
import org.josso.gateway.ws._1_2.protocol.GlobalSignoffResponseType;
import org.josso.gateway.ws._1_2.protocol.ResolveAuthenticationAssertionRequestType;
import org.josso.gateway.ws._1_2.protocol.ResolveAuthenticationAssertionResponseType;
import org.josso.gateway.ws._1_2.protocol.SSOIdentityProviderErrorType;

public class SSOIdentityProviderSOAPBindingImpl extends BaseSSOService implements SSOIdentityProvider  {

    private static Log logger = LogFactory.getLog(SSOIdentityProviderSOAPBindingImpl.class);

    public ResolveAuthenticationAssertionResponseType resolveAuthenticationAssertion(ResolveAuthenticationAssertionRequestType body) throws java.rmi.RemoteException, 
    			AssertionNotValidErrorType, SSOIdentityProviderErrorType {

        try {

            // ----------------------- <PREPARE SSO CTX>
            String aaId = body.getAssertionId();
            prepareCtx(AssertionManager.TOKEN_TYPE, aaId);
            // ----------------------- <PREPARE SSO CTX>

            if (StringUtils.isBlank(aaId)) {
            	throw new AssertionNotValidException("Assertion ID is empty!");
            }
            // Resolve assertion :
            SecurityDomain sd = SSOContext.getCurrent().getSecurityDomain();
            String ssoSessionId = sd.getIdentityProvider().resolveAuthenticationAssertion(aaId);

            // Create response obj.
            ResolveAuthenticationAssertionResponseType response = new ResolveAuthenticationAssertionResponseType();
            response.setSecurityDomain(sd.getName());
            response.setSsoSessionId(ssoSessionId);

            return response;
        } catch (AssertionNotValidException e) {
            throw new AssertionNotValidErrorType(body.getAssertionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityProviderErrorType("SSOIdentityProvider error : " + e.getMessage());
        }

    }

    public AssertIdentityWithSimpleAuthenticationResponseType assertIdentityWithSimpleAuthentication(AssertIdentityWithSimpleAuthenticationRequestType body) throws java.rmi.RemoteException, SSOIdentityProviderErrorType {
        try {
            // ----------------------- <PREPARE SSO CTX>
            prepareCtx(body.getSecurityDomain());
            // ----------------------- <PREPARE SSO CTX>

            String username = body.getUsername();
            String password = body.getPassword();

            if (StringUtils.isBlank(username)) {
            	throw new Exception("Username is empty!");
            }
            String aaId = SSOContext.getCurrent().getSecurityDomain().getIdentityProvider().assertIdentityWithSimpleAuthentication(username, password);

            AssertIdentityWithSimpleAuthenticationResponseType response = new AssertIdentityWithSimpleAuthenticationResponseType();
            response.setAssertionId(aaId);

            return response;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityProviderErrorType("SSOIdentityProvider error : " + e.getMessage());
        }

    }

    public GlobalSignoffResponseType globalSignoff(GlobalSignoffRequestType body) throws java.rmi.RemoteException, SSOIdentityProviderErrorType {
        try {
            // ----------------------- <PREPARE SSO CTX>
            String ssoSessionId = body.getSsoSessionId();
            prepareCtx(SSOSessionManager.TOKEN_TYPE, ssoSessionId);
            // ----------------------- <PREPARE SSO CTX>

            if (StringUtils.isBlank(ssoSessionId)) {
            	throw new Exception("SSOSessionId is empty!");
            }
            SSOContext.getCurrent().getSecurityDomain().getIdentityProvider().globalSignoff(ssoSessionId);

            GlobalSignoffResponseType response = new GlobalSignoffResponseType ();
            response.setSsoSessionId(ssoSessionId);

            return response;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOIdentityProviderErrorType("SSOIdentityProvider error : " + e.getMessage());
        }

    }

}
