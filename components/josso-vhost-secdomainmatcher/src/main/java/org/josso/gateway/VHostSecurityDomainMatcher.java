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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @org.apache.xbean.XBean element="vhost-matcher"
 *
 * Matches a Security Domain based on the partner app host or vhost name.
 *
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 603 $ $Date: 2008-08-21 10:53:53 -0300 (Thu, 21 Aug 2008) $
 */
public class VHostSecurityDomainMatcher implements SecurityDomainMatcher {

    private static Log logger = LogFactory.getLog(VHostSecurityDomainMatcher.class);

    private List<String> vhosts = new ArrayList<String>();

    public void init() {
        if (vhosts.isEmpty())
            logger.warn("No Virtual hosts defined for this matcher.  Check JOSSO configuration!");
    }

    public boolean match(SSORequest req) {
        String vhost = req.getParameter(org.josso.gateway.signon.Constants.PARAM_JOSSO_PARTNERAPP_HOST);
        boolean match = vhost != null && vhosts.contains(vhost.toLowerCase());

        if (logger.isDebugEnabled())
            logger.debug("Request does " + (match ? "" : "not") + " match vhost : " + vhost);

        return match;

    }

    /**
     * List of comma sepparated host or vhost names.
     */
    public void setVhosts(String stVhosts) {

        StringTokenizer st = new StringTokenizer(stVhosts, ",");

        while (st.hasMoreTokens()) {
            String vhost = st.nextToken();
            this.vhosts.add(vhost.toLowerCase());

            if (logger.isDebugEnabled())
                logger.debug("Adding VHost :" + vhost);
        }

    }

}
