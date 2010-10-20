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

package org.josso.selfservices;

import org.josso.util.id.AbstractIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @org.apache.xbean.XBean element="id-generator"
 *
 * @author <a href="mailto:sgonzalez@josso.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SelfServicesIdGeneratorImpl.java 974 2009-01-14 00:39:45Z sgonzalez $
 */
public class SelfServicesIdGeneratorImpl extends AbstractIdGenerator {

    private static final Log logger = LogFactory.getLog(SelfServicesIdGeneratorImpl.class);

    private int assertionIdLength = 8;

    /**
     * Generate and return a new assertion identifier.
     */
    public synchronized String generateId() {

        byte random[] = new byte[16];

        // Render the result as a String of hexadecimal digits
        StringBuffer result = new StringBuffer();
        int resultLenBytes = 0;
        while (resultLenBytes < assertionIdLength) {
            getRandomBytes(random);
            random = getDigest().digest(random);
            for (int j = 0;
                 j < random.length && resultLenBytes < assertionIdLength;
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
