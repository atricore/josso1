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
 * @org.apache.xbean.XBean element="liferay6-installer"
 */
public class Liferay6Installer extends VFSInstaller {

    private static final Log log = LogFactory.getLog(Liferay6Installer.class);


    public Liferay6Installer(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public Liferay6Installer() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        try {

            boolean valid = true;

            if (targetConfDir.exists() && !targetConfDir.getType().getName().equals(FileType.FOLDER.getName())
                    && targetLibDir.exists() && !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                valid = false;
                getPrinter().printErrStatus("LiferayHome", "Cannot find Liferay 6 webapp root.");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("Liferay 6 root", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }

        getPrinter().printOkStatus("Liferay 6 root");
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("josso-liferay6-agent")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                                   artifact.getClassifier() !=  null &&
                                   artifact.getClassifier().equals("axis")) {
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


        String webConfigFilePath = "";
        FileObject webXml;

        try {
            webConfigFilePath = "WEB-INF/liferay-web.xml";
            webXml = targetDir.resolveFile(webConfigFilePath);
            if (!webXml.exists()) {
                webConfigFilePath = "WEB-INF/web.xml";
                webXml = targetDir.resolveFile(webConfigFilePath);
            }

            // Get a DOM document of the web.xml :
            Node webXmlNode = loadAsDom(webXml);

            boolean modified = false;

            // Perform specific configurations
            if (configureFilters(webXmlNode, webConfigFilePath))
                modified = true;

            if (modified) {

                // Backup Container configuration.  If we cannot perform a backup, do nothing
                if (!backupFile(webXml, targetDir)) {
                    getPrinter().printActionWarnStatus("Configure", targetDir.getName().getFriendlyURI() + "/" + webConfigFilePath, "Must be done manually (Follow setup guide)");
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
            getPrinter().printActionWarnStatus("Configure", targetDir.getName().getFriendlyURI() + "/" + webConfigFilePath, "Must be done manually (Follow setup guide)");
        }


    }

    protected boolean configureFilters(Node xmlDom, String webConfigFilePath) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList filtersNodes = (NodeList) xpath.evaluate("/web-app/filter", xmlDom, XPathConstants.NODESET);

        // Check if josso is already installed
        XPathExpression jossoFilterClassExp = xpath.compile("/web-app/filter[filter-class='org.josso.liferay6.agent.LiferaySSOAgentFilter']");
        Node jossoFilterNode = (Node) jossoFilterClassExp.evaluate(xmlDom, XPathConstants.NODE);

        // Append josso filter after auto-login filter in web.xml
        if (jossoFilterNode != null) {
            getPrinter().printActionWarnStatus("Configure", "JOSSO SSO Filter", "Already configured: " + webConfigFilePath);
            return false;
        }

        // Find auto-filter node in web.xml
        // Append josso filter after auto-login filter in web.xml

        if (filtersNodes != null && filtersNodes.getLength() > 0) {
            // Note: we are not using xupdate because of namespace issues
            Document doc = (Document) xmlDom;

            // filter
            Element filterNameElem = doc.createElement("filter-name");
            filterNameElem.setTextContent("SSO Josso Filter");

            Element filterClassElem = doc.createElement("filter-class");
            filterClassElem.setTextContent("org.josso.liferay6.agent.LiferaySSOAgentFilter");

            Element filterElem = doc.createElement("filter");
            filterElem.appendChild(doc.createTextNode("\n\t\t"));
            filterElem.appendChild(filterNameElem);
            filterElem.appendChild(doc.createTextNode("\n\t\t"));
            filterElem.appendChild(filterClassElem);
            filterElem.appendChild(doc.createTextNode("\n\t"));

            XPathExpression autoLoginFilterExp = xpath.compile("/web-app/filter[filter-class='com.liferay.portal.servlet.filters.autologin.AutoLoginFilter']");
            Node autoLoginFilterNode = (Node) autoLoginFilterExp.evaluate(xmlDom, XPathConstants.NODE);
            autoLoginFilterNode.getParentNode().insertBefore(filterElem, autoLoginFilterNode.getNextSibling());
            autoLoginFilterNode.getParentNode().insertBefore(doc.createTextNode("\n\t"), autoLoginFilterNode.getNextSibling());

            // filter mapping
            Element filterNameMappingElem = doc.createElement("filter-name");
            filterNameMappingElem.setTextContent("SSO Josso Filter");

            Element urlPatternElem = doc.createElement("url-pattern");
            urlPatternElem.setTextContent("/*");

            Element filterMappingElem = doc.createElement("filter-mapping");
            filterMappingElem.appendChild(doc.createTextNode("\n\t\t"));
            filterMappingElem.appendChild(filterNameMappingElem);
            filterMappingElem.appendChild(doc.createTextNode("\n\t\t"));
            filterMappingElem.appendChild(urlPatternElem);
            filterMappingElem.appendChild(doc.createTextNode("\n\t"));

            XPathExpression firstFilterMappingExp = xpath.compile("/web-app/filter-mapping[1]");
            Node firstFilterMappingNode = (Node) firstFilterMappingExp.evaluate(xmlDom, XPathConstants.NODE);
            firstFilterMappingNode.getParentNode().insertBefore(filterMappingElem, firstFilterMappingNode);
            firstFilterMappingNode.getParentNode().insertBefore(doc.createTextNode("\n\t"), firstFilterMappingNode);

            getPrinter().printActionOkStatus("Added josso filter into web config file", "JOSSO Liferay 6 Agent ", webConfigFilePath);

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
                            "        urn:org:josso:agent:liferay6 jar:" + targetLibDir + "/josso-liferay6-agent-" + getProperty("version") + ".jar!/josso-liferay6-agent.xsd" +
                            "        urn:org:josso:protocol:client jar:" + targetLibDir + "/josso-agents-bin-" + getProperty("version") + "-axis.jar!/josso-protocol-client.xsd " +
                            "        urn:org:josso:agent:core jar:" + targetLibDir + "/josso-agents-bin-" + getProperty("version") + "-axis.jar!/josso-agent.xsd" +
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
                "org.josso.liferay6.agent.jaas.SSOGatewayLoginModule required debug=true;\n" +
                "};";

        if (tcInstallDir != null) {
            log.debug("[configureJaasModule]: Tomcat install dir: " + tcInstallDir);
            try {
                FileObject tomcatInstallDir = getFileSystemManager().resolveFile(tcInstallDir);
                FileObject jaasConfigFile = tomcatInstallDir.resolveFile("conf/jaas.conf");
                if (jaasConfigFile != null) {
                    String jaasFileContent = null;
                    if (jaasConfigFile.exists()) {
                        jaasFileContent = IOUtils.toString(jaasConfigFile.getContent().getInputStream());
                    }
                    if (jaasFileContent == null || !jaasFileContent.contains("org.josso.liferay6.agent.jaas.SSOGatewayLoginModule")) {
                        BufferedWriter writerJaas =
                                new BufferedWriter(
                                        new OutputStreamWriter(new FileOutputStream(jaasConfigFile.getURL().getFile(), true)));
                        writerJaas.write(JOSSO_TOMCAT_MODULE_DEFINITION);
                        writerJaas.flush();
                        writerJaas.close();
                    }

                    FileObject setEnvShFile = tomcatInstallDir.resolveFile("bin/setenv.sh");
                    if (setEnvShFile != null && setEnvShFile.exists()) {    // it's should always be there, it's distributed with Liferay/Tomcat
                        String setEnvFileContent = IOUtils.toString(setEnvShFile.getContent().getInputStream());
                        if (!setEnvFileContent.contains("jaas.conf")) {
                            BufferedWriter writerSetEnv =
                                    new BufferedWriter(
                                            new OutputStreamWriter(new FileOutputStream(setEnvShFile.getURL().getFile(), true)));
                            writerSetEnv.write("\nJAVA_OPTS=\"$JAVA_OPTS -Djava.security.auth.login.config=$CATALINA_HOME/conf/jaas.conf\"");
                            writerSetEnv.flush();
                            writerSetEnv.close();
                        }
                    }

                    FileObject setEnvBatFile = tomcatInstallDir.resolveFile("bin/setenv.bat");
                    if (setEnvBatFile != null && setEnvBatFile.exists()) {    // it's should always be there, it's distributed with Liferay/Tomcat
                        String setEnvFileContent = IOUtils.toString(setEnvBatFile.getContent().getInputStream());
                        if (!setEnvFileContent.contains("jaas.conf")) {
                            BufferedWriter writerSetEnv =
                                    new BufferedWriter(
                                            new OutputStreamWriter(new FileOutputStream(setEnvBatFile.getURL().getFile(), true)));
                            writerSetEnv.write("\nset JAVA_OPTS=\"%JAVA_OPTS% -Djava.security.auth.login.config=%CATALINA_HOME%\\conf\\jaas.conf\"");
                            writerSetEnv.flush();
                            writerSetEnv.close();
                        }
                    }

                    return true;
                } else {
                    getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "jaas.conf doesn't exist on given path");
                    return false;
                }
            } catch (FileSystemException e) {
                getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "Tomcat install directory is wrong.");
            } catch (IOException e) {
                getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "Cannot write to jaas.conf : " + e.getMessage());
            }
        }

        if (jbInstallDir != null) {
            log.debug("[configureJaasModule]: JBoss install dir: " + jbInstallDir);
            try {
                FileObject jbossInstallDir = getFileSystemManager().resolveFile(jbInstallDir);
                boolean jboss7Config = false;
                String loginConfigFilePath = "server/default/conf/login-config.xml";
                FileObject loginConfig = jbossInstallDir.resolveFile(loginConfigFilePath);
                if (loginConfig == null || !loginConfig.exists()) {
                    jboss7Config = true;
                    loginConfigFilePath = "standalone/configuration/standalone.xml";
                    loginConfig = jbossInstallDir.resolveFile(loginConfigFilePath);
                }
                if (loginConfig != null && loginConfig.exists()) {
                    Node xDom = readContentAsDom(loginConfig);

                    if ( xDom == null ) {
                        log.debug("[configureJaasModule]: XML is not loaded.  " + loginConfigFilePath);
                        return false;
                    }

                    boolean configChanged = false;
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    if (jboss7Config) {
                        // Note: we could also use SimpleNamespaceContext to select what we want
                        XPathExpression jossoSecurityDomainExp = xpath.compile("//*[local-name()='subsystem']/*[local-name()='security-domains']/*[local-name()='security-domain'][@name='josso']");
                        Node jossoSecurityDomainNode = (Node) jossoSecurityDomainExp.evaluate(xDom, XPathConstants.NODE);
                        if (jossoSecurityDomainNode == null) {
                            // Note: we are not using xupdate because of namespace issues
                            Document doc = (Document) xDom;

                            Element jossoModuleOptionElem = doc.createElement("module-option");
                            jossoModuleOptionElem.setAttribute("name", "debug");
                            jossoModuleOptionElem.setAttribute("value", "true");

                            Element jossoLoginModuleElem = doc.createElement("login-module");
                            jossoLoginModuleElem.setAttribute("code", "org.josso.liferay6.agent.jaas.SSOGatewayLoginModule");
                            jossoLoginModuleElem.setAttribute("flag", "required");
                            jossoLoginModuleElem.appendChild(doc.createTextNode("\n\t\t\t\t\t"));
                            jossoLoginModuleElem.appendChild(jossoModuleOptionElem);
                            jossoLoginModuleElem.appendChild(doc.createTextNode("\n\t\t\t\t"));

                            Element jossoAuthenticationElem = doc.createElement("authentication");
                            jossoAuthenticationElem.appendChild(doc.createTextNode("\n\t\t\t\t"));
                            jossoAuthenticationElem.appendChild(jossoLoginModuleElem);
                            jossoAuthenticationElem.appendChild(doc.createTextNode("\n\t\t\t"));

                            Element jossoSecurityDomainElem = doc.createElement("security-domain");
                            jossoSecurityDomainElem.setAttribute("name", "josso");
                            jossoSecurityDomainElem.appendChild(doc.createTextNode("\n\t\t\t"));
                            jossoSecurityDomainElem.appendChild(jossoAuthenticationElem);
                            jossoSecurityDomainElem.appendChild(doc.createTextNode("\n\t\t"));

                            XPathExpression securityDomainsExp = xpath.compile("//*[local-name()='subsystem']/*[local-name()='security-domains']");
                            Node securityDomainsNode = (Node) securityDomainsExp.evaluate(xDom, XPathConstants.NODE);
                            securityDomainsNode.appendChild(jossoSecurityDomainElem);
                            securityDomainsNode.appendChild(doc.createTextNode("\n\t"));

                            configChanged = true;
                        }
                    } else {
                        XPathExpression jossoAppPolicyExp = xpath.compile("/policy/application-policy[@name='josso']");
                        Node jossoAppPolicyNode = (Node) jossoAppPolicyExp.evaluate(xDom, XPathConstants.NODE);
                        if (jossoAppPolicyNode == null) {
                            String xupdJossoModule =
                                    "\n\t<xupdate:append select=\"/policy\" >\n" +
                                            "\t\t<xupdate:element name=\"application-policy\">\n" +
                                            "\t\t\t<xupdate:attribute name=\"name\">josso</xupdate:attribute>\n" +
                                            "\t\t\t<authentication>\n" +
                                            "\t\t\t\t<login-module code=\"org.josso.liferay6.agent.jaas.SSOGatewayLoginModule\" flag=\"required\">\n" +
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
                            configChanged = true;
                        }
                    }
                    if (configChanged) {
                        if (!backupFile(loginConfig, loginConfig.getParent())) {
                            getPrinter().printActionWarnStatus("Configure", loginConfigFilePath, "Must be done manually (Follow setup guide)");
                            return false;
                        }
                        writeContentFromDom(xDom, loginConfig);
                        getPrinter().printActionOkStatus("Changed login configuration", "JOSSO Liferay 6 Agent ", loginConfigFilePath);
                    } else {
                        getPrinter().printActionWarnStatus("Configure login", "JOSSO Liferay 6 Agent", "Already configured: " + loginConfigFilePath);
                    }
                } else {
                    log.debug("[configureJaasModule]: Unknown Jboss configuration!");
                    return false;
                }

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
