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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.JOSSOArtifact;

/**
 * @org.apache.xbean.XBean element="jee-installer"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 */
public class JEEInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(JEEInstaller.class);

    public JEEInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public JEEInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {
        boolean valid = true;

        try {

            if (!targetDir.exists() || !targetDir.getType().getName().equals(FileType.FOLDER.getName())) {
                getPrinter().printErrStatus("Target", "folder does not exist or is not a directory:" +targetDir.getName().getFriendlyURI());
                valid = false;
            }
            if (!valid)
                throw new InstallException("Invalid Target Platform");

            getPrinter().printOkStatus(getTargetPlatform().getDescription(), "Directory Layout");

        } catch (FileSystemException e) {
            log.error(e.getMessage(), e);
            throw new InstallException("Invalid Target Platform");
        }

    }

    @Override
    public void configureAgent() throws InstallException {
        throw new InstallException("Agent must be manually installed for this platform");
    }

    @Override
    public void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException {
        printInstallWarnStatus(artifact.getBaseName(), "Configuration must be done manually for this platform");
    }

    @Override
    public void installConfiguration(JOSSOArtifact artifact, String finalName, boolean replace) throws InstallException {
        printInstallWarnStatus(artifact.getBaseName(), "Configuration must be done manually for this platform");
    }

    @Override
    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        printInstallWarnStatus(artifact.getBaseName(), "3rd party artifacts must be manually installed for this platform");
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        printInstallWarnStatus(artifact.getBaseName(), "JOSSO Components must be manually installed for this platform");
    }



}
