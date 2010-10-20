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

package org.josso.agent.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.util.FactoryConfigurationError;

/**
 * Abstract factory to build ComponentKeeper instances.  If you want to use your own factory , you have to :
 * <p/>
 * 1. Subclass this abstract factory and implement the newComponentKeeper() method.
 * 2. Set the system property "org.josso.ComponentKeeperFactory" to your factory implementation's FQCN.
 * <p/>
 * The default factory, if no system property is specified is MBeanComponentKeeperFactoryImpl
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: ComponentKeeperFactory.java 974 2009-01-14 00:39:45Z sgonzalez $
 */

public abstract class ComponentKeeperFactory {

    private static final Log logger = LogFactory.getLog(ComponentKeeperFactory.class);

    /**
     * The system property that specifies the fully qualified class name of the specific ComponentKeeperFactory.
     * <p/>
     * The constant value is org.josso.ComponentKeeperFactory
     */
    public static final String COMPONENT_KEEKPER_FACTORY = "org.josso.agent.config.ComponentKeeperFactory";

    /**
     * The default factory class : org.josso.MBeanComponentKeeperFactoryImpl
     */
    private static String factoryClass = "org.josso.agent.config.SpringComponentKeeperFactoryImpl";

    /**
     * The name of the resource holding JOSSO configuration
     */
    private String _resourceFileName;

    public static ComponentKeeperFactory getInstance() {

        if (System.getProperty(COMPONENT_KEEKPER_FACTORY) != null)
            factoryClass = System.getProperty(COMPONENT_KEEKPER_FACTORY);

        try {
            return (ComponentKeeperFactory) Class.forName(factoryClass).newInstance();

        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
            throw new FactoryConfigurationError(e);

        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            throw new FactoryConfigurationError(e);

        } catch (ClassNotFoundException e) {
            logger.warn("Class not found : " + factoryClass);
            throw new FactoryConfigurationError(e);
        }

    }

    public static void setFactory(String f) {
        factoryClass = f;
    }

    public static String getFactory() {
        return factoryClass;
    }

    public abstract ComponentKeeper newComponentKeeper();


    public String getResourceFileName() {
        return _resourceFileName;
    }

    public void setResourceFileName(String resourceFileName) {
        this._resourceFileName = resourceFileName;
    }

}
