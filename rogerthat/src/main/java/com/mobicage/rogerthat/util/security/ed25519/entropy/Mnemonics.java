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

package com.mobicage.rogerthat.util.security.ed25519.entropy;

import com.mobicage.rogerthat.util.security.SecurityUtils;

import java.util.Arrays;
import java.util.StringTokenizer;

public class Mnemonics {

    public static byte[] stringToSeed(String mnemonic) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(mnemonic);
        int nt = tokenizer.countTokens ();
        if ( nt % 6 != 0 ) {
            throw new Exception("invalid mnemonic - word cound not divisible by 6");
        }
        boolean[] bits = new boolean[11 * nt];
        int i = 0;
        while (tokenizer.hasMoreElements ()) {
            int c = Arrays.binarySearch(English.WORDS, tokenizer.nextToken());
            for (int j = 0; j < 11; ++j) {
                bits[i++] = (c & (1 << (10 - j))) > 0;
            }
        }
        byte[] data = new byte[bits.length / 33 * 4];
        for (i = 0; i < bits.length / 33 * 32; ++i) {
            data[i / 8] |= (bits[i] ? 1 : 0) << (7 - (i % 8));
        }
        byte[] check = SecurityUtils.sha256Digest(data);
        for (i = bits.length / 33 * 32; i < bits.length; ++i) {
            if ((check[(i - bits.length / 33 * 32) / 8] & (1 << (7 - (i % 8))) ^ (bits[i] ? 1 : 0) << (7 - (i % 8))) != 0) {
                throw new Exception ("invalid mnemonic - checksum failed");
            }
        }
        return data;
    }

    public static String seedToString(byte[] seedBytes) throws Exception {
        if (seedBytes.length % 4 != 0) {
            throw new Exception("Invalid data length for mnemonic");
        }
        byte[] check = SecurityUtils.sha256Digest(seedBytes);

        boolean[] bits = new boolean[seedBytes.length * 8 + seedBytes.length / 4];

        for (int i = 0; i < seedBytes.length; i++) {
            for (int j = 0; j < 8; j++)  {
                bits[8 * i + j] = (seedBytes[i] & (1 << (7 - j))) > 0;
            }
        }
        for (int i = 0; i < seedBytes.length / 4; i++) {
            bits[8 * seedBytes.length + i] = (check[i / 8] & (1 << (7 - (i % 8)))) > 0;
        }

        int mlen = seedBytes.length * 3 / 4;
        StringBuffer mnemo = new StringBuffer();
        for (int i = 0; i < mlen; i++) {
            int idx = 0;
            for ( int j = 0; j < 11; j++ ) {
                idx += (bits[i * 11 + j] ? 1 : 0) << (10 - j);
            }
            mnemo.append(English.WORDS[idx]);
            if (i < mlen - 1) {
                mnemo.append(" ");
            }
        }
        return mnemo.toString();
    }
}
