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

/**
 * Base credential implementation.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseCredential.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class BaseCredential implements Credential {

    protected Object _credential;

    public BaseCredential() {
    }

    public BaseCredential(Object credential) {
        _credential = credential;
    }

    public void setValue(Object credential) {
        _credential = credential;
    }

    public Object getValue() {
        return _credential;
    }

    /**
     * Compare this Credential value against another BaseCedential value (getValue())
     *
     * @return true if getValue() equals another.getValue()
     */
    public boolean equals(Object another) {
        if (!(another instanceof BaseCredential))
            return false;

        Object anotherCredential = ((BaseCredential) another).getValue();

        boolean equals = false;
        if (_credential == null) {
            equals = anotherCredential == null;
        } else {
            equals = _credential.equals(anotherCredential);
        }

        return equals;
    }

    /**
     * Returns the hashcode of the credential value. getValue().hashCode().
     *
     * @return the hashcode of the credential value.
     */
    public int hashCode() {
        return (_credential == null ? 0 : _credential.hashCode());
    }

    public String toString() {
        return _credential.toString();
    }


}
