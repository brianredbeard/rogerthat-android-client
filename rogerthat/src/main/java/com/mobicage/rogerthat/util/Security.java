/*
 * Copyright 2016 Mobicage NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.1@@
 */
package com.mobicage.rogerthat.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;

import com.mobicage.rogerthat.util.logging.L;

public class Security {

    private static final int BUFFER_SIZE = 16384;
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String sha256(String value) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            L.bug(e1);
            return "";
        }
        digest.reset();
        byte[] data = digest.digest(value.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static byte[] md5(String value) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(value.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e1) {
            L.bug(e1);
            return new byte[] { -51, 125, 111, -19, -17, -20, 29, 2, -86, -118, 99, -76, 7, -123, -36, -85 };
        } catch (UnsupportedEncodingException e) {
            L.bug(e);
            return new byte[] { -51, 125, 111, -19, -17, -20, 29, 2, -86, -118, 99, -76, 7, -123, -36, -85 };
        }
        return digest.digest();
    }

    public static void decryptAES(byte[] key, byte[] iv, InputStream is, OutputStream os) throws Exception {
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        final SecretKey secretKey = getAESKey(key);

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        CipherOutputStream cos = new CipherOutputStream(os, cipher);
        try {
            IOUtils.copy(is, cos, BUFFER_SIZE);
            cos.flush();
        } finally {
            cos.close();
        }
    }

    @SuppressLint("TrulyRandom")
    public static void encryptAES(byte[] key, byte[] iv, InputStream is, OutputStream os) throws Exception {
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        final SecretKey secretKey = getAESKey(key);

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        try {
            IOUtils.copy(cis, os, BUFFER_SIZE);
            os.flush();
        } finally {
            cis.close();
        }
    }

    private static SecretKey getAESKey(byte[] bytes) throws Exception {
        return new SecretKeySpec(bytes, "AES");
    }

    public static String generateRandomString(int len) {
        final SecureRandom rnd = new SecureRandom();
        final StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

}
