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

package org.josso;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.util.config.ConfigurationContext;

import java.io.File;

/**
 * ConfigurationContext implementation.
 */
public class ConfigurationContextImpl implements ConfigurationContext {

    private File file;
    private boolean updatable;
    private boolean backupConfiguration;
    private static final Log logger = LogFactory.getLog(ConfigurationContextImpl.class);

    public ConfigurationContextImpl(File f, boolean updatable, boolean backupConfiguration) {
        this(f, updatable);
        this.backupConfiguration = backupConfiguration;
    }

    public ConfigurationContextImpl(File f, boolean updatable) {
        this(f);
        this.updatable = this.updatable && updatable;
    }

    public ConfigurationContextImpl(File f) {
        file = f;
        updatable = file != null && file.exists() && file.canRead() && file.canWrite() && !file.isDirectory();

        if (file != null && !file.exists())
            logger.warn("Configuration file does not exists : " + f.getAbsolutePath());
    }

    public boolean isConfigurationUpdatable() {
        return updatable;
    }

    public File getConfigurationFile() {
        return file;
    }

    public boolean isBackupEnabled() {
        return backupConfiguration;
    }
}
