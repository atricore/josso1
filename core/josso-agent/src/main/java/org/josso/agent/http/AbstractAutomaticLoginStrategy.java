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

package org.josso.agent.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:sgonzaelz@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public abstract class AbstractAutomaticLoginStrategy implements AutomaticLoginStrategy {

    private String mode;

    private HttpSSOAgent agent;

    public AbstractAutomaticLoginStrategy() {

    }

    public AbstractAutomaticLoginStrategy(String mode) {
        this.mode = mode;
    }

    /**
     * This tells the agent how to handle success or failure for this component.
     *
     * @return
     * @see org.josso.agent.Constants#JOSSO_AUTH_LOGIN_OPTIONAL
     * @see org.josso.agent.Constants#JOSSO_AUTH_LOGIN_SUFFICIENT
     * @see org.josso.agent.Constants#JOSSO_AUTH_LOGIN_REQUIRED
     */
    public String getMode() {
        return this.mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public HttpSSOAgent getAgent() {
        return agent;
    }

    public void setAgent(HttpSSOAgent agent) {
        this.agent = agent;
    }

    /**
     * Componenets must evaluate if automatic login is required for the received request.
     *
     * @return
     */
    public boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres) {
        return false;
    }
}
