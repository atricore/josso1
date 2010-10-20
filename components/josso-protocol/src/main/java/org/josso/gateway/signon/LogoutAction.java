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
import org.apache.struts.action.*;
import org.josso.Lookup;
import org.josso.gateway.SSOException;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.SSOWebConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: LogoutAction.java 613 2008-08-26 16:42:10Z sgonzalez $
 */

public class LogoutAction extends SignonBaseAction {

    private static final Log logger = LogFactory.getLog(LogoutAction.class);

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        //SSOSession session = null;
        //SSOUser user = null;

        if (logger.isDebugEnabled())
            logger.debug("JOSSO Command : [cmd="+getSSOCmd(request)+"]");

        try {

            // Logout user
            SSOGateway g = getSSOGateway();
            prepareContext(request);

            //user = g.findUserInSession(session.getProcessId());

            // Clear josso cookie
            // Cookie ssoCookie = newJossoCookie(request.getContextPath(), "-");
            // response.addCookie(ssoCookie);
            g.logout();

            removeJossoSessionId(request, response);

        } catch (SSOException e) {
            if (logger.isDebugEnabled())
                logger.debug(e.getMessage(), e);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ActionErrors errors = new ActionErrors();
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError("sso.login.failed"));
            saveErrors(request, errors);
        }

        // Redirect the user to the proper page, if any
        String back_to = request.getParameter(PARAM_JOSSO_BACK_TO);

        if (back_to == null) {
            // Try with the configured URL if any.
            SSOWebConfiguration c = Lookup.getInstance().lookupSSOWebConfiguration();
            back_to = c.getLogoutBackToURL();
        }

        if (back_to != null) {
            if (logger.isDebugEnabled())
                logger.debug("[logout()], ok->redirecting to : " + back_to);
            response.sendRedirect(response.encodeRedirectURL(back_to));
            return null; // No forward is needed.
        }

        if (logger.isDebugEnabled())
            logger.debug("[logout()], ok");

        return mapping.findForward("success");
    }

}
