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
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.bean.LoginBean;
import org.alfresco.web.bean.repository.User;
import org.josso.auth.SimplePrincipal;
import org.josso.gateway.identity.service.BaseRoleImpl;

import javax.security.auth.Subject;
import javax.servlet.http.HttpSession;
import java.security.PrivilegedAction;

/**
 * A PrivilegedActions implementations for setting authenticated user for Alfresco.
 */

class AlfrescoPrivilegdedActions {

    public static Subject getAdminSubject() {
        Subject subject = new Subject();
        SimplePrincipal adminPrincipal = new SimplePrincipal(AuthenticationUtil.getAdminUserName());
        subject.getPrincipals().add(adminPrincipal);
        BaseRoleImpl adminRole = new BaseRoleImpl(AuthenticationUtil.getAdminRoleName());
        adminRole.addMember(adminPrincipal);
        subject.getPrincipals().add(adminRole);
        return subject;
    }

    /*
    *   Privileged Action ClearCurrentSecurityContextAction
    */

    private static class ClearCurrentSecurityContextAction implements PrivilegedAction {
        private AuthenticationComponent authComponent;

        ClearCurrentSecurityContextAction(AuthenticationComponent aComp) {
            authComponent = aComp;
        }

        public Object run() {
            authComponent.clearCurrentSecurityContext();
            authComponent = null;
            return null;
        }
    }

    static ClearCurrentSecurityContextAction clearCurrentSecurityContextAction(AuthenticationComponent aComp) {
        ClearCurrentSecurityContextAction action = new ClearCurrentSecurityContextAction(aComp);
        return action;
    }

    /*
    *   Privileged Action SetCurrentUserAction
    */

    private static class SetCurrentUserAction implements PrivilegedAction {
        private String username;

        SetCurrentUserAction(String user) {
            username = user;
        }

        public Object run() {
            AuthenticationUtil.setRunAsUser(username);
            AuthenticationUtil.setFullyAuthenticatedUser(username);
            username = null;
            return null;
        }
    }

    static SetCurrentUserAction setCurrentUserAction(String user) {
        SetCurrentUserAction action = new SetCurrentUserAction(user);
        return action;
    }

    /*
    *   Privileged Action CreateUserAction
    */

    private static class CreateUserAction implements PrivilegedAction {
        private ServiceRegistry srvReg;
        private String username;
        private HttpSession httpSess;

        CreateUserAction(ServiceRegistry servRegistry, String user, HttpSession hSess) {
            username = user;
            srvReg = servRegistry;
            httpSess = hSess;
        }

        public Object run() {
            NodeService nodeService = srvReg.getNodeService();
            User user = new User(username, srvReg.getAuthenticationService().getCurrentTicket(), srvReg.getPersonService().getPerson(username));
            NodeRef homeSpaceRef = (NodeRef) nodeService.getProperty(srvReg.getPersonService().getPerson(username), ContentModel.PROP_HOMEFOLDER);
            user.setHomeSpaceId(homeSpaceRef.getId());

            httpSess.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
            httpSess.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);

            srvReg = null;
            username = null;
            return null;
        }
    }

    static CreateUserAction createUserAction(ServiceRegistry servRegistry, String user, HttpSession hSess) {
        CreateUserAction action = new CreateUserAction(servRegistry, user, hSess);
        return action;
    }
}