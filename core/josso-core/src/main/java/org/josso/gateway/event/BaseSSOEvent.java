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

package org.josso.gateway.event;

import java.util.EventObject;

/**
 * This is a JMX SSO Event implementation.
 */
public class BaseSSOEvent extends EventObject implements SSOEvent {

    // Event type
    private String type;
    private Throwable error;

    /**
     * Constructs a prototypical Event.
     */
    public BaseSSOEvent(String type, Object source) {
        super(source);
        this.type = type;
    }

    public BaseSSOEvent(String type, Object source, Throwable error) {
        super(source);
        this.type = type;
        this.error = error;
    }


    public String getType() {
        return type;
    }


    public Throwable getError() {
        return error;
    }
}
