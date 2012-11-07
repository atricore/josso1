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

import javax.security.auth.callback.*;
import java.io.IOException;

/**
 * Callback handler that provides SSO Session TOKEN as credentials.
 */
public class SSOGatewayHandler implements CallbackHandler {

    private String requester;
    private String ssoSessionId ;
    private String nodeId;

    public SSOGatewayHandler(String requester, String ssoSessionId, String nodeId) {
        this.ssoSessionId = ssoSessionId;
        this.requester = requester;
        this.nodeId = nodeId;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {


        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof NameCallback) {

                NameCallback nc = (NameCallback) callbacks[i];

                if (nc.getPrompt().equals("appID"))
                    nc.setName(requester);
                else
                if (nc.getPrompt().equals("nodeID"))
                    nc.setName(nodeId);
                else
                    nc.setName(ssoSessionId);

            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                pc.setPassword(ssoSessionId.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }
}
