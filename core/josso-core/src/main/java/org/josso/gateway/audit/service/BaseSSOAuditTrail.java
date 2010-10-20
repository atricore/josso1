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

import org.josso.gateway.audit.SSOAuditTrail;

import java.util.Date;
import java.util.Properties;

/**
 * Base Audit Trail implementation.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: BaseSSOAuditTrail.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class BaseSSOAuditTrail implements SSOAuditTrail {

    private String category;

    private String severity;

    private String action;

    private String outcome;

    private String subject;

    private Date time;

    private Throwable error;

    private Properties props;

    public BaseSSOAuditTrail(String category, String severity, String subject, String action, String outcome, Date time, Properties props) {
        this.category = category;
        this.severity = severity;
        this.subject = subject;
        this.action = action;
        this.outcome = outcome;
        this.time = time;
        this.props = props;
    }

    public BaseSSOAuditTrail(String category, String severity, String subject, String action, String outcome, Date time, Properties props, Throwable error) {
        this(category, severity, subject, action, outcome, time, props);
        this.error = error;
    }

    public String getCategory() {
        return category;
    }

    public String getSeverity() {
        return severity;
    }

    public String getSubject() {
        return subject;
    }

    public String getAction() {
        return action;
    }

    public String getOutcome() {
        return outcome;
    }

    public Date getTime() {
        return time;
    }

    public Properties getProperties() {
        return props;
    }

    public Throwable getError() {
        return error;
    }

}
