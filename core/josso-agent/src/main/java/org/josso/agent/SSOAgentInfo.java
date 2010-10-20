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

import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Agent Information bean
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOAgentInfo.java 543 2008-03-18 21:34:58Z sgonzalez $
 */
public class SSOAgentInfo {

    private static final Log logger = LogFactory.getLog(SSOAgentInfo.class);

    private Properties props;

    public SSOAgentInfo() throws MBeanException, RuntimeOperationsException {
        super();

        props = new Properties();
        InputStream in = getClass().getResourceAsStream("/org/josso/josso.properties");
        try {
            props.load(in);
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public String getVersion() {
        return props.getProperty("version");
    }

    public String getFullName() {
        return props.getProperty("fullname");
    }

    public String getName() {
        return props.getProperty("name");
    }

}
