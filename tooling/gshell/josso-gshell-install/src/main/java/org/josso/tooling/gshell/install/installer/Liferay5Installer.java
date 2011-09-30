package org.josso.tooling.gshell.install.installer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.josso.tooling.gshell.install.JOSSOArtifact;
import org.josso.tooling.gshell.install.TargetPlatform;
import org.josso.tooling.gshell.install.util.XUpdateUtil;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;

/**
 * @version $Id$
 * @org.apache.xbean.XBean element="liferay5-installer"
 */
public class Liferay5Installer extends VFSInstaller {

    private static final Log log = LogFactory.getLog(Liferay5Installer.class);


    public Liferay5Installer(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public Liferay5Installer() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        try {

            boolean valid = true;

            if (targetConfDir.exists() && !targetConfDir.getType().getName().equals(FileType.FOLDER.getName())
                    && targetLibDir.exists() && !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                valid = false;
                getPrinter().printErrStatus("LiferayHome", "Cannot find Liferay 5 webapp root.");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("Liferay 5 root", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }

        getPrinter().printOkStatus("Liferay 5 root");
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("josso-liferay5-agent")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agents-bin")) {
                installFile(srcFile, this.targetLibDir, replace);
            } else {
                log.debug("Artifact is not valid for selected platform : " + artifact);
            }
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        // do nothing - don't install 3rd Party libs
    }

    @Override
    public boolean backupAgentConfigurations(boolean remove) {
        try {
            // backup portal-ext.properties
            FileObject portalConfFile = targetConfDir.resolveFile("portal-ext.properties");
            if (portalConfFile.exists()) {
                // backup file in the same folder it is installed
                backupFile(portalConfFile, portalConfFile.getParent());
                if (remove) {
                    portalConfFile.delete();
                }
            }
        } catch (Exception e) {
            getPrinter().printErrStatus("BackupAgentConfigurations", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean removeOldComponents(boolean backup) {
        return true;
    }

    @Override
    public void installConfiguration(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            String name = srcFile.getName().getBaseName();

            if (name.equals("portal-log4j-ext.xml") || name.equals("log4j.dtd")) {
                FileObject metaInfDir = targetConfDir.resolveFile("META-INF/");

                if (!metaInfDir.exists())
                    metaInfDir.createFolder();

                installFile(srcFile, metaInfDir, replace);
            } else {
                installFile(srcFile, this.targetConfDir, replace);
            }

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public void configureAgent() throws InstallException {
        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        // For now, only web.xml to configure:
        configureWebXml();
        configureJaasModule();
    }

    protected void configureWebXml() throws InstallException {
        // --------------------------------------------------------------------
        // Configure web.xml
        // --------------------------------------------------------------------


        FileObject webXml = null;

        try {
            webXml = targetDir.resolveFile("WEB-INF/web.xml");

            // Get a DOM document of the web.xml :
            Node webXmlNode = loadAsDom(webXml);

            boolean modified = false;

            // Perform specific configurations
            if (configureFilters(webXmlNode))
                modified = true;

            if (modified) {

                // Backup Container configuration.  If we cannot perform a backup, do nothing
                if (!backupFile(webXml, targetDir)) {
                    getPrinter().printActionWarnStatus("Configure", targetDir.getName().getFriendlyURI() + "/WEB-INF/web.xml", "Must be done manually (Follow setup guide)");
                    return;
                }

                // Write modifications to file
                writeContentFromDom(webXmlNode, webXml);
                getPrinter().printActionOkStatus("Save", webXml.getName().getBaseName(), webXml.getName().getFriendlyURI());

            }

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure container : ", e.getMessage());
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure container : ", e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            getPrinter().printErrStatus("Cannot configure container : ", e.getMessage());
            getPrinter().printActionWarnStatus("Configure", targetDir.getName().getFriendlyURI() + "/WEB-INF/web.xml", "Must be done manually (Follow setup guide)");
        }


    }

    protected boolean configureFilters(Node xmlDom) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList filtersNodes = (NodeList) xpath.evaluate("/web-app/filter", xmlDom, XPathConstants.NODESET);

        // Check if josso is already installed
        XPathExpression jossoFilterClassExp = xpath.compile("/web-app/filter[filter-class='org.josso.liferay5.agent.LiferaySSOAgentFilter']");
        Node jossoFilterNode = (Node) jossoFilterClassExp.evaluate(xmlDom, XPathConstants.NODE);

        // Append josso filter after auto-login filter in web.xml
        if (jossoFilterNode != null) {
            getPrinter().printActionWarnStatus("Configure", "JOSSO SSO Filter", "Already configured : " + (jossoFilterNode != null ? jossoFilterNode.getNodeValue() : "<unknown>"));
            return false;
        }

        // Find auto-filter node in web.xml
        // Append josso filter after auto-login filter in web.xml

        if (filtersNodes != null && filtersNodes.getLength() > 0) {
            String xupdJossoFilter =
                    "\n\t<xupdate:insert-after select=\"/web-app/filter[filter-class='com.liferay.portal.servlet.filters.autologin.AutoLoginFilter']\" >\n" +
                            "\t\t<xupdate:element name=\"filter\"> \n" +
                            "\t\t\t<xupdate:element name=\"filter-name\">SSO Josso Filter</xupdate:element>\n" +
                            "\t\t\t<xupdate:element name=\"filter-class\">org.josso.liferay5.agent.LiferaySSOAgentFilter</xupdate:element>\n" +
                            "\t\t</xupdate:element>\n" +
                            "\t</xupdate:insert-after>\n\n" +
                            "\t<xupdate:insert-before select=\"/web-app/filter-mapping[1]\" >\n" +
                            "\t\t<xupdate:element name=\"filter-mapping\">\n" +
                            "\t\t\t<filter-name>SSO Josso Filter</filter-name>\n" +
                            "\t\t\t<url-pattern>/*</url-pattern>\n" +
                            "\t\t</xupdate:element>\n" +
                            "\t</xupdate:insert-before>";


            String qry = XUpdateUtil.XUPDATE_START + xupdJossoFilter + XUpdateUtil.XUPDATE_END;
            log.debug("XUPDATE QUERY: \n" + qry);
            XUpdateQuery xq = new XUpdateQueryImpl();
            xq.setQString(qry);
            xq.execute(xmlDom);

            getPrinter().printActionOkStatus("Added josso filter into web.xml", "JOSSO Liferay 5 Agent ", "WEB-INF/web.xml");


            return true;
        }
        return false;
    }


    @Override
    public boolean updateAgentConfiguration(String idpHostName, String idpPort, String idpType) {
        boolean updated;

        updated = super.updateAgentConfiguration(idpHostName, idpPort, idpType);    //To change body of overridden methods use File | Settings | File Templates.

        try {
            log.debug("targetJOSSOConfDir = " + targetJOSSOConfDir);
            FileObject agentConfigFile = targetJOSSOConfDir.resolveFile("josso-agent-config.xml");
            if (agentConfigFile.exists()) {
                // Get a DOM document of the josso-agent-config.xml
                Node configXmlDom = readContentAsDom(agentConfigFile);


                String updateSchemaLocations =
                    "<xupdate:update select=\"//@xsi:schemaLocation\">" +
                            "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd " +
                            "        urn:org:josso:agent:liferay5 jar:" + targetLibDir + "/josso-liferay5-agent-" + getProperty("version") + ".jar!/josso-liferay5-agent.xsd" +
                            "        urn:org:josso:protocol:client jar:" + targetLibDir + "/josso-agents-bin-" + getProperty("version") + ".jar!/josso-protocol-client.xsd " +
                            "        urn:org:josso:agent:core jar:" + targetLibDir + "/josso-agents-bin-" + getProperty("version") + ".jar!/josso-agent.xsd" +
                            "" +
                    "</xupdate:update>";

                String updateSchemaLocationQryStr = XUpdateUtil.XUPDATE_START + updateSchemaLocations + XUpdateUtil.XUPDATE_END;
                log.debug("XUPDATE QUERY: \n" +updateSchemaLocationQryStr);

                XUpdateQuery updateSchemaLocationQry = new XUpdateQueryImpl();
                updateSchemaLocationQry.setQString(updateSchemaLocationQryStr);
                updateSchemaLocationQry.execute(configXmlDom);

                getPrinter().printActionOkStatus("Configure","Schema Locations", "");

                // Write modifications to file
                writeContentFromDom(configXmlDom, agentConfigFile);
                getPrinter().printActionOkStatus("Save", agentConfigFile.getName().getBaseName(), agentConfigFile.getName().getFriendlyURI());


            }
        } catch (Exception e) {
            log.error("Error injecting schema locations to agent configuration", e);
            getPrinter().printErrStatus("UpdateAgentConfiguration", e.getMessage());
            updated = false;
        }

        return updated;
    }

    protected boolean configureJaasModule() {
        String tcInstallDir = getProperty("tomcatInstallDir");
        String jbInstallDir = getProperty("jbossInstallDir");
        final String JOSSO_TOMCAT_MODULE_DEFINITION = "\n\njosso {\n" +
                "org.josso.liferay5.agent.jaas.SSOGatewayLoginModule required debug=true;\n" +
                "};";

        if (tcInstallDir != null) {
            log.debug("[configureJaasModule]: Tomcat install dir: " + tcInstallDir);
            try {
                FileObject tomcatInstallDir = getFileSystemManager().resolveFile(tcInstallDir);
                FileObject jaasConfigFile = tomcatInstallDir.resolveFile("conf/jaas.config");
                if (jaasConfigFile != null) {
                    BufferedWriter writerJaas =
                            new BufferedWriter(
                                    new OutputStreamWriter(new FileOutputStream(jaasConfigFile.getURL().getFile(), true)));
                    writerJaas.write(JOSSO_TOMCAT_MODULE_DEFINITION);
                    writerJaas.flush();
                    writerJaas.close();
                    return true;
                } else {
                    getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "jaas.conf doesn't exist on given path");
                    return false;
                }
            } catch (FileSystemException e) {
                getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "Tomcat install directory is wrong.");
            } catch (IOException e) {
                getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "Can not write to jaas.conf.");
            }
        }

        if (jbInstallDir != null) {
            log.debug("[configureJaasModule]: JBoss install dir: " + jbInstallDir);
            FileObject jbossInstallDir = null;
            try {
                jbossInstallDir = getFileSystemManager().resolveFile(jbInstallDir);
                FileObject loginConfig = jbossInstallDir.resolveFile("server/default/conf/login-config.xml");
                Node xDom = readContentAsDom(loginConfig);

                if ( xDom == null ) {
                    log.debug("[configureJaasModule]: XML is not loaded.  " + loginConfig.getName().getFriendlyURI());
                    return false;
                }
                String xupdJossoModule =
                                "\n\t<xupdate:append select=\"/policy\" >\n" +
                                "\t\t<xupdate:element name=\"application-policy\">\n" +
                                "\t\t\t<xupdate:attribute name=\"name\">josso</xupdate:attribute>\n" +
                                "\t\t\t<authentication>\n" +
                                "\t\t\t\t<login-module code=\"org.josso.liferay5.agent.jaas.SSOGatewayLoginModule\" flag=\"required\">\n" +
                                "\t\t\t\t\t<module-option name=\"debug\">true</module-option>\n" +
                                "\t\t\t\t</login-module>\n" +
                                "\t\t\t</authentication>\n" + 
                                "\t\t</xupdate:element>\n" +
                                "\t</xupdate:append>";


                String qry = XUpdateUtil.XUPDATE_START + xupdJossoModule + XUpdateUtil.XUPDATE_END;
                log.debug("XUPDATE QUERY: \n" + qry);
                XUpdateQuery xq = new XUpdateQueryImpl();
                xq.setQString(qry);
                xq.execute(xDom);

                writeContentFromDom(xDom, loginConfig);

                getPrinter().printActionOkStatus("Changed login-config.xml", "JOSSO Liferay 5 Agent ", "server/default/conf/login-config.xml");
                return true;

            } catch (FileSystemException e) {
                getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "JBoss install directory is wrong.");
            } catch (Exception e) {
                e.printStackTrace(); 
            }
        }

        return false;
    }

    private Document loadAsDom(FileObject inFile) throws Exception {
        InputStream is = null;

        try {
            is = inFile.getContent().getInputStream();

            DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(false); // this is the only diference from readContentAsDom
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
                is = inFile.getContent().getInputStream();
                // disable dtd loading
                parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                builder = parserFactory.newDocumentBuilder();
                doc = builder.parse(is);
            }

            DocumentType docType = doc.getDoctype();

            return doc;

        } finally {
            if (is != null) try {
                is.close();
            } catch (IOException e) { /**/}
        }

    }


}
