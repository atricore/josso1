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
 * SSOSessionManagerSOAPBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.josso.gateway.ws._1_2.wsdl;

import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.SecurityDomain;
import org.josso.gateway.SSOContext;
import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.ws._1_2.protocol.AccessSessionRequestType;
import org.josso.gateway.ws._1_2.protocol.AccessSessionResponseType;
import org.josso.gateway.ws._1_2.protocol.NoSuchSessionErrorType;
import org.josso.gateway.ws._1_2.protocol.SSOSessionErrorType;
import org.josso.gateway.ws._1_2.protocol.SSOSessionType;
import org.josso.gateway.ws._1_2.protocol.SessionRequestType;
import org.josso.gateway.ws._1_2.protocol.SessionResponseType;

public class SSOSessionManagerSOAPBindingImpl extends BaseSSOService implements SSOSessionManager {

    private static Log logger = LogFactory.getLog(SSOSessionManagerSOAPBindingImpl.class);

    public AccessSessionResponseType accessSession(AccessSessionRequestType body) throws java.rmi.RemoteException, 
    			NoSuchSessionErrorType, SSOSessionErrorType {
        try {
            // ----------------------- <PREPARE SSO CTX>
            String ssoSessionId = body.getSsoSessionId();
            prepareCtx(org.josso.gateway.session.service.SSOSessionManager.TOKEN_TYPE, ssoSessionId);
            // ----------------------- <PREPARE SSO CTX>

            if (logger.isDebugEnabled())
                logger.debug("About to access session");

            // If no context is present is because we could not resolve our session id!
            if (SSOContext.getCurrent() == null) {
                throw new NoSuchSessionErrorType(body.getSsoSessionId() != null && !"".equals(body.getSsoSessionId()) ? body.getSsoSessionId() : "[NULL]");
            }

            SecurityDomain sd = SSOContext.getCurrent().getSecurityDomain();
            sd.getSessionManager().accessSession(ssoSessionId);

            if (logger.isDebugEnabled())
                logger.debug("After access session");

            AccessSessionResponseType response = new AccessSessionResponseType ();
            response.setSsoSessionId(ssoSessionId);

            return response;

        } catch (NoSuchSessionException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage());
            throw new NoSuchSessionErrorType(body.getSsoSessionId() != null && !"".equals(body.getSsoSessionId()) ? body.getSsoSessionId() : "[NULL]");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOSessionErrorType ("SSOSessionManager error : " + e.getMessage());
        }

    }

    public SessionResponseType getSession(SessionRequestType body) throws RemoteException, NoSuchSessionErrorType, SSOSessionErrorType {
        try {
            // ----------------------- <PREPARE SSO CTX>
            String ssoSessionId = body.getSessionId();
            prepareCtx(org.josso.gateway.session.service.SSOSessionManager.TOKEN_TYPE, ssoSessionId);
            // ----------------------- <PREPARE SSO CTX>

            if (StringUtils.isBlank(ssoSessionId)) {
            	throw new NoSuchSessionException("SSOSessionId is empty!");
            }
            SSOSession s = SSOContext.getCurrent().getSecurityDomain().getSessionManager().getSession(ssoSessionId);

            SessionResponseType response = new SessionResponseType ();
            response.setSSOSession(adaptSession(s));

            return response;

        } catch (NoSuchSessionException e) {
            throw new NoSuchSessionErrorType(body.getSessionId() != null && !"".equals(body.getSessionId()) ? body.getSessionId() : "[NULL]");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new SSOSessionErrorType ("SSOSessionManager error : " + e.getMessage());
        }
    }

    private SSOSessionType adaptSession(SSOSession s) {
        SSOSessionType st = new SSOSessionType ();
        st.setId(s.getId());
        st.setAccessCount(s.getAccessCount());
        st.setCreationTime(s.getCreationTime());
        st.setLastAccessTime(s.getLastAccessTime());
        st.setMaxInactiveInterval(s.getMaxInactiveInterval());
        st.setUsername(s.getUsername());
        st.setValid(s.isValid());

        return st;
    }

}
