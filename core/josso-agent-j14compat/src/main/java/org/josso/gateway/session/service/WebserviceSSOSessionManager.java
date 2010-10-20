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
package org.josso.gateway.session.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.josso.gateway.ws._1_2.wsdl.SSOSessionManager;
import org.josso.gateway.ws._1_2.protocol.*;

import org.josso.gateway.session.SSOSession;
import org.josso.gateway.session.exceptions.NoSuchSessionException;
import org.josso.gateway.session.exceptions.SSOSessionException;

import java.rmi.RemoteException;

/**
 * Webservice client implementation for the SSO Session Manager based on
 * the Axis-generated Stub & Skeleton.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: WebserviceSSOSessionManager.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public class WebserviceSSOSessionManager implements SSOSessionManagerService {

    private static final Log logger = LogFactory.getLog(WebserviceSSOSessionManager.class);

    private SSOSessionManager _wsSSOSessionManager;

    // Statistics
    private long _processedCount;
    private long _errorCount;

    /**
     * Build a Webservice SSO Session Manager.
     *
     * @param wsSSOSessionManager the SOAP stub to be invoked.
     */
    public WebserviceSSOSessionManager(SSOSessionManager wsSSOSessionManager) {
        _wsSSOSessionManager = wsSSOSessionManager;
    }

    /**
     * This method accesss the session associated to the received id.
     * This resets the session last access time and updates the access count.
     *
     * @param sessionId the session id previously returned by initiateSession.
     * @throws NoSuchSessionException if the session id is not related to any sso session.
     */
    public void accessSession(String requester, String sessionId)
            throws NoSuchSessionException, SSOSessionException {

        try {
            AccessSessionRequestType request = new AccessSessionRequestType();
            request.setRequester(requester);
            request.setSsoSessionId(sessionId);
            _wsSSOSessionManager.accessSession(request);

        } catch (NoSuchSessionErrorType e) {
            throw new NoSuchSessionException(e.getSessionId());
        } catch (SSOSessionErrorType e) {
            throw new SSOSessionException(e.getMessage(), e);
        } catch (RemoteException e) {
            _errorCount++;
            throw new SSOSessionException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new SSOSessionException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }

    }

    public SSOSession getSession(String requester, String sessionId) throws NoSuchSessionException, SSOSessionException {
        try {
            SessionRequestType request = new SessionRequestType();
            request.setRequester(requester);
            request.setSessionId(sessionId);
            SessionResponseType response = _wsSSOSessionManager.getSession(request);
            return adaptSSOSession(response.getSSOSession());
        } catch (NoSuchSessionErrorType e) {
            throw new NoSuchSessionException(e.getSessionId());
        } catch (SSOSessionErrorType e) {
            throw new SSOSessionException(e.getMessage(), e);
        } catch (RemoteException e) {
            _errorCount++;
            throw new SSOSessionException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new SSOSessionException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }
    }

    public long getErrorCount() {
        return _errorCount;
    }

    public long getProcessedCount() {
        return _processedCount;
    }


    /**
     * Maps a SOAP SSOSession type instance to a JOSSO SSOSession type instance.
     *
     * @param srcSSOSession the SOAP type instance to be mapped.
     * @return the mapped session
     */
    protected SSOSession adaptSSOSession(SSOSessionType srcSSOSession) {

        MutableBaseSession targetSSOSession = new MutableBaseSession();

        targetSSOSession.setId(srcSSOSession.getId());
        targetSSOSession.setCreationTime(srcSSOSession.getCreationTime());
        targetSSOSession.setLastAccessedTime(srcSSOSession.getLastAccessTime());
        targetSSOSession.setMaxInactiveInterval(srcSSOSession.getMaxInactiveInterval());
        targetSSOSession.setUsername(srcSSOSession.getUsername());
        targetSSOSession.setAccessCount(srcSSOSession.getAccessCount());

        return targetSSOSession;

    }

}
