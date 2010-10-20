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
package org.josso.gateway.identity.service;

import org.josso.auth.SimplePrincipal;
import org.josso.gateway.SSONameValuePair;

import java.util.ArrayList;
import java.util.List;


/**
 * Default BaseUser implementation, it also extends the auth principal impl.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseUserImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class BaseUserImpl extends SimplePrincipal implements BaseUser {

    // Instance variables
    private String _name;
    private List _properties;

    public BaseUserImpl() {
        super(null);
        _properties = new ArrayList();
    }

    public BaseUserImpl(String username) {
        super(username);
        _name = username;
        _properties = new ArrayList();
    }

    /**
     * User login name, is a unique name in a domain.
     */
    public String getName() {
        return _name;
    }

    /**
     * @return always null
     * @deprecated this method always returns null
     */
    public String getSessionId() {
        return null;
    }

    public SSONameValuePair[] getProperties() {
        return (SSONameValuePair[]) _properties.toArray(new SSONameValuePair[_properties.size()]);
    }

    // ---------------------------------------------
    // Package utils
    // ---------------------------------------------

    public void setName(String name) {
        _name = name;
    }

    /**
     * Replaces all user properties with the received ones.
     */
    public void setProperties(SSONameValuePair[] pairs) {
        _properties.clear();
        for (int i = 0; i < pairs.length; i++) {
            SSONameValuePair pair = pairs[i];
            _properties.add(pair);
        }
    }

    public void addProperty(String name, String value) {
        addProperty(new SSONameValuePair(name, value));
    }

    public void addProperty(SSONameValuePair property) {
        _properties.add(property);
    }

    /**
     * Compare this BaseUser's name against another BaseUser
     *
     * @return true if name equals another.getName();
     */
    public boolean equals(Object another) {
        if (!(another instanceof BaseUser))
            return false;
        String anotherName = ((BaseUser) another).getName();
        boolean equals = false;
        if (_name == null)
            equals = anotherName == null;
        else
            equals = _name.equals(anotherName);
        return equals;
    }

    public int hashCode() {
        return (_name == null ? 0 : _name.hashCode());
    }

    public String toString() {
        return _name;
    }

}
