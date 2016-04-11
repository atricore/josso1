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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.geronimo.gshell.common.io.PumpStreamHandler;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: WeblogicInstaller.java 1572 2009-12-09 20:03:27Z sgonzalez $
 * @org.apache.xbean.XBean element="weblogic-installer"
 */
public class WeblogicInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(WeblogicInstaller.class);

    private FileObject targetJOSSOMBeansDir;

    protected String wlVersionStr;

    public WeblogicInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public WeblogicInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        if (getTargetPlatform().getVersion().startsWith("9.2"))
            this.wlVersionStr =  "92";
        else if (getTargetPlatform().getVersion().startsWith("10"))
            this.wlVersionStr = "10";
        else if (getTargetPlatform().getVersion().startsWith("12"))
            this.wlVersionStr = "12";
        else
            throw new InstallException("Unsupported Weblogic version " + getTargetPlatform().getVersion());

        super.validatePlatform();


        try {
            boolean valid = true;

            if (!targetLibDir.exists() || !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                getPrinter().printErrStatus("Target conf", "folder does not exist or is not a directory:" + targetLibDir.getName().getFriendlyURI());
                valid = false;
            }
    
            FileObject weblogicJar = targetDir.resolveFile("server/lib/weblogic.jar");
            if (weblogicJar == null || !weblogicJar.exists() || !weblogicJar.getType().getName().equals(FileType.FILE.getName())) {
                valid = false;
                getPrinter().printErrStatus("WeblogicHome", "Cannot find weblogic");
            } else {
                getPrinter().printOkStatus("WeblogicHome");
            }

            // Validate domain

            String weblogicDomain = getProperty("weblogicDomain");
            FileObject weblogicDomainDir = targetDir.resolveFile(weblogicDomain);
            if (weblogicDomainDir == null || !weblogicDomainDir.exists() || !weblogicDomainDir.getType().getName().equals(FileType.FOLDER.getName())) {
                valid = false;
                getPrinter().printErrStatus("WeblogicDomain", "Cannot find domain " + weblogicDomainDir.getName().getFriendlyURI());
            } else {
                getPrinter().printOkStatus("WeblogicDomain", weblogicDomainDir.getName().getFriendlyURI());
            }

            // TODO : Validate Version ?

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("WeblogicHome", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void init() throws InstallException {


        try {
            log.debug("Init Weblogic installer");

            String weblogicDomain = getProperty("weblogicDomain");

            if (weblogicDomain == null)
                throw new InstallException("Weblogic Domain path not specified");

            log.debug("Weblogic Domain : " + weblogicDomain);

            registerVarResolution("domain", weblogicDomain);

            // Initialize installer, this will initialize standar folders.
            super.init();

            targetJOSSOMBeansDir = targetLibDir.resolveFile("mbeantypes");
        } catch (FileSystemException e) {
            throw new InstallException(e.getMessage(), e);
        }


    }


    /**
     * Installs JOSSO Configuration files in tomcat
     *
     * @param artifact
     * @throws InstallException
     */
    @Override
    public void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            installFile(srcFile, this.targetJOSSOConfDir, replace);
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            // Create targetLibDir if necessary

            if (!targetJOSSOMBeansDir.exists())
                targetJOSSOMBeansDir.createFolder();

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetJOSSOSharedLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                                   artifact.getClassifier() !=  null &&
                                   artifact.getClassifier().equals("axis")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);
            } else if (artifact.getBaseName().startsWith("josso-weblogic92-agent") &&
                    getTargetPlatform().getVersion().startsWith("9.2")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-weblogic10-agent") &&
                    getTargetPlatform().getVersion().startsWith("10.")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-weblogic12-agent") &&
                    getTargetPlatform().getVersion().startsWith("12.")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-servlet-agent")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else {
                log.debug("Artifact is not valid for selected platform : " + artifact);
            }


        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {

        // Only use commons logging api (commonst-logging-api) and skip commons-logging
        if (artifact.getBaseName().startsWith("commons-logging-1") || 
        		artifact.getBaseName().startsWith("spring-2.0"))
            return;

        if (artifact.getBaseName().startsWith("slf4j"))
            return;

        if (artifact.getBaseName().startsWith("jcl-over-slf4j"))
            return;

        if (artifact.getBaseName().startsWith("logback"))
            return;

        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
            installFile(srcFile, this.targetLibDir, replace);
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void installApplication(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            // If the war is already expanded, copy it with a new name.
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Is this the josso gateaway ?
            String name = artifact.getBaseName();
            boolean isFolder = srcFile.getType().equals(FileType.FOLDER);

            if (artifact.getType().equals("war") && name.startsWith("josso-gateway-web")) {
                // INSTALL GWY
                String newName = "josso.war";

                // Do we have to explode the war ?
                if (getTargetPlatform().isJOSSOWarExploded() && !isFolder) {
                    installJar(srcFile, this.targetDeployDir, newName, true, replace);
                } else {
                    installFile(srcFile, this.targetDeployDir, newName, replace);
                }
                return;
            }

            if (artifact.getType().equals("ear") && artifact.getBaseName().startsWith("josso-partner-wl" + wlVersionStr)) {
                installFile(srcFile, this.targetDeployDir, replace);
                return;
            }

            log.debug("Skipping partner application : " + srcFile.getName().getFriendlyURI());

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void configureAgent() {

    }

    @Override
    public void installComponentFromSrc(JOSSOArtifact artifact, boolean replace) throws InstallException {

        try {

            if (!artifact.getBaseName().contains(this.wlVersionStr))
                return;

            // Prepare paths

            FileObject homeDir = getFileSystemManager().resolveFile(System.getProperty("josso-gsh.home"));
            FileObject srcDir = homeDir.resolveFile("dist/agents/src/josso-weblogic" + wlVersionStr + "-agent-mbeans-src");
            FileObject jossoLibDir = homeDir.resolveFile("dist/agents/bin");
            FileObject thrdPartyLibDir = jossoLibDir.resolveFile("3rdparty");


            FileObject descriptorFile = srcDir.resolveFile("org/josso/wls" + wlVersionStr + "/agent/mbeans/JOSSOAuthenticatorProviderImpl.xml");
            FileObject mbeanFile = this.targetJOSSOMBeansDir.resolveFile("josso-weblogic" + wlVersionStr + "-agent-mbeans.jar");

            FileObject javaDir = getFileSystemManager().resolveFile(System.getProperty("java.home") + "/../");
            FileObject javaToolsFile = javaDir.resolveFile("lib/tools.jar");
            FileObject javaFile = javaDir.resolveFile("bin/java");

            getPrinter().printMsg("Using JAVA JDK at " + getLocalFilePath(javaDir));

            if (!javaDir.exists()) {
                getPrinter().printActionErrStatus("Generate", "WL MBeans Descriptors", "JAVA JDK is required : " + getLocalFilePath(javaDir));
                throw new InstallException("JAVA JDK is required for WL : " + getLocalFilePath(javaDir));
            }

            if (!javaToolsFile.exists()) {
                getPrinter().printActionErrStatus("Generate", "WL MBeans Descriptors", "JAVA JDK is required : " + getLocalFilePath(javaToolsFile));
                throw new InstallException("JAVA JDK is required for WL : " + getLocalFilePath(javaToolsFile));
            }

            if (!javaToolsFile.exists()) {
                getPrinter().printActionErrStatus("Generate", "WL MBeans Descriptors", "JAVA JDK is required : " + getLocalFilePath(javaToolsFile));
                throw new InstallException("JAVA JDK is required for WL : " + getLocalFilePath(javaToolsFile));
            }

            // Java CMD and Class path :
            String javaCmd = getLocalFilePath(javaFile);

            String classpath = "";
            String pathSeparator = "";

            // JOSSO Jars
            for (FileObject child : jossoLibDir.getChildren()) {
                if (!child.getName().getBaseName().endsWith(".jar"))
                    continue;

                classpath += pathSeparator + getLocalFilePath(child);
                pathSeparator = System.getProperty("path.separator");
            }

            // JOSSO 3rd party Jars
            for (FileObject child : thrdPartyLibDir.getChildren()) {
                if (!child.getName().getBaseName().endsWith(".jar"))
                    continue;

                classpath += pathSeparator + getLocalFilePath(child);
                pathSeparator = System.getProperty("path.separator");
            }


            for (FileObject child : this.targetDir.resolveFile("server/lib").getChildren()) {
                if (!child.getName().getBaseName().endsWith(".jar"))
                    continue;
                classpath += pathSeparator + getLocalFilePath(child);
                pathSeparator = System.getProperty("path.separator");
            }

            classpath += pathSeparator + getLocalFilePath(javaToolsFile);
            pathSeparator = System.getProperty("path.separator");

            for (FileObject child : javaDir.resolveFile("jre/lib").getChildren()) {
                classpath += pathSeparator + getLocalFilePath(child);
                pathSeparator = System.getProperty("path.separator");
            }


            //  ----------------------------------------------------------------
            // 1. Create the MBean Descriptor Files
            //  ----------------------------------------------------------------

            {
                /*
                   <argument>-Dfiles=${basedir}/target/generated-sources</argument>
                   <argument>-DMDF=${project.build.directory}/generated-sources/org/josso/wls92/agent/mbeans/JOSSOAuthenticatorProviderImpl.xml</argument>
                   <argument>-DtargetNameSpace=urn:org:josso:wls92:agent:mbeans</argument>
                   <argument>-DpreserveStubs=false</argument>
                   <argument>-DcreateStubs=true</argument>
                   <argument>-classpath</argument>
                   <classpath/>
                   <argument>weblogic.management.commo.WebLogicMBeanMaker</argument>
                */

                ProcessBuilder generateMBeanDescriptorProcessBuilder = new ProcessBuilder(javaCmd,
                        "-Dfiles=" + getLocalFilePath(srcDir),
                        "-DMDF=" + getLocalFilePath(descriptorFile),
                        "-DtargetNameSpace=urn:org:josso:wls" + wlVersionStr + ":agent:mbeans",
                        "-DschemaLocation=" + getLocalFilePath(descriptorFile),
                        "-DpreserveStubs=false",
                        "-DcreateStubs=true",
                        "-Dtarget=1.6",
                        "-target=1.6",
                        "-classpath",
                        classpath,
                        "weblogic.management.commo.WebLogicMBeanMaker");

                log.info("Executing: " + generateMBeanDescriptorProcessBuilder.command());

                Process generateMBeanDescriptorProcess = generateMBeanDescriptorProcessBuilder.start();

                PumpStreamHandler generateMBeanHandler = new PumpStreamHandler(getPrinter().getIo().inputStream, getPrinter().getIo().outputStream, getPrinter().getIo().errorStream);
                generateMBeanHandler.attach(generateMBeanDescriptorProcess);
                generateMBeanHandler.start();

                log.debug("Waiting for process to exit...");
                int statusDescr = generateMBeanDescriptorProcess.waitFor();

                log.info("Process exited w/status: " + statusDescr);

                generateMBeanHandler.stop();
                getPrinter().printActionOkStatus("Generate", "WL MBeans Descriptors", "");
            }
            
            //  ----------------------------------------------------------------
            // 2. Create the MBean JAR File
            //  ----------------------------------------------------------------
            {
                /*
               <argument>-Dfiles=${project.build.directory}/generated-sources</argument>
               <argument>-DMJF=${project.build.directory}/josso-weblogic92-agent-mbeans-${pom.version}.jar</argument>
               <argument>-DpreserveStubs=false</argument>
               <argument>-DcreateStubs=true</argument>
               <argument>-classpath</argument>
               <classpath/>
               <argument>weblogic.management.commo.WebLogicMBeanMaker</argument>

                */

                ProcessBuilder generateMBeanJarProcessBuilder = new ProcessBuilder(javaCmd,
                        "-Dfiles=" + getLocalFilePath(srcDir),
                        "-DMJF=" + getLocalFilePath(mbeanFile),
                        "-DpreserveStubs=false",
                        "-DcreateStubs=true",
                        "-Dtarget=1.6",
                        "-target=1.6",
                        "-classpath",
                        classpath,
                        "weblogic.management.commo.WebLogicMBeanMaker");

                log.info("Executing: " + generateMBeanJarProcessBuilder.command());

                Process generateMBeanJarProcess = generateMBeanJarProcessBuilder.start();

                PumpStreamHandler generateMBeanJarHandler = new PumpStreamHandler(getPrinter().getIo().inputStream, getPrinter().getIo().outputStream, getPrinter().getIo().errorStream);
                generateMBeanJarHandler.attach(generateMBeanJarProcess);
                generateMBeanJarHandler.start();

                log.debug("Waiting for process to exit...");
                int statusJar = generateMBeanJarProcess.waitFor();
                log.info("Process exited w/status: " + statusJar);


                generateMBeanJarHandler.stop();
                getPrinter().printActionOkStatus("Generate", "WL MBeans JAR", getLocalFilePath(mbeanFile));

            }
        } catch (Exception e) {
            getPrinter().printActionErrStatus("Generate", "WL MBeans", e.getMessage());
            throw new InstallException("Cannot generate WL MBeans Descriptors : " + e.getMessage(), e);
        }


        // 2. Create the MBean JAR File

        // 3. Install the file in the target platform

        // We need to create WL Mbeans using MBean Maker!

    }
}
