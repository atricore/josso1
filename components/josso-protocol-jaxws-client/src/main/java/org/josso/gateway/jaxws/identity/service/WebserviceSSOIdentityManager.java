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
package org.josso.gateway.jaxws.identity.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.exceptions.NoSuchUserException;
import org.josso.gateway.identity.exceptions.SSOIdentityException;
import org.josso.gateway.identity.service.BaseRoleImpl;
import org.josso.gateway.identity.service.BaseUserImpl;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.josso.gateway.ws._1_2.protocol.*;
import org.josso.gateway.ws._1_2.wsdl.InvalidSessionErrorMessage;
import org.josso.gateway.ws._1_2.wsdl.NoSuchUserErrorMessage;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityManager;
import org.josso.gateway.ws._1_2.wsdl.SSOIdentityManagerErrorMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Webservice client implementation for the SSO Identity Manager based on
 * the JAX-WS .
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
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
        } catch (NoSuchUserErrorMessage e) {
            throw new NoSuchUserException(e.getFaultInfo().getUsername());
        } catch (SSOIdentityManagerErrorMessage e) {
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
            FindUserInSessionRequestType request = new FindUserInSessionRequestType();
            request.setRequester(requester);
            request.setSsoSessionId(sessionId);
            FindUserInSessionResponseType response = _wsSSOIdentityManager.findUserInSession(request);
            return adaptSSOUser(response.getSSOUser());
        } catch (InvalidSessionErrorMessage e) {
            throw new SSOIdentityException(e.getMessage());
        } catch (NoSuchUserErrorMessage e) {
            throw new NoSuchUserException(e.getFaultInfo().getUsername());
        } catch (SSOIdentityManagerErrorMessage e) {
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

        } catch (InvalidSessionErrorMessage e) {
            throw new SSOIdentityException(e.getMessage());
        } catch (SSOIdentityManagerErrorMessage e) {
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
            UserExistsRequestType request = new UserExistsRequestType();
            request.setRequester(requester);
            request.setSecurityDomain(securityDomain);
            request.setUsername(username);
            UserExistsResponseType response = _wsSSOIdentityManager.userExists(request);
            if (!response.isUserexists())
                throw new NoSuchUserException(username);

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

        if (srcSSOUser.getProperties() != null) {
            SSONameValuePair[] properties = new SSONameValuePair[srcSSOUser.getProperties().size()];

            for (int i = 0; i < srcSSOUser.getProperties().size(); i++) {
                SSONameValuePairType nvpt = srcSSOUser.getProperties().get(i);
                properties[i] = new SSONameValuePair(nvpt.getName(), nvpt.getValue());
            }

            targetSSOUser.setProperties(properties);
        }

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
        SSONameValuePair targetSSONameValuePair = new
                SSONameValuePair(srcSSONameValuePair.getName(),
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
     *
     * @param srcSSORoles the SOAP type instances to be mapped.
     * @return the mapped roles
     */
    protected SSORole[] adaptSSORoles(List<SSORoleType> srcSSORoles) {

        if (srcSSORoles == null) {
            return new SSORole[0];
        }

        ArrayList<SSORole> targetSSORoles = new ArrayList<SSORole>();
        for (Iterator<SSORoleType> iterator = srcSSORoles.iterator(); iterator.hasNext(); ) {
            SSORoleType role = iterator.next();

            targetSSORoles.add(adaptSSORole(role));
        }

        return targetSSORoles.toArray(new BaseRoleImpl[targetSSORoles.size()]);
    }


    public int getErrorCount() {
        return _errorCount;
    }

    public int getProcessedCount() {
        return _processedCount;
    }

}
