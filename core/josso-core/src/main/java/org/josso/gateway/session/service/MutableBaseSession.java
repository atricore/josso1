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
package org.josso.gateway.session.service;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This session implementation can be modified after creation, session stores that need re-build session
 * state can use this class to restore original session attribute values.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: MutableBaseSession.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class MutableBaseSession extends BaseSessionImpl {

    private static final Log logger = LogFactory.getLog(MutableBaseSession.class);

    /**
     * Setter for the expirig property, normale set to false.
     */
    public void setExpiring(boolean expiring) {
        _expiring = expiring;
    }

    /**
     * Setter for the last access time.  This value is also modified by the setCreation time method.
     */
    public void setLastAccessedTime(long lastAccessedTime) {
        _lastAccessedTime = lastAccessedTime;
    }

    /**
     * Setter for the access count.
     */
    public void setAccessCount(long accessCount) {
        _accessCount = accessCount;
    }


}
