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
package org.josso.auth;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.security.Principal;

/**
 * Simple principal implementation.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SimplePrincipal.java 568 2008-07-31 18:39:20Z sgonzalez $
 */

public class SimplePrincipal implements Principal, Serializable {

    private static final Log logger = LogFactory.getLog(SimplePrincipal.class);
    private String _name;

    public SimplePrincipal() {

    }

    public SimplePrincipal(String username) {
        _name = username;
    }

    public String getName() {
        return _name;
    }

    /**
     * Compare this Principal name against another Principal name
     *
     * @return true if name equals another.getName()
     */
    public boolean equals(Object another) {
        if (!(another instanceof Principal))
            return false;

        String anotherName = ((Principal) another).getName();

        boolean equals = false;
        if (_name == null) {
            equals = anotherName == null;
        } else {
            equals = _name.equals(anotherName);
        }

        return equals;
    }

    /**
     * Returns the hashcode of the name
     */
    public int hashCode() {
        return (_name == null ? 0 : _name.hashCode());
    }

    public String toString() {
        return _name;
    }


}