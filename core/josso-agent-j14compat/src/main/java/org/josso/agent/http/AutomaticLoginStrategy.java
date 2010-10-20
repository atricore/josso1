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
 * Represents a component that an agent can use to determine if automatic login must be executed.
 *
 * @author <a href="mailto:sgonzaelz@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public interface AutomaticLoginStrategy {

    /**
     * This tells the agent how to handle success or failure for this component.
     *
     * @see org.josso.agent.Constants#JOSSO_AUTH_LOGIN_OPTIONAL
     * @see org.josso.agent.Constants#JOSSO_AUTH_LOGIN_SUFFICIENT
     * @see org.josso.agent.Constants#JOSSO_AUTH_LOGIN_REQUIRED
     *
     * @return
     */
    String getMode();

    /**
     * Componenets must evaluate if automatic login is required for the received request.
     *
     * @return
     */
    boolean isAutomaticLoginRequired(HttpServletRequest hreq, HttpServletResponse hres);

}
