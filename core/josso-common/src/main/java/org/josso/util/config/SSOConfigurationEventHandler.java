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
package org.josso.util.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.SSOConfigurationEventListener;

import javax.management.AttributeChangeNotification;
import java.util.EventObject;

/**
 * This ConfigurationHandler receives SSOEvents and uses them to update JOSSO configuration files.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOConfigurationEventHandler.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SSOConfigurationEventHandler extends XUpdateConfigurationHandler implements SSOConfigurationEventListener {

    private static final Log logger = LogFactory.getLog(SSOConfigurationEventHandler.class);

    private Object source;

    private String[] ignoredAttrs;

    /**
     * @param ctx                     the ConfigurationContext used by this handler.
     * @param elementsBaseLocation    a XPath expression used to determine where elements to be updated are found in the configuration file.
     * @param newElementsBaseLocation a XPath expression used to determine where new elements will be inserted as siblings in the configuration file.
     * @param source                  the event source this handler uses
     */
    public SSOConfigurationEventHandler(ConfigurationContext ctx, String elementsBaseLocation, String newElementsBaseLocation, Object source, String[] ignoredAttrs) {
        super(ctx, elementsBaseLocation, newElementsBaseLocation);
        this.source = source;
        this.ignoredAttrs = ignoredAttrs;
    }


    /**
     * Handles an BaseSSOEvent and updates JOSSO config if necessary.
     * The event object must be instance of javax.management.AttributeChangeNotification
     *
     * @param eventType the event type (this may be a JMX event type)
     * @param event     the event object. Only AttributeChangeNotification events are supported by this handler.
     */
    public void handleEvent(String eventType, EventObject event) {

        // An attribute has change in the SSOAgent ... update config file.
        if (event instanceof AttributeChangeNotification) {
            AttributeChangeNotification notification = (AttributeChangeNotification) event;
            String attrName = notification.getAttributeName();

            if (ignore(attrName))
                return;

            // This should cover longs, ints and of course, strings.
            String newValue = notification.getNewValue().toString();
            String oldValue = (notification.getOldValue() != null ? notification.getOldValue().toString() : null);

            this.saveElement(attrName, oldValue, newValue);
        }

    }

    /**
     * Only handles events of type : "jmx.attribute.change" for the supported resource.
     */
    public boolean isEventEnabled(String eventType, EventObject event) {
        // This will only handle SSOAgent related events :
        return source.equals(event.getSource());
    }

    /**
     * Util to determine if an attribute must be ignored.
     * Subclasses may add different behavior.
     */
    protected boolean ignore(String attrName) {
        for (int i = 0; i < ignoredAttrs.length; i++) {
            String ignoredAttr = ignoredAttrs[i];
            if (ignoredAttr.equals(attrName))
                return true;
        }
        return false;
    }


}
