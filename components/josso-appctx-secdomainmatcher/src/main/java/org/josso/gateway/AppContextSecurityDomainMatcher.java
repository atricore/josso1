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

package org.josso.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @org.apache.xbean.XBean element="appctx-matcher"
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 606 $ $Date: 2008-08-21 12:11:13 -0300 (Thu, 21 Aug 2008) $
 */
public class AppContextSecurityDomainMatcher implements SecurityDomainMatcher {

    private static Log logger = LogFactory.getLog(AppContextSecurityDomainMatcher.class);

    private List<String> appWebCtxs = new ArrayList<String>();

    public void init() {
        if (appWebCtxs.isEmpty())
            logger.warn("No web contexts defined for this matcher, check JOSSO configuration!");
    }

    public boolean match(SSORequest req) {
        String appWebCtx = req.getParameter(org.josso.gateway.signon.Constants.PARAM_JOSSO_PARTNERAPP_CONTEXT);
        boolean match = appWebCtx != null && appWebCtxs.contains(appWebCtx.toLowerCase());

        if (logger.isDebugEnabled())
            logger.debug("Request does " + (match ? "" : "not") + " match ctx : " + appWebCtx);

        return match;

    }

    /**
     * List of comma sepparated host or vhost names.
     */
    public void setAppWebContexts(String stAppWebCtxs) {

        StringTokenizer st = new StringTokenizer(stAppWebCtxs, ",");

        while (st.hasMoreTokens()) {
            String appWebCtx = st.nextToken();
            this.appWebCtxs.add(appWebCtx.toLowerCase());

            if (logger.isDebugEnabled())
                logger.debug("Adding AppWebCtx :" + appWebCtx);
        }

    }

}
