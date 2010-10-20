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
package org.josso.agent;

/**
 * A Session specific to a SSO partner application domain.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: LocalSession.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface LocalSession {

    final String LOCAL_SESSION_CREATED_EVENT = "createLocalSession";

    final String LOCAL_SESSION_DESTROYED_EVENT = "destroyLocalSession";

    long getCreationTime();

    String getId();

    long getLastAccessedTime();

    void setMaxInactiveInterval(int i);

    int getMaxInactiveInterval();

    Object getWrapped();

    void expire();

    void addSessionListener(LocalSessionListener sessionListener);

    void removeSessionListener(LocalSessionListener sessionListener);

    void exipre();
}
