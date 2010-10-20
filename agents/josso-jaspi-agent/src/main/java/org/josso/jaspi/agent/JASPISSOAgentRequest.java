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

package org.josso.jaspi.agent;

import org.josso.agent.LocalSession;
import org.josso.agent.http.HttpSSOAgentRequest;
import org.josso.agent.http.JOSSOSecurityContext;

/**
 * This SSO Agent Request wrapps original servlet request and response objects.
 *
 * It also provides a placeholder for the JOSSO Security context created by the JASPI SSO Agent during authentication.
 */
public class JASPISSOAgentRequest extends HttpSSOAgentRequest {

    /**
     * This will hold JOSSO security context created by the agent during authentication.
     */
    private JOSSOSecurityContext ctx;

    public JASPISSOAgentRequest(String requester, int action, String sessionId, LocalSession session, String assertionId) {
        super(requester, action, sessionId, session, assertionId);
    }

    public JASPISSOAgentRequest(String requester, int action, String sessionId, LocalSession session) {
        super(requester, action, sessionId, session);
    }

    public void setSecurityContext(JOSSOSecurityContext ctx) {
        this.ctx = ctx;
    }

    public JOSSOSecurityContext  getSecurityContext() {
        return this.ctx;
    }

}
