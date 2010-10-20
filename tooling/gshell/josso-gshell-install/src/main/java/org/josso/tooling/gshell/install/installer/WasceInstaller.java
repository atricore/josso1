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
import org.apache.commons.vfs.Selectors;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;

/**
 * @org.apache.xbean.XBean element="wasce-installer"
 */
public class WasceInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(WasceInstaller.class);

    protected FileObject deploymentScript;
    
    protected String user = "system";
    
    protected String password = "manager";
    
    public WasceInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public WasceInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {
    	super.validatePlatform();
    }

    @Override
    public void init() throws InstallException {

        log.debug("Init WASCE installer");
        getPrinter().printMsg("WASCE needs to be running in order to successfully deploy JOSSO");
        
        // Initialize installer
        super.init();
        
        // find deployment script
        try {
        	deploymentScript = targetBinDir.resolveFile("deploy.sh");
			if (!deploymentScript.exists()) {
				deploymentScript = targetBinDir.resolveFile("deploy.bat");
			}
			if (!deploymentScript.exists()) {
	        	throw new InstallException("WASCE deployment application can not be found!!!");
			}
        } catch (Exception e) {
            throw new InstallException(e.getMessage(), e);
        }
        
        // set user and password for server login
        String consoleUser = getProperty("user");
        if (consoleUser != null) {
        	user = consoleUser;
        }
        String consolePass = getProperty("password");
        if (consolePass != null) {
        	password = consolePass;
        }
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
    	// do nothing
    }


    @Override
    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        // do nothing
    }
    
    @Override
	public void performAdditionalTasks(FileObject libsDir) throws InstallException {
    	try {
    		// undeploy wasce tomcat6 module if exists
    		FileObject tomcatModule = targetDir.resolveFile("repository/org/apache/geronimo/configs/tomcat6");
    		if (tomcatModule.exists()) {
    			getPrinter().printMsg("Undeploying tomcat6 module");
        		int status = undeploy("tomcat6");
        		if (status == 0) {
        			getPrinter().printOkStatus("Undeploy tomcat6 module", "Successful");
                } else {
                	getPrinter().printErrStatus("Undeploy tomcat6 module", "Error");
                	throw new InstallException("Error undeploying tomcat6 module!!!");
                }
    		}
    		
    		// undeploy old josso wasce agent if exists
    		FileObject jossoWasceAgentModule = targetDir.resolveFile("repository/org/josso/josso-wasce-agent");
    		if (jossoWasceAgentModule.exists()) {
        		getPrinter().printMsg("Undeploying old josso wasce agent");
        		int status = undeploy("josso-wasce-agent");
        		if (status == 0) {
        			getPrinter().printOkStatus("Undeploy josso wasce agent", "Successful");
                } else {
                	getPrinter().printErrStatus("Undeploy josso wasce agent", "Error");
                	throw new InstallException("Error undeploying josso wasce agent!!!");
                }
    		}
    		
	    	// install jars to wasce repository
	    	try {
	    		getPrinter().printMsg("Installing new jars to WASCE repository");
	        	FileObject wasceRepo = targetDir.resolveFile("repository");
				FileObject wasceRepoFolder = libsDir.resolveFile("repository");
				wasceRepo.copyFrom(wasceRepoFolder, Selectors.SELECT_ALL);
				getPrinter().printOkStatus("Install new jars", "Successful");
			} catch (FileSystemException e) {
				getPrinter().printErrStatus("Install new jars", "Error");
				throw new InstallException("Error copying jars to wasce repository!!!");
			}
	        
	        // deploy josso wasce agent
			getPrinter().printMsg("Deploying josso wasce agent");
	        FileObject jossoWasceCarFile = null;
	        FileObject[] agentBins = libsDir.getChildren();
	        for (int i = 0; i < agentBins.length; i++) {
	            FileObject agentBin = agentBins[i];
	            if (agentBin.getName().getBaseName().startsWith("josso-wasce")) {
	            	jossoWasceCarFile = agentBin;
	            	break;
	            }
	        }
	        if (jossoWasceCarFile == null) {
	        	throw new InstallException("Josso wasce agent car file doesn't exist!!!");
	        }
        	int status = installPlugin(jossoWasceCarFile);
        	if (status == 0) {
        		getPrinter().printOkStatus("Install josso wasce agent", "Successful");
        	} else {
        		getPrinter().printErrStatus("Install josso wasce agent", "Error");
            	throw new InstallException("Error installing josso wasce agent!!!");
            }
        	
			// start stopped services
        	getPrinter().printMsg("Starting tomcat related services");
	        status = startTomcatRelatedServices();
	        if (status == 0) {
	        	getPrinter().printOkStatus("Start tomcat related services", "Successful");
	        } else {
	        	getPrinter().printErrStatus("Start tomcat related services", "Error");
            	throw new InstallException("Error starting tomcat related services!!!");
            }
		} catch (IOException e) {
			throw new InstallException(e.getMessage(), e);
		}
	}

    @Override
	public void installApplication(JOSSOArtifact artifact, boolean replace) throws InstallException {
		try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Is this the josso gateway or wasce partner application?
            String name = artifact.getBaseName();
            if (artifact.getType().equals("war") && (name.startsWith("josso-gateway-web") || 
            		name.startsWith("josso-partner-wasce"))) {
            	int status = deployApplication(srcFile, replace);
                if (status != 0) {
                	String srcFileName = srcFile.getName().getBaseName();
                	printInstallErrStatus(srcFileName, "Error deploying " + getLocalFilePath(srcFile));
                	throw new InstallException("Error deploying " + getLocalFilePath(srcFile));
                }
                return;
            }
            
            log.debug("Skipping partner application : " + srcFile.getName().getFriendlyURI());

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public boolean removeOldComponents(boolean backup) {
    	// do nothing
    	// josso components are stored in wasce repository
    	// (in a structure similar to maven repository)
    	// so there won't be any conflicts
    	return true;
    }
    
    /**
     * Deploys application to wasce.
     * 
     * @param application deployment file
     * @param redeploy true if application should be redeployed, false otherwise (it is always true, do not use it)
     * @return 0 if deployment was successful, otherwise returns error code
     * @throws IOException
     */
    public int deployApplication(FileObject application, boolean redeploy) throws IOException {
    	String cmd = "deploy";
    	//if (redeploy) {
    	//	cmd = "redeploy";
    	//}
    	String[] cmdarray = {getLocalFilePath(deploymentScript), 
    			"--user", user, "--password", password, 
    			cmd, getLocalFilePath(application)};
    	return executeExternalCommand(cmdarray);
    }
    
    /**
     * Installs geronimo plugin to wasce.
     * 
     * @param plugin plugin file
     * @return 0 if install was successful, otherwise returns error code
     * @throws IOException
     */
    public int installPlugin(FileObject plugin) throws IOException {
    	String[] cmdarray = {getLocalFilePath(deploymentScript), 
    			"--user", user, "--password", password, 
    			"install-plugin", getLocalFilePath(plugin)};
    	return executeExternalCommand(cmdarray);
    }
    
    /**
     * Starts services that were stopped when tomcat6 module was undeployed.
     * 
     * @return 0 if it was successful, otherwise returns error code
     * @throws IOException
     */
    public int startTomcatRelatedServices() throws IOException {
    	String[] cmdarray = {getLocalFilePath(deploymentScript), 
    			"--user", user, "--password", password, 
    			"start", "uddi-tomcat", "console-tomcat", 
    			"activemq-ra", "system-database", "agent-ds", 
    			"mconsole-ds", "collector-tool-agent-config", 
    			"welcome-tomcat", "ca-helper-tomcat", 
    			"dojo-legacy-tomcat", "dojo-tomcat", 
    			"remote-deploy-tomcat", "activemq-console-tomcat", 
    			"debugviews-console-tomcat", "mconsole-tomcat",
    			"plancreator-console-tomcat", "plugin-console-tomcat", 
    			"sysdb-console-tomcat", "tomcat6-clustering-builder-wadi",
    			"tomcat6-deployer", "tomcat6-no-ha"};
    	return executeExternalCommand(cmdarray);
    }
    
    /**
     * Undeploys geronimo module from wasce.
     * 
     * @param moduleID module id
     * @return 0 if undeployment was successfull, otherwise returns error code
     * @throws IOException
     */
    public int undeploy(String moduleID) throws IOException {
    	String[] cmdarray = {getLocalFilePath(deploymentScript), 
    			"--user", user, "--password", password, 
    			"undeploy", moduleID};
    	return executeExternalCommand(cmdarray);
    }
}
