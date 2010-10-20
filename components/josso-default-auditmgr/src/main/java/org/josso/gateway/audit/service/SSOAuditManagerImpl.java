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
package org.josso.gateway.audit.service;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.audit.SSOAuditManager;
import org.josso.gateway.audit.SSOAuditTrail;
import org.josso.gateway.audit.exceptions.SSOAuditException;
import org.josso.gateway.audit.service.handler.SSOAuditTrailHandler;
import org.josso.gateway.event.BaseSSOEvent;
import org.josso.gateway.event.SSOEvent;
import org.josso.gateway.event.SSOEventListener;
import org.josso.gateway.event.security.SSOIdentityEvent;
import org.josso.gateway.event.security.SSOSessionEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * This implementation logs all events using the standard logger.
 * The logger category is this implementation FQCN.
 * <p/>
 * Enalbe/Disable audit audit using your logger configuration.
 * Messages are logged with INFO priority.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAuditManagerImpl.java 568 2008-07-31 18:39:20Z sgonzalez $
 *
 * @org.apache.xbean.XBean element="audit-manager"
 */

public class SSOAuditManagerImpl implements SSOAuditManager, SSOEventListener {

    private static final Log logger = LogFactory.getLog(SSOAuditManagerImpl.class);

    public static final String OUTCOME_SUCCESS = "success";

    public static final String OUTCOME_FAILURE = "failure";

    private String _name;

    // List of SSOAuditTrailHandlers ...
    protected List handlers;

    public SSOAuditManagerImpl() {
        handlers = new ArrayList();
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void initialize() {
    }

    public void destroy() {

    }

    /**
     * @org.apache.xbean.Property alias="handlers" nestedType="org.josso.gateway.audit.service.handler.SSOAuditTrailHandler"
     * @return
     */
    public List<SSOAuditTrailHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<SSOAuditTrailHandler> handlers) {
        this.handlers = handlers;
    }

    public void addHandler(SSOAuditTrailHandler handler) {
        logger.info("Adding handler : " + handler.getClass().getName());
        handlers.add(handler);
    }

    /**
     * Receives SSO events to generate audit trails.
     */
    public void handleSSOEvent(SSOEvent event) {
        try {

            if (logger.isDebugEnabled())
                logger.debug("handling sso event : " + event);

            SSOAuditTrail auditTrail = buildAuditTrail(event);
            processAuditTrail(auditTrail);

        } catch (Exception e) {
            logger.error("Can't generate audit : " + e.getMessage(), e);
        }

    }

    /**
     * This implementation just logs the received trail using this audit manager's logger.
     * Subclasses may provide more complex functionallity.
     */
    public void processAuditTrail(SSOAuditTrail trail) throws SSOAuditException {

        for (int i = 0; i < handlers.size(); i++) {

            SSOAuditTrailHandler handler = (SSOAuditTrailHandler) handlers.get(i);

            if (handler.handle(trail) == SSOAuditTrailHandler.STOP_PROCESS) {
                if (logger.isDebugEnabled())
                    logger.debug("Process interrupted by : " + handler);

                break;
            }
        }

    }

    /**
     * This method builds a SSOAuditTrail based on a SSEvent instance.
     */
    protected SSOAuditTrail buildAuditTrail(SSOEvent event) {

        if (logger.isDebugEnabled())
            logger.debug("Creating Audit Trail form SSO event");

        String category = null;
        String severity = "info";
        String subject = null;
        String outcome = null;
        Throwable error = null;

        // General SSOEvent handling
        Date time = new Date();
        String action = event.getType();

        if (event instanceof BaseSSOEvent) {

            error = ((BaseSSOEvent) event).getError();
            outcome = error != null ? OUTCOME_FAILURE : OUTCOME_SUCCESS;
        }

        Properties props = new Properties();

        // Build detailed informaton based on a SSOIdentityEvent
        if (event instanceof SSOIdentityEvent) {

            category = "sso-user";

            SSOIdentityEvent ie = (SSOIdentityEvent) event;
            subject = ((SSOIdentityEvent) event).getUsername();

            // Add other properties :
            if (ie.getRemoteHost() != null)
                props.setProperty("remoteHost", ie.getRemoteHost());

            if (ie.getScheme() != null)
                props.setProperty("authScheme", ie.getScheme());

            if (ie.getSessionId() != null)
                props.setProperty("ssoSessionId", ie.getSessionId());

            // Build detailed informaton based on a SSOSessionEvent
        } else if (event instanceof SSOSessionEvent) {

            category = "sso-session";

            SSOSessionEvent se = (SSOSessionEvent) event;
            subject = se.getUsername();

            props.setProperty("ssoSessionId", se.getSessionId());

            if (se.getData() != null)
                props.setProperty("data", se.getData().toString());

        }


        // Return the new SSOAuditTrailInstance ...
        return new BaseSSOAuditTrail(category, severity, subject, action, outcome, time, props, error);

    }

}
