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

import org.josso.tooling.gshell.install.commands.support.InstallCommandSupport;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;

/**
 * The execution method is a template method used to deploy samples</br>
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 18, 2008
 * Time: 8:35:46 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DeploySamplesCommand   {

    /*
    // -----------------------------------------------
    protected TargetPlatform targetPlatform;

    protected FileObject targetDir;
    protected FileObject targetDeployDir;

    // -----------------------------------------------
    protected FileObject homeDir;
    protected FileObject samplesDir;

    protected DeploySamplesCommand() {
        this.setShell("samples");
    }

    protected void setup() throws Exception {

        registerVar("target", getTarget());
        
        targetPlatform = getTargetPlatform();

        targetDir = getFileSystemManager().resolveFile(getTarget());
        targetDeployDir = targetDir.resolveFile(targetPlatform.getDeployDir());

        homeDir = getFileSystemManager().resolveFile(getHomeDir());
        samplesDir = homeDir.resolveFile("dist/samples/apps");

    }

    protected void validate() throws Exception {
        if (!isTargetPlatformIdValid())
            throw new Exception("Invalid id ["+getTargetPlatformId()+"] specified!");
    }

    protected void deploySamples() throws Exception {

    }

    protected Object doExecute() throws Exception {

        try {

            validate();
            setup();

            io.out.println();
            io.out.println("@|bold Deploying " + targetPlatform.getPlatformName() + " " + targetPlatform.getVersion() + " JOSSO Samples v." + getJOSSOVersion() + "|");
            io.out.println();

            io.out.println("Validate Target " + targetPlatform.getDescription());
            validatePlatform();
            io.out.println();

            io.out.println("Deploy JOSSO Samples");
            deploySamples();
            io.out.println();

            // -----------------------------------------------------------------------
            // 6. Inform outcome
            io.out.println(targetPlatform.getDescription() + " JOSSO Samples v." + getJOSSOVersion());
            printOkStatus("Overall Installation", "Successful!");
            io.out.println();

            io.out.println("@|bold Congratulations!| You successfully installed the samples.");
            io.out.println();

        } catch (Exception e) {
            // 5. Inform outcome (error)
            io.out.println();
            io.out.println(targetPlatform.getDescription() + " JOSSO Samples v." + getJOSSOVersion());
            printErrStatus("Overall Installation", e.getMessage());
            io.out.println();
            io.out.println("See ../log/gshell.log for details");
            log.error(e.getMessage(), e);

        }

        return null;

    }


    protected boolean validatePlatform() throws Exception {

        boolean valid = true;

        if (!targetDir.exists() || !targetDir.getType().getName().equals(FileType.FOLDER.getName())) {
            printErrStatus("Target", "folder does not exists or is not a directory:" +targetDir.getName().getFriendlyURI());
            valid = false;
        }
        if (!targetDeployDir.exists() || !targetDeployDir.getType().getName().equals(FileType.FOLDER.getName())) {
            printErrStatus("Target deploy", "folder does not exist or is not a directory:" +targetDeployDir.getName().getFriendlyURI());
            valid = false;
        }

        return valid;

    }
    */
}
