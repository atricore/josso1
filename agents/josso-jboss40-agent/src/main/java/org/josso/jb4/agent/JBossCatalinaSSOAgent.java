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

package org.josso.jb4.agent;

import org.apache.catalina.Context;
import org.josso.agent.SSOAgentRequest;
import org.josso.agent.SingleSignOnEntry;
import org.josso.tc55.agent.CatalinaSSOAgent;
import org.josso.tc55.agent.CatalinaSSOAgentRequest;

/**
 * @org.apache.xbean.XBean element="agent"
 *
 * JBoss Agent implementation.
 * On each processRequest() call it does two things :
 *
 * <p>
 * 1. Replaces the partner web application context's realm with our JBossCatalinaRealm.
 * <p>
 * 2. Associates the Active Subject information to the current thread so that partner web
 *    applications can have an authenticated http request.
 * <p>
 * The JBossCatalinaSSOAgent must be used only in JBoss by configuring the agent configuration
 * file in the following way :
 *
<pre>
&lt;agent&gt;
  &lt;class&gt;org.josso.agent.JBossCatalinaSSOAgent&lt;/class&gt;
     ...
&lt;/agent&gt;
</pre>
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: JBossCatalinaSSOAgent.java 1607 2010-05-11 13:39:08Z sgonzalez $
 */

public class JBossCatalinaSSOAgent extends CatalinaSSOAgent {

    protected SingleSignOnEntry execute(SSOAgentRequest request) {
        CatalinaSSOAgentRequest r = (CatalinaSSOAgentRequest) request;
        Context c = r.getContext();


        if (debug > 0)
            log("Executing authenticate for jboss");

        // In JBoss this will allow the JBoss Security Manager (JaasSecurityManager) to
        // associate the authenticated Subject to the current Thread.
        // This is needed so that when the Security Manager gets called by Catalina it
        // will have which is the Subject for performing authorization procedures like
        // isUserInRole().
        // Since the JBoss Security Manager has a cache with all the authenticated Principals,
        // it won't invoke the JAAS login module each time, avoiding a performance impact.
        authenticate(request);

        return super.execute(request);
    }
}
