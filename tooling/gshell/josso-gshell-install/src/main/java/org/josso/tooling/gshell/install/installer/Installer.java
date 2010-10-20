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

package org.josso.tooling.gshell.install.installer;

import org.apache.commons.vfs.FileObject;
import org.josso.tooling.gshell.install.JOSSOArtifact;

/**
 *
 * TODO : Provide an artifact model ?
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: Installer.java 1607 2010-05-11 13:39:08Z sgonzalez $
 */
public interface Installer {

    /**
     * Use this method to create a new Installer instance.
     * @return
     */
    Installer createInstaller() throws InstallException;

    /**
     * Setups and validates the target platform.
     *
     * @throws InstallException
     */
    void init() throws InstallException;


    /**
     * Validates that the target platform is ok.
     * @throws InstallException
     */
    void validatePlatform() throws InstallException;

    /**
     * Installs an artifact in the target platform
     *
     * @param artifact
     * @throws InstallException
     */
    void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException;

    /**
     * Installs an artifact in the target platform
     *
     * @param artifact
     * @throws InstallException
     */
    void installConfiguration(JOSSOArtifact artifact, String finalName, boolean replace) throws InstallException;


    /**
     * Installs an artifact in the target platform
     *
     * @param artifact
     * @throws InstallException
     */
    void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException;

    /**
     * Installs an artifact in the target platform
     *
     * @param artifact
     * @throws InstallException
     */
    void installComponentFromSrc(JOSSOArtifact artifact, boolean replace) throws InstallException;


    /**
     * Installs an artifact in the target platform
     *
     * @param artifact
     * @throws InstallException
     */
    void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException;

    /**
     * Installs an artifact in the target platform
     *
     * @param artifact
     * @throws InstallException
     */
    void installApplication(JOSSOArtifact artifact, boolean replace) throws InstallException;

    /**
     * Configure the Container, this could be done using simpler operations.
     */
    void configureAgent() throws InstallException;


    /**
     * todo : commands could load container files and install them later as artifacts, the container wolud be like a 'repository'
     * @return
     */
    byte[] loadArtifact();

    /**
     * Returns the target platform name
     * @return
     */
    String getPlatformId();

    /**
     * Returns the target platform name
     * @return
     */
    String getPlatformName();

    /**
     * Returns the target platform version
     * @return
     */
    String getPlatformVersion();

    /**
     * Returns the target platform description
     * @return
     */
    String getPlatformDescription();

    /**
     * This is to avoid using a contex.
     * @param name
     * @param value
     */
    void setProperty(String name, String value);


    String getProperty(String name);
    
    /**
     * Backups gateway configuration files.
     * 
     * @param remove true if original configuration files should be removed, false otherwise
     * @return true if backup was successful, false otherwise
     */
    boolean backupGatewayConfigurations(boolean remove);

    /**
     * Backups agent configuration files.
     * 
     * @param remove true if original configuration files should be removed, false otherwise
     * @return true if backup was successful, false otherwise
     */
    boolean backupAgentConfigurations(boolean remove);
    
    /**
     * Update josso-agent-config.xml with the given identity provider 
     * host name, port and type.
     * 
     * @param idpHostName idp host name, default "localhost"
     * @param idpPort idp port, default "8080"
     * @param idpType idp type (values: josso, atricore-idbus), default "josso"
     * @return true if agent configuration updated, false otherwise
     */
    boolean updateAgentConfiguration(String idpHostName, String idpPort, String idpType);
    
    /**
     * Removes old JOSSO 1.8.0 components.
     * 
     * @param backup true if old components should be backed up, false otherwise
     * @return true if removing was successful, false otherwise
     */
    boolean removeOldComponents(boolean backup);
    
    /**
     * Perform additional tasks.
     * 
     * @param libsDir directory containing agent binaries
     * @throws InstallException
     */
    void performAdditionalTasks(FileObject libsDir) throws InstallException;
}
