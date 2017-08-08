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
package com.mobicage.rogerthat.util.security.secp256k1;


import com.mobicage.rogerthat.MainService;
import com.mobicage.rogerthat.util.security.SecurityUtils;

import org.jivesoftware.smack.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Secp256k1 {

    public static final String ALGORITHM = "secp256k1";

    public static PrivateKey getPrivateKey(PKCS8EncodedKeySpec keySpec) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("ECDSA", "SC");
        return fact.generatePrivate(keySpec);
    }

    public static PublicKey getPublicKey(X509EncodedKeySpec keySpec) throws Exception {
        KeyFactory fact = KeyFactory.getInstance("ECDSA", "SC");
        return fact.generatePublic(keySpec);
    }

    public static Signature getSignature() throws Exception {
        return Signature.getInstance("SHA256withECDSA");
    }

    public static byte[] getPayload(byte[] data) throws Exception {
        return SecurityUtils.sha256Digest(data);
    }

    public static String createKeyPair(MainService mainService, String pin, String name) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "SC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(ALGORITHM);
        keyGen.initialize(ecSpec, new SecureRandom());

        KeyPair kp = keyGen.generateKeyPair();
        String publicKeyString =  Base64.encodeBytes(kp.getPublic().getEncoded(), Base64.DONT_BREAK_LINES);
        byte[] decodedPrivateKey = kp.getPrivate().getEncoded();

        SecurityUtils.savePublicKey(mainService, ALGORITHM, name, null, publicKeyString);
        SecurityUtils.saveSecureInfo(mainService, pin, ALGORITHM, name, null, publicKeyString, decodedPrivateKey, null, null);
        return publicKeyString;
    }
}
