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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 * @org.apache.xbean.XBean element="alfresco-installer"
 */

public class AlfrescoInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(AlfrescoInstaller.class);

    public AlfrescoInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public AlfrescoInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        try {
            boolean valid = true;

            if (targetConfDir.exists() && !targetConfDir.getType().getName().equals(FileType.FOLDER.getName())
                    && targetLibDir.exists() && !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                valid = false;
                getPrinter().printErrStatus("AlfrescoHome", "Cannot find Alfresco webapp root.");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("Alfresco root", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }

        getPrinter().printOkStatus("Alfresco root");
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("josso-alfresco-agent")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-servlet-agent")) {
                installFile(srcFile, this.targetLibDir, replace);
            } else if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetLibDir, replace);
            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                       artifact.getClassifier() !=  null &&
                       artifact.getClassifier().equals("axis")) {
                installFile(srcFile, this.targetJOSSOLibDir, replace);
            }  else {
                log.debug("Artifact is not valid for selected platform : " + artifact);
            }
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    public void install3rdPartyComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {

        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install newer (josso) version of xbean-spring
            if (artifact.getBaseName().startsWith("xbean-spring")) {
                removeOldJar(srcFile.getName().getBaseName(), this.targetLibDir, true);
                installFile(srcFile, this.targetLibDir, replace);
            }

        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }
    }

    @Override
    public boolean backupAgentConfigurations(boolean remove) {
        String tcInstallDir = getProperty("tomcatInstallDir");
        //String jbInstallDir = getProperty("jbossInstallDir");

        if (tcInstallDir != null) {
            log.debug("[configureJaasModule]: Tomcat install dir: " + tcInstallDir);

            // backup jaas.conf
            try {
                FileObject tomcatInstallDir = getFileSystemManager().resolveFile(tcInstallDir);
                FileObject tcBinDir = tomcatInstallDir.resolveFile("bin/");
                FileObject jaasConfigFile = tomcatInstallDir.resolveFile("conf/jaas.conf");
                if (jaasConfigFile.exists()) {
                    // backup file in the same folder it is installed
                    backupFile(jaasConfigFile, jaasConfigFile.getParent());
                    if (remove) {
                        jaasConfigFile.delete();
                    }
                }
                // backup setenv.sh and setenv.bat
                FileObject[] libs = tcBinDir.getChildren();
                for (int i = 0; i < libs.length; i++) {
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
            } catch (FileSystemException e) {
                getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "Tomcat install directory is wrong.");
            }
        }

        return true;
    }

    @Override
    public void installConfiguration(JOSSOArtifact artifact, boolean replace)
            throws InstallException {
        String tcInstallDir = getProperty("tomcatInstallDir");
        //String jbInstallDir = getProperty("jbossInstallDir");

        log.debug("[configureJaasModule]: Tomcat install dir: " + tcInstallDir);

        try {
            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());
            String name = srcFile.getName().getBaseName();

            if (tcInstallDir != null) {
                FileObject tomcatInstallDir = getFileSystemManager().resolveFile(tcInstallDir);
                FileObject tcBinDir = tomcatInstallDir.resolveFile("bin/");
                FileObject jaasConfigDir = tomcatInstallDir.resolveFile("conf/");

                if (name.startsWith("setenv")) {
                    installFile(srcFile, tcBinDir, replace);
                } else if (name.equals("jaas.conf")) {
                    installFile(srcFile, jaasConfigDir, replace);
                }
            }
            if (name.startsWith("josso")) {
                installFile(srcFile, this.targetConfDir, replace);
            }

        } catch (FileSystemException e) {
            getPrinter().printActionErrStatus("Configure", "JOSSO SSO Filter", "Tomcat install directory is wrong.");
        } catch (IOException e) {
            throw new InstallException(e.getMessage(), e);
        }

    }

    @Override
    public boolean removeOldComponents(boolean backup) {
        return true;
    }

    @Override
	public boolean updateAgentConfiguration(String idpHostName, String idpPort,
			String idpType) {
		return false;
	}

	@Override
    public void configureAgent() throws InstallException {
        // Setup XUpdate :
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        // For now, only web.xml to configure:
        configureWebXml();
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
        XPathExpression jossoGenericFilterClassExp = xpath.compile("/web-app/filter[filter-class='org.josso.servlet.agent.GenericServletSSOAgentFilter']");
        Node jossoGenericFilterNode = (Node) jossoGenericFilterClassExp.evaluate(xmlDom, XPathConstants.NODE);
        XPathExpression jossoAlfrescoFilterClassExp = xpath.compile("/web-app/filter[filter-class='org.josso.alfresco.agent.AlfrescoSSOAgentFilter']");
        Node jossoAlfrescoFilterNode = (Node) jossoAlfrescoFilterClassExp.evaluate(xmlDom, XPathConstants.NODE);


        if (jossoGenericFilterNode != null || jossoAlfrescoFilterNode != null) {
            getPrinter().printActionWarnStatus("Configure", "JOSSO SSO Filter", "Already configured : " +
                    (jossoGenericFilterNode != null ? jossoGenericFilterNode.getNodeValue() : "<unknown>") +
                    (jossoAlfrescoFilterNode != null ? jossoAlfrescoFilterNode.getNodeValue() : "<unknown>"));
            return false;
        }

        // Find Authentication Filter node in web.xml
        // Append josso filters after Authentication Filter in web.xml

        if (filtersNodes != null && filtersNodes.getLength() > 0) {
            String xupdJossoFilter =
                    "<xupdate:insert-before select=\"/web-app/filter[filter-name='Authentication Filter']\" >\n" +
                            "<xupdate:element name=\"filter\"> \n" +
                            "\t<xupdate:element name=\"filter-name\">Josso Alfresco Filter</xupdate:element>\n" +
                            "\t<xupdate:element name=\"filter-class\">org.josso.alfresco.agent.AlfrescoSSOAgentFilter</xupdate:element>\n" +
                            "</xupdate:element>\n" +
                            "<xupdate:element name=\"filter\"> \n" +
                            "\t<xupdate:element name=\"filter-name\">SSO Josso Filter</xupdate:element>\n" +
                            "\t<xupdate:element name=\"filter-class\">org.josso.servlet.agent.GenericServletSSOAgentFilter</xupdate:element>\n" +
                            "</xupdate:element>\n" +
                            "</xupdate:insert-before>";

            //We are using alf_temp_one and alf_temp_two nodes as variables for Josso filter mappings, which is removed at the end of processing
            String xupdAlfTempNodes =
                    "<xupdate:variable name=\"GlobalAuthenticationMappings\" select=\"/web-app/filter-mapping[filter-name='Global Authentication Filter']\"/>\n" +
                            "<xupdate:append select=\"/web-app\" >\n" +
                            "\t<xupdate:element name=\"alf_temp_one\"><xupdate:value-of select=\"$GlobalAuthenticationMappings\"/></xupdate:element>\n" +
                            "</xupdate:append>\n\n" +
                            "<xupdate:append select=\"/web-app\" >\n" +
                            "\t<xupdate:element name=\"alf_temp_two\"><xupdate:value-of select=\"$GlobalAuthenticationMappings\"/></xupdate:element>\n" +
                            "</xupdate:append>";

            //Substitute all Global Authentication Filter mappings to Josso Filters
            String xupdJossoFilterMappings =
                    "\n<xupdate:update select=\"/web-app/alf_temp_one/filter-mapping[filter-name='Global Authentication Filter']/filter-name\">SSO Josso Filter</xupdate:update>" +
                            "\n<xupdate:update select=\"/web-app/alf_temp_two/filter-mapping[filter-name='Global Authentication Filter']/filter-name\">Josso Alfresco Filter</xupdate:update>";

            //Insert more SSO Josso Filter mappings
            String xupdJossoFilterMappingsSpecific =
                    "<xupdate:insert-before select=\"/web-app/alf_temp_one/filter-mapping[1]\" >\n" +
                            filterMappingElement("SSO Josso Filter", "/josso_login/") +
                            filterMappingElement("SSO Josso Filter", "/josso_logout/") +
                            filterMappingElement("SSO Josso Filter", "/josso_security_check") +
                            "</xupdate:insert-before>";

            String qry = XUpdateUtil.XUPDATE_START +
                    xupdJossoFilter + "\n\n" +
                    xupdAlfTempNodes + "\n\n" +
                    xupdJossoFilterMappings + "\n\n" +
                    xupdJossoFilterMappingsSpecific + "\n\n"
                    + XUpdateUtil.XUPDATE_END;

            XUpdateQuery xq = new XUpdateQueryImpl();

            log.debug("XUPDATE QUERY [PART 1]: \n" + qry);
            xq.setQString(qry);
            xq.execute(xmlDom);

            //Copy Josso Filter Mappings from variable nodes, and remove all Global Filter mappings and remove variables
            String xupdAlfrescoFilterMappings =
                    "<xupdate:insert-before select=\"/web-app/filter-mapping[1]\" >\n" +
                            "\n\t<xupdate:value-of select=\"/web-app/alf_temp_two/node()\"/>\n" +
                            "</xupdate:insert-before>\n" +
                            "\n<xupdate:insert-before select=\"/web-app/filter-mapping[1]\" >\n" +
                            "\n\t<xupdate:value-of select=\"/web-app/alf_temp_one/node()\"/>\n" +
                            "</xupdate:insert-before>\n\n" +
                            "<xupdate:remove select=\"/web-app/filter-mapping[filter-name='Global Authentication Filter']\"/>\n" +
                            "<xupdate:remove select=\"/web-app/alf_temp_one\"/>\n" +
                            "<xupdate:remove select=\"/web-app/alf_temp_two\"/>";

            qry = XUpdateUtil.XUPDATE_START + "\n" + xupdAlfrescoFilterMappings + "\n" + XUpdateUtil.XUPDATE_END;
            log.debug("XUPDATE QUERY [PART 2]: \n" + qry);
            xq.setQString(qry);
            xq.execute(xmlDom);

            getPrinter().printActionOkStatus("Added josso filter into web.xml", "JOSSO Alfresco Agent ", "WEB-INF/web.xml");

            return true;
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

    private String filterMappingElement(String filterName, String url) {
        return "<xupdate:element name=\"filter-mapping\">\n" +
                "\t<filter-name>" + filterName + "</filter-name>\n" +
                "\t<url-pattern>" + url + "</url-pattern>\n" +
                "</xupdate:element>\n";
    }
}