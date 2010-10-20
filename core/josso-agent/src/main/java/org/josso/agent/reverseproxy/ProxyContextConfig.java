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

import java.io.Serializable;

/**
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: ProxyContextConfig.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public final class ProxyContextConfig implements Serializable {
    private String _name;
    private String _context;
    private String _forwardHost;
    private String _forwardUri;

    public ProxyContextConfig(String name, String context, String forwardHost, String forwardUri) {
        _name = name;
        _context = context;
        _forwardHost = forwardHost;
        _forwardUri = forwardUri;
    }

    String getName() {
        return _name;
    }

    public String getContext() {
        return _context;
    }

    public String getForwardHost() {
        return _forwardHost;
    }

    public String getForwardUri() {
        return _forwardUri;
    }

    public String toString() {
        return "{name=" + _name + ",context=" + _context + ",forwardHost=" + _forwardHost +
                ",forwardUri=" + _forwardUri + "}";
    }

}
