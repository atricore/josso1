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

package org.josso.auth.test;

import org.josso.auth.util.CipherUtil;
import org.junit.Test;


import javax.crypto.spec.SecretKeySpec;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 17, 2008
 * Time: 5:57:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class CipherUtilTest {

    @Test
    public void testAES() throws Exception {
        SecretKeySpec key = CipherUtil.generateAESKey();
        String keyText = CipherUtil.encodeBase64(key.getEncoded());

        String msg = "My Test!";

        String text = CipherUtil.encryptAES(msg, keyText);

        String newMsg = CipherUtil.decryptAES(text, keyText);

        assert msg.equals(newMsg) : "Messages do not match : ["+msg+"] != ["+newMsg+"]";


    }
}
