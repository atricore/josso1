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

import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.util.XUpdateUtil;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;

/**
 * @org.apache.xbean.XBean element="tomcat-installer"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class TomcatInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(TomcatInstaller.class);

    public TomcatInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public TomcatInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        super.validatePlatform();
        try {

            boolean valid = true;

            FileObject catalinaJar = targetLibDir.resolveFile("catalina.jar");

            if (catalinaJar == null || !catalinaJar.exists() || !catalinaJar.getType().getName().equals(FileType.FILE.getName())) {
                valid = false;
                getPrinter().printErrStatus("CatalinaHome", "Cannot find catalina");
            }

            FileObject serverXml = targetConfDir.resolveFile("server.xml");
            if (serverXml == null || !serverXml.exists() || !serverXml.getType().getName().equals(FileType.FILE.getName())) {
                valid = false;
                getPrinter().printErrStatus("CatalinaHome", "Cannot find server.xml");
            }

            // TODO : Validate Version ?

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("CatalinaHome", e.getMessage());
            throw new InstallException (e.getMessage(), e);
        }

        getPrinter().printOkStatus("CatalinaHome");
    }

    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            // TODO : This should be part of the install command, maybe based on 'features' definitions.

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetJOSSOSharedLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agents-bin")) {

                // For Tomcat 6 and 7, jaxws is used now
                if (getTargetPlatform().getVersion().startsWith("6.0") ||
                    getTargetPlatform().getVersion().startsWith("7.0") ||
                    getTargetPlatform().getVersion().startsWith("8.0") ||
                        getTargetPlatform().getVersion().startsWith("8.5") ||
                        getTargetPlatform().getVersion().startsWith("9.0")) {

                    if (artifact.getClassifier().equals("jaxws")) {
                        installFile(srcFile, this.targetJOSSOLibDir, replace);
                    }

                } else if (artifact.getClassifier() == null || artifact.getClassifier().equals("axis")) {
                    // This is either Tomcat 5.0 or 5.5 ...
                    installFile(srcFile, this.targetJOSSOLibDir, replace);
                }

            } else  if (artifact.getBaseName().startsWith("josso-tomcat50-agent") &&
                    getTargetPlatform().getVersion().startsWith("5.0")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat55-agent") &&
                    getTargetPlatform().getVersion().startsWith("5.5")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat60-agent") &&
                    getTargetPlatform().getVersion().startsWith("6.0")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat70-agent") &&
                    getTargetPlatform().getVersion().startsWith("7.0")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat80-agent") &&
                    getTargetPlatform().getVersion().startsWith("8.0")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat85-agent") &&
                    getTargetPlatform().getVersion().startsWith("8.5")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);
            } else if (artifact.getBaseName().startsWith("josso-tomcat85-agent") &&
                        getTargetPlatform().getVersion().startsWith("9.0")) {
                    installFile(srcFile, this.targetJOSSOLibDir, replace);
            } else {
                log.debug("Artifact is not valid for selected platform : " + artifact);
            }


        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {

        if (artifact.getBaseName().startsWith("log4j") || 
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
            
            // Some jars must be copied in special directories in Tomcat 5.5 / 5.0
            if (getTargetPlatform().getVersion().startsWith("5.")) {

                if (artifact.getBaseName().startsWith("commons-collections")){
                	removeOldJar(srcFile.getName().getBaseName(), this.targetEndorsedLibDir, true);
                    installFile(srcFile, this.targetEndorsedLibDir, replace);
                } else {
                	removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
                    installFile(srcFile, this.targetLibDir, replace);
                }

            } else if (getTargetPlatform().getVersion().startsWith("6.0") ||
                    getTargetPlatform().getVersion().startsWith("7.0") ||
                    getTargetPlatform().getVersion().startsWith("8.0") ||
                    getTargetPlatform().getVersion().startsWith("8.5") ||
                    getTargetPlatform().getVersion().startsWith("9.0")) {

                // Minmal set of dependencies for TC 6/7/8/8.5/9
                if (artifact.getBaseName().startsWith("spring-")) {
                    removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
                    installFile(srcFile, this.targetLibDir, replace);
                } else if (artifact.getBaseName().startsWith("xbean-")) {
                    removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
                    installFile(srcFile, this.targetLibDir, replace);
                } else if (artifact.getBaseName().startsWith("commons-")) {
                    removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
                    installFile(srcFile, this.targetLibDir, replace);
                }


            } else  {
            	removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
                installFile(srcFile, this.targetLibDir, replace);
            }


        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void installApplication(JOSSOArtifact artifact, boolean replace) throws InstallException {

        try {

            if (!artifact.getType().equals("war")) {
                log.debug("Application type : " + artifact.getType() + " not supported.");
                return ;
            }

            // If the war is already expanded, copy it with a new name.
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Is this the josso gateaway ?
            String name = artifact.getBaseName();
            boolean isFolder = srcFile.getType().equals(FileType.FOLDER);

            if (name.startsWith("josso-gateway-web")) {
                // INSTALL GWY
                // Do we have to explode the war ?
                if (getTargetPlatform().isJOSSOWarExploded() && !isFolder) {
                    installJar(srcFile, this.targetDeployDir, "josso", true, replace);
                } else {
                    installFile(srcFile, this.targetDeployDir, "josso.war", replace);
                }
                return;
            }

            if (artifact.getBaseName().startsWith("josso-partner-tomcat-web")) {
                installJar(srcFile, this.targetDeployDir, "partnerapp", true, replace);
                return;
            }


        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            String name = srcFile.getName().getBaseName();

            if (name.startsWith("setenv")) {
                installFile(srcFile, this.targetBinDir, replace);
            } else if (name.equals("jaas.conf")) {
                installFile(srcFile, this.targetConfDir, replace);
            } else {
                installFile(srcFile, this.targetJOSSOConfDir, replace);
            }
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }


    }

    @Override
    public boolean backupAgentConfigurations(boolean remove) {
    	try {
    		super.backupAgentConfigurations(remove);
    		// backup jaas.conf
    		FileObject jaasConfigFile = targetConfDir.resolveFile("jaas.conf");
    		if (jaasConfigFile.exists()) {
    			// backup file in the same folder it is installed
            	backupFile(jaasConfigFile, jaasConfigFile.getParent());
            	if (remove) {
            		jaasConfigFile.delete();
            	}
    		}
    		// backup setenv.sh and setenv.bat
    		FileObject[] libs = targetBinDir.getChildren();
	        for (int i = 0 ; i < libs.length; i ++) {
	            FileObject cfgFile = libs[i];
	
	            if (!cfgFile.getType().getName().equals(FileType.FILE.getName())) {
	                // ignore folders
	                continue;
	            }
	            if (cfgFile.getName().getBaseName().startsWith("setenv") 
	            		&& (cfgFile.getName().getBaseName().endsWith(".sh") || cfgFile.getName().getBaseName().endsWith(".bat"))) {
	            	// backup files in the same folder they're installed in
	            	backupFile(cfgFile, cfgFile.getParent());
	            	if (remove) {
	            		cfgFile.delete();
	            	}
	            }
	        }
    	} catch (Exception e) {
            getPrinter().printErrStatus("BackupAgentConfigurations", e.getMessage());
            return false;
		}
    	return true;
    }
    
    public void configureAgent() throws InstallException {

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        // For now, only one file to configure:
        configureServerXml();
    }

    protected void configureServerXml() throws InstallException {
        // --------------------------------------------------------------------
        // Configure server.xml
        // --------------------------------------------------------------------

        // We will use xupdate to alter catalina configuration : server.xml
        FileObject serverXml = null;

        try {

            serverXml = targetConfDir.resolveFile("server.xml");

            // Get a DOM document of the server.xml :
            Node serverXmlDom = readContentAsDom(serverXml);

            boolean modified = false;

            // Perfomr specific configurations
            if (configureRealm(serverXmlDom))
                modified = true;

            if (configureValve(serverXmlDom))
                modified = true;

            if (modified ) {

                // Backup Container configuration.  If we cannot perform a backup, do nothing
                if (!backupFile(serverXml, targetConfDir)) {
                    getPrinter().printActionWarnStatus("Configure", targetConfDir.getName().getFriendlyURI()+"/server.xml", "Must be done manually (Follow setup guide)");
                    return;
                }

                // Write modifications to file
                writeContentFromDom(serverXmlDom, serverXml);
                getPrinter().printActionOkStatus("Save", serverXml.getName().getBaseName(), serverXml.getName().getFriendlyURI());
                
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure container : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", targetConfDir.getName().getFriendlyURI()+"/server.xml", "Must be done manually (Follow setup guide)");
        }


    }

    protected boolean configureRealm(Node serverXmlDom) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();

        // Because we removed all realms, we always add JOSSO realm
        String usersClassNames = "org.josso.gateway.identity.service.BaseUserImpl";
        String roleClassNames = "org.josso.gateway.identity.service.BaseRoleImpl";

        // TODO : Just for now:
        String tcVersion = getPlatformId();
        if (tcVersion.equals("tc90")) tcVersion = "tc85";

        String realmClass = "org.josso." + tcVersion + ".agent.jaas.CatalinaJAASRealm"; // TODO : Be carefull with platform ID, this could not match the agent pacakge

        // Check if josso agent valve is already present

        XPathExpression findAgentRealm = xpath.compile("/Server/Service/Engine/Realm[@className=\""+realmClass+"\"]");
        NodeList agentRealms = (NodeList)  findAgentRealm.evaluate(serverXmlDom, XPathConstants.NODESET);

        // If we already have a JOSSO Valve, do nothing!
        if (agentRealms != null && agentRealms.getLength() > 0) {
            for (int i = 0 ; i < agentRealms.getLength() ; i++) {
                Node valve = agentRealms.item(i);
                Node valveClassNode = valve.getAttributes().getNamedItem("className");
                getPrinter().printActionWarnStatus("Configure", "JOSSO JASS Realm", "Already configured : " + (valveClassNode != null ? valveClassNode.getNodeValue() : "<unknown>"));
            }
            return false;
        }

        XPathExpression findRealmsExpr = xpath.compile("/Server/Service/Engine/Realm");
        NodeList realms = (NodeList) findRealmsExpr.evaluate(serverXmlDom, XPathConstants.NODESET);

        if (realms != null && realms.getLength() > 0) {
            String qryModifications ="\n\t<xupdate:remove select=\"/Server/Service/Engine/Realm\"/>";
            String qry = XUpdateUtil.XUPDATE_START + qryModifications + XUpdateUtil.XUPDATE_END;
            log.debug("XUPDATE QUERY: \n" + qry);
            XUpdateQuery xq = new XUpdateQueryImpl();
            xq.setQString(qry);
            xq.execute(serverXmlDom);

            for (int i = 0 ; i < realms.getLength() ; i++) {
                Node realmDom = realms.item(i);
                Node className = realmDom.getAttributes().getNamedItem("className");
                getPrinter().printActionOkStatus("Removed",  "Tomcat default Realm ",  (className != null ? className.getNodeValue() : "<unknown>"));
            }
        }

        String appendJossoRealmQryMod =
                "\n\t<xupdate:insert-before select=\"//Server/Service/Engine/Host[1]\" >" +
                "\n\t<xupdate:comment>" +
                        " ================================================== " +
                "</xupdate:comment>" +
                "\n\t<xupdate:comment>" +
                        "   JOSSO JAAS Realm, configuration automatially generated by JOSSO Installer " +
                "</xupdate:comment>" +
                "\n\t<xupdate:element name=\"Realm\">" +
                "<xupdate:attribute name=\"appName\">josso</xupdate:attribute>" +
                "<xupdate:attribute name=\"debug\">1</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"className\">"+realmClass+"</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"userClassNames\">"+usersClassNames+"</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"roleClassNames\">"+roleClassNames+"</xupdate:attribute>" +
                "</xupdate:element>" +
                "\n\t<xupdate:comment>" +
                        " ================================================== " +
                "</xupdate:comment>\n\t" +
                "</xupdate:insert-before>";

        String appendJossoRealmQryStr = XUpdateUtil.XUPDATE_START + appendJossoRealmQryMod + XUpdateUtil.XUPDATE_END;
        log.debug("XUPDATE QUERY: \n" + appendJossoRealmQryStr);

        XUpdateQuery appendJossoRealmQry = new XUpdateQueryImpl();
        appendJossoRealmQry.setQString(appendJossoRealmQryStr);
        appendJossoRealmQry.execute(serverXmlDom);

        getPrinter().printActionOkStatus("Configured", "JOSSO JAAS Realm ",  realmClass);

        return true;

    }

    protected boolean configureValve(Node serverXmlDom) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();

        // Check if josso agent valve is already present

        // TODO : Just for now:
        String tcVersion = getPlatformId();
        if (tcVersion.equals("tc90")) tcVersion = "tc85";

        String valveClass = "org.josso." + tcVersion + ".agent.SSOAgentValve"; // TODO : Be carefull with platform ID, this could not match the agent pacakge
        XPathExpression findAgentValve = xpath.compile("/Server/Service/Engine/Host/Valve[@className=\""+valveClass+"\"]");
        NodeList agentValves = (NodeList) findAgentValve.evaluate(serverXmlDom, XPathConstants.NODESET);

        // If we already have a JOSSO Valve, do nothing!
        if (agentValves != null && agentValves.getLength() > 0) {
            for (int i = 0 ; i < agentValves.getLength() ; i++) {
                Node valve = agentValves.item(i);
                Node valveClassNode = valve.getAttributes().getNamedItem("className");
                getPrinter().printActionWarnStatus("Configure", "JOSSO Agent Valve", "Already configured : " + (valveClassNode != null ? valveClassNode.getNodeValue() : "<unknown>"));
            }
            return false;
        }

        String appendJossoValveQryMod =
                "\n\t<xupdate:append select=\"//Server/Service/Engine/Host\" >" +
                "\n\t<xupdate:comment>" +
                        " ================================================== " +
                "</xupdate:comment>" +
                "\n\t<xupdate:comment>" +
                        "   JOSSO Agent Valve, configuration automatially generated by JOSSO Installer " +
                "</xupdate:comment>" +
                "\n\t<xupdate:element name=\"Valve\">" +
                "<xupdate:attribute name=\"appName\">josso</xupdate:attribute>" +
                "<xupdate:attribute name=\"debug\">1</xupdate:attribute>" +
                "\n\t\t<xupdate:attribute name=\"className\">"+valveClass+"</xupdate:attribute>" +
                "</xupdate:element>" +
                "\n\t<xupdate:comment>" +
                        " ================================================== " +
                "</xupdate:comment>\n\t" +
                "</xupdate:append>";

        String appendJossoValveQryStr = XUpdateUtil.XUPDATE_START + appendJossoValveQryMod + XUpdateUtil.XUPDATE_END;
        log.debug("XUPDATE QUERY: \n" + appendJossoValveQryStr);

        XUpdateQuery appendJossoValveQry = new XUpdateQueryImpl();
        appendJossoValveQry.setQString(appendJossoValveQryStr);
        appendJossoValveQry.execute(serverXmlDom);

        getPrinter().printActionOkStatus("Configured","JOSSO Agent Valve ",  valveClass);

        return true;

    }

}
