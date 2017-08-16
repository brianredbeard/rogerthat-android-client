/*
 * Copyright 2017 GIG Technology NV
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
 * @@license_version:1.3@@
 */
package com.mobicage.rogerthat.util.security.ed25519;

import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.TextUtils;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.security.SecurityUtils;
import com.mobicage.rogerthat.util.security.ed25519.entropy.Mnemonics;
import com.mobicage.to.payment.CryptoTransactionTO;

import net.i2p.crypto.eddsa.EdDSAEngine;
import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.math.GroupElement;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import org.jivesoftware.smack.util.Base64;
import org.json.simple.JSONValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Ed25519 {

    public static final String ALGORITHM = "ed25519";

    public static PrivateKey getPrivateKey(PKCS8EncodedKeySpec keySpec) throws Exception {
        return new EdDSAPrivateKey(keySpec);
    }

    public static PublicKey getPublicKey(X509EncodedKeySpec keySpec) throws Exception {
        return new EdDSAPublicKey(keySpec);
    }

    public static Signature getSignature() throws Exception {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ALGORITHM);
        return new EdDSAEngine(MessageDigest.getInstance(spec.getHashAlgorithm()));
    }

    public static byte[] getPayload(byte[] data) throws Exception {
        return SecurityUtils.blake2b256Digest(data);
    }

    public static byte[] getPayload(byte[]... data) throws Exception {
        return SecurityUtils.blake2b256Digest(data);
    }

    public static String getSeed(byte[] seedBytes) throws Exception {
        return Mnemonics.seedToString(seedBytes);
    }

    public static String getAddress(MainService mainService, String pin, String name, long index, byte[] seedBytes) throws Exception {
        final byte[] algorithmBytes = new byte[16];
        byte[] originalAlgorithmBytes = ALGORITHM.getBytes();
        System.arraycopy(originalAlgorithmBytes, 0, algorithmBytes, 0, originalAlgorithmBytes.length);

        Map<String, Object> keys = createKeyPairForAddress(seedBytes, index);
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keys.get("public_key");
        EdDSAPrivateKey pKey = (EdDSAPrivateKey) keys.get("private_key");

        byte[] timelock = SecurityUtils.longToBytes(0);

        final byte[] publicByteSlice = new byte[8];
        publicByteSlice[0] = 32;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(algorithmBytes);
        bos.write(publicByteSlice);
        bos.write(publicKey.getAbyte());
        byte[] siaPublicKeys = bos.toByteArray();

        byte[] signaturesRequired = SecurityUtils.longToBytes(Long.reverseBytes(1));

        List<byte[]> hashList = new ArrayList<>();
        hashList.add(SecurityUtils.blake2b256Digest(addH(0, timelock)));
        hashList.add(SecurityUtils.blake2b256Digest(addH(0,siaPublicKeys)));
        hashList.add(SecurityUtils.blake2b256Digest(addH(0, signaturesRequired)));

        MerkleTree mt = new MerkleTree(hashList);
        byte[] mtRoot = mt.getRoot().sig;

        final byte[] mtRootFullchecksum = new byte[mtRoot.length + 6];
        System.arraycopy(mtRoot, 0, mtRootFullchecksum, 0, mtRoot.length);
        System.arraycopy(SecurityUtils.blake2b256Digest(mtRoot), 0, mtRootFullchecksum, mtRoot.length, 6);

        final String address = SecurityUtils.lowercaseHash(mtRootFullchecksum);

        String publicKeyString =  Base64.encodeBytes(publicKey.getEncoded(), Base64.DONT_BREAK_LINES);
        byte[] decodedPrivateKey = pKey.getEncoded();

        SecurityUtils.savePublicKey(mainService, ALGORITHM, name, index, publicKeyString);
        SecurityUtils.saveSecureInfo(mainService, pin, ALGORITHM, name, index, publicKeyString, decodedPrivateKey, null, address);
        return address;
    }

    public static String createKeyPair(MainService mainService, String pin, String name, String seed) throws Exception {
        if (TextUtils.isEmptyOrWhitespace(seed)) {
            byte[] b = new byte[32];
            new Random().nextBytes(b);
            seed = Mnemonics.seedToString(b);
        }
        final byte[] checksumSeedBytes = Mnemonics.stringToSeed(seed);
        final int entropySize = 32;
        final int seedChecksumSize = 6;

        final byte[] seedBytes = new byte[entropySize];
        System.arraycopy(checksumSeedBytes, 0, seedBytes, 0, entropySize);

        final byte[] fullChecksum = new byte[seedChecksumSize];
        System.arraycopy(SecurityUtils.blake2b256Digest(seedBytes), 0, fullChecksum, 0, seedChecksumSize);

        final byte[] checksumBytes = new byte[seedChecksumSize];
        System.arraycopy(checksumSeedBytes, entropySize, checksumBytes, 0, seedChecksumSize);

        if (checksumSeedBytes.length != entropySize + seedChecksumSize || !Arrays.equals(fullChecksum, checksumBytes)) {
            throw new Exception("checksum seed length is wrong");
        }

        Map<String, Object> keys = createKeyPair(seedBytes);
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keys.get("public_key");
        EdDSAPrivateKey pKey = (EdDSAPrivateKey) keys.get("private_key");

        String publicKeyString =  Base64.encodeBytes(publicKey.getEncoded(), Base64.DONT_BREAK_LINES);
        byte[] decodedPrivateKey = pKey.getEncoded();

        SecurityUtils.savePublicKey(mainService, ALGORITHM, name, null, publicKeyString);
        SecurityUtils.saveSecureInfo(mainService, pin, ALGORITHM, name, null, publicKeyString, decodedPrivateKey, seedBytes, null);
        return publicKeyString;
    }

    public static Map<String, Object> createKeyPair(byte[] seedBytes) throws Exception {
        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ALGORITHM);

        EdDSAPrivateKeySpec privateKey = new EdDSAPrivateKeySpec(seedBytes, spec);
        Map<String, Object> info = new HashMap<>();
        info.put("public_key", new EdDSAPublicKey(new EdDSAPublicKeySpec(privateKey.getA(), spec)));
        info.put("private_key", new EdDSAPrivateKey(privateKey));
        return info;
    }

    public static Map<String, Object> createKeyPairForAddress(byte[] seedBytes, long index) throws Exception {
        final byte[] entropy = SecurityUtils.blake2b256Digest(seedBytes, SecurityUtils.longToBytes(Long.reverseBytes(index)));

        EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName(ALGORITHM);

        MessageDigest hash = MessageDigest.getInstance(spec.getHashAlgorithm());
        final byte[] h = hash.digest(entropy);

        h[0] &= 248;
        h[31] &= 127;
        h[31] |= 64;

        final byte[] a = Arrays.copyOfRange(h, 0, 32);
        GroupElement A = spec.getB().scalarMultiply(a);

        EdDSAPrivateKeySpec privateKey = new EdDSAPrivateKeySpec(entropy, h, a, A, spec);

        Map<String, Object> info = new HashMap<>();
        info.put("public_key", new EdDSAPublicKey(new EdDSAPublicKeySpec(privateKey.getA(), spec)));
        info.put("private_key", new EdDSAPrivateKey(privateKey));
        return info;
    }

    private static byte[] getAlgorithmBytes() {
        final byte[] algorithmBytes = new byte[16];
        byte[] originalAlgorithmBytes = ALGORITHM.getBytes();
        System.arraycopy(originalAlgorithmBytes, 0, algorithmBytes, 0, originalAlgorithmBytes.length);
        return algorithmBytes;
    }

    public static String createTransactionData(final PublicKey publicKey, final long index, final String address, final CryptoTransactionTO to) throws Exception {
        if (!to.from_address.equals(address)) {
            L.d(String.format("Address %s did not match %s", to.from_address, address));
            throw new Exception("Address did not match");
        }

        byte[] signaturesRequired = SecurityUtils.longToBytes(Long.reverseBytes(1));

        EdDSAPublicKey pk = (EdDSAPublicKey) publicKey;
        String publicKeyString =  Base64.encodeBytes(publicKey.getEncoded(), Base64.DONT_BREAK_LINES);
        final byte[] publicByteSlice = new byte[8];
        publicByteSlice[0] = 32;

        ByteArrayOutputStream coinInputBos = new ByteArrayOutputStream();
        ByteArrayOutputStream coinOutputBos = new ByteArrayOutputStream();

        for (int i = 0; i < to.data.length; i++) {
            coinInputBos.write(to.data[i].input.parent_id.getBytes());
            coinInputBos.write(SecurityUtils.longToBytes(Long.reverseBytes(to.data[i].input.timelock)));
            coinInputBos.write(getAlgorithmBytes());
            coinInputBos.write(publicByteSlice);
            coinInputBos.write(pk.getAbyte());
            coinInputBos.write(signaturesRequired);

            for (int j = 0; j < to.data[i].outputs.length; j++) {
                String unlockHash = to.data[i].outputs[j].unlockhash;
                if (!to.from_address.equals(unlockHash) && !to.to_address.equals(unlockHash)) {
                    throw new Exception("Address did not match");
                }

                coinOutputBos.write(to.data[i].outputs[j].value.getBytes());
                coinOutputBos.write(unlockHash.getBytes());
            }
        }
        final byte[] coinInputs = coinInputBos.toByteArray();
        final byte[] coinOutputs = coinOutputBos.toByteArray();
        final byte[] minerfees = to.minerfees.getBytes();
        final byte[] publicKeyIndex = SecurityUtils.longToBytes(Long.reverseBytes(index));

        for (int i = 0; i < to.data.length; i++) {
            final byte[] parentId = to.data[i].input.parent_id.getBytes();
            final byte[] timelock = SecurityUtils.longToBytes(Long.reverseBytes(to.data[i].timelock));
            final byte[] signatureHash = createTransactionData(publicKeyIndex, timelock, coinInputs, coinOutputs, minerfees, parentId);
            to.data[i].algorithm = ALGORITHM;
            to.data[i].public_key_index = index;
            to.data[i].public_key = publicKeyString;
            to.data[i].signature_hash = Base64.encodeBytes(signatureHash, Base64.DONT_BREAK_LINES);
        }

        return JSONValue.toJSONString(to.toJSONMap());
    }

    private static byte[] createTransactionData(final byte[] fromPublicKeyIndex, final byte[] timelock, final byte[] coinInputs, final byte[] coinOutputs, final byte[] minerfees, final byte[] parentId) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(coinInputs);
        bos.write(coinOutputs);
        bos.write(minerfees);
        bos.write(parentId);
        bos.write(fromPublicKeyIndex);
        bos.write(timelock);
        return bos.toByteArray();
    }

    private static byte[] addH(int h, byte[] data) {
        final byte[] b = new byte[data.length + 1];
        b[0] = (byte) h;
        System.arraycopy(data, 0, b, 1, data.length);
        return b;
    }
}
