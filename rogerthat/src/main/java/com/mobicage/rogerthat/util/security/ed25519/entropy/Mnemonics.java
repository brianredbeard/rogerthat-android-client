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

package com.mobicage.rogerthat.util.security.ed25519.entropy;

import android.text.TextUtils;

import com.mobicage.rogerthat.util.security.SecurityUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Mnemonics {

    public static byte[] stringToSeed(String str) throws Exception {
        return fromPhrase(str.split(" "));
    }

    public static String seedToString(byte[] seedBytes) throws Exception {
        final int entropySize = 32;
        final int seedChecksumSize = 6;
        final byte[] seed = new byte[entropySize + seedChecksumSize];
        System.arraycopy(seedBytes, 0, seed, 0, entropySize);

        final byte[] fullChecksum = new byte[seedChecksumSize];
        System.arraycopy(SecurityUtils.blake2b256Digest(seedBytes), 0, fullChecksum, 0, seedChecksumSize);

        System.arraycopy(fullChecksum, 0, seed, entropySize, seedChecksumSize);
        return TextUtils.join(" ", toPhrase(seed));
    }

    public static byte[] fromPhrase(String[] phrase) throws Exception {
        BigInteger entropy = phraseToInt(phrase);
        return intToBytes(entropy);
    }

    public static String[] toPhrase(byte[] entropy) throws Exception {
        BigInteger intEntropy = bytesToInt(entropy);
        return intToPhrase(intEntropy);
    }

    public static BigInteger phraseToInt(String[] phrase) throws Exception {
        BigInteger base = BigInteger.valueOf(English.WORDS.size());
        BigInteger exp = BigInteger.ONE;
        BigInteger result = BigInteger.valueOf(-1);
        boolean found = false;

        for (String word : phrase) {
            String prefix = word.substring(0, English.UNIQUE_PREFIX_LENGTH);

            BigInteger index = BigInteger.valueOf(0);
            for (int i = 0; i < English.WORDS.size(); i++) {
                String entropyWord = English.WORDS.get(i);
                if (entropyWord.startsWith(prefix)) {
                    index = BigInteger.valueOf(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new Exception("Error unknown word: " + word);
            }

            index = index.add(BigInteger.ONE);
            index = index.multiply(exp);
            exp = exp.multiply(base);
            result = result.add(index);
        }
        return result;
    }

    public static String[] intToPhrase(BigInteger intEntropy) throws Exception {
        BigInteger base = BigInteger.valueOf(English.WORDS.size());
        List<String> result = new ArrayList<>();
        while (intEntropy.compareTo(base) >= 0) {
            int i = intEntropy.mod(base).intValue();
            result.add(English.WORDS.get(i));
            intEntropy = intEntropy.subtract(base);
            intEntropy = intEntropy.divide(base);
        }
        result.add(English.WORDS.get(intEntropy.intValue()));
        return result.toArray(new String[0]);
    }

    public static BigInteger bytesToInt(byte[] entropy) {
        BigInteger base = BigInteger.valueOf(256);
        BigInteger exp = BigInteger.ONE;
        BigInteger result = BigInteger.valueOf(-1);
        for (int i = 0; i < entropy.length; i++) {
            BigInteger index = BigInteger.valueOf(entropy[i] & 0xFF);
            index = index.add(BigInteger.ONE);
            index = index.multiply(exp);
            exp = exp.multiply(base);
            result = result.add(index);
        }
        return result;
    }

    public static byte[] intToBytes(BigInteger entropy) {
        BigInteger base = BigInteger.valueOf(256);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        while (entropy.compareTo(base) >= 0) {
            int i = entropy.mod(base).intValue();
            bs.write(i);
            entropy = entropy.subtract(base);
            entropy = entropy.divide(base);
        }
        bs.write(entropy.intValue());
        return bs.toByteArray();
    }
}
