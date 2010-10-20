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

import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.exceptions.SSOEventException;

import java.util.Properties;

/**
 * Evente Manager.  This component sends SSO events.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOEventManagerClient.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public interface SSOEventManagerClient {

    void fireSSOEvent(SSOEvent event) throws SSOEventException;

    void fireSessionEvent(String username, String sessionId, String type, Properties data) throws SSOEventException;

}
