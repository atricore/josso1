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

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

public class MutableHttpServletRequestImpl extends HttpServletRequestWrapper implements MutableHttpServletRequest {

    private HashMap mutableHeaders = new HashMap();

    public MutableHttpServletRequestImpl(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }


    public int getIntHeader(java.lang.String name) {
        return Integer.parseInt(getHeader(name));
    }


    public long getDateHeader(java.lang.String name) {
        return new Date(getHeader(name)).getTime();
    }

    public java.lang.String getHeader(java.lang.String name) {
        if (mutableHeaders.get(name) != null) {
            Vector values = (Vector) mutableHeaders.get(name);
            return (String) values.get(0);
        }

        return super.getHeader(name);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public java.util.Enumeration getHeaders(java.lang.String name) {
        Vector targetHeaders = new Vector();

        if (mutableHeaders.get(name) != null) {
            Vector values = (Vector) mutableHeaders.get(name);
            targetHeaders.addAll(values);
        }

        Enumeration eh = super.getHeaders(name);

        while (eh.hasMoreElements()) {
            Object o = eh.nextElement();

            targetHeaders.add(o);
        }

        return targetHeaders.elements();


    }

    public java.util.Enumeration getHeaderNames() {
        Enumeration hn = super.getHeaderNames();
        Vector targetHeaderNames = new Vector();

        while (hn.hasMoreElements()) {
            Object o = hn.nextElement();

            targetHeaderNames.add(o);
        }

        Set mutableHdrKeys = mutableHeaders.keySet();

        for (Iterator iterator = mutableHdrKeys.iterator(); iterator.hasNext();) {
            Object headerName = iterator.next();

            targetHeaderNames.add(headerName);
        }

        return targetHeaderNames.elements();
    }

    public void addHeader(String name, String value) {
        if (mutableHeaders.get(name) == null) {
            mutableHeaders.put(name, new Vector());
        }

        Vector values = (Vector) mutableHeaders.get(name);
        values.add(value);
    }
}
