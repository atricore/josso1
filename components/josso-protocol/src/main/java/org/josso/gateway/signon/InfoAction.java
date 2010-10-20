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

package org.josso.gateway.signon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.josso.gateway.SSOException;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.identity.SSORole;
import org.josso.gateway.identity.SSOUser;
import org.josso.gateway.session.SSOSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: InfoAction.java 598 2008-08-16 05:41:50Z gbrigand $
 */

public class InfoAction extends SignonBaseAction {

    private static final Log logger = LogFactory.getLog(InfoAction.class);

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        if (logger.isDebugEnabled())
            logger.debug("JOSSO Command : [cmd="+getSSOCmd(request)+"]");
        
        String jossoSessionId = getJossoSessionId(request);

        if (jossoSessionId != null) {
            try {
                SSOGateway g = getSSOGateway();
                SSOUser user = g.findUserInSession(jossoSessionId);
                SSOSession session = g.findSession(jossoSessionId);
                SSORole[] roles = g.findRolesByUsername(user.getName());

                request.setAttribute(KEY_JOSSO_USER, user);
                request.setAttribute(KEY_JOSSO_USER_ROLES, roles);
                request.setAttribute(KEY_JOSSO_SESSION, session);

                if (logger.isDebugEnabled())
                    logger.debug("[execute()] stored user : " + user + " under key : " + KEY_JOSSO_USER);

                if (logger.isDebugEnabled())
                    logger.debug("[execute()] stored session : " + session + " under key : " + KEY_JOSSO_SESSION);

            } catch (SSOException e) {
                if (logger.isDebugEnabled())
                    logger.error(e.getMessage(), e);

            } catch (Exception e) {
                logger.error(e, e);
            }

        }

        return mapping.findForward("success");
    }

}
