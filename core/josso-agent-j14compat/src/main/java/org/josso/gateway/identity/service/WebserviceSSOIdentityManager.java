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
package org.josso.gateway.identity.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityManager;
import org.josso.gateway.ws._1_2.protocol.*;
import org.josso.gateway.SSONameValuePair;

import java.util.ArrayList;
import java.rmi.RemoteException;

/**
 * Webservice client implementation for the SSO Identity Manager based on
 * the Axis-generated Stub & Skeleton.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: WebserviceSSOIdentityManager.java 578 2008-08-04 12:01:44Z gbrigand $
 */

public class WebserviceSSOIdentityManager implements SSOIdentityManagerService {

    private static final Log logger = LogFactory.getLog(WebserviceSSOIdentityManager.class);

    private SSOIdentityManager _wsSSOIdentityManager;

    private int _errorCount;

    private int _processedCount;

    /**
     * Build a Webservice SSO Identity Manager.
     *
     * @param wsSSOIdentityManager the SOAP stub to be invoked.
     */
    public WebserviceSSOIdentityManager(SSOIdentityManager wsSSOIdentityManager) {
        _wsSSOIdentityManager = wsSSOIdentityManager;
    }

    /**
     * Initializes this manager instance.
     */
    public void initialize() {

    }

    /**
     * Finds a user based on its security domain and name.  The name is a unique identifier of the user in the security domain, probably the user login.
     *
     * @throws org.josso.gateway.identity.exceptions.NoSuchUserException
     *          if the user does not exist.
     */
    public SSOUser findUser(String requester, String securityDomain, String username)
            throws NoSuchUserException, SSOIdentityException {

        try {
            if (logger.isDebugEnabled())
                logger.debug("[findUser()] : " + securityDomain + ":" + username);

            FindUserInSecurityDomainRequestType request = new FindUserInSecurityDomainRequestType();
            request.setRequester(requester);
            request.setSecurityDomain(securityDomain);
            request.setUsername(username);

            FindUserInSecurityDomainResponseType response = _wsSSOIdentityManager.findUserInSecurityDomain(request);
            return adaptSSOUser(response.getSSOUser());
        } catch (NoSuchUserErrorType e) {
            throw new NoSuchUserException(e.getUsername());
        } catch (SSOIdentityManagerErrorType e) {
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (RemoteException e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }
    }

    /**
     * Finds the user associated to a sso session
     *
     * @param sessionId the sso session identifier
     * @throws org.josso.gateway.identity.exceptions.NoSuchUserException
     *          if no user is associated to this session id.
     */
    public SSOUser findUserInSession(String requester, String sessionId)
            throws NoSuchUserException, SSOIdentityException {

        if (logger.isDebugEnabled())
            logger.debug("[findUserInSession()] : " + sessionId);
        try {
            FindUserInSessionRequestType request = new FindUserInSessionRequestType(requester, sessionId);
            FindUserInSessionResponseType response = _wsSSOIdentityManager.findUserInSession(request);
            return adaptSSOUser(response.getSSOUser());
        } catch (InvalidSessionErrorType e) {
            throw new SSOIdentityException(e.getMessage());
        } catch (NoSuchUserErrorType e) {
            throw new NoSuchUserException(e.getUsername());
        } catch (SSOIdentityManagerErrorType e) {
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (RemoteException e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }
    }

    /**
     * Finds a collection of user's roles.
     * Elements in the collection are SSORole instances.
     *
     * @param ssoSessionId
     * @throws org.josso.gateway.identity.exceptions.SSOIdentityException
     *
     */
    public SSORole[] findRolesBySSOSessionId(String requester, String ssoSessionId)
            throws SSOIdentityException {

        try {
            if (logger.isDebugEnabled())
                logger.debug("[findRolesBySSOSessionId()] : " + ssoSessionId);

            FindRolesBySSOSessionIdRequestType request = new FindRolesBySSOSessionIdRequestType();
            request.setRequester(requester);
            request.setSsoSessionId(ssoSessionId);
            FindRolesBySSOSessionIdResponseType response = _wsSSOIdentityManager.findRolesBySSOSessionId(request);
            return adaptSSORoles(response.getRoles());

        } catch (InvalidSessionErrorType e) {
            throw new SSOIdentityException(e.getMessage());
        } catch (SSOIdentityManagerErrorType e) {
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (RemoteException e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }
    }

    public void userExists(String requester, String securityDomain, String username) throws NoSuchUserException, SSOIdentityException {
        try {
            if (logger.isDebugEnabled())
                logger.debug("[userExists()] : " + username);
            UserExistsRequestType request = new UserExistsRequestType(requester, securityDomain, username);
            UserExistsResponseType response = _wsSSOIdentityManager.userExists(request);
            if (!response.isUserexists())
                throw new NoSuchUserException(username);

        } catch (java.rmi.RemoteException e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } catch (Exception e) {
            _errorCount++;
            throw new SSOIdentityException(e.getMessage(), e);
        } finally {
            _processedCount++;
        }
    }


    /**
     * Maps a SOAP SSOUser type instance to a JOSSO SSOUser type instance.
     *
     * @param srcSSOUser the SOAP type instance to be mapped.
     * @return the mapped user
     */
    protected SSOUser adaptSSOUser(SSOUserType srcSSOUser) {

        BaseUserImpl targetSSOUser = new BaseUserImpl();

        targetSSOUser.setName(srcSSOUser.getName());

        // map Properties
        SSONameValuePair[] properties = new SSONameValuePair[srcSSOUser.getProperties().length];
        for (int i = 0; i < srcSSOUser.getProperties().length; i++) {
            SSONameValuePairType nvpt = srcSSOUser.getProperties()[i];
            properties[i] = new SSONameValuePair(nvpt.getName(), nvpt.getValue());
        }

        targetSSOUser.setProperties(properties);

        return targetSSOUser;
    }

    /**
     * Maps a SOAP SSOValuePair type instance to a JOSSO SSOValuePair type instance.
     *
     * @param srcSSONameValuePair the SOAP type instance to be mapped.
     * @return the mapped value pair
     */
    protected SSONameValuePair adaptSSOValuePair(SSONameValuePair srcSSONameValuePair) {

        BaseUserImpl targetSSOUser = new BaseUserImpl();
        org.josso.gateway.SSONameValuePair targetSSONameValuePair = new
                org.josso.gateway.SSONameValuePair(srcSSONameValuePair.getName(),
                srcSSONameValuePair.getValue());

        return targetSSONameValuePair;
    }


    /**
     * Maps a SOAP SSORole type instance to a JOSSO SSORole type instance.
     *
     * @param srcSSORole the SOAP type instance to be mapped.
     * @return the mapped role
     */
    protected SSORole adaptSSORole(SSORoleType srcSSORole) {

        BaseRoleImpl targetSSORole = new BaseRoleImpl();

        targetSSORole.setName(srcSSORole.getName());
        return targetSSORole;
    }

    /**
     * Maps one or more SOAP SSORole type instancess to one or more JOSSO SSORole type instances.
     *
     * @param srcSSORoles the SOAP type instances to be mapped.
     * @return the mapped roles
     */
    protected SSORole[] adaptSSORoles(SSORoleType[] srcSSORoles) {

        ArrayList targetSSORoles = new ArrayList();
        for (int i = 0; i < srcSSORoles.length; i++) {
            targetSSORoles.add(adaptSSORole(srcSSORoles[i]));
        }

        return (SSORole[]) targetSSORoles.toArray(new BaseRoleImpl[targetSSORoles.size()]);
    }


    public int getErrorCount() {
        return _errorCount;
    }

    public int getProcessedCount() {
        return _processedCount;
    }

}
