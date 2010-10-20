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

import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.josso.tooling.gshell.install.commands.support.InstallCommandSupport;
import org.josso.tooling.gshell.install.JOSSOScope;

/**
 * @org.apache.xbean.XBean element="install-web-samples"
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 23, 2008
 * Time: 6:12:55 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="josso-samples:install", description="Install JOSSO Samples")
public class InstallWebSamplesCommand extends InstallCommandSupport {

    // -----------------------------------------------------------------------
    protected FileObject homeDir;
    protected FileObject appDir;

    public InstallWebSamplesCommand() {
        this.setShell("samples");
    }

    protected void init() throws Exception {
        getInstaller().init();
    }

    protected void verifyTarget() throws Exception {
        if (!isForceInstall())
            getInstaller().validatePlatform();
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
        appDir = homeDir.resolveFile("dist/samples/apps");
    }


    protected void installConfig() throws Exception {

    }

    protected void deployWar() throws Exception {

        for (FileObject child : appDir.getChildren()) {
            getInstaller().installApplication(createArtifact(appDir.getURL().toString(), JOSSOScope.AGENT, child.getName().getBaseName()), true);
        }

    }

    protected Object doExecute() throws Exception {

        try {

            init();

            validate();

            setup();

            io.out.println();
            io.out.println("@|bold Deploying " + getInstaller().getPlatformName() + " " + getInstaller().getPlatformVersion() + " JOSSO Gateway v." + getJOSSOVersion() + "|");
            io.out.println();

            printer.printMsg("Verifying Target " + getInstaller().getPlatformDescription());
            verifyTarget();
            printer.printMsg();

            io.out.println("Install JOSSO Samples Configuration");
            installConfig();
            io.out.println();

            io.out.println("Deploy JOSSO Samples Applications");
            deployWar();
            io.out.println();

            // -----------------------------------------------------------------------
            // 6. Inform outcome
            io.out.println(getInstaller().getPlatformDescription() + " JOSSO Samples v." + getJOSSOVersion());
            printer.printOkStatus("Overall Installation", "Successful!");
            io.out.println();

            io.out.println("@|bold Congratulations!| You've successfully installed the samples.");
            io.out.println();

        } catch (Exception e) {
            // 5. Inform outcome (error)
            io.out.println();
            io.out.println(getInstaller().getPlatformDescription() + " JOSSO Samples v." + getJOSSOVersion());
            printer.printErrStatus("Overall Installation", e.getMessage());
            io.out.println();
            io.out.println("See ../log/gshell.log for details");
            log.error(e.getMessage(), e);

        }

        return null;

    }


}
