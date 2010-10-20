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

import java.util.Properties;

/**
 * @org.apache.xbean.XBean element="platform"
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 18, 2008
 * Time: 1:23:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class TargetPlatformImpl implements TargetPlatform {

    private String id;

    private String platformName;

    private String family;

    private String version;

    private String deployDir;

    private String libDir;

    private String JOSSOSharedLibDir;

    private String endorsedLibDir;

    private String configDir;

    private String binDir;

    private String JOSSOLibDir;

    private String JOSSOWarName;

    private String JOSSOConfDir;

    private boolean isJOSSOWarExploded;

    public TargetPlatformImpl() {

    }

    public TargetPlatformImpl(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }


    public String getFamily() {
        return family;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getDescription() {
        return platformName + " " + version;
    }

    public String getVersion() {
        return version;
    }

    /**
     * @org.apache.xbean.Property alias="deploy"
     */
    public String getDeployDir() {
        return deployDir;
    }

    /**
     * @org.apache.xbean.Property alias="lib"
     */
    public String getLibDir() {
        return libDir;
    }

    /**
     * @org.apache.xbean.Property alias="endorsed"
     */
    public String getEndorsedLibDir() {
        return endorsedLibDir;
    }

    /**
     * @org.apache.xbean.Property alias="conf"
     */
    public String getConfigDir() {
        return configDir;
    }

    /**
     * @org.apache.xbean.Property alias="bin"
     */
    public String getBinDir() {
        return binDir;
    }

    /**
     * @org.apache.xbean.Property alias="josso-shared"
     */
    public String getJOSSOSharedLibDir() {
        return JOSSOSharedLibDir;
    }

    /**
     * @org.apache.xbean.Property alias="josso-lib"
     */
    public String getJOSSOLibDir() {
        return JOSSOLibDir;
    }

    /**
     * @org.apache.xbean.Property alias="josso-conf"
     */
    public String getJOSSOConfDir() {
        return JOSSOConfDir;
    }

    /**
     * @org.apache.xbean.Property alias="josso-war-name"
     */
    public String getJOSSOWarName() {
        return JOSSOWarName;
    }

    /**
     * @org.apache.xbean.Property alias="josso-war-exploded"
     */
    public boolean isJOSSOWarExploded() {
        return isJOSSOWarExploded;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDeployDir(String deployDir) {
        this.deployDir = deployDir;
    }

    public void setLibDir(String libDir) {
        this.libDir = libDir;
    }

    public void setJOSSOSharedLibDir(String JOSSOSharedLibDir) {
        this.JOSSOSharedLibDir = JOSSOSharedLibDir;
    }

    public void setEndorsedLibDir(String endorsedLibDir) {
        this.endorsedLibDir = endorsedLibDir;
    }

    public void setConfigDir(String confDir) {
        this.configDir= confDir;
    }

    public void setBinDir(String binDir) {
        this.binDir = binDir;
    }

    public void setJOSSOLibDir(String JOSSOLibDir) {
        this.JOSSOLibDir = JOSSOLibDir;
    }

    public void setJOSSOConfDir(String JOSSOConfDir) {
        this.JOSSOConfDir = JOSSOConfDir;
    }

    public void setJOSSOWarName(String JOSSOWarName) {
        this.JOSSOWarName = JOSSOWarName;
    }

    public void setJOSSOWarExploded(boolean JOSSOWarExploded) {
        isJOSSOWarExploded = JOSSOWarExploded;
    }

}
