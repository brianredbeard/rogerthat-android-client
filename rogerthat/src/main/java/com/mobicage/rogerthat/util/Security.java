/*
 * Copyright 2017 Mobicage NV
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
 * @@license_version:1.2@@
 */
package com.mobicage.rogerthat.util;

import android.annotation.SuppressLint;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;

import org.jivesoftware.smack.util.Base64;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Security {

    private static final int BUFFER_SIZE = 16384;
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String DEVICE_KEY_ALIAS = "device";
    public static final String CONFIGKEY = "com.mobicage.rogerthat.util.Security";
    public static final String CONFIG_PIN = "pin";
    public static final String CONFIG_PUBLIC_KEY = "public_key";
    public static final String CONFIG_PRIVATE_KEY = "private_key";
    public static final String CONFIG_PIN_RETRY_COUNT = "pin_retry_count";
    public static final String CONFIG_PIN_TIMEOUT = "pin_timeout";
    
    private static final byte[] ENCRYPTION_IV = new byte[] { -66, -70, 3, 86, -32, -49, -37, 46, -88, -126, -108, 26,
            113, -37, 27, -111 }; // IV used in 1.0.1013.A for AES encryption

    static {
        if (Build.VERSION.SDK_INT >= 23) {
            java.security.Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        }
    }

    public static String sha256Lower(String value) {
        byte[] data = sha256Digest(value);
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
    }

    public static String sha256(String value) {
        byte[] data = sha256Digest(value);
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static byte[] sha256Digest(String value) {
        return sha256Digest(value.getBytes());
    }

    public static byte[] sha256Digest(byte[] value) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            L.bug(e1);
            return null;
        }
        digest.reset();
        return digest.digest(value);
    }

    public static byte[] sha256Digest(byte[]... data) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            L.bug(e1);
            return null;
        }
        digest.reset();
        for (byte[] bytes : data) {
            digest.update(bytes);
        }
        return digest.digest();
    }

    public static byte[] sha256Digest(File f) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e1) {
            L.bug(e1);
            return null;
        }
        digest.reset();
        return IOUtils.digest(digest, f);
    }


    public static byte[] md5(String value) {
        MessageDigest digest;
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

    public static byte[] decryptAES(byte[] key, byte[] data) throws Exception {
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(ENCRYPTION_IV);
        final SecretKey secretKey = getAESKey(key);

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(data);
    }

    public static byte[] encryptAES(byte[] key, byte[] data) throws Exception {
        final IvParameterSpec ivParameterSpec = new IvParameterSpec(ENCRYPTION_IV);
        final SecretKey secretKey = getAESKey(key);

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(data);
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

    public static boolean isPinSet(MainService mainService) {
        T.UI();
        Configuration cfg = getConfiguration(mainService);
        return cfg.get(CONFIG_PIN, null) != null && cfg.get(CONFIG_PRIVATE_KEY, null) != null;
    }

    public static boolean isValidPin(MainService mainService, String pin) throws Exception {
        T.UI();
        Configuration cfg = getConfiguration(mainService);
        String correctPinString = cfg.get(CONFIG_PIN, null);
        return correctPinString != null && correctPinString.equals(sha256(pin));
    }

    public static PrivateKey getPrivateKey(MainService mainService, String pin) throws Exception{
        T.UI();
        Configuration cfg = getConfiguration(mainService);
        String privateKeyString = cfg.get(CONFIG_PRIVATE_KEY, null);
        if (privateKeyString == null) {
            return null;
        }

        byte[] encryptedPrivateKey = decryptAES(Security.md5(pin), Base64.decode(privateKeyString));
        byte[] decodedPrivateKey = deviceDecryptValue(encryptedPrivateKey);

        KeyFactory fact = KeyFactory.getInstance("ECDSA", "SC");
        return fact.generatePrivate(new PKCS8EncodedKeySpec(decodedPrivateKey));
    }

    public static PublicKey getPublicKey(MainService mainService) throws Exception {
        T.UI();
        Configuration cfg = getConfiguration(mainService);
        String publicKeyString = cfg.get(CONFIG_PUBLIC_KEY, null);
        if (publicKeyString == null) {
            return null;
        }

        byte[] decodedPubliceKey = Base64.decode(publicKeyString);
        KeyFactory fact = KeyFactory.getInstance("ECDSA", "SC");
        return fact.generatePublic(new X509EncodedKeySpec(decodedPubliceKey));
    }

    public static Configuration getConfiguration(MainService mainService) {
        T.UI();
        ConfigurationProvider configProvider = mainService.getConfigurationProvider();
        return configProvider.getConfiguration(CONFIGKEY);
    }

    public static void setPinRetry(MainService mainService, long pinRetryCount, long pinTimeout) {
        T.UI();
        ConfigurationProvider configProvider = mainService.getConfigurationProvider();
        Configuration cfg = configProvider.getConfiguration(CONFIGKEY);
        cfg.put(CONFIG_PIN_RETRY_COUNT, pinRetryCount);
        cfg.put(CONFIG_PIN_TIMEOUT, pinTimeout);
        configProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    private static KeyStore createKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return keyStore;
    }

    private static PrivateKey getDevicePrivateKey() throws Exception  {
        KeyStore ks = createKeyStore();
        return (PrivateKey) ks.getKey(DEVICE_KEY_ALIAS, null);
    }

    public static PublicKey getDevicePublicKey() throws Exception  {
        KeyStore ks = createKeyStore();
        return ks.getCertificate(DEVICE_KEY_ALIAS).getPublicKey();
    }

    private static byte[] encryptValue(PublicKey publicKey, byte[] value) throws Exception  {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        cipher.update(value);
        return cipher.doFinal();
    }

    private static byte[] deviceEncryptValue(byte[] value) throws Exception  {
        PublicKey publicKey = getDevicePublicKey();
        return encryptValue(publicKey,  value);
    }

    private static byte[] deviceDecryptValue(byte[] value) throws Exception {
        PrivateKey privateKey = getDevicePrivateKey();
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        cipher.update(value);
        return cipher.doFinal();
    }

    public static void setupKeyStore() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        DEVICE_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT |
                                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                        .build());
        keyPairGenerator.generateKeyPair();
    }

    public static void setPin(MainService mainService, String pin) throws Exception {
        if (isPinSet(mainService)) {
            throw new Exception("Pin was already set");
        }

        PrivateKey tmpDevicePrivateKey = getDevicePrivateKey();
        if (tmpDevicePrivateKey == null) {
            setupKeyStore();
        }

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "SC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        keyGen.initialize(ecSpec, new SecureRandom());

        KeyPair kp = keyGen.generateKeyPair();
        String publicKeyString =  Base64.encodeBytes(kp.getPublic().getEncoded(), Base64.DONT_BREAK_LINES);
        byte[] decodedPrivateKey = kp.getPrivate().getEncoded();

        ConfigurationProvider configProvider = mainService.getConfigurationProvider();
        Configuration cfg = configProvider.getConfiguration(CONFIGKEY);
        saveSecureInfo(configProvider, cfg, publicKeyString, pin, decodedPrivateKey);

        FriendsPlugin friendsPlugin = mainService.getPlugin(FriendsPlugin.class);
        friendsPlugin.setSecureInfo(publicKeyString);
    }
    
    private static void saveSecureInfo(ConfigurationProvider configProvider, Configuration cfg, final String publicKey, final String pin, final byte[] privateKey) throws Exception {
        cfg.put(CONFIG_PIN, sha256(pin));
        cfg.put(CONFIG_PUBLIC_KEY, publicKey);
        cfg.put(CONFIG_PRIVATE_KEY, Base64.encodeBytes(encryptAES(Security.md5(pin), deviceEncryptValue(privateKey)), Base64.DONT_BREAK_LINES));
        configProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }
}
