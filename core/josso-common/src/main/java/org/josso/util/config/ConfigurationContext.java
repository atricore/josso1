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

import java.io.File;

/**
 * Represents a ConfigurationHandler context. Provides information related to specific configurations like file.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: ConfigurationContext.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public interface ConfigurationContext {

    /**
     * System property that allows JOSSO to update configuration files when values are dinamically changed, probably using JMX.
     */
    public static final String SYS_PROP_CONFIGURATION_UPDATABLE = "josso.config.update";

    /**
     * System property that tells JOSSO to backup configuration files before modifying them.
     */
    public static final String SYS_PROP_CONFIGURATION_BACKUP = "josso.config.backup";

    /**
     * @return boolean if the associated configuration can be udpated.
     */
    boolean isConfigurationUpdatable();

    /**
     * The file descriptor representing the associated configuration.
     */
    File getConfigurationFile();

    /**
     * @return true if configuration backup is enable.
     */
    boolean isBackupEnabled();

}
