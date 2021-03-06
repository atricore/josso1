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

package org.josso.selfservices;

import org.josso.gateway.identity.SSOUser;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: PasswordAssertion.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public interface PasswordAssertion {

    /**
     * Gets the assertion identifier.
     */
    String getId();

    /**
     * Indicates if this is a valid assertion.
     */
    boolean isValid();

    /**
     * Gets this session creation time in milliseconds.
     */
    long getCreationTime();

    /**
     * Sends a session event.
     */
    void fireAssertionEvent(String type, Object data);

    /**
     * SSO Session ID associated with the assertion.
     * @return
     */
    SSOUser getSSOUser();

}
