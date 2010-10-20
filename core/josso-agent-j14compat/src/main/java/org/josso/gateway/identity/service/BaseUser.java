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


import org.josso.gateway.SSONameValuePair;
import org.josso.gateway.identity.SSOUser;

/**
 * Identity-internal user definition, provides methods to update a user state.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseUser.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public interface BaseUser extends SSOUser {

    /**
     * Set the user name.
     *
     * @param name
     */
    void setName(String name);

    /**
     * Replace all properties associated to this user.
     *
     * @param pairs
     */
    void setProperties(SSONameValuePair[] pairs);

    void addProperty(String name, String value);

    void addProperty(SSONameValuePair property);


}
