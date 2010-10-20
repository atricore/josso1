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
package org.josso.tooling.gshell.install.util;


import org.w3c.dom.Node;
import org.xmldb.common.xml.queries.XUpdateQuery;
import org.xmldb.xupdate.lexus.XUpdateQueryImpl;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: XUpdate.java 543 2008-03-18 21:34:58Z sgonzalez $
 */

public class XUpdateUtil {

    private static final Log log = LogFactory.getLog(XUpdateUtil.class);

    /**
     * XUPDATE constant used to begin all queries.
     */
    public static final String XUPDATE_START = "<xupdate:modifications version=\"1.0\"\n" +
            "       xmlns:xupdate=\"http://www.xmldb.org/xupdate\">\n";

    /**
     * XUPDATE constant used to end all queries.
     */
    public static final String XUPDATE_END = "\n</xupdate:modifications> ";

}
