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

package org.josso.tc60.agent;


import org.apache.catalina.Context;
import org.josso.agent.LocalSession;
import org.josso.agent.SSOAgentRequestImpl;
import org.josso.agent.http.HttpSSOAgentRequest;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: CatalinaSSOAgentRequest.java 1607 2010-05-11 13:39:08Z sgonzalez $
 */

public class CatalinaSSOAgentRequest extends HttpSSOAgentRequest {

    private Context _context;


    public CatalinaSSOAgentRequest(String requester, int action, String sessionId, LocalSession session, String assertionId) {
        super(requester, action, sessionId, session, assertionId, null);
    }

    public CatalinaSSOAgentRequest(String requester, int action, String sessionId, LocalSession session) {
        this(requester, action, sessionId, session, null);
    }

    void setContext(Context c) {
        _context = c;
    }

    /**
     * The context associated with this request.
     */
    public Context getContext() {
        return _context;
    }

}
