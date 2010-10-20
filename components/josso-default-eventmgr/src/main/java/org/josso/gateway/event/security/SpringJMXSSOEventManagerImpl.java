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
import org.josso.SSOConfigurationEventListener;
import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.SSOEventListener;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.ArrayList;
import java.util.List;

/**
 * JMX Based event manager. This manager is a SSOSecurityEventManager and a MBean.
 * It uses JMX Notification scheme to deliver SSO Events.
 *
 * @org.apache.xbean.XBean element="springjmx-event-manager"
 *
 * @author <a href="mailto:ggarcia@josso.org">Gustavo Garcia</a>
 */

public class SpringJMXSSOEventManagerImpl extends SSOSecurityEventManagerImpl implements NotificationPublisherAware, NotificationListener {

    public static final Log logger = LogFactory.getLog(SpringJMXSSOEventManagerImpl.class);

    private NotificationPublisher publisher;

    private static long sequnece;

    public SpringJMXSSOEventManagerImpl() {
        super();
    }

    protected Notification buildNotification(SSOEvent event) {
        return new SSOEventNotification(event, sequnece++);
    }

    public void fireSSOEvent(SSOEvent event) {
        try {

            this.publisher.sendNotification(buildNotification(event));
            if (logger.isDebugEnabled())
                logger.debug("Sent notification : " + event);

        } catch (Exception e) {
            logger.error("Can't send JMX Notification : " + e.getMessage(), e);
        }
    }

    public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
        this.publisher = notificationPublisher;
    }

    public void handleNotification(Notification notification, Object handback) {

        if (notification instanceof SSOEventNotification) {

            if (logger.isDebugEnabled())
                logger.debug("Received SSO Event Notification  : " + notification.getType());

            if (getListeners().size() < 1) {
                logger.warn("No listeners registered!");
            }

            try {

                String eventType = notification.getType();

                for (SSOEventListener l : getListeners()) {

                    if (l instanceof SSOConfigurationEventListener) {
                        SSOConfigurationEventListener listener = (SSOConfigurationEventListener) l;
                        if (listener.isEventEnabled(eventType, notification)) {

                            if (logger.isDebugEnabled())
                                logger.debug("Handling notification to configuration listener : " + l);

                            listener.handleEvent(eventType, notification);
                        }
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug("Handling notification to listener : " + l);

                        l.handleSSOEvent(((SSOEventNotification) notification).getEvent());
                    }
                }

            } catch (Exception e) {
                logger.error("Can't handle notification " + notification + ": \n" + e.getMessage(), e);
            }
        }
    }

}
