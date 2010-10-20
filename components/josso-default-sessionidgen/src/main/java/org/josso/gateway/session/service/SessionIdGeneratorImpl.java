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
package org.josso.gateway.session.service;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.util.id.AbstractIdGenerator;

/**
 * This is an implementation of a session id generatod based on Jakarta Tomcat 5.0
 * session id generation.
 * This implementation is thread safe.
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SessionIdGeneratorImpl.java 568 2008-07-31 18:39:20Z sgonzalez $
 *
 * @org.apache.xbean.XBean element="id-generator"
 */

public class SessionIdGeneratorImpl extends AbstractIdGenerator implements SessionIdGenerator {

    private static final Log logger = LogFactory.getLog(SessionIdGeneratorImpl.class);

    private int _sessionIdLength = 16;

    /**
     * Generate and return a new session identifier.
     */
    public synchronized String generateId() {

        byte random[] = new byte[16];

        // Render the result as a String of hexadecimal digits
        StringBuffer result = new StringBuffer();
        int resultLenBytes = 0;
        while (resultLenBytes < _sessionIdLength) {
            getRandomBytes(random);
            random = getDigest().digest(random);
            for (int j = 0;
                 j < random.length && resultLenBytes < _sessionIdLength;
                 j++) {
                byte b1 = (byte) ((random[j] & 0xf0) >> 4);
                byte b2 = (byte) (random[j] & 0x0f);
                if (b1 < 10)
                    result.append((char) ('0' + b1));
                else
                    result.append((char) ('A' + (b1 - 10)));
                if (b2 < 10)
                    result.append((char) ('0' + b2));
                else
                    result.append((char) ('A' + (b2 - 10)));
                resultLenBytes++;
            }
        }
        return (result.toString());

    }


}
