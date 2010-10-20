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
package org.josso.gateway.event;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Default SSO Event manager implementation
 *
 * @org.apache.xbean.XBean element="event-manager"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOEventManagerImpl.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SSOEventManagerImpl implements SSOEventManager {

    private static Log log = LogFactory.getLog(SSOEventManagerImpl.class);

    private List<SSOEventListener> listeners;

    public SSOEventManagerImpl() {
        listeners = new ArrayList();
    }

    public void initialize() {
        
    }

    public void destroy() {

    }

    public void fireSSOEvent(SSOEvent event) {
        for (int i = 0; i < listeners.size(); i++) {
            SSOEventListener listener = (SSOEventListener) listeners.get(i);

            if (log.isDebugEnabled())
                log.debug("Handling SSO event to " + listener.getName() + " ["+listener.getClass().getName()+"]");
            
            listener.handleSSOEvent(event);
        }
    }

    public void registerListener(SSOEventListener listener) {
        listeners.add(listener);
    }

    /**
     * @org.apache.xbeean.Property alias="listeners" nestedType="org.josso.gateway.event.SSOEventListener"
     * @return
     */
    public List<SSOEventListener> getListeners() {
        return listeners;
    }


    public void removeAllListeners() {
        listeners.clear();
    }

    public void setListeners(List<SSOEventListener> listeners) {

        for (SSOEventListener listener : listeners) {
            registerListener(listener);
        }
    }

}
