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

import java.io.OutputStream;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.geronimo.gshell.clp.Option;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.codehaus.plexus.util.StringUtils;
import org.josso.auth.util.CipherUtil;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.JOSSOScope;
import org.josso.tooling.gshell.install.commands.support.InstallCommandSupport;

/**
 * @org.apache.xbean.XBean element="install-web-gateway"
 *
 * The execution method is a template method used to install web gateways</br>
 *
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 18, 2008
 * Time: 4:09:49 PM
 * To change this template use File | Settings | File Templates.
 */
@CommandComponent(id="josso-gateway:install", description="Install JOSSO Gateway")
public class InstallWebGatewayCommand extends InstallCommandSupport {


    @Option(name = "--persistence", description = "Install default configuration for the specified persistence mechanism (db, ldap, memory)", required = false, argumentRequired = true)
    private String persistence = "memory";

    @Option(name = "-s", aliases ={"--source"} ,description = "Custom gateway's artifact source location, specified as mvn:groupId/artifactId/version/type (e.g. mvn:org.josso/josso-gateway-web/1.8.0/war)", required = false, argumentRequired = true)
    private String artifactLocation = "";
    
    @Option(name = "--copy-configuration", description = "Copy configuration files. Should be false for custom gateway.", required = false, argumentRequired = true)
    private boolean copyConfigFiles = true;
    // -----------------------------------------------------------------------
    protected FileObject homeDir;
    protected FileObject appDir;
    protected FileObject confDir;
    protected FileObject tmpDir;

    public InstallWebGatewayCommand() {
        this.setShell("gateway");

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
        appDir = homeDir.resolveFile("dist/gateway/apps");
        confDir = homeDir.resolveFile("dist/gateway/config/");

        log.debug("JAVA TMP : " + System.getProperty("java.io.tmpdir"));
        tmpDir = fs.resolveFile(System.getProperty("java.io.tmpdir"));
    }


    protected void installConfig() throws Exception {

        if(copyConfigFiles){
	        // Generate a key for rememberme auth
	        SecretKeySpec key = CipherUtil.generateAESKey();
	        byte[] keyBytes = key.getEncoded();
	        String keyStr = CipherUtil.encodeBase64(keyBytes);
	
	        FileObject authProperties = tmpDir.resolveFile("josso-auth.properties");
	
	        authProperties.createFile();
	        OutputStream os = authProperties.getContent().getOutputStream(true);
	        java.util.Properties authProps = new java.util.Properties();
	
	        authProps.setProperty("josso.rememberme.authscheme.key", keyStr);
	        authProps.store(os, "JOSSO 'Remember Me' authentication schemem properties.");
	
	        printer.printActionOkStatus("Generating",  "'Remember Me' AES key", "Created " + authProperties.getName().getFriendlyURI());
	
	        getInstaller().installConfiguration(createArtifact(tmpDir.getURL().toString(), JOSSOScope.GATEWAY, "josso-auth.properties"), isReplaceConfig());
	        try { authProperties.delete(); } catch (java.io.IOException e) { /* */ }


            String persistenceFileName = "josso-gateway-" + persistence + "-stores.xml";
            printer.printActionOkStatus("Using",  "'"+persistence+"' default configuration", "Installing " + persistenceFileName + " as " + "josso-gateway-stores.xml");
            
	        // Install all configuration files :
	        FileObject[] libs = confDir.getChildren();
	        for (int i = 0 ; i < confDir.getChildren().length ; i ++) {
	            FileObject cfgFile = libs[i];
	
	            if (!cfgFile.getType().getName().equals(FileType.FILE.getName())) {
	                // ignore folders
	                continue;
	            }
	
	            String fileName = cfgFile.getName().getBaseName();
	            if (fileName.equals(persistenceFileName)) {
	                getInstaller().installConfiguration(createArtifact(confDir.getURL().toString(), JOSSOScope.GATEWAY, fileName), "josso-gateway-stores.xml", isReplaceConfig());
	            } 
	
	            getInstaller().installConfiguration(createArtifact(confDir.getURL().toString(), JOSSOScope.GATEWAY, fileName), isReplaceConfig());
	        }
        } else {
        	//TODO backup configuration files, if they exist
        	io.out.println("Backup and remove existing configuration files");
        	getInstaller().backupGatewayConfigurations(true);
        }

    }

    protected void deployWar() throws Exception {
    	if(StringUtils.isEmpty(artifactLocation)){
    		getInstaller().installApplication(createGatewayArtifact(appDir.getURL().toString(), "josso-gateway-web", null, "war"), true);
    	} else {
    		JOSSOArtifact customArtifact = createCustomGatewayArtifact(artifactLocation, "josso-gateway-web", null, "war");
    		getInstaller().installApplication(customArtifact, true);
    	}
    }

    protected void backupGatewayConfigurations() throws Exception {
        if (isReplaceConfig() && copyConfigFiles) {
        	getInstaller().backupGatewayConfigurations(false);
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

            printer.printMsg("Backing up JOSSO Gateway configuration");
            backupGatewayConfigurations();
            printer.printMsg();
            
            io.out.println("Install JOSSO Gateway Configuration");
            installConfig();
            io.out.println();

            io.out.println("Deploy JOSSO Gateway Application");
            deployWar();
            io.out.println();

            // -----------------------------------------------------------------------
            // 6. Inform outcome
            io.out.println(getInstaller().getPlatformDescription() + " JOSSO Gateway v." + getJOSSOVersion());
            printer.printOkStatus("Overall Installation", "Successful!");
            io.out.println();

            io.out.println("@|bold Congratulations!| You've successfully installed the gateway.");
            io.out.println("Now Follow the @|bold JOSSO Gateway Configuration guide| and setup JOSSO as needed");
            io.out.println();

        } catch (Exception e) {
            // 5. Inform outcome (error)
            io.out.println();
            io.out.println(getInstaller().getPlatformDescription() + " JOSSO Gateway v." + getJOSSOVersion());
            printer.printErrStatus("Overall Installation", e.getMessage());
            io.out.println();
            io.out.println("See ../log/gshell.log for details");
            log.error(e.getMessage(), e);

        }

        return null;

    }
}
