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
package org.josso.util.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a base configuration handler that uses XUpdate to add, update and remove elements from a XML files.
 * The XML file to be updated is referred by the ConfigurationContext instance related to this handler.
 *
 * @deprecated This component is NO longer valid!
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: XUpdateConfigurationHandler.java 568 2008-07-31 18:39:20Z sgonzalez $
 */
public class XUpdateConfigurationHandler implements ConfigurationHandler {

    // log4j logger instance.
    private static final Log logger = LogFactory.getLog(XUpdateConfigurationHandler.class);

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss_SSS");

    // The configuration context related to the handler.
    private ConfigurationContext _ctx;

    /**
     * XUPDATE constant used to begin all queries.
     */
    public static final String XUPDATE_START = "<xupdate:modifications version=\"1.0\"\n" +
            "       xmlns:xupdate=\"http://www.xmldb.org/xupdate\">\n";

    /**
     * XUPDATE constant used to end all queries.
     */
    public static final String XUPDATE_END = "\n</xupdate:modifications> ";

    /**
     * XPath expression to find where existing elements are found in the document.
     */
    private String elementsBaseLocation;

    /**
     * XPath expression to find where new elements will be inserted AFTER in the document (as siblings)
     */
    private String newElementsBaseLocation;

    /**
     * @param ctx                     The configuration context used by this handler.
     * @param elementsBaseLocation    XPath expression to find where existing elements are found in the document.
     * @param newElementsBaseLocation XPath expression to find where new elements will be inserted AFTER in the document (as siblings)
     */
    public XUpdateConfigurationHandler(ConfigurationContext ctx, String elementsBaseLocation, String newElementsBaseLocation) {
        this(elementsBaseLocation, newElementsBaseLocation);
        _ctx = ctx;
    }

    /**
     * @param elementsBaseLocation    XPath expression to find where existing elements are found in the document.
     * @param newElementsBaseLocation XPath expression to find where new elements will be inserted AFTER in the document (as siblings)
     */
    public XUpdateConfigurationHandler(String elementsBaseLocation, String newElementsBaseLocation) {
        // Setup XUpdateUtil factory.
        System.setProperty("org.xmldb.common.xml.queries.XPathQueryFactory",
                "org.xmldb.common.xml.queries.xalan2.XPathQueryFactoryImpl");

        this.elementsBaseLocation = elementsBaseLocation;
        this.newElementsBaseLocation = newElementsBaseLocation;
    }

    /**
     * Setter for this handler configuration context.
     */
    public void setSSOConfigurationContext(ConfigurationContext ctx) {
        _ctx = ctx;
    }

    /**
     * Getter for this handler configuration context.
     */
    public ConfigurationContext getSSOConfigurationContext() {
        return _ctx;
    }

    /**
     * This method will add/update the specified element based on it's old and new values.
     * If the oldValue is null, this method will insert the element, if it's not, it will try to update an existing element.
     *
     * @param element
     * @param oldValue
     * @param newValue
     */
    public void saveElement(String element, String oldValue, String newValue) {

        if (!_ctx.isConfigurationUpdatable())
            return;

        // Log what we're doing ....
        if (logger.isDebugEnabled()) {
            logger.debug("saveElement : " + element + " [" + oldValue + "/" + newValue + "] at " +
                    this.elementsBaseLocation + " " + this.newElementsBaseLocation);
        }

        try {
            // Update an element in a josso configuration file :
            String qry;
            if (oldValue == null) {
                // The attribute is null, add a new element.
                qry = buildXInsertAfterElementQueryString(this.newElementsBaseLocation, element, unicodeEscape(newValue));
            } else {
                // The attribute has a value, update the existing element.
                qry = buildXUPdateElementQueryString(this.elementsBaseLocation + "/" + element, unicodeEscape(newValue));
            }
            this.updateConfiguration(qry);
        } catch (Exception e) {
            logger.error("Can't update configuration element for : " + element + ", new value : " + newValue + " :\n" + e.getMessage(), e);
        }
    }

    /**
     * This method will remove the specified element. If the configuration is not updatable, this method does nothing.
     *
     * @see ConfigurationContext#isConfigurationUpdatable()
     */
    public void removeElement(String element) {
        try {

            if (!_ctx.isConfigurationUpdatable())
                return;

            // Log what we're doing ....
            if (logger.isDebugEnabled()) {
                logger.debug("removeElement : " + element + " at " + this.elementsBaseLocation);
            }

            // Delete an element from the configuration file :
            String qry = buildXDeleteElementQuery(this.elementsBaseLocation, element);
            this.updateConfiguration(qry);

        } catch (Exception e) {
            logger.error("Can't update configuration element for : " + element + " :\n" + e.getMessage(), e);
        }
    }


    /**
     * Updates the element located after the xpathExpr.  If the configuration is not updatable, the method does nothing.
     * It checks that configuration backup is enable.
     *
     * @param qry the XUpdate query to be used to update the configuration file.
     * @see ConfigurationContext#isConfigurationUpdatable()
     * @see ConfigurationContext#isBackupEnabled()
     */
    protected void updateConfiguration(String qry) throws Exception {

        if (!_ctx.isConfigurationUpdatable())
            return;


    }

