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
import org.apache.commons.modeler.Registry;
import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.SSOEventListener;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;

/**
 * JMX Based event manager. This manager is a SSOSecurityEventManager and a MBean.
 * It uses JMX Notification scheme to deliver SSO Events.
 *
 * @org.apache.xbean.XBean element="jmx-event-manager"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: JMXSSOEventManagerImpl.java 568 2008-07-31 18:39:20Z sgonzalez $
 */
public class JMXSSOEventManagerImpl extends SSOSecurityEventManagerImpl {

    public static final Log logger = LogFactory.getLog(JMXSSOEventManagerImpl.class);


    // Event sequence number ...
    private static long sequnece;

    // This is the MBean used to fire notifications and to attach listeners..
    private ObjectName mbeanOname;
    private String oname;
    private boolean initialized = false;

    private List preRegistered;

    /**
     * Common Modelere MBean registry
     */
    private Registry registry;

    public JMXSSOEventManagerImpl() {
        super();
        this.registry = Registry.getRegistry(null, null);
        this.preRegistered = new ArrayList();
    }

    public void initialize() {
        super.initialize();

        if (initialized)
            return;

        try {

            mbeanOname = new ObjectName(oname);
            // this.registry.registerComponent(this, oname, null);

            if (logger.isDebugEnabled())
                logger.debug("Using MBean for notifications : " + mbeanOname);

            // Mark this manager as initialized.
            initialized = true;

            for (int i = 0; i < preRegistered.size(); i++) {
                SSOEventListener listener = (SSOEventListener) preRegistered.get(i);
                this.registerListener(listener);
            }
        } catch (Exception e) {
            logger.error("Can't create MBean Objectname : " + e.getMessage(), e);
        }
    }

    public void destroy() {
        super.destroy();
    }

    /**
     * This method invokes the fireSSOEvent of the MBean registered under the "oname" name.
     *
     * @param event
     */
    public void fireSSOEvent(SSOEvent event) {
        try {
            // Invoke fireSSOEvent on MBean ...
            MBeanServer server = getMBeanServer();

            // Build a JMX Notification based on the SSOEvent.
            Notification notification = buildNotification(event);

            server.invoke(mbeanOname,
                    "fireJMXSSOEvent",
                    new Object[]{notification},
                    new String[]{"javax.management.Notification"});

        } catch (Exception e) {
            logger.error("Can't send SSO Event : " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new event listener.
     * This implementation creates a NotificationSSOEventListener instance to wrapp the recieved listener.
     * It registers the NotificationSSOEventListener as a JMX listener of the configured MBean.
     */
    public void registerListener(SSOEventListener listener) {

        // If this component has not been initialized, store the listener so we can register it on initialization.
        if (!initialized) {
            preRegistered.add(listener);
            return;
        }

        try {
            logger.info("Adding listener : " + listener + " for : " + mbeanOname);
            getMBeanServer().addNotificationListener(mbeanOname, new NotificationSSOEventListener(listener), null, null);

        } catch (Exception e) {
            logger.error("Can't add listener : " + listener + " to mbean : " + mbeanOname + "\n" + e.getMessage(), e);
        }
    }

    /**
     * Configuration parameter containing the MBean object name that this manager uses to send JMX notifications.
     */
    public String getOname() {
        return oname;
    }

    /**
     * Configuration parameter containing the MBean object name that this manager uses to send JMX notifications.
     */
    public void setOname(String oname) {
        this.oname = oname;
    }

    /**
     * Finds the proper MBeanServer instance.
     */
    protected MBeanServer getMBeanServer() {
        return registry.getMBeanServer();
    }

    /**
     * This implementation builds a NotifiactionSSOEvent using received event information.
     */
    protected Notification buildNotification(SSOEvent event) {
        return new SSOEventNotification(event, sequnece++);
    }


}
