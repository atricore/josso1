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

package org.josso.agent.config;

import org.josso.agent.reverseproxy.ReverseProxyConfiguration;
import org.josso.agent.SSOAgent;
import org.josso.gateway.SSOException;
import org.springframework.context.ApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import java.util.Map;

/**
 * Date: Sep 5, 2007
 * Time: 10:00:00 AM
 *
 * @author <a href="mailto:ggarcia@josso.org">Gustavo Garcia</a>
 */
public class SpringComponentKeeperImpl implements org.josso.agent.config.ComponentKeeper {

    // Spring / XBean application context
    private ApplicationContext context;

    /**
     * Creates a new Spring Component Keeper.
     *
     * @param resource The xml file holding JOSSO Spring/xbean configuration.
     */
    public SpringComponentKeeperImpl(String resource) {
        // Use XBean XML file system application context.
        context = new ClassPathXmlApplicationContext(resource);
    }

    public SSOAgent fetchSSOAgent() throws Exception {
        Map agents = context.getBeansOfType(SSOAgent.class);
        if (agents.values().size() < 1) {
            throw new SSOException("No agent defined. Verify JOSSO Configuration");
        } else if (agents.values().size() > 1)
            throw new SSOException("Multiple agent definitions are not supported! Found : " + agents.values().size());

        // We are sure that there is one and only one SSOGateway instance.
        return (SSOAgent) agents.values().iterator().next();
    }

   public ReverseProxyConfiguration fetchReverseProxyConfiguration() throws Exception {
        return (ReverseProxyConfiguration) context.getBean("reverseProxyConfiguration");
    }


    public ApplicationContext getSpringContext() {
        return context;
    }


}

