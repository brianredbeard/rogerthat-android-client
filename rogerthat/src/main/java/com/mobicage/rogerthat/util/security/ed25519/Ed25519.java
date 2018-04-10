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
import org.xbill.DNS.utils.base16;

import java.io.ByteArrayOutputStream;
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
import java.math.BigInteger;

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
        Map<String, Object> keys = createKeyPairForAddress(seedBytes, index);
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keys.get("public_key");
        EdDSAPrivateKey privateKey = (EdDSAPrivateKey) keys.get("private_key");

        final byte[] publicByteSlice = new byte[8];
        publicByteSlice[0] = 32;

        ByteArrayOutputStream conditionBos = new ByteArrayOutputStream();
        conditionBos.write(getAlgorithmBytes());
        conditionBos.write(publicByteSlice);
        conditionBos.write(publicKey.getAbyte());
        byte[] condition = conditionBos.toByteArray();
        byte[] conditionLength = SecurityUtils.longToBytes(Long.reverseBytes(condition.length));
        byte[] conditionDigest = SecurityUtils.blake2b256Digest(conditionLength, condition);

        final byte[] addressHash = new byte[conditionDigest.length + 1];
        addressHash[0] = 1;
        System.arraycopy(conditionDigest, 0, addressHash, 1, conditionDigest.length);

        final byte[] addressBytes = new byte[conditionDigest.length + 7];
        addressBytes[0] = 1;
        System.arraycopy(conditionDigest, 0, addressBytes, 1, conditionDigest.length);
        System.arraycopy(SecurityUtils.blake2b256Digest(addressHash), 0, addressBytes, 1 + conditionDigest.length, 6); // checksum of type, hash
        final String address = SecurityUtils.lowercaseHash(addressBytes);

        String publicKeyString =  Base64.encodeBytes(publicKey.getEncoded(), Base64.DONT_BREAK_LINES);
        String publicKeyAbyte = Base64.encodeBytes(publicKey.getAbyte(), Base64.DONT_BREAK_LINES);

        SecurityUtils.savePublicKey(mainService, ALGORITHM, name, index, publicKeyAbyte);
        SecurityUtils.saveSecureInfo(mainService, pin, ALGORITHM, name, index, publicKeyString, privateKey.getEncoded(), null, address);
        return address;
    }

    public static String createKeyPair(MainService mainService, String pin, String name, String seed) throws Exception {
        if (TextUtils.isEmptyOrWhitespace(seed)) {
            byte[] b = new byte[32];
            new Random().nextBytes(b);
            seed = Mnemonics.seedToString(b);
        }

        final byte[] seedBytes = Mnemonics.stringToSeed(seed);
        Map<String, Object> keys = createKeyPair(seedBytes);
        EdDSAPublicKey publicKey = (EdDSAPublicKey) keys.get("public_key");
        EdDSAPrivateKey privateKey = (EdDSAPrivateKey) keys.get("private_key");

        String publicKeyString =  Base64.encodeBytes(publicKey.getEncoded(), Base64.DONT_BREAK_LINES);
        String publicKeyAbyte = Base64.encodeBytes(publicKey.getAbyte(), Base64.DONT_BREAK_LINES);

        SecurityUtils.savePublicKey(mainService, ALGORITHM, name, null, publicKeyAbyte);
        SecurityUtils.saveSecureInfo(mainService, pin, ALGORITHM, name, null, publicKeyString, privateKey.getEncoded(),
                seedBytes, null);
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

        EdDSAPublicKey pk = (EdDSAPublicKey) publicKey;
        final byte[] publicByteSlice = new byte[8];
        publicByteSlice[0] = 32;

        ByteArrayOutputStream coinInputBos = new ByteArrayOutputStream();
        ByteArrayOutputStream coinOutputBos = new ByteArrayOutputStream();

        long numberOfOutputs = 0;
        for (int i = 0; i < to.data.length; i++) {
            ByteArrayOutputStream conditionBos = new ByteArrayOutputStream();
            conditionBos.write(getAlgorithmBytes());
            conditionBos.write(publicByteSlice);
            conditionBos.write(pk.getAbyte());
            byte[] condition = conditionBos.toByteArray();
            byte[] conditionLength = SecurityUtils.longToBytes(Long.reverseBytes(condition.length));
            byte[] conditionDigest = SecurityUtils.blake2b256Digest(conditionLength, condition);

            coinInputBos.write(base16.fromString(to.data[i].input.parent_id));
            coinInputBos.write(1); // unlock type
            coinInputBos.write(conditionDigest);

            for (int j = 0; j < to.data[i].outputs.length; j++) {
                numberOfOutputs += 1;
            }
        }

        coinOutputBos.write(SecurityUtils.longToBytes(Long.reverseBytes(numberOfOutputs))); // length coin outputs
        for (int i = 0; i < to.data.length; i++) {
          for (int j = 0; j < to.data[i].outputs.length; j++) {
                String unlockHash = to.data[i].outputs[j].unlockhash;
                if (!to.from_address.equals(unlockHash) && !to.to_address.equals(unlockHash)) {
                    throw new Exception("Address did not match");
                }
                byte[] cov = bigIntegerToBytes(new BigInteger(to.data[i].outputs[j].value));
                coinOutputBos.write(SecurityUtils.longToBytes(Long.reverseBytes(cov.length))); // length of bigint
                coinOutputBos.write(cov);
                coinOutputBos.write(copyByteArray(base16.fromString(unlockHash), 33));
            }
        }
        final byte[] coinInputs = coinInputBos.toByteArray();
        final byte[] coinOutputs = coinOutputBos.toByteArray();
        final byte[] minerfees = bigIntegerToBytes(new BigInteger(to.minerfees));

        for (int i = 0; i < to.data.length; i++) {
            final byte[] inputIndex = SecurityUtils.longToBytes(Long.reverseBytes(i));
            final byte[] signatureHash = createTransactionData(inputIndex, coinInputs, coinOutputs, minerfees);
            to.data[i].algorithm = ALGORITHM;
            to.data[i].public_key_index = index;
            to.data[i].public_key = SecurityUtils.lowercaseHash(pk.getAbyte());
            to.data[i].signature_hash = Base64.encodeBytes(signatureHash, Base64.DONT_BREAK_LINES);
        }

        return JSONValue.toJSONString(to.toJSONMap());
    }

    public static byte[] copyByteArray(byte[] data, int size) {
        final byte[] result = new byte[size];
        System.arraycopy(data, 0, result, 0, size);
        return result;
    }

    public static byte[] bigIntegerToBytes(final BigInteger bigInteger) {
        byte[] bytes = bigInteger.toByteArray();
        if (bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }

    private static byte[] createTransactionData(final byte[] inputIndex, final byte[] coinInputs, final byte[] coinOutputs, final byte[] minerfees) throws Exception {
        return SecurityUtils.blake2b256Digest(inputIndex,
                coinInputs,
                coinOutputs,
//                SecurityUtils.longToBytes(Long.reverseBytes(0)), // BlockStakeInputs
                SecurityUtils.longToBytes(Long.reverseBytes(0)), // BlockStakeOutputs
                SecurityUtils.longToBytes(Long.reverseBytes(1)), // length of minerfees
                SecurityUtils.longToBytes(Long.reverseBytes(minerfees.length)), // length of bigint
                minerfees,
                SecurityUtils.longToBytes(Long.reverseBytes(0)));
    }
}
