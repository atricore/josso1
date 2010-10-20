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

package org.josso.selfservices.password;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.josso.gateway.SSOGateway;
import org.josso.gateway.SSOException;
import org.josso.gateway.MutableSSOContext;
import org.josso.gateway.SSORequestImpl;
import org.josso.Lookup;
import org.josso.auth.exceptions.SSOAuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SelfServicesBaseAction.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class SelfServicesBaseAction extends Action {

    /**
     * Key to store a String representing the name of a SSO Security Domain
     */
    public static final String KEY_JOSSO_SECURITY_DOMAIN_NAME = "org.josso.gateway.securityDomainName";


    private static final Log logger = LogFactory.getLog(SelfServicesBaseAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // We need the seccurity domain !!!
        prepareContext(request);



        return mapping.findForward("");
    }

    protected void prepareContext(HttpServletRequest request) throws SSOException, SSOAuthenticationException {

        // Use gateway to select a security domain
        SSOGateway gwy = getSSOGateway();

        // The first thing to do is to create the context and publish the security domain !!!
        MutableSSOContext ctx = (MutableSSOContext) gwy.prepareSSOContext(new SSORequestImpl(request));
        ctx.setUserLocation(request.getRemoteHost());

        // Store current SD name in session
        request.getSession().setAttribute(org.josso.gateway.signon.Constants.KEY_JOSSO_SECURITY_DOMAIN_NAME, ctx.getSecurityDomain().getName());
        if (logger.isDebugEnabled())
            logger.debug("[prepareContext()] Storing security domain name in session [" + KEY_JOSSO_SECURITY_DOMAIN_NAME + "] : " +
                    ctx.getSecurityDomain().getName() + " (" + request.getSession().getId() + ")");
    }

    /**
     * Gets current sso gateway.
     */
    protected SSOGateway getSSOGateway() {

        try {
            return Lookup.getInstance().lookupSSOGateway();
        } catch (Exception e) {
            logger.error("Cannot get Gateway instance " + e.getMessage(), e);
            return null;
        }
    }


}
