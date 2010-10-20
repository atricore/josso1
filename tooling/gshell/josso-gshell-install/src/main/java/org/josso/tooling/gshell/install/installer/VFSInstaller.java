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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileUtil;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.local.LocalFileName;
import org.apache.geronimo.gshell.ansi.Renderer;
import org.josso.tooling.gshell.core.support.MessagePrinter;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.VariableSolver;
import org.josso.tooling.gshell.install.VariableSolverPlatform;
import org.josso.tooling.gshell.install.util.XUpdateUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;

/**
 * This is a base installer that works on a virtual filesystem.
 * Becase the commons vfs supports difererent providres, we could be installing
 * the agent using FTP, HTTP, SSH ? WEB-DAV ? or even creating our on provider.
 * <p/>
 * We will subclass this installer for now because we do not expect much variabillity for now
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: VFSInstaller.java 1607 2010-05-11 13:39:08Z sgonzalez $
 */
public abstract class VFSInstaller implements Installer, VariableSolver, ApplicationContextAware {

    /**
     * This flag indicates that the installer is a copy of the original so we do not use the prototype installer accidentally.
     */
    boolean copied = false;

    private FileSystemManager fsManager;

    private MessagePrinter printer;

    private Renderer renderer = new Renderer();

    private TargetPlatform targetPlatform;

    protected ApplicationContext appCtx;

    protected Properties properties = new Properties();

    private static final Log log = LogFactory.getLog(VFSInstaller.class);


    // -----------------------------------------------------------------------

    protected FileObject targetDir;
    protected FileObject targetLibDir;
    protected FileObject targetBinDir;
    protected FileObject targetConfDir;
    protected FileObject targetEndorsedLibDir;
    protected FileObject targetDeployDir;

    protected FileObject targetJOSSOSharedLibDir;
    protected FileObject targetJOSSOLibDir;
    protected FileObject targetJOSSOConfDir;

    protected boolean agentConfigFileInstalled = true;
    
    // Registered variable values used for variable solving.
    private Map<String, String> varsResolution = new HashMap<String, String>();

    /**
     */
    protected VFSInstaller(TargetPlatform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    /**
     * We need the default constructor because we instanciate installers reflectively
     */
    protected VFSInstaller() {

    }

    // ----------------------------------------------------------< Variable Solver >

    /**
     * Simple variable solver implementation.
     */
    public String resolveVariables(String text) throws InstallException {

        if (text == null)
            return null;

        String newText = text;
        log.debug("About to replace variables in text ");

        Iterator it = varsResolution.keySet().iterator();

        while (it.hasNext()) {

            String varName = (String) it.next();
            String varValue = varsResolution.get(varName);

            if (varValue == null) {
                throw new InstallException("Cannot resolve variable " + varName);
            }

            if (newText.indexOf('$') >= 0) {
                newText = newText.replaceAll("\\$\\{" + varName + "\\}", varsResolution.get(varName));
                log.debug("Resolved variable " + varName + " to " + varValue);
            }
        }

        if (log.isDebugEnabled())
            log.debug("Resolved variables from " + text + " to " + newText);

        return newText;
    }

    protected void registerVarResolution(String name, String value) {
        varsResolution.put(name, value);
    }

    // ----------------------------------------------------------< Applicaton Context Aware >

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = applicationContext;
    }

    // ----------------------------------------------------------< Installer >

