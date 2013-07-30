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

import org.apache.commons.vfs.Selectors;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.util.XUpdateUtil;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;

/**
 * @org.apache.xbean.XBean element="jboss-installer"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id$
 */
public class JBossInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(JBossInstaller.class);

    public JBossInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public JBossInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {
        super.validatePlatform();

        boolean valid = true;

        try {

        	String jbossWeb = "jbossweb.sar"; // jboss 5 and 6 by default

        	if (getPlatformVersion().startsWith("4.2"))
                jbossWeb = "jboss-web.deployer";
            if (getPlatformVersion().startsWith("4.0"))
                jbossWeb = "jbossweb-tomcat55.sar";
            else if (getPlatformVersion().startsWith("3.2"))
                jbossWeb = "jbossweb-tomcat50.sar";

            FileObject serverXml = targetDeployDir.resolveFile(jbossWeb + "/server.xml");
            if (serverXml == null || !serverXml.exists() || !serverXml.getType().getName().equals(FileType.FILE.getName())) {
                valid = false;
                getPrinter().printErrStatus("JBossHome", "Cannot find server.xml");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

            FileObject loginConfigXml = targetConfDir.resolveFile("login-config.xml");
            if (loginConfigXml == null || !loginConfigXml.exists() || !loginConfigXml.getType().getName().equals(FileType.FILE.getName())) {
                valid = false;
                getPrinter().printErrStatus("JBossHome", "Cannot find login-config.xml");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("JBossHome", e.getMessage());
            throw new InstallException (e.getMessage(), e);
        }

        getPrinter().printOkStatus("JBossHome");
        // TODO : Validate version ?
    }

    @Override
    public void init() throws InstallException {

        log.debug("Init JBoss installer");

        String instance = getProperty("jbossInstance");

        if (instance == null)
            throw new InstallException("JBoss instance name not specified");

        log.debug("Using JBoss intance : " + instance);

        registerVarResolution("instance", instance);

        // Initialize installer
        super.init();
    }

    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {

        // TODO : This should be part of the install command, maybe based on 'features' definitions.

        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetJOSSOSharedLibDir, replace);
                
            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                                   artifact.getClassifier() !=  null && 
                                   artifact.getClassifier().equals("axis") &&
                    !getTargetPlatform().getVersion().startsWith("6.")) {
                // JBoss 6 uses jaxws
                            installFile(srcFile, this.targetJOSSOLibDir, replace);
            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                    artifact.getClassifier() !=  null &&
                    artifact.getClassifier().equals("axis") &&
                    !getTargetPlatform().getVersion().startsWith("6.")) {
                // JBoss 6 uses jaxws
                installFile(srcFile, this.targetJOSSOLibDir, replace);
            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                    artifact.getClassifier() !=  null &&
                    artifact.getClassifier().equals("jaxws") &&
                    getTargetPlatform().getVersion().startsWith("6.")) {
                // JBoss 6 uses jaxws
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jboss5-agent") &&
                    (getTargetPlatform().getVersion().startsWith("5.") ||
                    getTargetPlatform().getVersion().startsWith("6."))) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jaspi-agent") &&
                    (getTargetPlatform().getVersion().startsWith("5.") ||
                    getTargetPlatform().getVersion().startsWith("6."))) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jboss42-agent") &&
                    getTargetPlatform().getVersion().startsWith("4.2")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jboss40-agent") &&
                    getTargetPlatform().getVersion().startsWith("4.0")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat55-agent") &&
                    getTargetPlatform().getVersion().startsWith("4.")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-tomcat50-agent") &&
                    getTargetPlatform().getVersion().startsWith("3.2")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jboss32-agent") &&
                    getTargetPlatform().getVersion().startsWith("3.2")) {
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

        // do not install commons logging in jboss 6.x since it conflicts with the slf4j implementation
        if ((getTargetPlatform().getVersion().startsWith("6") &&
                artifact.getBaseName().startsWith("commons-logging")))
            return;


        if (getTargetPlatform().getVersion().startsWith("6")) {
            // In JBoss 6, we only need spring, CXF and all the rest is already in JBoss
            if (!(artifact.getBaseName().startsWith("spring-") ||
                  artifact.getBaseName().startsWith("xbean-spring-"))) {
                return;
            }
        }

        // do not install WSDL4J in jboss 4.x and 5.x platforms !
        if ((getTargetPlatform().getVersion().startsWith("4") ||
             getTargetPlatform().getVersion().startsWith("5")) &&
             artifact.getBaseName().startsWith("axis-wsdl"))
            return;


        if (artifact.getBaseName().startsWith("log4j") ||
        		artifact.getBaseName().startsWith("spring-2.0"))
            return;
        
        if (artifact.getBaseName().startsWith("slf4j"))
            return;
        
        if (artifact.getBaseName().startsWith("jcl-over-slf4j"))
            return;
        
        if (artifact.getBaseName().startsWith("logback"))
            return;

        super.install3rdPartyComponent(artifact, replace);
    }

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

                    if (getTargetPlatform().getVersion().startsWith("6")) {
                        // Under JBoss 6 remove commons logging JAR files which conflict with slf4j replacement
                        FileObject webInfLibFolder = targetDeployDir.resolveFile(newName + "/WEB-INF/lib");

                        boolean exists = webInfLibFolder.exists();

                        if (exists) {
                            FileObject[] sharedLibs = webInfLibFolder.getChildren();
                            for (int i = 0 ; i < sharedLibs.length; i ++) {
                                FileObject jarFile = sharedLibs[i];

                                if (!jarFile.getType().getName().equals(FileType.FILE.getName())) {
                                    // ignore folders
                                    continue;
                                }
                                if (jarFile.getName().getBaseName().startsWith("commons-logging")
                                        && jarFile.getName().getBaseName().endsWith(".jar")) {
                                    jarFile.delete();
                                }
                            }
                        }
                    }
                } else {
                    installFile(srcFile, this.targetDeployDir, replace);
                }
                return;
            }

            if ((getTargetPlatform().getVersion().startsWith("5") || getTargetPlatform().getVersion().startsWith("6")) &&
            		artifact.getType().equals("ear") && artifact.getBaseName().startsWith("josso-partner-jboss5")) {
            	installFile(srcFile, this.targetDeployDir, replace);
                return;
            } else if (!(getTargetPlatform().getVersion().startsWith("5") || getTargetPlatform().getVersion().startsWith("6")) &&
            		artifact.getType().equals("ear") && artifact.getBaseName().startsWith("josso-partner-jboss-app")) {
            	installFile(srcFile, this.targetDeployDir, replace);
                return;
            }
            
            log.debug("Skipping partner application : " + srcFile.getName().getFriendlyURI());

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public boolean removeOldComponents(boolean backup) {
    	 if (getPlatformVersion().startsWith("3.2")) {
    		 // jboss 3.2 loads classes from backup jar files
    		 backup = false;
    	 }
    	return super.removeOldComponents(backup);
    }
    
    public void configureAgent() throws InstallException {

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        configureServerXml();
        configureLoginConfigXml();
    }

    protected void configureServerXml() throws InstallException {

        // --------------------------------------------------------------------
        // Configure server.xml
        // --------------------------------------------------------------------
        // We will use xupdate to alter catalina configuration : server.xml
        FileObject serverXml = null;
        try {

        	String jbossWeb = "jbossweb.sar"; // jboss 5 by default
        	if (getPlatformVersion().startsWith("4.2"))
                jbossWeb = "jboss-web.deployer";
        	else if (getPlatformVersion().startsWith("4.0"))
                jbossWeb = "jbossweb-tomcat55.sar";
            else if (getPlatformVersion().startsWith("3.2"))
                jbossWeb = "jbossweb-tomcat50.sar";
            
            FileObject targetJBossWebDir = targetDeployDir.resolveFile(jbossWeb);
            serverXml = targetJBossWebDir .resolveFile("server.xml");

            // Get a DOM document of the server.xml :
            Node serverXmlDom = readContentAsDom(serverXml);

            boolean modified = false;

            // Perfomr specific configurations
            if (configureRealm(serverXmlDom))
                modified = true;

            if (configureValve(serverXmlDom))
                modified = true;

            if (modified) {
                // Backup Container configuration.  If we cannot perform a backup, do nothing
                if (!backupFile(serverXml, targetJBossWebDir )) {
                    getPrinter().printActionWarnStatus("Configure", targetJBossWebDir .getName().getFriendlyURI()+"/server.xml", "Must be done manually (Follow setup guide)");
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

    protected void configureLoginConfigXml() throws InstallException {
        // --------------------------------------------------------------------
        // Configure login-config.xml
        // --------------------------------------------------------------------

        // We will use xupdate to alter catalina configuration : server.xml
        FileObject loginConfigXml= null;

        try {

            // Get a DOM document of the server.xml :
            loginConfigXml = targetConfDir.resolveFile("login-config.xml");
            Document loginConfigXmlDom = readContentAsDom(loginConfigXml);

            boolean modified = false;
            // Perfomr specific configurations
            if (configureLoginConfig(loginConfigXmlDom))
                modified = true;

            // Write modifications to file
            if (modified) {
                if (!backupFile(loginConfigXml, targetConfDir)) {
                    getPrinter().printActionWarnStatus("Configure", loginConfigXml.getName().getFriendlyURI(), "Must be done manually (Follow setup guide)");
                    return;
                }

                writeContentFromDom(loginConfigXmlDom, loginConfigXml);
                getPrinter().printActionOkStatus("Save", loginConfigXml.getName().getBaseName(), loginConfigXml.getName().getFriendlyURI());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure container : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", targetConfDir.getName().getFriendlyURI()+"/login-config.xml", "Must be done manually (Follow setup guide)");
        }


    }

    protected boolean configureRealm(Node serverXmlDom) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();

        // Because we removed all realms, we always add JOSSO realm
        String usersClassNames = "org.josso.gateway.identity.service.BaseUserImpl";
        String roleClassNames = "org.josso.gateway.identity.service.BaseRoleImpl";

        String realmClass = "org.josso.jb5.agent.JBossCatalinaRealm";

        if (getPlatformVersion().startsWith("4.2")) {
        	realmClass = "org.josso.jb42.agent.JBossCatalinaRealm";
        } else if (getPlatformVersion().startsWith("4.0")) {
            realmClass = "org.josso.jb4.agent.JBossCatalinaRealm";
        } else if (getPlatformVersion().startsWith("3.2")) {
            realmClass = "org.josso.jb32.agent.JBossCatalinaRealm";
        }


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
            String qryModifications ="\n<xupdate:remove select=\"/Server/Service/Engine/Realm\"/>";
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
                        " ================================================== \n" +
                "</xupdate:comment>" +
                "\n\t<xupdate:comment>" +
                        "   JOSSO JAAS Realm, configuration automatially generated by JOSSO Installer\n" +
                "</xupdate:comment>" +
                "\n\t<xupdate:element name=\"Realm\">" +
                "<xupdate:attribute name=\"appName\">josso</xupdate:attribute>" +
                "<xupdate:attribute name=\"debug\">1</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"className\">"+realmClass+"</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"userClassNames\">"+usersClassNames+"</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"roleClassNames\">"+roleClassNames+"</xupdate:attribute>" +
                "</xupdate:element>" +
                "\n\t<xupdate:comment>" +
                        " ================================================== \n" +
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

    	if (getPlatformVersion().startsWith("5") || getPlatformVersion().startsWith("6") ) {
    		return false;
    	}
    	
        XPath xpath = XPathFactory.newInstance().newXPath();

        String valveClass = "org.josso.tc55.agent.SSOAgentValve";

        if (getPlatformVersion().startsWith("4.0"))
            valveClass = "org.josso.tc55.agent.SSOAgentValve";
        else if (getPlatformVersion().startsWith("3.2"))
            valveClass = "org.josso.tc50.agent.SSOAgentValve";


        // Check if josso agent valve is already present

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
                        " ================================================== \n" +
                "</xupdate:comment>" +
                "\n\t<xupdate:comment>" +
                        "   JOSSO Agent Valve, configuration automatially generated by JOSSO Installer\n" +
                "</xupdate:comment>" +
                "\n\t<xupdate:element name=\"Valve\">" +
                "<xupdate:attribute name=\"appName\">josso</xupdate:attribute>" +
                "<xupdate:attribute name=\"debug\">1</xupdate:attribute>" +
                "\n\t<xupdate:attribute name=\"className\">"+valveClass+"</xupdate:attribute>" +
                "</xupdate:element>" +
                "\n\t<xupdate:comment>" +
                        " ================================================== \n" +
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


    protected boolean configureLoginConfig(Node loginConfigXmlDom) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Check if josso agent valve is already present

        XPathExpression findAppPolicy = xpath.compile("/policy/application-policy[@name=\"josso\"]");
        NodeList jossoAppPolicies = (NodeList) findAppPolicy.evaluate(loginConfigXmlDom, XPathConstants.NODESET);

        // If we already have a JOSSO Valve, do nothing!
        if (jossoAppPolicies != null && jossoAppPolicies.getLength() > 0) {
            for (int i = 0 ; i < jossoAppPolicies.getLength() ; i++) {
                Node jossoAppPolicy = jossoAppPolicies.item(i);
                Node jossoAppPolicyName = jossoAppPolicy.getAttributes().getNamedItem("name");
                getPrinter().printActionWarnStatus("Configure", "JOSSO JAAS Login Module", "Already configured : " + (jossoAppPolicyName != null ? jossoAppPolicyName.getNodeValue() : "<unknown>"));
            }
            return false;
        }

        String appendJossoAppPolicyQryMod = null;
        
        String loginModuleClass = "org.josso.jb42.agent.JBossSSOGatewayLoginModule";
        if (getPlatformVersion().startsWith("4.0"))
            loginModuleClass = "org.josso.jb4.agent.JBossSSOGatewayLoginModule";
        else if (getPlatformVersion().startsWith("3.2"))
            loginModuleClass = "org.josso.jb32.agent.JBossSSOGatewayLoginModule";
        
        if (getPlatformVersion().startsWith("5") || getPlatformVersion().startsWith("6")) {
        	// Append a new JAAS Configuration ...
	        appendJossoAppPolicyQryMod =
	                "\n<xupdate:append select=\"//policy\" >" +
	                "\n\n\n    <xupdate:comment>" +
	                        " ================================================== " +
	                "</xupdate:comment>" +
	                "\n    <xupdate:comment>" +
	                        "   JOSSO JAAS Login Configuration, automatially generated by JOSSO Installer" +
	                "</xupdate:comment>" +
	                "\n    <xupdate:element name=\"application-policy\">" +
	                "    <xupdate:attribute name=\"name\">josso</xupdate:attribute>" +
	                        "        <authentication-jaspi>\n" +
				            "            <login-module-stack name=\"lm-stack\">\n" +
				            "				<!--login-module code=\"org.josso.jaspi.agent.SSOGatewayLoginModule\" flag=\"required\"-->\n" +
				            "				<!--JBossSSOGatewayLoginModule just adds support for unauthenticatedIdentity option-->\n" +
					        "				<login-module code=\"org.josso.jb5.agent.JBossSSOGatewayLoginModule\" flag=\"required\">\n" +
					        "					<module-option name=\"debug\">true</module-option>\n" +
					        "				</login-module>\n" +
					        "			</login-module-stack>\n" +
	                        "		 	<auth-module code=\"org.josso.jaspi.agent.JASPISSOAuthModule\" />\n" +
	                        "        </authentication-jaspi>\n" +
	                "    </xupdate:element>" +
	                "\n    <xupdate:comment>" +
	                        " ================================================== \n" +
	                "</xupdate:comment>\n\n\n\n" +
	                "</xupdate:append>";
        } else {
	        // Append a new JAAS Configuration ...
	        appendJossoAppPolicyQryMod =
	                "\n<xupdate:append select=\"//policy\" >" +
	                "\n\n\n    <xupdate:comment>" +
	                        " ================================================== " +
	                "</xupdate:comment>" +
	                "\n    <xupdate:comment>" +
	                        "   JOSSO JAAS Login Configuration, automatially generated by JOSSO Installer" +
	                "</xupdate:comment>" +
	                "\n    <xupdate:element name=\"application-policy\">" +
	                "    <xupdate:attribute name=\"name\">josso</xupdate:attribute>" +
	                        "        <authentication>\n" +
	                        "            <login-module code = \""+loginModuleClass+"\" flag = \"required\">\n" +
	                        "                <module-option name=\"debug\">true</module-option>\n" +
	                        "            </login-module>\n" +
	                        "        </authentication>\n" +
	                "    </xupdate:element>" +
	                "\n    <xupdate:comment>" +
	                        " ================================================== \n" +
	                "</xupdate:comment>\n\n\n\n" +
	                "</xupdate:append>";
        }

        String appendJossoAppPolicyQryStr = XUpdateUtil.XUPDATE_START + appendJossoAppPolicyQryMod + XUpdateUtil.XUPDATE_END;
        log.debug("XUPDATE QUERY: \n" + appendJossoAppPolicyQryStr);

        XUpdateQuery appendJossoValveQry = new XUpdateQueryImpl();
        appendJossoValveQry.setQString(appendJossoAppPolicyQryStr);
        appendJossoValveQry.execute(loginConfigXmlDom);

        if (!(getPlatformVersion().startsWith("5") || getPlatformVersion().startsWith("6")) ) {
        	getPrinter().printActionOkStatus("Configured","JOSSO JAAS Login Module ",  loginModuleClass);
        }

        return true;

    }
}
