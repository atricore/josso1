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
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAgentRequest.java 602 2008-08-20 23:58:11Z gbrigand $
 */

public interface SSOAgentRequest {

    public final int ACTION_ESTABLISH_SECURITY_CONTEXT = 1;
    public final int ACTION_RELAY = 2;
    public final int ACTION_ASSERT_SESSION = 3;
    public final int ACTION_CUSTOM_AUTHENTICATION = 4;

    /**
     * Returns the action for the request
     */
    int getAction();

    void setAction(int action);

    /**
     * The partner application ID associated with this request.
     * @return
     */
    String getRequester();

    void setRequester(String id);

    String getNodeId();

    void setNodeId(String nodeId);

    /**
     * The SSO Session identifire associated to this request.
     */
    String getSessionId();

    void setSessionId(String sessionId);

    /**
     * The local sesion.
     */
    LocalSession getLocalSession();

    void setLocalSession(LocalSession localSession);

    /**
     * Obtain the authentication assertion identifier.
     */
    String getAssertionId();

    void setAssertionId(String assertionId);

    SSOPartnerAppConfig getConfig(SSOAgent agent);

}
