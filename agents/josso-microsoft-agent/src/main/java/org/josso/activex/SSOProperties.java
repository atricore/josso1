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

package org.josso.activex;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.gateway.SSONameValuePair;

/**
 * Represents a set of user properties.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SSOProperties.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public class SSOProperties {

    private static final Log logger = LogFactory.getLog(SSOProperties.class);

    private SSONameValuePair _props[];

    public SSOProperties(SSONameValuePair[] props) {
        _props = props;
    }

    public int count() {
        return _props.length;
    }

    public String getValue(int idx) {
        return _props[idx].getValue();
    }

    public String getValue(String name) {
        for (int i = 0; i < _props.length; i++) {
            SSONameValuePair prop = _props[i];
            if (prop.getName().equals(name)) {
                return prop.getValue();
            }
        }
        throw new IllegalArgumentException("The name : " + name + " is not a valid property name");
    }

    public String getName(int idx) {
        return _props[idx].getName();
    }

}
