/*
 * Copyright 2018 GIG Technology NV
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
 * @@license_version:1.4@@
 */
package com.mobicage.rogerthat.util.security;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.mobicage.models.properties.profiles.PublicKeyTO;
import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.config.Configuration;
import com.mobicage.rogerthat.config.ConfigurationProvider;
import com.mobicage.rogerthat.plugins.friends.FriendsPlugin;
import com.mobicage.rogerthat.plugins.security.SecurityPlugin;
import com.mobicage.rogerthat.util.IOUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.ed25519.Ed25519;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rpc.config.AppConstants;
import com.mobicage.to.payment.CryptoTransactionTO;

import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.EdDSASecurityProvider;

import org.jivesoftware.smack.util.Base64;
import org.spongycastle.jcajce.provider.digest.Blake2b;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

    private static final int BUFFER_SIZE = 16384;
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String DEVICE_KEY_ALIAS = "device";

    public static final List<String> PUBLIC_KEYS_ALGORITHMS = Arrays.asList(new String[]{Ed25519.ALGORITHM});

    public static final String CONFIGKEY = "com.mobicage.rogerthat.util.security.SecurityUtils";
    public static final String CONFIG_PIN = "pin";
    public static final String CONFIG_PIN_RETRY_COUNT = "pin_retry_count";
    public static final String CONFIG_PIN_TIMEOUT = "pin_timeout";
    
    private static final byte[] ENCRYPTION_IV = new byte[] { -66, -70, 3, 86, -32, -49, -37, 46, -88, -126, -108, 26,
            113, -37, 27, -111 }; // IV used in 1.0.1013.A for AES encryption

    static {
        if (Build.VERSION.SDK_INT >= 23) {
//            java.security.Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
            java.security.Security.insertProviderAt(new EdDSASecurityProvider(), 1);
        }
    }

    public static byte[] longToBytes(long x) {
        return longToBytes(x, 8);
    }

    public static byte[] longToBytes(long x, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }

    public static String sha256Lower(String value) {
        byte[] data = sha256Digest(value);
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
    }

    public static String sha256(String value) {
        byte[] data = sha256Digest(value);
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

    public static String lowercaseHash(byte[] data) {
        return String.format("%0" + (data.length * 2) + "x", new BigInteger(1, data));
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

    public static byte[] blake2b256Digest(byte[]... data) {
        Blake2b.Blake2b256 digest = new Blake2b.Blake2b256();
        digest.reset();
        for (byte[] bytes : data) {
            digest.update(bytes);
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

    public static String getKeyString(String algorithm, String name, Long index) {
        if (index == null) {
            return algorithm + "." + name;
        } else {
            return algorithm + "." + name + "." + index;
        }
    }

    public static PrivateKey getPrivateKey(MainService mainService, String pin, String key) throws Exception {
        String[] keyData = key.split("\\.");
        String keyAlgorithm;
        String keyName;
        Long keyIndex = null;
        if (keyData.length == 2) {
            keyAlgorithm = keyData[0];
            keyName = keyData[1];
        } else if (keyData.length == 3) {
            keyAlgorithm = keyData[0];
            keyName = keyData[1];
            keyIndex = Long.parseLong(keyData[2]);
        } else {
            L.bug("getPrivateKey failed for key '" + key + "' ...");
            return null;
        }

        return getPrivateKey(mainService, pin, keyAlgorithm, keyName, keyIndex);
    }

    public static PrivateKey getPrivateKey(MainService mainService, String pin, String algorithm, String name, Long index) throws Exception {
        T.UI();

        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        String privateKeyString = securityPlugin.getSecurityKey("private", algorithm, name, index);
        if (privateKeyString == null) {
            return null;
        }

        byte[] decodedPrivateKey = decryptAES(SecurityUtils.md5(pin), deviceDecryptValue(Base64.decode(privateKeyString)));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPrivateKey);

        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getPrivateKey(keySpec);
        }

        return null;
    }

    public static String getSeed(MainService mainService, String pin, String algorithm, String name) throws Exception {
        T.UI();
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        String seedString = securityPlugin.getSecurityKey("seed", algorithm, name, null);
        if (seedString == null) {
            return null;
        }

        byte[] decodedSeed = decryptAES(SecurityUtils.md5(pin), deviceDecryptValue(Base64.decode(seedString)));

        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getSeed(decodedSeed);
        }

        return null;
    }

    public static List<Map<String, String>> listAddress(MainService mainService, String algorithm, String name) throws Exception {
        T.UI();
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        return securityPlugin.listAddresses(algorithm, name);
    }

    public static String getAddress(MainService mainService, String algorithm, String name, long index) throws Exception {
        T.UI();
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        return securityPlugin.getSecurityKey("address", algorithm, name, index);
    }

    public static String getAddress(MainService mainService, String pin, String algorithm, String name, long index) throws Exception {
        T.UI();
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        String seedString = securityPlugin.getSecurityKey("seed", algorithm, name, null);
        if (seedString == null) {
            return null;
        }

        String address = securityPlugin.getSecurityKey("address", algorithm, name, index);
        if (address != null) {
            return address;
        }

        byte[] decodedSeed = decryptAES(SecurityUtils.md5(pin), deviceDecryptValue(Base64.decode(seedString)));

        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getAddress(mainService, pin, name, index, decodedSeed);
        }
        return null;
    }

    public static PublicKey getPublicKey(MainService mainService, String algorithm, String name, Long index) throws Exception {
        T.UI();
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        String publicKeyString = securityPlugin.getSecurityKey("public", algorithm, name, index);
        if (publicKeyString == null) {
            return null;
        }

        byte[] decodedPublicKey = Base64.decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedPublicKey);

        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getPublicKey(keySpec);
        }
        return null;
    }

    public static String getPublicKeyString(MainService mainService, String algorithm, String name, Long index) throws Exception {
        PublicKey key = getPublicKey(mainService, algorithm, name, index);
        if (key == null) {
            return null;
        }
        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Base64.encodeBytes(((EdDSAPublicKey) key).getAbyte(), Base64.DONT_BREAK_LINES);
        }
        return null;
    }

    public static Signature getSignature(String algorithm) throws Exception {
        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getSignature();
        }
        return null;
    }

    public static byte[] getPayload(String algorithm, byte[] payload) throws Exception {
        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getPayload(payload);
        }
        return null;
    }

    public static byte[] getPayload(String algorithm, byte[]... payload) throws Exception {
        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getPayload(payload);
        }
        return null;
    }

    public static byte[] getPayload(String algorithm, String payload) throws Exception {
        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.getPayload(payload.getBytes());
        }
        return null;
    }

    public static byte[] getPayload(String algorithm, File f) throws IOException {
        if (Ed25519.ALGORITHM.equals(algorithm)) {
            Blake2b.Blake2b256 digest = new Blake2b.Blake2b256();
            return IOUtils.digest(digest, f);
        }
        return null;
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

    private static byte[] deviceEncryptValue(byte[] value) throws Exception  {
        PublicKey publicKey = getDevicePublicKey();
        OAEPParameterSpec sp = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, sp);
        return cipher.doFinal(value);
    }
    
    private static byte[] deviceDecryptValue(byte[] value) throws Exception {
        PrivateKey privateKey = getDevicePrivateKey();
        OAEPParameterSpec sp = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-1"), PSource.PSpecified.DEFAULT);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey, sp);
        return cipher.doFinal(value);
    }

    @TargetApi(23)
    public static void setupKeyStore() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                DEVICE_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .build();
        keyPairGenerator.initialize(spec);
        keyPairGenerator.generateKeyPair();
    }

    public static boolean isPinSet(MainService mainService) {
        T.UI();
        Configuration cfg = getConfiguration(mainService);
        return cfg.get(CONFIG_PIN, null) != null;
    }

    public static boolean isValidPin(MainService mainService, String pin) throws Exception {
        T.UI();
        Configuration cfg = getConfiguration(mainService);
        String correctPinString = cfg.get(CONFIG_PIN, null);
        return correctPinString != null && correctPinString.equals(sha256(pin));
    }

    public static void setPin(MainService mainService, String pin) throws Exception {
        if (isPinSet(mainService)) {
            throw new Exception("Pin was already set");
        }

        PrivateKey tmpDevicePrivateKey = getDevicePrivateKey();
        if (tmpDevicePrivateKey == null) {
            setupKeyStore();
        }

        ConfigurationProvider configProvider = mainService.getConfigurationProvider();
        Configuration cfg = configProvider.getConfiguration(CONFIGKEY);

        cfg.put(CONFIG_PIN, sha256(pin));
        configProvider.updateConfigurationNow(CONFIGKEY, cfg);
    }

    public static boolean createKeyAlgorithmSupported(String algorithm) {
        return PUBLIC_KEYS_ALGORITHMS.contains(algorithm);
    }

    public static String createKeyPair(MainService mainService, String pin, String algorithm, String name, String seed) throws Exception {
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        if (!securityPlugin.isValidName(name)) {
            L.i("Cannot create keyPair. Invalid name (a-z A-Z 0-9)");
            return null;
        }

        if (Ed25519.ALGORITHM.equals(algorithm)) {
            return Ed25519.createKeyPair(mainService, pin, name, seed);
        } else {
            L.e("Creating a keypair with an algorithm named '" + algorithm + "' is not supported.");
            return null;
        }
    }

    public static boolean hasKey(MainService mainService, String type, String algorithm, String name, Long index) {
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        return securityPlugin.hasSecurityKey(type, algorithm, name, index);
    }
    
    public static String createTransactionData(MainService mainService, String algorithm, final String name,
                                               final Long index, final CryptoTransactionTO data) throws Exception {

        if (Ed25519.ALGORITHM.equals(algorithm)) {
            PublicKey publicKey = SecurityUtils.getPublicKey(mainService, algorithm, name, index);
            String address = getAddress(mainService, algorithm, name, index);
            if (publicKey != null && address != null) {
                return Ed25519.createTransactionData(publicKey, index, address, data);
            }
            L.e("Creating transaction data failed public key or addres was null.");
        } else {
            L.e("Creating transaction data with an algorithm named '" + algorithm + "' is not supported.");
        }
        return null;
    }

    public static void savePublicKey(MainService mainService, String algorithm, final String name, final Long index, final String publicKey) {
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        FriendsPlugin friendsPlugin = mainService.getPlugin(FriendsPlugin.class);

        PublicKeyTO[] publicKeys = new PublicKeyTO[1];

        PublicKeyTO pk = new PublicKeyTO();
        pk.algorithm = algorithm;
        pk.name = name;
        pk.index = securityPlugin.getIndexString(index);
        pk.public_key = publicKey;
        publicKeys[0] = pk;
        friendsPlugin.setSecureInfo(publicKeys);
    }

    public static void saveSecureInfo(MainService mainService, final String pin, final String algorithm,
                                      final String name, final Long index, final String publicKey, final byte[] privateKey,
                                      final byte[] seed, final String address) throws Exception {

        final String privateKeyData = Base64.encodeBytes(deviceEncryptValue(encryptAES(SecurityUtils.md5(pin), privateKey)), Base64.DONT_BREAK_LINES);
        final String seedData;
        if (seed == null) {
            seedData = null;
        } else {
            seedData = Base64.encodeBytes(deviceEncryptValue(encryptAES(SecurityUtils.md5(pin), seed)), Base64.DONT_BREAK_LINES);
        }
        SecurityPlugin securityPlugin = mainService.getPlugin(SecurityPlugin.class);
        securityPlugin.saveSecurityKey(algorithm, name, index, publicKey, privateKeyData, seedData, address);
    }

    public static boolean usesAppWidePinCode() {
        return AppConstants.Security.ENABLED && AppConstants.Security.APP_KEY_NAME != null
                && AppConstants.Security.APP_KEY_ALGORITHM != null;
    }

}
