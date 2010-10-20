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
package org.josso.gateway.audit.service.handler;

import org.josso.gateway.audit.SSOAuditTrail;

/**
 * Audit Trail handler providing base functionallity
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseAuditTrailHandler.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class BaseAuditTrailHandler implements SSOAuditTrailHandler {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (getName() != null && !getName().equals(name))
            throw new IllegalStateException("Name has already been set : " + getName());

        this.name = name;
    }

    public int handle(SSOAuditTrail trail) {
        return CONTINUE_PROCESS;
    }
}
