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

package org.josso.alfresco.agent;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.FacesHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.agent.Lookup;
import org.josso.agent.http.HttpSSOAgent;
import org.josso.agent.http.WebAccessControlUtil;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.identity.service.SSOIdentityManagerService;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.faces.context.FacesContext;
import javax.security.auth.Subject;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * JOSSO Servlet Filter for Alfresco SSO Agent
 * The fillter will handle web logic to authenticate, login and logout users.
 * <p/>
 * Date: Jun 10, 2010
 * Time: 16:00:00
 */

public class AlfrescoSSOAgentFilter implements Filter {

    private static final String KEY_SESSION_MAP = "org.josso.servlet.agent.sessionMap";

    private ServletContext _ctx;
    private HttpSSOAgent _agent;

    private ServiceRegistry serviceRegistry;
    private PersonService personService;
    private PermissionService permissionService;
    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authComponent;
    private TicketComponent ticketComponent;

    /**
     * Logger
     */
    private static final Log logger = LogFactory.getLog(AlfrescoSSOAgentFilter.class);


    public void init(FilterConfig filterConfig) throws ServletException {
        // Validate and update our current component state
        _ctx = filterConfig.getServletContext();
        WebApplicationContext webCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(_ctx);
        _ctx.setAttribute(KEY_SESSION_MAP, new HashMap());

        if (_agent == null) {

            try {

                Lookup lookup = Lookup.getInstance();
                lookup.init("josso-agent-config.xml"); // For spring compatibility ...

                // We need at least an abstract SSO Agent
                _agent = (HttpSSOAgent) lookup.lookupSSOAgent();
                if (logger.isDebugEnabled())
                    _agent.setDebug(1);
                _agent.start();

                // Publish agent in servlet context
                filterConfig.getServletContext().setAttribute("org.josso.agent", _agent);

            } catch (Exception e) {
                throw new ServletException("Error starting SSO Agent : " + e.getMessage(), e);
            }
        }

        try {
            this.serviceRegistry = (ServiceRegistry) webCtx.getBean(ServiceRegistry.SERVICE_REGISTRY);
            this.authenticationService = serviceRegistry.getAuthenticationService();
            this.personService = serviceRegistry.getPersonService();
            this.permissionService = (PermissionService) webCtx.getBean("PermissionService");
            this.authComponent = (AuthenticationComponent) webCtx.getBean("AuthenticationComponent");
            this.ticketComponent = (TicketComponent) webCtx.getBean("ticketComponent");
        } catch (BeansException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest hReq = (HttpServletRequest) request;
        HttpServletResponse hRes = (HttpServletResponse) response;
        HttpSession hSession = hReq.getSession();

        Cookie jCookie = getJossoCookie(hReq);
        // token is jossoSessionId
        String token = "";
        if (jCookie != null && !jCookie.getValue().equals("-"))
            token = jCookie.getValue();

        boolean isLoginRequest = isLoginRequest(hReq);
        boolean isLogoutRequest = isLogoutRequest(hReq);
        boolean isGuestRequest = (("").equals(token) && !isLoginRequest && !isLogoutRequest);
        boolean isNormalRequest = (!("").equals(token) && !isLoginRequest && !isLogoutRequest);

        String alfrescoContext = hReq.getContextPath();

        if (isLoginRequest) {
            String alfRedirect = (String) hSession.getAttribute("_alfRedirect");
            if (alfRedirect == null) {
            	alfRedirect = "";
            }
            _agent.setAttribute(hReq, hRes, WebAccessControlUtil.KEY_JOSSO_SAVED_REQUEST_URI, alfRedirect);
            //set non cache headers
            _agent.prepareNonCacheResponse(hRes);
            hRes.sendRedirect(alfrescoContext + _agent.getJossoLoginUri());
        }

        if (isLogoutRequest) {
            hSession.invalidate();
            hRes.sendRedirect(alfrescoContext + _agent.getJossoLogoutUri());
        }

        if (isGuestRequest) {
            filterChain.doFilter(hReq, hRes);
        }

        if (isNormalRequest) {
            try {
                SSOIdentityManagerService im = Lookup.getInstance().lookupSSOAgent().getSSOIdentityManager();
                SSOUser ssoUser = im.findUserInSession(token, token);
                String principal = "";
                if (ssoUser != null)
                    principal = ssoUser.getName();

                if (!existUser(principal)) { //user does not exist, create new one
                    HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
                    properties.put(ContentModel.PROP_USERNAME, principal);

                    for (SSONameValuePair nameValuePair : ssoUser.getProperties()) {

                        if (nameValuePair.getName().equals("user.name")) {
                            properties.put(ContentModel.PROP_FIRSTNAME, nameValuePair.getValue());

                        } else if (nameValuePair.getName().equals("urn:org:atricore:idbus:user:property:firstName")) {
                            properties.put(ContentModel.PROP_FIRSTNAME, nameValuePair.getValue());

                        } else if (nameValuePair.getName().equals("user.lastName")) {
                            properties.put(ContentModel.PROP_LASTNAME, nameValuePair.getValue());

                        } else if (nameValuePair.getName().equals("urn:org:atricore:idbus:user:property:lastName")) {
                            properties.put(ContentModel.PROP_LASTNAME, nameValuePair.getValue());

                        } else if (nameValuePair.getName().equals("email")) {
                            properties.put(ContentModel.PROP_EMAIL, nameValuePair.getValue());

                        } else if (nameValuePair.getName().equals("urn:org:atricore:idbus:user:property:email")) {
                            properties.put(ContentModel.PROP_EMAIL, nameValuePair.getValue());
                        }

                    }
                    createUser(principal, properties);
                }

                setAuthenticatedUser(hReq, hRes, hSession, principal);
                filterChain.doFilter(hReq, hRes);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
    }

    public void destroy() {
    }

    private Cookie getJossoCookie(HttpServletRequest hreq) {
        Cookie cookie = null;
        Cookie cookies[] = hreq.getCookies();
        if (cookies == null)
            cookies = new Cookie[0];
        for (int i = 0; i < cookies.length; i++) {
            if (org.josso.gateway.Constants.JOSSO_SINGLE_SIGN_ON_COOKIE.equals(cookies[i].getName())) {
                cookie = cookies[i];
                break;
            }
        }

        return cookie;
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        Enumeration parameterNames = request.getParameterNames();
        String alfRedirect = (String) request.getSession().getAttribute("_alfRedirect");
        
        if (request.getRequestURI().endsWith("login.jsp") && !"".equals(alfRedirect))
            return true;

        while (parameterNames.hasMoreElements()) {
            String parameter = (String) parameterNames.nextElement();
            String[] string = request.getParameterValues(parameter);
            for (int i = 0; i < string.length; i++) {
                if (string[i] != null && string[i].contains(":login")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameter = (String) parameterNames.nextElement();
            String[] string = request.getParameterValues(parameter);
            for (int i = 0; i < string.length; i++) {
                if (string[i] != null && string[i].contains(":logout")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean existUser(final String username) {
        AuthenticationUtil.RunAsWork<Boolean> existUserWork = new AuthenticationUtil.RunAsWork<Boolean>() {
            public Boolean doWork() throws Exception {
                return personService.personExists(username);
            }
        };
        Boolean result = AuthenticationUtil.runAs(existUserWork, AuthenticationUtil.getAdminUserName());

        return result.booleanValue();
    }

    public void createUser(final String username, final HashMap<QName, Serializable> userDetails) {
        AuthenticationUtil.RunAsWork<Object> createUserWork = new AuthenticationUtil.RunAsWork<Object>() {
            public Object doWork() throws Exception {
                authenticationService.createAuthentication(username, username.toCharArray());
                NodeRef newPerson = personService.createPerson(userDetails);
                permissionService.setPermission(newPerson, username, permissionService.getAllPermission(), true);
                authenticationService.setAuthenticationEnabled(username, true);
                return null;
            }
        };
        AuthenticationUtil.runAs(createUserWork, AuthenticationUtil.getAdminUserName());
    }

    protected void setAuthenticatedUser(final HttpServletRequest req, final HttpServletResponse res, final HttpSession httpSess, final String userName) {

        UserTransaction tx = serviceRegistry.getTransactionService().getUserTransaction();

        Subject.doAs(AlfrescoPrivilegdedActions.getAdminSubject(),
                AlfrescoPrivilegdedActions.clearCurrentSecurityContextAction(authComponent));
        ticketComponent.clearCurrentTicket();

        try {
            tx.begin();
            Subject.doAs(AlfrescoPrivilegdedActions.getAdminSubject(),
                    AlfrescoPrivilegdedActions.setCurrentUserAction(userName));
            Subject.doAs(AlfrescoPrivilegdedActions.getAdminSubject(),
                    AlfrescoPrivilegdedActions.createUserAction(serviceRegistry, userName, httpSess));

            FacesHelper.getFacesContext(req, res, _ctx);
            FacesContext fc = FacesContext.getCurrentInstance();
            Map session = fc.getExternalContext().getSessionMap();
            session.remove(AuthenticationHelper.SESSION_INVALIDATED);
            tx.commit();
        } catch (Throwable ex) {
            logger.error(ex);
            try {
                tx.rollback();
            } catch (Exception ex2) {
                logger.error("Failed to rollback transaction", ex2);
            }

            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new RuntimeException("Failed to execute transactional method", ex);
            }
        }
    }
}