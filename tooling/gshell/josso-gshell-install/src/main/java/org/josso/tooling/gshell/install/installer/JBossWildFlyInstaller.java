package org.josso.tooling.gshell.install.installer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.util.XUpdateUtil;
import org.w3c.dom.*;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;

import javax.xml.xpath.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * WildFly (JBoss 7 community) installer
 *
 * @org.apache.xbean.XBean element="jboss-wildfly-installer"
 *
 * @author: sgonzalez@atriocore.com
 * @date: 8/5/13
 */
public class JBossWildFlyInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(JBossWildFlyInstaller.class);

    protected boolean installSpring = false;

    public JBossWildFlyInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public JBossWildFlyInstaller() {
        super();
    }


    @Override
    public void init() throws InstallException {

        log.debug("Init JBoss 7 (WildFly) installer");

        String instance = getProperty("jbossInstance");

        if (instance == null)
            throw new InstallException("JBoss configuration name not specified");

        log.debug("Using JBoss configuration : " + instance);

        registerVarResolution("instance", instance);

        // Initialize installer
        super.init();

        try {
            this.installSpring = !springModuleExists();
        } catch (FileSystemException e) {
            throw new InstallException(e);
        }
    }

    @Override
    public void validatePlatform() throws InstallException {

        String instance = getProperty("jbossInstance");
        boolean valid = true;

        try {

            // Create JOSSO Module folders
            if (!targetDir.exists() || !targetDir.getType().getName().equals(FileType.FOLDER.getName())) {
                getPrinter().printErrStatus("Target", "folder does not exist or is not a directory:" + targetDir.getName().getFriendlyURI());
                valid = false;
            }

            // Create JOSSO Module folders

            if (targetJOSSOConfDir.exists()) {
                if (!targetJOSSOConfDir.getType().getName().equals(FileType.FOLDER.getName())) {
                    getPrinter().printErrStatus("Target", "configuration property is not a directory:" + targetJOSSOConfDir.getName().getFriendlyURI());
                    valid = false;
                }
            } else {
                targetJOSSOConfDir.createFolder();
                getPrinter().printActionOkStatus("Target", targetJOSSOConfDir.getName().getFriendlyURI(), "Created configuration property");
            }

            super.validatePlatform();

            FileObject jbossConfigurationFile = targetConfDir.resolveFile(instance + ".xml");

            if (!jbossConfigurationFile.exists() || !jbossConfigurationFile.getType().getName().equals(FileType.FILE.getName())) {
                getPrinter().printErrStatus("Target", "configuration file not found:" + jbossConfigurationFile.getName().getFriendlyURI());
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
    public void configureAgent() throws InstallException {

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");


        configureJOSSOModule();

        configureJBossASServerModule();
        configureJBossASSecurityModule();
        configurePicketBoxModule();

        configureEEModules();
        configureSecurityDomain();

    }


    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {

        // TODO : Create module configuration file

        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetJOSSOSharedLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                    artifact.getClassifier() !=  null &&
                    artifact.getClassifier().equals("jaxws") &&
                    getTargetPlatform().getVersion().startsWith("7.")) {
                // JBoss 6 uses jaxws
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jboss7-agent") &&
                    getTargetPlatform().getVersion().startsWith("7.")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jaspi-agent") &&
                    getTargetPlatform().getVersion().startsWith("7.")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jaspi-agent") &&
                    getTargetPlatform().getVersion().startsWith("7.")) {
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

        // All we need is commons logging
        if ((getTargetPlatform().getVersion().startsWith("7") &&
             artifact.getBaseName().startsWith("commons-logging"))) {

            try {
                FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
                removeOldJar(srcFile.getName().getBaseName(), this.targetJOSSOLibDir, false);
                installFile(srcFile, this.targetJOSSOLibDir, replace);
            } catch (IOException e) {
                throw new InstallException(e.getMessage(), e);
            }
        }

        if ((getTargetPlatform().getVersion().startsWith("7") &&
                artifact.getBaseName().startsWith("commons-codec"))) {

            try {
                FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
                removeOldJar(srcFile.getName().getBaseName(), this.targetJOSSOLibDir, false);
                installFile(srcFile, this.targetJOSSOLibDir, replace);
            } catch (IOException e) {
                throw new InstallException(e.getMessage(), e);
            }
        }


        if (getTargetPlatform().getVersion().startsWith("7") &&
                (artifact.getBaseName().startsWith("spring-") || artifact.getBaseName().startsWith("xbean-spring"))
                && installSpring) {

            // Avoid installing spring 2.0 artifacts
            if (artifact.getBaseName().startsWith("spring-2.0"))
                return;

            try {

                if (!springModuleExists())
                    createSpringModuleLayout();

                FileObject springLib = this.targetLibDir.resolveFile("org/springframework/spring/main");

                FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
                removeOldJar(srcFile.getName().getBaseName(), springLib, false);
                installFile(srcFile, springLib, replace);

                updateSpringModuleResource(srcFile.getName().getBaseName());


            } catch (Exception e) {
                throw new InstallException(e.getMessage(), e);
            }

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

            if ((getTargetPlatform().getVersion().startsWith("7")) &&
                    artifact.getType().equals("ear") && artifact.getBaseName().startsWith("josso-partner-jboss7")) {
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


    protected boolean configureJOSSOModule()  {

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        // Create module file
        String moduleCfgStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\n" +
                "<module xmlns=\"urn:jboss:module:1.1\" name=\"org.josso\">\n" +
                "    <resources>\n" +
                "       <resource-root path=\"config\" />\n" +
                "    </resources>\n" +
                "    <dependencies>\n" +
                "        <module name=\"javax.security.auth.message.api\" />\n" +
                "        <module name=\"org.jboss.logging\"/>\n" +
                "        <module name=\"javax.servlet.api\"/>\n" +
                "        <module name=\"org.picketbox\"/>\n" +
                "        <module name=\"org.jboss.as.web\"/>\n" +
                "        <module name=\"org.apache.cxf\"/>\n" +
                "\n" +
                "        <module name=\"javax.api\"/>\n" +
                "        <module name=\"javax.jws.api\"/>\n" +
                "        <module name=\"javax.xml.ws.api\"/>\n" +
                "        <module name=\"javax.xml.ws.api\"/>\n" +
                "        <module name=\"javax.xml.rpc.api\" />\n" +
                "        <module name=\"javax.xml.bind.api\"/>\n" +
                "        <module name=\"javax.wsdl4j.api\"/>\n" +
                "        <module name=\"org.springframework.spring\"/>\n" +
                "    </dependencies>\n" +
                "</module>";


        try {

            // Create module base configuration:
            FileObject moduleCfg = targetJOSSOLibDir.resolveFile("module.xml");
            if (moduleCfg.exists()) {
                backupFile(moduleCfg, targetJOSSOLibDir);
            }

            writeContentFromString(moduleCfgStr, moduleCfg);

            Document moduleCfgDom = readContentAsDom(moduleCfg, false);

            FileObject[] children = targetJOSSOLibDir.getChildren();
            for (int i = 0; i < children.length; i++) {
                FileObject child = children[i];
                if (child.getName().getExtension().equals("jar")) {
                    String resourceName = child.getName().getBaseName();

                    String appendJossoResourceQryStr =
                            "\n\t<xupdate:append select=\"//module/resources\" >" +
                                    "\n\t<xupdate:element name=\"resource-root\" namespace=\"urn:jboss:module:1.1\">" +
                                    "<xupdate:attribute name=\"path\">"+resourceName+"</xupdate:attribute>" +
                                    "</xupdate:element>" +
                                    "\n\t</xupdate:append>";

                    String appendJossoResourceQryModStr = XUpdateUtil.XUPDATE_START + appendJossoResourceQryStr + XUpdateUtil.XUPDATE_END;
                    log.debug("XUPDATE QUERY: \n" + appendJossoResourceQryModStr);

                    XUpdateQuery appendJossoResourceQry = new XUpdateQueryImpl();
                    appendJossoResourceQry.setQString(appendJossoResourceQryModStr);
                    appendJossoResourceQry.execute(moduleCfgDom);

                    getPrinter().printActionOkStatus("Configured","JOSSO Module resource",  resourceName);

                }
            }

            writeContentFromDom(moduleCfgDom, moduleCfg);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure JOSSO Module : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", targetJOSSOLibDir.getName().getFriendlyURI()+"", "Must be done manually (Follow setup guide)");
            return false;
        }


    }

    protected boolean springModuleExists() throws FileSystemException {
        FileObject springModule = targetLibDir.resolveFile("org/springframework/spring/main");
        return springModule.exists();
    }

    protected void createSpringModuleLayout() throws InstallException {

        try {

            FileObject springModule = targetLibDir.resolveFile("org/springframework/spring/main");

            if (springModule.exists()) {
                // Keep current spring module
                getPrinter().printActionOkStatus("Installing", "Spring Module", "Already present, skipping");
                return;
            }

            springModule.createFolder();

            FileObject springModuleCfg = springModule.resolveFile("module.xml");
            springModuleCfg.createFile();

            String springModuleCfgStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "\n" +
                    "<module xmlns=\"urn:jboss:module:1.1\" name=\"org.springframework.spring\">\n" +
                    "    <resources>\n" +
                    "\n" +
                    "        <!-- 3.0.5.RELEASE Version -->\n" +
                    "<!--\n" +
                    "        <resource-root path=\"com.springsource.org.aopalliance-1.0.0.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.aop-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.asm-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.beans-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.context-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.core-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.expression-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.jdbc-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.jms-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.transaction-3.0.5.RELEASE.jar\"/>\n" +
                    "        <resource-root path=\"org.springframework.web-3.0.5.RELEASE.jar\"/>\n" +
                    "-->\n" +
                    "    </resources>\n" +
                    "    <dependencies>\n" +
                    "        <module name=\"javax.api\"/>\n" +
                    "        <module name=\"javax.jms.api\"/>\n" +
                    "        <module name=\"javax.annotation.api\"/>\n" +
                    "        <module name=\"org.apache.commons.logging\"/>\n" +
                    "    </dependencies>\n" +
                    "</module>\n";

            OutputStream os = springModuleCfg.getContent().getOutputStream(false);
            IOUtils.write(springModuleCfgStr.getBytes(), os);
            IOUtils.closeQuietly(os);
            springModuleCfg.close();

            getPrinter().printActionOkStatus("Configured", "Spring Module layout", springModule.getName().getFriendlyURI());

        } catch (IOException e) {
            throw new InstallException(e);
        }

    }

    protected void updateSpringModuleResource(String resource) throws Exception {

        FileObject springModule = targetLibDir.resolveFile("org/springframework/spring/main/module.xml");
        Node springModuleDom = readContentAsDom(springModule, false);

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        String appendSpringResourceQryMod =
                "\n\t<xupdate:append select=\"//module/resources\" >" +
                        "\n\t<xupdate:element name=\"resource-root\" namespace=\"urn:jboss:module:1.1\">" +
                        "<xupdate:attribute name=\"path\">"+resource+"</xupdate:attribute>" +
                        "</xupdate:element>" +
                        "\n\t</xupdate:append>";

        String appendSpringResourceQryModStr = XUpdateUtil.XUPDATE_START + appendSpringResourceQryMod + XUpdateUtil.XUPDATE_END;
        log.debug("XUPDATE QUERY: \n" + appendSpringResourceQryModStr);

        XUpdateQuery appendSpringResourceQry = new XUpdateQueryImpl();
        appendSpringResourceQry.setQString(appendSpringResourceQryModStr);
        appendSpringResourceQry.execute(springModuleDom);

        writeContentFromDom(springModuleDom, springModule);

        getPrinter().printActionOkStatus("Configured","Spring Module resource",  resource);

    }

    protected void configureJBossASServerModule() {

        // Add josso dependency to org.jboss.as.server (main)
        FileObject jbossAsCfg = null;
        try {
            jbossAsCfg = targetLibDir.resolveFile("org/jboss/as/server/main/module.xml");
            if (jbossAsCfg.exists()) {
                if (addDependency(jbossAsCfg, "org.josso")) {
                    getPrinter().printActionOkStatus("Configure", jbossAsCfg.getName().getFriendlyURI(), "Updated");
                }
            } else {
                getPrinter().printActionErrStatus("Configure", jbossAsCfg.getName().getFriendlyURI(), "does not exists");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure JBoss AS Server Module : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", jbossAsCfg != null ? jbossAsCfg.getName().getFriendlyURI() : "JBoss AS Server module", "Must be done manually (Follow setup guide)");
        }

    }

    protected void  configureJBossASSecurityModule() {

        //  Add/Update josso dependency to org.jboss.as.security (main)
        FileObject jbossSecCfg = null;
        try {
            jbossSecCfg = targetLibDir.resolveFile("org/jboss/as/security/main/module.xml");
            if (jbossSecCfg.exists()) {
                if (addDependency(jbossSecCfg, "org.josso")) {
                    getPrinter().printActionOkStatus("Configure", jbossSecCfg.getName().getFriendlyURI(), "Updated");
                }
            } else {
                getPrinter().printActionErrStatus("Configure", jbossSecCfg.getName().getFriendlyURI(), "does not exists");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure JBoss AS Security Module : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", jbossSecCfg != null ? jbossSecCfg.getName().getFriendlyURI() : "JBoss AS Security module", "Must be done manually (Follow setup guide)");
        }




    }

    protected void  configurePicketBoxModule() {

        //  Add/Update josso dependency to org.picketbox (main)
        FileObject jbossSecCfg = null;
        try {
            jbossSecCfg = targetLibDir.resolveFile("org/picketbox/main/module.xml");
            if (jbossSecCfg.exists()) {
                if (addDependency(jbossSecCfg, "org.josso")) {
                    getPrinter().printActionOkStatus("Configure", jbossSecCfg.getName().getFriendlyURI(), "Updated");
                }
            } else {
                getPrinter().printActionErrStatus("Configure", jbossSecCfg.getName().getFriendlyURI(), "does not exists");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure JBoss AS Security Module : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", jbossSecCfg != null ? jbossSecCfg.getName().getFriendlyURI() : "JBoss AS Security module", "Must be done manually (Follow setup guide)");
        }

    }


    protected boolean addDependency(FileObject moduleCfg, String dependency) throws Exception {

        Node moduleAsDom = readContentAsDom(moduleCfg, false);

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        XPath xpath = XPathFactory.newInstance().newXPath();

        String xpathExp = "/module/dependencies/module[@name=\"" + dependency + "\"]";
        XPathExpression findDep = xpath.compile(xpathExp);
        NodeList depModules = (NodeList)  findDep.evaluate(moduleAsDom, XPathConstants.NODESET);

        // If we already have a JOSSO Valve, do nothing!
        if (depModules != null && depModules.getLength() > 0) {
            return false;
        } else {

            String addDependencyQryStr =
                    "\n\t<xupdate:append select=\"//module/dependencies\" >" +
                            "\n\t\t<xupdate:element name=\"module\" namespace=\"urn:jboss:module:1.1\">" +
                            "<xupdate:attribute name=\"name\">"+dependency+"</xupdate:attribute>" +
                            "</xupdate:element>" +
                            "\n</xupdate:append>";

            String addDependencyQryStrMod = XUpdateUtil.XUPDATE_START + addDependencyQryStr + XUpdateUtil.XUPDATE_END;
            log.debug("XUPDATE QUERY: \n" + addDependencyQryStrMod);

            XUpdateQuery addDependencyQry = new XUpdateQueryImpl();
            addDependencyQry.setQString(addDependencyQryStrMod);
            addDependencyQry.execute(moduleAsDom);

            backupFile(moduleCfg, moduleCfg.getParent());
            writeContentFromDom(moduleAsDom, moduleCfg);

            return true;

        }


    }

    protected boolean configureEEModules() {

        String instance = getProperty("jbossInstance");
        FileObject jbossCfg = null;
        try {
            jbossCfg = targetConfDir.resolveFile(instance + ".xml");
            Document jbossCfgDom = readContentAsDom(jbossCfg, false);

            // Configure EE domain and add org.josso as global module (so all EE apps have a dependency to it)

            // Setup XUpdate :
            System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                    "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

            XPath xpath = XPathFactory.newInstance().newXPath();

            //String findSubsystems = "/server/profile/subsystem/global-modules/module[@name=\"org.josso\"]";
            String findSubsystems = "/server/profile/subsystem";

            XPathExpression findSubsystemsExp = xpath.compile(findSubsystems);
            NodeList subSystems = (NodeList)  findSubsystemsExp.evaluate(jbossCfgDom, XPathConstants.NODESET);
            // Look only for specific Namespace

            for (int i = 0; i < subSystems.getLength() ; i++) {
                Node subsystem = subSystems.item(i);

                NamedNodeMap attrs = subsystem.getAttributes();
                Node attr = attrs.getNamedItem("xmlns");

                String ns = attr.getNodeValue();

                if (ns.equals("urn:jboss:domain:ee:1.0")) {
                    String findJossoGlobalModule = "global-modules/module[@name=\"org.josso\"]";
                    XPathExpression findJossoGlobalModuleExp = xpath.compile(findJossoGlobalModule);
                    NodeList jossoGlobalModule = (NodeList)  findJossoGlobalModuleExp.evaluate(subsystem, XPathConstants.NODESET);

                    if (jossoGlobalModule.getLength() < 1) {
                        // JBoss XML File is way to complex to work with xupdate ...

                        // GLobal modules
                        Node gm = jbossCfgDom.createElement("global-modules");

                        // Module
                        gm.appendChild(jbossCfgDom.createTextNode("\n\t\t\t"));
                        Element module = jbossCfgDom.createElement("module");
                        module.setAttribute("name", "org.josso");
                        gm.appendChild(module);
                        gm.appendChild(jbossCfgDom.createTextNode("\n\t\t"));

                        // Global modules added to EE subsystem
                        subsystem.appendChild(jbossCfgDom.createTextNode("\n\t\t"));
                        subsystem.appendChild(jbossCfgDom.createComment(" Added by JOSSO "));
                        subsystem.appendChild(jbossCfgDom.createTextNode("\n\t\t"));
                        subsystem.appendChild(gm);
                        subsystem.appendChild(jbossCfgDom.createTextNode("\n\t"));

                        backupFile(jbossCfg, targetConfDir);
                        writeContentFromDom(jbossCfgDom, jbossCfg);

                        getPrinter().printActionOkStatus("Configured", "JOSSO EE Global Module", "name=org.josso");

                        return true;
                    }

                    break;
                }
            }

            getPrinter().printActionWarnStatus("Configure", "JOSSO EE Global Module", "Already configured");

            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure JBoss AS instance : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", jbossCfg != null ? jbossCfg.getName().getFriendlyURI() : "JBoss Configuration", "Must be done manually (Follow setup guide)");
            return false;
        }
    }

    protected boolean configureSecurityDomain() {

        String instance = getProperty("jbossInstance");
        FileObject jbossCfg = null;
        try {
            jbossCfg = targetConfDir.resolveFile(instance + ".xml");
            Document jbossCfgDom = readContentAsDom(jbossCfg, false);

            // Setup XUpdate :
            System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                    "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

            XPath xpath = XPathFactory.newInstance().newXPath();

            String findJossoSecDomain = "/server/profile/subsystem/security-domains/security-domain[@name=\"josso\"]";

            XPathExpression findJossoSecDomainExp = xpath.compile(findJossoSecDomain);
            NodeList jossoSecDomains = (NodeList)  findJossoSecDomainExp.evaluate(jbossCfgDom, XPathConstants.NODESET);
            if (jossoSecDomains.getLength() > 0) {
                // Nothing to do
                getPrinter().printActionWarnStatus("Configure", "JOSSO Security Domain", "Already configured");
                return false;
            }

            String addJossoSecDomainQry =
                    "\n\t<xupdate:append select=\"//server/profile/subsystem/security-domains\" >" +
                            "\n\t\t\t\t<xupdate:element name=\"security-domain\" namespace=\"urn:jboss:domain:security:1.1\">" +
                            "<xupdate:attribute name=\"name\">josso</xupdate:attribute>\n" +
                            "                    <authentication-jaspi>\n" +
                            "                        <login-module-stack name=\"josso-stack\">\n" +
                            "                            <login-module code=\"org.josso.jb7.agent.JBossSSOGatewayLoginModule\" flag=\"required\">\n" +
                            "                                <module-option name=\"debug\" value=\"true\"/>\n" +
                            "                            </login-module>\n" +
                            "                        </login-module-stack>\n" +
                            "                        <auth-module code=\"org.josso.jaspi.agent.JASPISSOAuthModule\" login-module-stack-ref=\"josso-stack\"/>\n" +
                            "                    </authentication-jaspi>\n" +
                            "\n\t\t</xupdate:element>" +
                            "\n\t</xupdate:append>";

            String addJossoSecDomainQryMod = XUpdateUtil.XUPDATE_START + addJossoSecDomainQry + XUpdateUtil.XUPDATE_END;
            log.debug("XUPDATE QUERY: \n" + addJossoSecDomainQryMod);

            XUpdateQuery addDependencyQry = new XUpdateQueryImpl();
            addDependencyQry.setQString(addJossoSecDomainQryMod);
            addDependencyQry.execute(jbossCfgDom);

            backupFile(jbossCfg, jbossCfg.getParent());
            writeContentFromDom(jbossCfgDom, jbossCfg);

            // Already configured
            return true;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure JBoss AS instance : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", jbossCfg != null ? jbossCfg.getName().getFriendlyURI() : "JBoss Configuration", "Must be done manually (Follow setup guide)");
            return false;
        }
    }
}
