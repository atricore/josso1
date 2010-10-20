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
package org.josso.gateway.event.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.event.SSOEventListener;
import org.josso.util.mbeans.JOSSOBaseMBean;

import javax.management.*;

/**
 * JMX MBean that sends SSO Events as JMX Notificacions
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOEventManagerMBean.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

/**
 * This class is
 */
public class SSOEventManagerMBean extends JOSSOBaseMBean {

    private static final Log logger = LogFactory.getLog(SSOEventManagerMBean.class);


    public SSOEventManagerMBean() throws MBeanException, RuntimeOperationsException {
        super();
    }

    public void addNotificationListener(SSOEventListener listener) throws InstanceNotFoundException {

        MBeanServer server = this.registry.getMBeanServer();

        // Wrapp received listener with JMX listener.
        // This hardly works ... because in different versions of common-modeler, oname is a String or a ObjectName instance !!!
        server.addNotificationListener(oname, new NotificationSSOEventListener(listener), null, null);
    }

    public String getOName() {
        // This hardly works ... because in different versions of common-modeler, oname is a String or a ObjectName instance !!!
        return oname + "";
    }

    /**
     * This method will be invoked by the outer class when sending SSO Events.
     *
     * @param event
     */
    public void fireJMXSSOEvent(Notification event) {
        try {

            sendNotification(event);

            if (logger.isDebugEnabled())
                logger.debug("Sent notification : " + event);
        } catch (MBeanException e) {
            logger.error("Can't send JMX Notification : " + e.getMessage(), e);
        }
    }

}


