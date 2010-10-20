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
import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.SSOEventListener;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
 * This class wrapps a SSOEventListener and adapts JMX event notifications to SSOEvents.
 * The expected Notification type is SSOEventNotification
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: NotificationSSOEventListener.java 603 2008-08-21 13:53:53Z sgonzalez $
 */
public class NotificationSSOEventListener implements NotificationListener {

    private static final Log logger = LogFactory.getLog(NotificationSSOEventListener.class);

    // Addapted listener
    private SSOEventListener listener;

    /**
     * @param listener the wrapped SSOEventListener instance.
     */
    public NotificationSSOEventListener(SSOEventListener listener) {
        this.listener = listener;
    }

    /**
     * Sends the SSOEvent found in this notification instance source property to the wrapped listener.
     *
     * @param notification a SSOEventNotification instance containing the SSOEvent.
     * @param object       the object that generated the notification.
     */
    public void handleNotification(Notification notification, Object object) {

        if (logger.isDebugEnabled())
            logger.debug("Received notification " + notification + " for listener : " + listener);

        if (notification instanceof SSOEventNotification) {

            SSOEventNotification ssoNotification = (SSOEventNotification) notification;
            SSOEvent ssoEvent = ssoNotification.getEvent();
            listener.handleSSOEvent(ssoEvent);
            return;
        }

        logger.warn("Unknown notification type :  " + notification.getClass().getName());
    }
}
