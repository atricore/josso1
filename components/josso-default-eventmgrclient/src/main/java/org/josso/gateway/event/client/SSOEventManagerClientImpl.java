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
package org.josso.gateway.event.client;

import org.josso.Lookup;
import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.exceptions.SSOEventException;
import org.josso.gateway.event.security.SSOSecurityEventManager;

import java.util.Properties;

/**
 * SSOEvent Manager client, used by non-Gateway components (like agents) to fire SSO events
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOEventManagerClientImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SSOEventManagerClientImpl implements SSOEventManagerClient {

    public void fireSSOEvent(SSOEvent event) throws SSOEventException {
        try {
            Lookup.getInstance().lookupSecurityDomain().getEventManager().fireSSOEvent(event);
        } catch (Exception e) {
            throw new SSOEventException("Can't call SSOEventManager : " + e.getMessage(), e);
        }

    }

    public void fireSessionEvent(String username, String sessionId, String type, Properties data) throws SSOEventException {
        try {
            ((SSOSecurityEventManager) Lookup.getInstance().lookupSecurityDomain().getEventManager()).fireSessionEvent(username, sessionId, type, data);
        } catch (Exception e) {
            throw new SSOEventException("Can't call SSOEventManager : " + e.getMessage(), e);
        }

    }
}
