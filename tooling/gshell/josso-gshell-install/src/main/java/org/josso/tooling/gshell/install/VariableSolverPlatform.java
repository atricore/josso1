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

package org.josso.tooling.gshell.install;

import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.installer.InstallException;
import org.josso.tooling.gshell.install.VariableSolver;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * // TODO : We could use a Dynamic Proxy !
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 18, 2008
 * Time: 9:28:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariableSolverPlatform implements TargetPlatform {

    private TargetPlatform platform;

    private VariableSolver solver;

    private Log log = LogFactory.getLog(VariableSolverPlatform.class);

    public VariableSolverPlatform (TargetPlatform platform, VariableSolver solver) {
        this.platform = platform;
        this.solver = solver;
    }

    public String getId() {
        try {
            return solver.resolveVariables(platform.getId());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getFamily() {
        try {
            return solver.resolveVariables(platform.getFamily());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getPlatformName() {
        try {
            return solver.resolveVariables(platform.getPlatformName());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getDescription() {
        try {
            return solver.resolveVariables(platform.getDescription());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getVersion() {
        try {
            return solver.resolveVariables(platform.getVersion());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getDeployDir() {
        try {
            return solver.resolveVariables(platform.getDeployDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getLibDir() {
        try {
            return solver.resolveVariables(platform.getLibDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getEndorsedLibDir() {
        try {
            return solver.resolveVariables(platform.getEndorsedLibDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getBinDir() {
        try {
            return solver.resolveVariables(platform.getBinDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getConfigDir() {
        try {
            return solver.resolveVariables(platform.getConfigDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getJOSSOSharedLibDir() {
        try {
            return solver.resolveVariables(platform.getJOSSOSharedLibDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getJOSSOLibDir() {
        try {
            return solver.resolveVariables(platform.getJOSSOLibDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getJOSSOConfDir() {
        try {
            return solver.resolveVariables(platform.getJOSSOConfDir());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public String getJOSSOWarName() {
        try {
            return solver.resolveVariables(platform.getJOSSOWarName());
        } catch (InstallException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean isJOSSOWarExploded() {
        return platform.isJOSSOWarExploded();
    }


}
