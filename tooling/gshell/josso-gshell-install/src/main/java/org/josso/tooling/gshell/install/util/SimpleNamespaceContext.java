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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleNamespaceContext implements NamespaceContext {

    private final Map<String, String> prefixNamespaceMap = new HashMap<String, String>();

    public SimpleNamespaceContext() {
    }

    public SimpleNamespaceContext(String prefix, String namespaceURI) {
        this.prefixNamespaceMap.put(prefix, namespaceURI);
    }

    public SimpleNamespaceContext(final Map<String, String> prefixNamespaceMap) {
        this.prefixNamespaceMap.putAll(prefixNamespaceMap);
    }

    public String getNamespaceURI(String prefix) {
        String namespaceURI;
        if (prefix == null) {
            throw new NullPointerException("Null prefix");
        } else if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        } else if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        } else {
            namespaceURI = prefixNamespaceMap.get(prefix);
        }
        return namespaceURI != null ? namespaceURI : "";
    }

    public String getPrefix(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    public Iterator getPrefixes(String namespaceURI) {
        throw new UnsupportedOperationException();
    }

    public void addNamespaceURI(String prefix, String namespaceURI) {
        prefixNamespaceMap.put(prefix, namespaceURI);
    }

    public void removePrefix(String prefix) {
        prefixNamespaceMap.remove(prefix);
    }

    public void clear() {
        prefixNamespaceMap.clear();
    }
}
