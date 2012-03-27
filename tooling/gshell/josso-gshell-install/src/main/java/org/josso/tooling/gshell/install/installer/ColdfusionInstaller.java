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
 * @org.apache.xbean.XBean element="coldfusion-installer"
 */
public class ColdfusionInstaller extends VFSInstaller {

    private static final Log log = LogFactory.getLog(ColdfusionInstaller.class);


    public ColdfusionInstaller(TargetPlatform targetPlatform) {
        super(targetPlatform);
    }

    public ColdfusionInstaller() {
        super();
    }

    @Override
    public void validatePlatform() throws InstallException {

        try {

            boolean valid = true;

            if (targetConfDir.exists() && !targetConfDir.getType().getName().equals(FileType.FOLDER.getName())
                    && targetLibDir.exists() && !targetLibDir.getType().getName().equals(FileType.FOLDER.getName())) {
                valid = false;
                getPrinter().printErrStatus("Coldfusion Home", "Cannot find Coldfusion webapp root.");
            }

            if (!valid)
                throw new InstallException("Target does not seem a " + getTargetPlatform().getDescription() + " install.");

        } catch (IOException e) {
            getPrinter().printErrStatus("Coldfusion Home", e.getMessage());
            throw new InstallException(e.getMessage(), e);
        }

        getPrinter().printOkStatus("Coldfusion root");
    }

    @Override
    public void installComponent(JOSSOArtifact artifact, boolean replace) throws InstallException {
        try {

            FileObject srcFile = getFileSystemManager().resolveFile(artifact.getLocation());

            // Install only the proper artifact for the target platform ...
            if (artifact.getBaseName().startsWith("josso-servlet-agent")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agent-shared")) {
                installFile(srcFile, this.targetLibDir, replace);

            } else if (artifact.getBaseName().startsWith("josso-agents-bin") &&
                        artifact.getClassifier() !=  null &&
                        artifact.getClassifier().equals("jaxws")) {
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


        if (artifact.getBaseName().startsWith("slf4j") ||
            artifact.getBaseName().startsWith("jcl-over-slf4j") ||
            artifact.getBaseName().startsWith("logback") ||
            artifact.getBaseName().startsWith("spring") ||
            artifact.getBaseName().startsWith("xbean") ||
            artifact.getBaseName().startsWith("commons-codec")) {

            super.install3rdPartyComponent(artifact, replace);

        }

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

            if (name.equals("logback.xml") ) {
                FileObject classesDir = targetConfDir.resolveFile(".");

                if (!classesDir.exists())
                    classesDir.createFolder();

                installFile(srcFile, classesDir, replace);
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
    }

    protected void configureWebXml() throws InstallException {
        // --------------------------------------------------------------------
        // Configure web.xml
        // --------------------------------------------------------------------


        FileObject webXml = null;

        try {
            webXml = targetDir.resolveFile("wwwroot/WEB-INF/web.xml");

            // Get a DOM document of the web.xml :
            Node webXmlNode = loadAsDom(webXml);

            boolean modified = false;

            // Perform specific configurations
            if (configureFilters(webXmlNode))
                modified = true;

            if (modified) {

                // Backup Container configuration.  If we cannot perform a backup, do nothing
                if (!backupFile(webXml, targetDir)) {
                    getPrinter().printActionWarnStatus("Configure", targetDir.getName().getFriendlyURI() + "/wwwroot/WEB-INF/web.xml", "Must be done manually (Follow setup guide)");
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
            getPrinter().printActionWarnStatus("Configure", targetDir.getName().getFriendlyURI() + "/wwwroot/WEB-INF/web.xml", "Must be done manually (Follow setup guide)");
        }


    }

    protected boolean configureFilters(Node xmlDom) throws Exception {

        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList filtersNodes = (NodeList) xpath.evaluate("/web-app/filter", xmlDom, XPathConstants.NODESET);

        // Check if josso is already installed
        XPathExpression jossoFilterClassExp = xpath.compile("/web-app/filter[filter-class='org.josso.servlet.agent.GenericServletSSOAgentFilter']");
        Node jossoFilterNode = (Node) jossoFilterClassExp.evaluate(xmlDom, XPathConstants.NODE);

        // Append josso filter after auto-login filter in web.xml
        if (jossoFilterNode != null) {
            getPrinter().printActionWarnStatus("Configure", "JOSSO SSO Filter", "Already configured : " + (jossoFilterNode != null ? jossoFilterNode.getNodeValue() : "<unknown>"));
            return false;
        }

        if (filtersNodes != null && filtersNodes.getLength() > 0) {
            String xupdJossoFilter =
                    "\n\t<xupdate:insert-before select=\"/web-app/filter[filter-name='CFMonitoringFilter']\" >\n" +
                            "\t\t<xupdate:element name=\"filter\"> \n" +
                            "\t\t\t<xupdate:element name=\"filter-name\">JOSSOGenericServletFilter</xupdate:element>\n" +
                            "\t\t\t<xupdate:element name=\"filter-class\">org.josso.servlet.agent.GenericServletSSOAgentFilter</xupdate:element>\n" +
                            "\t\t\t<xupdate:element name=\"init-param\"> \n" +
                            "\t\t\t\t<xupdate:element name=\"param-name\">init</xupdate:element>\n" +
                            "\t\t\t\t<xupdate:element name=\"param-value\">lazy</xupdate:element>\n" +
                            "\t\t\t</xupdate:element>\n" +
                            "\t\t</xupdate:element>\n" +
                            "\t</xupdate:insert-before>\n\n" +
                            "\t<xupdate:insert-before select=\"/web-app/filter-mapping[1]\">\n" +
                            "\t\t<xupdate:element name=\"filter-mapping\">\n" +
                            "\t\t\t<filter-name>JOSSOGenericServletFilter</filter-name>\n" +
                            "\t\t\t<url-pattern>/*</url-pattern>\n" +
                            "\t\t</xupdate:element>\n" +
                            "\t</xupdate:insert-before>";


            String qry = XUpdateUtil.XUPDATE_START + xupdJossoFilter + XUpdateUtil.XUPDATE_END;
            log.debug("XUPDATE QUERY: \n" + qry);
            XUpdateQuery xq = new XUpdateQueryImpl();
            xq.setQString(qry);
            xq.execute(xmlDom);

            getPrinter().printActionOkStatus("Added josso filter into web.xml", "JOSSO Coldfusion Agent ", "WEB-INF/web.xml");


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


}
