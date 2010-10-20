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

package org.josso.gateway.protocol.handler;

import org.josso.auth.Credential;
import org.josso.auth.exceptions.SSOAuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 25, 2008
 * Time: 1:54:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProtocolHandler {
    // TODO refactor with better names
    /**
     * @param request
     * @param response
     * @return
     */
    public boolean acceptJob(HttpServletRequest request, HttpServletResponse response);

    /**
     * @param request
     * @param response
     * @return
     */
    public boolean doJob(HttpServletRequest request, HttpServletResponse response);

    /**
     * @param credentials is <Credential>[]
     * @return true when the authentication is succesful, false otherwise
     * @throws SSOAuthenticationException
     */
    public boolean authenticate(Credential[] credentials) throws SSOAuthenticationException;
}
