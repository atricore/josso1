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
package org.josso.util.mbeans;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.BaseModelMBean;

import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

/**
 * <p>A convenience base class for <strong>ModelMBean</strong> implementations
 * where the underlying base class (and therefore the set of supported
 * properties) is different for varying implementations of a standard
 * interface.  For Catalina, that includes at least the following:
 * Connector, Logger, Realm, and Valve.  This class creates an artificial
 * MBean attribute named <code>className</code>, which reports the fully
 * qualified class name of the managed object as its value.</p>
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: JOSSOBaseMBean.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class JOSSOBaseMBean extends BaseModelMBean {

    private static final Log logger = LogFactory.getLog(JOSSOBaseMBean.class);
    // ---------------------------------------------------------- Constructors

    /**
     * Construct a <code>ModelMBean</code> with default
     * <code>ModelMBeanInfo</code> information.
     *
     * @throws MBeanException             if the initialize of an object
     *                                    throws an exception
     * @throws RuntimeOperationsException if an IllegalArgumentException
     *                                    occurs
     */
    public JOSSOBaseMBean()
            throws MBeanException, RuntimeOperationsException {
        super();
    }

    /**
     * Return the fully qualified Java class name of the managed object
     * for this MBean.
     */
    public String getClassName() {
        return (this.resource.getClass().getName());
    }

}

