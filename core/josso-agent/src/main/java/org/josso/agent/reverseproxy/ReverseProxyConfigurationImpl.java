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
package org.josso.agent.reverseproxy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: ReverseProxyConfigurationImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public final class ReverseProxyConfigurationImpl implements ReverseProxyConfiguration {
    private List _pcl = new ArrayList();

    public ReverseProxyConfigurationImpl() {
    }

    public void addProxyContext(String name, String context, String forwardHost, String forwardUri) {
        _pcl.add(
                new ProxyContextConfig(
                        name, context, forwardHost, forwardUri
                )
        );
    }

    public ProxyContextConfig[] getProxyContexts() {
        return (ProxyContextConfig[]) _pcl.toArray(new ProxyContextConfig[_pcl.size()]);
    }


}
