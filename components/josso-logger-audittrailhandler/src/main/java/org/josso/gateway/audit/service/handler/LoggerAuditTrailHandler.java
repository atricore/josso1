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
package org.josso.gateway.audit.service.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.audit.SSOAuditTrail;

import java.util.Enumeration;
import java.util.Properties;

/**
 * This audit trail handler sends all received trails to configured logger.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: LoggerAuditTrailHandler.java 543 2008-03-18 21:34:58Z sgonzalez $
 *
 * @org.apache.xbean.XBean element="audittrail-logger"
 */

public class LoggerAuditTrailHandler extends BaseAuditTrailHandler {

    private Log trailsLogger = LogFactory.getLog(LoggerAuditTrailHandler.class);

    // The trailsLogger category :
    private String category;

    public int handle(SSOAuditTrail trail) {

        StringBuffer line = new StringBuffer();

        // Append TIME : CATEGORY - SEVERITY -
        line.append(trail.getTime()).append(" - ").append(trail.getCategory()).append(" - ").append(trail.getSeverity());

        // Append SUBJECT - ACTION=OUTCOME
        line.append(" - ").append(trail.getSubject() == null ? "" : trail.getSubject()).append(" - ").append(trail.getAction()).append("=").append(trail.getOutcome());


        // Append properties PROPERTIES:p1=v1,p2=v2
        Properties properties = trail.getProperties();
        Enumeration names = properties.propertyNames();

        if (names.hasMoreElements()) {
            line.append(" - ");
        }

        while (names.hasMoreElements()) {
            String key = (String) names.nextElement();
            String value = properties.getProperty(key);
            line.append(key).append("=").append(value);

            if (names.hasMoreElements())
                line.append(",");
        }

        // Log error information !?
        // Append error informatino if any : ERROR:<message><classname>
        if (trail.getError() != null) {
            line.append(" - ERROR:").append(trail.getError().getMessage()).append(":").append(trail.getError().getClass().getName());
            // Append error cause informatino if any : ERROR_CAUSE:<message><classname>
            if (trail.getError().getCause() != null) {
                line.append(" ERROR_CAUSE:").append(trail.getError().getCause().getMessage()).append(":").append(trail.getError().getClass().getName());
            }
        }

        // Logging the proper line :
        trailsLogger.info(line);

        return CONTINUE_PROCESS;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        trailsLogger = LogFactory.getLog(category);
    }
}
