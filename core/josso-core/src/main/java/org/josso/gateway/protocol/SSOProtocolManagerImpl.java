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

package org.josso.gateway.protocol;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.protocol.handler.ProtocolHandler;
import org.josso.auth.Credential;
import org.josso.auth.exceptions.SSOAuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * @org.apache.xbean.XBean element="default-protocol-manager"
 *
 * Created by IntelliJ IDEA.
 * User: ajadzinsky
 * Date: Apr 25, 2008
 * Time: 5:08:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SSOProtocolManagerImpl implements SSOProtocolManager {

    private static final Log logger = LogFactory.getLog(SSOProtocolManagerImpl.class);

    private List<ProtocolHandler> handlers = new ArrayList<ProtocolHandler>();


    public void initialize() {

    }


    public boolean dispatchRequest(HttpServletRequest request, HttpServletResponse response) {

        for (ProtocolHandler ph : handlers) {
            if (ph.acceptJob(request, response))
                return ph.doJob(request, response);
        }
        return true;
    }

    public boolean authenticate(Credential[] credentials) throws SSOAuthenticationException {
        boolean authenticationSucceded = false;

        for (ProtocolHandler ph : handlers) {
            authenticationSucceded = ph.authenticate(credentials);

            if(authenticationSucceded)
                break;
        }

        return authenticationSucceded;
    }

    /**
     * @org.apache.xbean.Property alias="handlers" nestedType="org.josso.gateway.protocol.handler.ProtocolHandler"
     */
    public List<ProtocolHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<ProtocolHandler> handlers) {
        this.handlers = handlers;
    }


    public void dispose() {
    }

}