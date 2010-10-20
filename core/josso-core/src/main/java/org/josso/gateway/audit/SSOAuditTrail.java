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
package org.josso.gateway.audit;

import java.util.Date;
import java.util.Properties;

/**
 * Represents an audit trail, it provides information about security relevant events.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAuditTrail.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public interface SSOAuditTrail {

    /**
     * This trail category
     */
    String getCategory();

    /**
     * This trail severity
     */
    String getSeverity();

    /**
     * The action performed by the subject.
     */
    String getAction();

    /**
     * The action outcome.
     */
    String getOutcome();


    /**
     * The subject name that performed the action.
     */
    String getSubject();

    /**
     * The time when the action was performed.
     */
    Date getTime();

    /**
     * Action relevant properties.
     */
    Properties getProperties();

    /**
     * The error, if any, associated with this action.
     */
    Throwable getError();
}
