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

package org.josso.tooling.gshell.install.commands;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.josso.tooling.gshell.install.JOSSOScope;
import org.josso.tooling.gshell.install.commands.support.InstallCommandSupport;

/**
 * @org.apache.xbean.XBean element="install-java-agent"
 *
 * The execution method is a template method used to install java agents</br>
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 18, 2008
 * Time: 1:36:27 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="josso-agent:install", description="Install JOSSO Agent")
public class InstallJavaAgentCommand extends InstallCommandSupport {

    // -----------------------------------------------------------------------
    protected FileObject homeDir;
    protected FileObject libsDir;
    protected FileObject srcsDir;
    protected FileObject trdpartyDir;
    protected FileObject confDir;

    public InstallJavaAgentCommand() {
        this.setShell("agent");
    }

    protected void init() throws Exception {
        getInstaller().init();
    }

    protected void validate() throws Exception {
        if (!isTargetPlatformIdValid())
            throw new Exception("Invalid id ["+getTargetPlatformId()+"] specified!");
    }

    protected void setup() throws Exception {
        // -----------------------------------------------------------------------
        // TODO : We could use a remote repository to get our artifacts instead of the vfs or we could use vfs providers.
        FileSystemManager fs = VFS.getManager();
        homeDir = fs.resolveFile(getHomeDir());
        libsDir = homeDir.resolveFile("dist/agents/bin");
        srcsDir = homeDir.resolveFile("dist/agents/src");
        trdpartyDir = libsDir.resolveFile("3rdparty");
        confDir = homeDir.resolveFile("dist/agents/config/" + getTargetPlatformId());
    }

    protected void verifyTarget() throws Exception {
        if (!isForceInstall())
            getInstaller().validatePlatform();
    }

    private void processDir(FileObject dir, boolean recursive) throws Exception { //recursively traverse directories
        FileObject[] children = dir.getChildren();
        for (FileObject subfile : children) {
            if (subfile.getType() == FileType.FOLDER) {
                if (recursive)
                    processDir(subfile, recursive);
            } else {
                getInstaller().installComponent(createArtifact(subfile.getParent().getURL().toString(), JOSSOScope.AGENT, subfile.getName().getBaseName()), true);
            }
        }
    }

    protected void installJOSSOAgentJars() throws Exception {
        processDir(libsDir, false);
    }

    protected void installJOSSOAgentJarsFromSrc() throws Exception {

        if (!srcsDir.exists())
            return;

        FileObject[] agentBins = srcsDir.getChildren();
        for (int i = 0; i < agentBins.length; i++) {
            FileObject agentBin = agentBins[i];
            getInstaller().installComponentFromSrc(createArtifact(srcsDir.getURL().toString(), JOSSOScope.AGENT, agentBin.getName().getBaseName()), true);
        }
    }

    protected void installJOSSOAgentConfig() throws Exception {
        FileObject[] libs = confDir.getChildren();
        for (int i = 0 ; i < confDir.getChildren().length ; i ++) {
            FileObject trdPartyFile = libs[i];
            String fileName = trdPartyFile.getName().getBaseName();
            getInstaller().installConfiguration(createArtifact(confDir.getURL().toString(), JOSSOScope.AGENT, fileName), isReplaceConfig());
        }
        getInstaller().updateAgentConfiguration(getIdpHostName(), getIdpPort(), getIdpType());
    }

    public void install3rdParty() throws Exception {
        FileObject[] libs = trdpartyDir.getChildren();
        for (int i = 0 ; i < trdpartyDir.getChildren().length ; i ++) {
            FileObject trdPartyFile = libs[i];
            String fileName = trdPartyFile.getName().getBaseName();

            log.debug("Processing 3rd party artifact [" + fileName + "]");
            printer.printMsg("3rd party artifact [" + fileName + "]");

            getInstaller().install3rdPartyComponent(createArtifact(trdpartyDir.getURL().toString(), JOSSOScope.AGENT, fileName), isReplaceConfig());
        }
    }

    protected void configureContainer() throws Exception {
        // TODO : work on this, we could have primitives
        getInstaller().configureAgent();
    }

    protected void backupAndRemoveOldArtifacts() throws Exception {
        getInstaller().removeOldComponents(true);
        if (isReplaceConfig()) {
            getInstaller().backupAgentConfigurations(false);
        }
    }

    protected void performAdditionalTasks() throws Exception {
        getInstaller().performAdditionalTasks(libsDir);
    }

    /**
     * Template method
     */
    protected Object doExecute() throws Exception {

        try {

            init();

            validate();

            setup();

            printer.printMsg();
            printer.printMsg("@|bold Installing " + getInstaller().getPlatformName() + " " + getInstaller().getPlatformVersion() + " JOSSO Agent v." + getJOSSOVersion() + "|");
            printer.printMsg();

            printer.printMsg("Verifying Target " + getInstaller().getPlatformDescription());
            verifyTarget();
            printer.printMsg();

            printer.printMsg("Backing up and removing old JOSSO artifacts");
            backupAndRemoveOldArtifacts();
            printer.printMsg();
            // -----------------------------------------------------------------------

            // 1. 3rd party
            printer.printMsg("Installing JOSSO 3rd party JARs");
            install3rdParty();
            printer.printMsg();

            // 2. Install agent jars
            printer.printMsg("Installing JOSSO Agent JARs");
            installJOSSOAgentJars();
            printer.printMsg();

            // 3. Agent configuration files
            printer.printMsg("Installing JOSSO Agent JARs from Source");
            installJOSSOAgentJarsFromSrc();
            printer.printMsg();

            // 4. Container configuration files
            printer.printMsg("Configuring Container");
            configureContainer();
            printer.printMsg();

            // 5. Agent configuration files
            printer.printMsg("Installing JOSSO Agent Configuration files");
            installJOSSOAgentConfig();
            printer.printMsg();

            performAdditionalTasks();

            // 6. Inform outcome
            printer.printMsg(getInstaller().getPlatformDescription() + " JOSSO Agent v." + getJOSSOVersion());
            printer.printOkStatus("Overall Installation", "Successful.");
            printer.printMsg();

            printer.printMsg("@|bold Congratulations!| You've successfully installed the agent.");
            printer.printMsg("Now Follow the @|bold JOSSO Agent Configuration guide| for SSO-enabling applications.");
            printer.printMsg();

        } catch (Exception e) {
            // 5. Inform outcome (error)
            printer.printMsg();
            printer.printErrStatus("Overall Installation", e.getMessage());
            printer.printMsg();
            printer.printMsg("See ../log/gshell.log for details");
            log.error(e.getMessage(), e);
        }

        return null;

    }

}
