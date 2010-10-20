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

import java.security.Principal;

/**
 * A class representing entries in the cache of authenticated users.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: SingleSignOnEntry.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SingleSignOnEntry {

    public String authType = null;
    public String password = null;
    public Principal principal = null;
    public LocalSession sessions[] = new LocalSession[0];
    public String ssoId = null;
    public long lastAccessTime;

    public SingleSignOnEntry(String ssoId, Principal principal,
                             String authType) {
        super();
        this.principal = principal;
        this.ssoId = ssoId;
        this.authType = authType;
    }

    /**
     * Associates a Local Session (i.e.: Servlet Container Session) with
     * the authenticated Single Sign-On Session.
     *
     * @param localSession the local session to be associated with the single sign-on session.
     */
    public synchronized void addSession(LocalSession localSession) {
        for (int i = 0; i < sessions.length; i++) {
            if (localSession == sessions[i])
                return;
        }
        LocalSession results[] = new LocalSession[sessions.length + 1];
        System.arraycopy(sessions, 0, results, 0, sessions.length);
        results[sessions.length] = localSession;
        sessions = results;
    }

    /**
     * Provides the list of local sessions associated with the Single Sign-on Session.
     */
    public synchronized LocalSession[] findSessions() {
        return (this.sessions);
    }

    public synchronized void updatePrincipal(Principal p) {
         this.principal = p;
     }
    
}
