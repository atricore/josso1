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
package org.josso.agent;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.util.mbeans.JOSSOBaseMBean;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.RuntimeOperationsException;
import java.util.List;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAgentMBean.java 598 2008-08-16 05:41:50Z gbrigand $
 */

public class SSOAgentMBean extends JOSSOBaseMBean {

    /**
     * String used as event type when notifying new partner app configurations using JMX.
     */
    public static final String JOSSO_AGENT_EVENT_ADD_PARTNER_APP = "josso.agent.addPartnerApp";

    /**
     * String used as event type when notifying partner app configuration removal using JMX.
     */
    public static final String JOSSO_AGENT_EVENT_REMOVE_PARTNER_APP = "josso.agent.removePartnerApp";


    private static final Log logger = LogFactory.getLog(SSOAgentMBean.class);

    private int _seq = 0;

    public SSOAgentMBean() throws MBeanException, RuntimeOperationsException {
        super();
    }

    public void addPartnerApp(String id, String vhost, String context, String[] ignoredWebResources) {

        if (context == null) {
            logger.warn("Tryint to add 'null' context as partner app.");
            return;
        }

        if (ignoredWebResources == null) {
            ignoredWebResources = new String[0];
        }

        SSOAgent a = getSSOAgent();
        SSOAgentConfiguration cfg = a.getConfiguration();
        cfg.addSSOPartnerApp(id, vhost, context, ignoredWebResources, null);

        List papps = cfg.getSsoPartnerApps();

        for (int i = 0; i < papps.size(); i++) {
            SSOPartnerAppConfig papp = (SSOPartnerAppConfig) papps.get(i);
            if (papp.getContext().equals(context)) {

                // Send a JMX notification, use parent ObjectName instance (oname).
                Notification n = new SSOAgentMBeanNotification(JOSSO_AGENT_EVENT_ADD_PARTNER_APP, oname, _seq++);
                n.setUserData(papp);

                try {
                    this.sendNotification(n);
                    return;

                } catch (MBeanException e) {
                    logger.warn("Can't send JMX notificatin : \n" + e.getMessage(), e);
                }
            }
        }

    }

    public void addPartnerApp(String id, String vhost, String context) {
        this.addPartnerApp(id, vhost, context, new String[0]);
    }

    public void removePartnerApp(String context) {

        if (context == null) {
            logger.warn("Trying to remove 'null' context");
            return;
        }
        SSOAgent a = getSSOAgent();
        a.getConfiguration().removeSSOPartnerApp(context);
        try {
            // Send a JMX notification, use parent ObjectName instance (oname).
            Notification n = new SSOAgentMBeanNotification(JOSSO_AGENT_EVENT_REMOVE_PARTNER_APP, oname, _seq++);
            n.setUserData(context);
            this.sendNotification(n);

        } catch (MBeanException e) {
            logger.warn("Can't send JMX notificatin : \n" + e.getMessage(), e);
        }
    }

    public SSOPartnerAppConfig[] listPartnerApps() {
        SSOAgent a = getSSOAgent();
        return (SSOPartnerAppConfig[]) a.getConfiguration().getSsoPartnerApps().toArray(new SSOPartnerAppConfig[0]);
    }

    protected SSOAgent getSSOAgent() {
        return (SSOAgent) this.resource;
    }

    public class SSOAgentMBeanNotification extends Notification {

        public SSOAgentMBeanNotification(String type, Object source, long sequence) {
            super(type, source, sequence);
        }
    }

}
