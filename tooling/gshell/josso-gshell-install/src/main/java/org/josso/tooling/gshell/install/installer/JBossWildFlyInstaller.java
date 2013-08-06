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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;

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

        // TODO : Add/Update josso dependency to org.jboss.as.server/main
        // TODO : Add/Update josso dependency to org.jboss.as.security/main
        // TODO : Add/Update josso dependency to org.picketbox/main

        // TODO : Configure login module (i.e. standalone.xml)

        // TODO : Configure ee domain w/global module
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
                    getTargetPlatform().getVersion().startsWith("6.")) {
                // JBoss 6 uses jaxws
                installFile(srcFile, this.targetJOSSOLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-jboss7-agent") &&
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
                (artifact.getBaseName().startsWith("spring")) || artifact.getBaseName().startsWith("xbean-spring"))) {

            try {

                if (!springModuleExists()) {
                    createSpringModuleLayout();
                }

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

    protected boolean springModuleExists() throws FileSystemException {
        FileObject springModule = targetLibDir.resolveFile("org/springframework/spring/main");
        return springModule.exists();
    }

    protected void createSpringModuleLayout() throws IOException {

        FileObject springModule = targetLibDir.resolveFile("org/springframework/spring/main");
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

    }

    protected void updateSpringModuleResource(String resource) throws Exception {

        FileObject springModule = targetLibDir.resolveFile("org/springframework/spring/main/module.xml");
        Node springModuleDom = readContentAsDom(springModule, false);

        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        String appendSpringResourceQryMod =
                "\n\t<xupdate:append select=\"//module/resources\" >" +
                        "\n\t<xupdate:element name=\"resource-root\">" +
                        "<xupdate:attribute name=\"path\">"+resource+"</xupdate:attribute>" +
                        "</xupdate:element>" +
                        "</xupdate:append>";

        String appendSpringResourceQryModStr = XUpdateUtil.XUPDATE_START + appendSpringResourceQryMod + XUpdateUtil.XUPDATE_END;
        log.debug("XUPDATE QUERY: \n" + appendSpringResourceQryModStr);

        XUpdateQuery appendSpringResourceQry = new XUpdateQueryImpl();
        appendSpringResourceQry.setQString(appendSpringResourceQryModStr);
        appendSpringResourceQry.execute(springModuleDom);

        writeContentFromDom(springModuleDom, springModule);

        getPrinter().printActionOkStatus("Configured","Spring Module resource",  resource);

    }
}