    /**
     * //  TODO : This could be a in a super class.
     * Prototype pattern.
     *
     * @return
     */
    public Installer createInstaller() throws InstallException {
        try {

            VFSInstaller newInstaller = getClass().newInstance();

            newInstaller.setPrinter(printer);
            newInstaller.setTargetPlatform(new VariableSolverPlatform(targetPlatform, newInstaller));
            newInstaller.appCtx = appCtx;
            newInstaller.copied = true;

            return newInstaller;

        } catch (IllegalAccessException e) {
            throw new InstallException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void init() throws InstallException {

        try {

            if (!copied)
                throw new InstallException("You must use a copy of the 'prototype' installer.  Use the createInstaller method");

            String target = properties.getProperty("target");
            if (target == null)
                throw new InstallException("No target property defined");

            registerVarResolution("target", target);

            // At least we need a target
            targetDir = getFileSystemManager().resolveFile("file://" + target);

            if (getTargetPlatform().getLibDir() != null)
                targetLibDir = getFileSystemManager().resolveFile(getTargetPlatform().getLibDir());

            if (getTargetPlatform().getBinDir() != null)
                targetBinDir = getFileSystemManager().resolveFile(getTargetPlatform().getBinDir());

            if (getTargetPlatform().getConfigDir() != null)
                targetConfDir = getFileSystemManager().resolveFile(getTargetPlatform().getConfigDir());

            if (getTargetPlatform().getEndorsedLibDir() != null)
                targetEndorsedLibDir = getFileSystemManager().resolveFile(getTargetPlatform().getEndorsedLibDir());

            if (getTargetPlatform().getDeployDir() != null)
                targetDeployDir = getFileSystemManager().resolveFile(getTargetPlatform().getDeployDir());

            if (getTargetPlatform().getJOSSOSharedLibDir() != null)
                targetJOSSOSharedLibDir = getFileSystemManager().resolveFile(getTargetPlatform().getJOSSOSharedLibDir());

            if (getTargetPlatform().getJOSSOLibDir() != null)
                targetJOSSOLibDir = getFileSystemManager().resolveFile(getTargetPlatform().getJOSSOLibDir());

            if (getTargetPlatform().getJOSSOConfDir() != null)
                targetJOSSOConfDir = getFileSystemManager().resolveFile(getTargetPlatform().getJOSSOConfDir());

            // Register target variable resolution
            registerVarResolution("target", targetDir.getURL().toString());

        } catch (Exception e) {
            throw new InstallException(e.getMessage(), e);
        }

    }

    public byte[] loadArtifact() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    public void validatePlatform() throws InstallException {

        boolean valid = true;

        try {

            if (!targetDir.exists() || !targetDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target", "folder does not exist or is not a directory:" + targetDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetLibDir.exists() || !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target lib", "folder does not exist or is not a directory:" + targetLibDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetBinDir.exists() || !targetBinDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target bin", "folder does not exist or is not a directory:" + targetBinDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetConfDir.exists() || !targetConfDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target conf", "folder does not exist or is not a directory:" + targetConfDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetEndorsedLibDir.exists() || !targetEndorsedLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target endorsed lib", "folder does not exist or is not a directory:" + targetEndorsedLibDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetJOSSOSharedLibDir.exists() || !targetJOSSOSharedLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target JOSSO shared lib", "folder does not exist or is not a directory:" + targetJOSSOSharedLibDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetJOSSOLibDir.exists() || !targetJOSSOLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target JOSSO lib", "folder does not exist or is not a directory:" + targetJOSSOLibDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!targetJOSSOConfDir.exists() || !targetJOSSOConfDir.getType().getName().equals(FileType.FOLDER.getName())) {
                printer.printErrStatus("Target JOSSO conf", "folder does not exist or is not a directory:" + targetJOSSOConfDir.getName().getFriendlyURI());
                valid = false;
            }

            if (!valid)
                throw new InstallException("Invalid Target Platform");

            printer.printOkStatus(getTargetPlatform().getDescription(), "Directory Layout");

        } catch (FileSystemException e) {
            log.error(e.getMessage(), e);
            throw new InstallException("Invalid Target Platform");
        }

    }

    /**
     * Default implementation for configuration install
     *
     * @param artifact
     * @param replace
     * @throws InstallException
     */
    public void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            installFile(srcFile, this.targetJOSSOConfDir, replace);
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    /**
     * Default implementation for configuration install
     *
     * @param artifact
     * @param replace
     * @throws InstallException
     */
    public void installConfiguration(JOSSOArtifact artifact, String finalName, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            installFile(srcFile, this.targetJOSSOConfDir, finalName, replace);
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    /**
     * Default implementation for 3rd party jars install
     *
     * @param artifact
     * @param replace
     * @throws InstallException
     */
    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, false);
            installFile(srcFile, this.targetLibDir, replace);
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        // Do nothing ! we don't know how and where to isntall a JOSSO Component by defatult
    }

    public void installComponentFromSrc(JOSSOArtifact artifact, boolean replace) throws InstallException {
        // Do nothing ! we don't know how and where to isntall a JOSSO Component by defatult
    }

    public void installApplication(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            // If the war is already expanded, copy it with a new name.
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            if (srcFile.getType().equals(FileType.FOLDER)) {
                String name = artifact.getBaseName();
                log.debug("Installing Application folder : " + name);
                installFolder(srcFile, targetDeployDir, name, replace);

            } else {
                String name = artifact.getBaseName();
                log.debug("Installing Application file : " + name);
                installFile(srcFile, this.targetDeployDir, name, replace);
            }

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void configureAgent() throws InstallException {
        // Do nothing, subclasses should know how to do this.
    }

    public String getPlatformId() {
        return targetPlatform.getId();
    }

    public String getPlatformName() {
        return targetPlatform.getPlatformName();
    }

    public String getPlatformVersion() {
        return targetPlatform.getVersion();
    }

    public String getPlatformDescription() {
        return targetPlatform.getDescription();
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    /**
     * Should be implemented by subclasses
     *
     * @param artifact
     */
    public void deployApp(JOSSOArtifact artifact) {
        printInstallErrStatus(artifact.getId(), "Application deployment not supported by this installer.");
    }


    // -----------------------------------------------< Installer Primitives >

    protected boolean backupFile(FileObject src, FileObject targetDir) {

        try {
            int attempt = 1;
            FileObject bkp = targetDir.resolveFile(src.getName().getBaseName() + ".bkp." + attempt);

            while (bkp.exists() && attempt < 99) {
                attempt++;
                bkp = targetDir.resolveFile(src.getName().getBaseName() + ".bkp." + attempt);
            }

            if (bkp.exists()) {
                getPrinter().printActionErrStatus("Backup", src.getName().getBaseName(), "Too many backups already");
                return false;
            }

            bkp.copyFrom(src, Selectors.SELECT_SELF);
            getPrinter().printActionOkStatus("Backup", src.getName().getBaseName(), bkp.getName().getFriendlyURI());

            return true;

        } catch (IOException e) {
            getPrinter().printActionErrStatus("Backup", src.getName().getBaseName(), e.getMessage());
            return false;
        }
    }
    
    public boolean backupGatewayConfigurations(boolean remove) {
    	try{
    		FileObject[] libs = targetJOSSOConfDir.getChildren();
	        for (int i = 0 ; i < libs.length; i ++) {
	            FileObject cfgFile = libs[i];
	
	            if (!cfgFile.getType().getName().equals(FileType.FILE.getName())) {
	                // ignore folders
	                continue;
	            }
	            if(cfgFile.getName().getBaseName().startsWith("josso-") 
	            		&& !cfgFile.getName().getBaseName().startsWith("josso-agent") 
	            		&& (cfgFile.getName().getBaseName().endsWith(".xml") || (cfgFile.getName().getBaseName().endsWith(".properties")))){
	            	//backup files in the same folder they're installed in
	            	backupFile(cfgFile, cfgFile.getParent());
	            	if (remove) {
	            		cfgFile.delete();
	            	}
	            }
	        }
    	}catch (Exception e) {
            getPrinter().printErrStatus("BackupGatewayConfigurations", e.getMessage());
            return false;
		}
    	return true;
    }
    
    public boolean backupAgentConfigurations(boolean remove) {
    	try {
    		// backup josso-agent-config.xml
    		FileObject agentConfigFile = targetJOSSOConfDir.resolveFile("josso-agent-config.xml");
    		if (agentConfigFile.exists()) {
    			// backup file in the same folder it is installed
            	backupFile(agentConfigFile, agentConfigFile.getParent());
            	if (remove) {
            		agentConfigFile.delete();
            	}
    		}
    	} catch (Exception e) {
            getPrinter().printErrStatus("BackupAgentConfigurations", e.getMessage());
            return false;
		}
    	return true;
    }
    
    public boolean updateAgentConfiguration(String idpHostName, String idpPort, String idpType) {
    	if (agentConfigFileInstalled) {
    		String hostAndPort = idpHostName;
    		if (!idpPort.equals("80")) {
    			hostAndPort += ":" + idpPort;
    		}
    		
    		if (!hostAndPort.equals("localhost:8080") || idpType.equals("atricore-idbus")) {
	    		try {
	    	    	FileObject agentConfigFile = targetJOSSOConfDir.resolveFile("josso-agent-config.xml");
	    	    	if (agentConfigFile.exists()) {
	    				// Get a DOM document of the josso-agent-config.xml
	    	            Node configXmlDom = readContentAsDom(agentConfigFile);
	    	            
	    	            String gatewayLoginUrl = "http://" + hostAndPort;
	    	            String gatewayLogoutUrl = "http://" + hostAndPort;

                        // TODO : PB-1 should also be variable!
	    	            if (idpType.equals("atricore-idbus")) {
	    	            	gatewayLoginUrl += "/IDBUS/BP-1/JOSSO/SSO/REDIR";
	    	            	gatewayLogoutUrl += "/IDBUS/BP-1/JOSSO/SLO/REDIR";
	    	            } else {
	    	            	gatewayLoginUrl += "/josso/signon/login.do";
	    	            	gatewayLogoutUrl += "/josso/signon/logout.do";
	    	            }
	    	            
	    	            String updateIDP = "<xupdate:update select=\"//gatewayLoginUrl\">" +
	    	            					gatewayLoginUrl + "</xupdate:update>";
	    	            
	    	            updateIDP += "<xupdate:update select=\"//gatewayLogoutUrl\">" +
    									gatewayLogoutUrl + "</xupdate:update>";

	    	            updateIDP += "<xupdate:update select=\"//@endpoint\">" +
										hostAndPort + "</xupdate:update>";
	    	            
	    	            updateIDP += "<xupdate:update select=\"//property[@name='ignoredReferrers']/list/value\">" +
										"http://" + hostAndPort + "/IDBUS</xupdate:update>";
	    	            
	    	            if (idpType.equals("atricore-idbus")) {
		    	            updateIDP += "<xupdate:append select=\"//protocol:ws-service-locator\">";
		    	            updateIDP += "<xupdate:attribute name=\"identityManagerServicePath\">IDBUS/BP-1/JOSSO/SSOIdentityManager/SOAP</xupdate:attribute>";
		    	            updateIDP += "<xupdate:attribute name=\"identityProviderServicePath\">IDBUS/BP-1/JOSSO/SSOIdentityProvider/SOAP</xupdate:attribute>";
		    	            updateIDP += "<xupdate:attribute name=\"sessionManagerServicePath\">IDBUS/BP-1/JOSSO/SSOSessionManager/SOAP</xupdate:attribute>";
		    	            updateIDP += "</xupdate:append>";
	    	            }
	    	            
	    	            String updateIDPQryStr = XUpdateUtil.XUPDATE_START + updateIDP + XUpdateUtil.XUPDATE_END;
	    	            log.debug("XUPDATE QUERY: \n" + updateIDPQryStr);

	    	            XUpdateQuery updateIDPQry = new XUpdateQueryImpl();
	    	            updateIDPQry.setQString(updateIDPQryStr);
	    	            updateIDPQry.execute(configXmlDom);

                        getPrinter().printActionOkStatus("Configure","IDP type", idpType);
	    	            getPrinter().printActionOkStatus("Configure","IDP host and port", hostAndPort);
	    	            
	    	            // Write modifications to file
	                    writeContentFromDom(configXmlDom, agentConfigFile);
	                    getPrinter().printActionOkStatus("Save", agentConfigFile.getName().getBaseName(), agentConfigFile.getName().getFriendlyURI());

	    	            return true;
	    			}
	        	} catch (Exception e) {
	                getPrinter().printErrStatus("UpdateAgentConfiguration", e.getMessage());
	                return false;
	    		}
    		}
    	}
    	
		return false;
    }
    
    public boolean removeOldComponents(boolean backup) {
    	try {
    		FileObject[] sharedLibs = targetJOSSOSharedLibDir.getChildren();
	        for (int i = 0 ; i < sharedLibs.length; i ++) {
	            FileObject jarFile = sharedLibs[i];
	
	            if (!jarFile.getType().getName().equals(FileType.FILE.getName())) {
	                // ignore folders
	                continue;
	            }
	            if (jarFile.getName().getBaseName().startsWith("josso-") 
	            		&& jarFile.getName().getBaseName().endsWith(".jar")) {
	            	if (backup) {
	            		// backup files in the same folder they're installed in
	            		backupFile(jarFile, jarFile.getParent());
	            	}
	            	jarFile.delete();
	            }
	        }
	        
	        FileObject[] libs = targetJOSSOLibDir.getChildren();
	        for (int i = 0 ; i < libs.length; i ++) {
	            FileObject jarFile = libs[i];
	
	            if (!jarFile.getType().getName().equals(FileType.FILE.getName())) {
	                // ignore folders
	                continue;
	            }
	            if (jarFile.getName().getBaseName().startsWith("josso-") 
	            		&& jarFile.getName().getBaseName().endsWith(".jar")) {
	            	if (backup) {
	            		// backup files in the same folder they're installed in
	            		backupFile(jarFile, jarFile.getParent());
	            	}
	            	jarFile.delete();
	            }
	        }
    	} catch (Exception e) {
            getPrinter().printErrStatus("RemoveOldComponents", e.getMessage());
            return false;
		}
    	return true;
    }
    
    public boolean removeOldJar(String fullJarName, FileObject destDir, boolean backup) {
    	try {
    		if (fullJarName.endsWith(".jar") && fullJarName.lastIndexOf("-") != -1) {
	    		String jarNameWithoutVersion = fullJarName.substring(0, fullJarName.lastIndexOf("-"));
	    		
	    		FileObject[] files = destDir.getChildren();
		        for (int i = 0 ; i < files.length; i ++) {
		            FileObject file = files[i];
		            
		            if (!file.getType().getName().equals(FileType.FILE.getName())) {
		                // ignore folders
		                continue;
		            }
		            
		            String fileName = file.getName().getBaseName();
		            if (fileName.startsWith(jarNameWithoutVersion) 
		            		&& fileName.endsWith(".jar")
		            		&& fileName.substring(jarNameWithoutVersion.length(), 
		            			jarNameWithoutVersion.length() + 2).matches("-[0-9]|\\.j")) {
		            	if (backup) {
		            		// backup files in the same folder they're installed in
		            		backupFile(file, file.getParent());
		            	}
		            	file.delete();
		            	break;
		            }
		        }
    		}
    	} catch (Exception e) {
    		getPrinter().printErrStatus("RemoveOldJar", e.getMessage());
            return false;
		}
    	return true;
    }
    
    public boolean installJar(FileObject srcFile, FileObject destFolder, String newName, boolean explode, boolean replace) throws IOException {

        try {
            
            if (explode) {

                FileObject jarFile = getFileSystemManager().resolveFile("jar:" + srcFile.getName().getFriendlyURI());

                if (log.isDebugEnabled())
                    log.debug("Exploding JAR ["+srcFile.getName().getFriendlyURI()+ "] to ["+destFolder.getName().getFriendlyURI()+"]");

                installFolder(jarFile, destFolder, newName, replace);
            } else {
                installFile(srcFile, destFolder, newName, replace);
            }

            getPrinter().printActionOkStatus("Unjar", srcFile.getName().getBaseName(), destFolder.getName().getFriendlyURI());

            return true;

        } catch (FileSystemException e) {
            log.error("Cannot unjar file " + srcFile.getName().getFriendlyURI() + " : " + e.getMessage(), e);
            getPrinter().printActionErrStatus("Unjar", srcFile.getName().getBaseName(), e.getMessage());
        }
        return false;
    }



    protected boolean installFile(String fileName, FileObject srcDir, FileObject destDir, boolean replace) throws IOException {

        try {

            FileObject srcFile = srcDir.resolveFile(fileName);
            return installFile(srcFile, destDir, srcFile.getName().getBaseName(), replace);
        } finally {
            printer.flush();
        }

    }

    protected boolean installFile(String fileName, FileObject srcDir, FileObject destDir) throws IOException {
        return installFile(fileName, srcDir, destDir, true);
    }

    protected boolean installFile(FileObject srcFile, FileObject destDir, boolean replace) throws IOException {
        return installFile(srcFile, destDir, srcFile.getName().getBaseName(), replace);
    }


    protected boolean installFile(FileObject srcFile, FileObject destDir, String newName, boolean replace) throws IOException {

        try {

            String fname = srcFile.getName().getBaseName();
            if (srcFile == null || !srcFile.exists()) {
                printInstallErrStatus(fname, "Source file not found " + fname);
                throw new IOException("Source file not found " + fname);
            }

            // Validates src and dest files
            if (destDir == null || !destDir.exists()) {
                printInstallErrStatus(fname, "Target directory not found " + destDir.getURL());
                throw new IOException("Target directory not found " + destDir.getURL());
            }

            FileObject destFile = destDir.resolveFile(newName);

            boolean exists = destFile.exists();

            if (!replace && exists) {
                printInstallWarnStatus(fname, "Not replaced (see --replace option)");
                if (fname.equals("josso-agent-config.xml")) {
                	agentConfigFileInstalled = false;
                }
                return false;
            }

            FileUtil.copyContent(srcFile, destFile);
            printInstallOkStatus(fname, (exists ? "Replaced " : "Created ") + destFile.getName().getFriendlyURI());

            return true;

        } finally {
            printer.flush();
        }

    }

    /**
     * @param srcFolder
     * @param destDir
     * @param replace
     * @throws java.io.IOException
     */
    protected void installFolder(FileObject srcFolder, FileObject destDir, String newName, boolean replace) throws IOException {


        // Check destination folder
        if (!destDir.exists()) {
            printInstallErrStatus(srcFolder.getName().getBaseName(), "Target directory not found " + destDir.getName().getBaseName());
            throw new IOException("Target directory not found " + destDir.getName().getBaseName());
        }

        // This is the new folder we're creating in destination.
        FileObject newFolder = destDir.resolveFile(newName);

        boolean exists = newFolder.exists();

        if (!replace && exists) {
            printInstallWarnStatus(newFolder.getName().getBaseName(), "Not replaced (see --replace option)");
            return;
        }
        
        if (exists) {
        	// remove all descendents (removes old jars and other files when upgrading josso gateway)
        	newFolder.delete(Selectors.EXCLUDE_SELF);
        }
        
        newFolder.copyFrom(srcFolder, Selectors.SELECT_ALL); // Copy ALL descendats

        printInstallOkStatus(srcFolder.getName().getBaseName(), (exists ? "Replaced " : "Created ") + newFolder.getName().getFriendlyURI());
    }

    protected void installFiles(FileObject[] srcFiles, FileObject destDir, boolean replace) throws IOException {
        for (int i = 0; i < srcFiles.length; i++) {
            FileObject srcFile = srcFiles[i];
            installFile(srcFile, destDir, srcFile.getName().getBaseName(), replace);
        }
    }


    protected Document readContentAsDom(FileObject file) throws Exception {
        InputStream is = null;

        try {
            is = file.getContent().getInputStream();

            DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(true);
            parserFactory.setIgnoringElementContentWhitespace(false);
            parserFactory.setIgnoringComments(false);

            DocumentBuilder builder = parserFactory.newDocumentBuilder();
            
            boolean dtdNotFound = false;
            Document doc = null;
            try {
            	doc = builder.parse(is);
            } catch (FileNotFoundException e) {
            	dtdNotFound = true;
            }
            
            // if dtd doesn't exist parse the document again without trying to load dtd
            if (dtdNotFound) {
            	is = file.getContent().getInputStream();
            	// disable dtd loading
            	parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            	builder = parserFactory.newDocumentBuilder();
            	doc = builder.parse(is);
            }
            
            DocumentType docType = doc.getDoctype();

            if (log.isDebugEnabled() && docType != null) {
                log.debug("docType.getPublicId()=" + docType.getPublicId());
                log.debug("docType.getSystemId()=" + docType.getSystemId());
            }

            return doc;

        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) { /**/}
        }

    }

    protected void writeContentFromDom(String publicId, String systemId, Node node, FileObject file) throws Exception {
        OutputStream os = null;

        try {
            os = file.getContent().getOutputStream();
            Result result = new StreamResult(os);

            // Write the Server.xml document back to the file!
            Source source = new DOMSource(node);

            Transformer xformer = TransformerFactory.newInstance().newTransformer();

            if (publicId != null)
                xformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);

            if (systemId != null)
                xformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);

            xformer.transform(source, result);

        } finally {
            if (os != null) try {
                os.close();
            } catch (IOException e) { /**/}
        }
    }

