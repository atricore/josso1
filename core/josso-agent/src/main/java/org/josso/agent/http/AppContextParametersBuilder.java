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

package org.josso.agent.http;

import org.josso.agent.SSOPartnerAppConfig;
import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.signon.Constants;

import javax.servlet.http.HttpServletRequest;

/**
 @org.apache.xbean.XBean element="appctx-parameters-builder"
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 606 $ $Date: 2008-08-21 12:11:13 -0300 (Thu, 21 Aug 2008) $
 */
public class AppContextParametersBuilder extends AbstractFrontChannelParametersBuilder {
    public SSONameValuePair[] buildParamters(SSOPartnerAppConfig cfg, HttpServletRequest hreq) {

        String context = hreq.getContextPath();
        if (context.equals(""))
            context = "/";

        return new SSONameValuePair[]{new SSONameValuePair(Constants.PARAM_JOSSO_PARTNERAPP_CONTEXT, context)};
    }


}
