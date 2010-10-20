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

import org.josso.agent.reverseproxy.ProxyContextConfig;
import org.josso.agent.reverseproxy.ReverseProxyConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: Sep 5, 2007
 * Time: 10:00:00 AM
 *
 * @author <a href="mailto:ggarcia@josso.org">Gustavo Garcia</a>
 */
public class SpringReverseProxyConfigurationImpl implements ReverseProxyConfiguration {
    private List _pcl;

    public SpringReverseProxyConfigurationImpl() {
        _pcl = new ArrayList();
    }

    public void addProxyContext(String name, String context, String forwardHost, String forwardUri) {
        _pcl.add(new ProxyContextConfig(name, context, forwardHost, forwardUri));
    }

    public ProxyContextConfig[] getProxyContexts() {
        return (ProxyContextConfig[]) _pcl.toArray(new ProxyContextConfig[_pcl.size()]);
    }

    public void setProxyContexts(List proxyContexts) {
        _pcl = proxyContexts;
    }

}
