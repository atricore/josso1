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
package org.josso.auth.scheme;

import org.josso.gateway.protocol.handler.NtlmProtocolHandler;

/**
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 15, 2008
 * Time: 10:54:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class NtlmPasswordAuthenticationCredential extends NtlmCredential {
    public NtlmPasswordAuthenticationCredential() {
        super();
    }

    public NtlmPasswordAuthenticationCredential(Object credential) {
        super(credential);
    }

    public String toString() {
        return NtlmProtocolHandler.getPasswordAuthentication(this);
    }
}