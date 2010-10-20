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
import org.josso.util.config.ConfigurationContext;
import org.josso.util.config.SSOConfigurationEventHandler;

import java.util.EventObject;

/**
 * This ConfigurationHandler listens to SSOAgentMBean notifications to add or remove JOSSO partner application definitions
 * from JOSSO agent configuration file.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAgentConfigurationHandler.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class SSOAgentConfigurationHandler extends SSOConfigurationEventHandler {

    private static final Log logger = LogFactory.getLog(SSOAgentConfigurationHandler.class);


    /**
     * @param ctx                     the configuration context used by this handler.
     * @param elementsBaseLocation    a XPath expression used to determine where elements to be updated are found in the configuration file.
     * @param newElementsBaseLocation a XPath expression used to determine where new elements will be inserted as siblings in the configuration file.
     * @param source                  the event source this handler uses
     */
    public SSOAgentConfigurationHandler(ConfigurationContext ctx, String elementsBaseLocation, String newElementsBaseLocation, Object source, String[] ignoredAttrs) {
        super(ctx, elementsBaseLocation, newElementsBaseLocation, source, ignoredAttrs);
    }

    /**
     * @return true only if the notification is instance of SSOAgentMBean.SSOAgentMBeanNotification
     * @see SSOAgentMBean.SSOAgentMBeanNotification
     */
    public boolean isEventEnabled(String eventType, EventObject event) {
        return event instanceof SSOAgentMBean.SSOAgentMBeanNotification;
    }

    /**
     * This method expects events of type SSOAgentMBean.SSOAgentMBeanNotification
     * It adds or removes a JOSSO partner application definition from the JOSSO agent configuration file.
     */
    public void handleEvent(String eventType, EventObject event) {

        // Get data needed to process this event.
        SSOAgentMBean.SSOAgentMBeanNotification notification = (SSOAgentMBean.SSOAgentMBeanNotification) event;

        // Build a XUpdateUtil query string.
        if (eventType.equals(SSOAgentMBean.JOSSO_AGENT_EVENT_ADD_PARTNER_APP)) {
            SSOPartnerAppConfig cfg = (SSOPartnerAppConfig) notification.getUserData();
            addSSOPartnerAppConfig(cfg);

        } else if (eventType.equals(SSOAgentMBean.JOSSO_AGENT_EVENT_REMOVE_PARTNER_APP)) {
            String context = (String) notification.getUserData();
            removeSSOPartnerAppConfig(context);
        }

    }

    /**
     * This method will add a new partner app definition to josso configuration file.
     */
    protected void addSSOPartnerAppConfig(SSOPartnerAppConfig cfg) {
        String context = cfg.getContext();
        if (context == null || context.equals("")) {
            logger.error("addSSOPartnerAppConfig : received context is null or empty");
            return;
        }

        String xml = "            <context>" + cfg.getContext() + "</context>\n";

        if (cfg.getIgnoredWebRources() != null && cfg.getIgnoredWebRources().length > 0) {
            xml += "              <security-constraint>\n";
            for (int i = 0; i < cfg.getIgnoredWebRources().length; i++) {
                String s = cfg.getIgnoredWebRources()[i];
                xml += "                <ignore-web-resource-collection>" + s + "</ignore-web-resource-collection>\n";
            }
            xml += "              </security-constraint>";
        }

        String qry = this.buildXAppendElementXMLQueryString(getElementsBaseLocation(), "partner-app", xml);
        try {
            updateConfiguration(qry);
        } catch (Exception e) {
            logger.error("Can't add SSO partner application from to config (" + cfg.getContext() + ")");
        }
    }

    /**
     * This method will remove a partner app definition from josso configuration file.
     */
    protected void removeSSOPartnerAppConfig(String context) {
        if (context == null || context.equals("")) {
            logger.error("removeSSOPartnerAppConfig : received context is null or empty");
            return;
        }
        String qry = this.buildXDeleteElementQuery(getElementsBaseLocation(), "partner-app[context='" + context + "']");
        try {
            updateConfiguration(qry);
        } catch (Exception e) {
            logger.error("Can't remove SSO partner application from config (" + context + ")");
        }
    }
}