    protected void writeContentFromDom(Node node, FileObject file) throws Exception {
        this.writeContentFromDom(null, null, node, file);
    }

    public void performAdditionalTasks(FileObject libsDir) throws InstallException {
    	// do nothing
    }
    
    /**
     * Executes the specified external command and arguments in a separate process.
     * 
     * @param cmdarray array containing the command to call and its arguments
     * @return the exit value of the process. By convention, 0 indicates normal termination.
     * @throws IOException
     */
    public int executeExternalCommand(String[] cmdarray) throws IOException {
    	int exitVal = -1;
    	
    	try {
    		ProcessBuilder pb = new ProcessBuilder(cmdarray);
        	pb.redirectErrorStream(true);
        	Process pr = pb.start();
        	BufferedReader input = new BufferedReader(
        			new InputStreamReader(pr.getInputStream()));
	        String line = null;
	        while ((line=input.readLine()) != null) {
	        	log.debug(line);
	        }
	        exitVal = pr.waitFor();
		} catch (Exception e) {
			exitVal = -1;
			log.debug(e.getMessage());
		}
		return exitVal;
    }
    
    /**
     * Get local file path.
     * 
     * @param file file
     * @return local file path
     */
    protected String getLocalFilePath(FileObject file) {
    	FileName fileName = file.getName();
    	String localPath = fileName.getPath();
    	String rootFile = ((LocalFileName)fileName).getRootFile();
    	if (rootFile != null) {
    		localPath = rootFile + fileName.getPath();
    	}
    	return localPath;
    }
    
    // -----------------------------------------------< Some Installer print utils >

    protected void printInstallErrStatus(String fileName, String msg) {
        getPrinter().printActionErrStatus("Installing", fileName, msg);
    }

    protected void printInstallWarnStatus(String fileName, String msg) {
        getPrinter().printActionWarnStatus("Installing", fileName, msg);
    }

    protected void printInstallOkStatus(String fileName, String msg) {
        getPrinter().printActionOkStatus("Installing", fileName, msg);
    }


    public TargetPlatform getTargetPlatform() {
        return targetPlatform;
    }

    // -----------------------------------------------< Some protected utils >

    protected FileSystemManager getFileSystemManager() {
        if (fsManager == null) {
            try {
                fsManager = VFS.getManager();
            } catch (FileSystemException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return fsManager;
    }


    protected Renderer getRenderer() {
        return renderer;
    }

    public MessagePrinter getPrinter() {
        return printer;
    }

    public void setPrinter(MessagePrinter printer) {
        this.printer = printer;
    }

    protected void setTargetPlatform(TargetPlatform targetPlatform) {
        this.targetPlatform = targetPlatform;
    }
}