    // Utils to build different type of queries :

    /**
     * Builds an XUpdate query string to update an element's value.
     *
     * @param xpathExpr the XPath expression to locate the element.
     * @param newValue  the new element value, must be already escaped.
     */
    protected String buildXUPdateElementQueryString(String xpathExpr, String newValue) {
        // Build a new XUPDATE expression :
        String qry =
                XUPDATE_START +
                        "\t<xupdate:update select=\"" + xpathExpr + "\">" + newValue + "</xupdate:update>" +
                        XUPDATE_END;

        if (logger.isDebugEnabled())
            logger.debug("buildXUPdateElementQueryString(" + xpathExpr + "," + newValue + ") = \n" + qry);

        return qry;
    }

    /**
     * Builds a XUpdate query string to append a new element to the document.
     *
     * @param xpathExpr the XPath expression to locate the element where the new element will be appended.
     * @param element   the new element name
     * @param value     new element value, must be already escaped.
     */
    protected String buildXAppendElementQueryString(String xpathExpr, String element, String value) {
        String qry =
                XUPDATE_START +
                        "\t<xupdate:append select=\"" + xpathExpr + "\" >\n" +
                        "\t\t<xupdate:element name=\"" + element + "\">" + value + "</xupdate:element>\n" +
                        "\t</xupdate:append>" +
                        XUPDATE_END;

        if (logger.isDebugEnabled())
            logger.debug("buildXAppendElementQueryString(" + xpathExpr + "," + element + "," + value + ") = \n" + qry);

        return qry;


    }

    /**
     * Builds a XUpdate query string to insert an element after another.
     *
     * @param xpathExpr the XPath expression to locate the element where the new element will be inserted.
     * @param element   the new element name
     * @param value     new element value, must be already escaped.
     */
    protected String buildXInsertAfterElementQueryString(String xpathExpr, String element, String value) {
        String qry =
                XUPDATE_START +
                        "\t<xupdate:insert-after select=\"" + xpathExpr + "\" >\n" +
                        "\t\t<xupdate:element name=\"" + element + "\">" + value + "</xupdate:element>\n" +
                        "\t</xupdate:insert-after>" +
                        XUPDATE_END;

        if (logger.isDebugEnabled())
            logger.debug("buildXAppendElementQueryString(" + xpathExpr + "," + element + "," + value + ") = \n" + qry);

        return qry;

    }

    /**
     * Builds a XUpdate query string to append a new element with the specified XML as body.
     *
     * @param xpathExpr
     * @param element
     * @param xml
     */
    protected String buildXAppendElementXMLQueryString(String xpathExpr, String element, String xml) {
        String qry =
                XUPDATE_START +
                        "\t<xupdate:append select=\"" + xpathExpr + "\" >\n" +
                        "\t\t<xupdate:element name=\"" + element + "\">\n" +
                        xml + "\n" +
                        "\t\t</xupdate:element>\n" +
                        "\t</xupdate:append>" +
                        XUPDATE_END;

        if (logger.isDebugEnabled())
            logger.debug("buildXInsertXMLQueryString(" + xpathExpr + "," + element + "," + xml + ") = \n" + qry);

        return qry;
    }

    /**
     * Builds a XUpdate query string to delete the specified element.
     *
     * @param xpathExpr
     * @param element
     */
    protected String buildXDeleteElementQuery(String xpathExpr, String element) {
        String qry =
                XUPDATE_START +
                        "\t<xupdate:remove select=\"" + xpathExpr + "/" + element + "\"/>" +
                        XUPDATE_END;

        if (logger.isDebugEnabled())
            logger.debug("buildXDeleteElementQuery(" + xpathExpr + "," + element + ") = \n" + qry);

        return qry;
    }


    protected String getElementsBaseLocation() {
        return elementsBaseLocation;
    }

    protected String getNewElementsBaseLocation() {
        return newElementsBaseLocation;
    }


    /**
     * This will scape all spetial chars like <, >, &, \, " and unicode chars.
     */
    public String unicodeEscape(String v) {

        StringWriter w = new StringWriter();

        int len = v.length();
        for (int j = 0; j < len; j++) {
            char c = v.charAt(j);
            switch (c) {
                case '&':
                    w.write("&amp;");
                    break;
                case '<':
                    w.write("&lt;");
                    break;
                case '>':
                    w.write("&gt;");
                    break;
                case '\'':
                    w.write("&apos;");
                    break;
                case '"':
                    w.write("&quot;");
                    break;
                default:
                    if (canEncode(c)) {
                        w.write(c);
                    } else {
                        w.write("&#");
                        w.write(Integer.toString(c));
                        w.write(';');
                    }
                    break;
            }
        }
        ByteArrayInputStream d;
        return w.toString();
    }

    public static boolean canEncode(char c) {
        return c < 127;
    }


}
