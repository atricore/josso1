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

package org.josso.auth.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.*;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: sgonzalez
 * Date: Nov 17, 2008
 * Time: 5:06:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class CipherUtil {

    /**
     * This generates a 128 AES key.
     *
     * @throws NoSuchAlgorithmException
     */
    public static SecretKeySpec generateAESKey() throws NoSuchAlgorithmException {

        SecretKeySpec skeySpec;

        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();

        skeySpec = new SecretKeySpec(key, "AES");

        return skeySpec;
    }

    /**
     * Creates an ecnrypted string using AES of the given message.  The string is encoded using base 64.
     */
    public static String encryptAES(String msg, String base64Key) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        byte[] key = decodeBase64 (base64Key);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] text = cipher.doFinal(msg.getBytes());

        return encodeBase64(text);
    }

    /**
     * Decrypts the given text using AES 
     */
    public static String decryptAES(String base64text, String base64Key) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        byte[] key = decodeBase64 (base64Key);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] text = decodeBase64(base64text);
        byte[] msg = cipher.doFinal(text);

        return new String (msg);

    }


    /**
     * Base64 encoding.  Charset ISO-8859-1 is assumed.
     */
    public static String encodeBase64(byte[] bytes) throws UnsupportedEncodingException {
        byte[] enc = Base64.encodeBase64(bytes);
        return new String(enc);
    }

    /**
     * Base64 encoding.  Charset ISO-8859-1 is assumed.
     */
    public static byte[] decodeBase64(String text) throws UnsupportedEncodingException {
        byte[] bin = Base64.decodeBase64(text.getBytes());
        return bin;
    }


    /**
     * Base16 encoding (HEX).
     */
    public static String encodeBase16(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            // top 4 bits
            char c = (char) ((b >> 4) & 0xf);
            if (c > 9)
                c = (char) ((c - 10) + 'a');
            else
                c = (char) (c + '0');
            sb.append(c);
            // bottom 4 bits
            c = (char) (b & 0xf);
            if (c > 9)
                c = (char) ((c - 10) + 'a');
            else
                c = (char) (c + '0');
            sb.append(c);
        }
        return sb.toString();
    }


}
