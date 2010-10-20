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
package org.josso.gateway;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Axis Client-side Handler that identifies the SOAP message for authentication
 * purposes.
 *
 * @author <a href="mailto:gbrigand@josso.org">Gianluca Brigandi</a>
 * @version CVS $Id: WebserviceClientAuthentication.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class WebserviceClientAuthentication extends BasicHandler {
    private static final Log logger = LogFactory.getLog(WebserviceClientAuthentication.class);

    private static String _username;
    private static String _password;

    public static void setUsername(String username) {
        if (_username == null)
            _username = username;
    }

    public static void setPassword(String password) {
        if (_password == null)
            _password = password;
    }

    public void invoke(MessageContext msgContext) throws AxisFault {

        try {

            if (_username != null && _password != null) {
                logger.debug("Injecting identity to SOAP request, username='" + _username + "' " +
                        "password='" + _password + "'"
                );

                msgContext.setUsername(_username);
                msgContext.setPassword(_password);
            }

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public void onFault(MessageContext msgContext) {
        try {
            // probably needs to fault.
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
