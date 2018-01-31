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

package com.mobicage.rogerthat.util;

import com.mobicage.rogerthat.util.logging.L;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class ZipUtils {

    public static String decompressToString(byte[] bytesToDecompress) throws IOException {
        byte[] bytesDecompressed = decompress(bytesToDecompress);

        String returnValue = null;

        try {
            returnValue = new String(bytesDecompressed, 0, bytesDecompressed.length, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            L.bug(uee);
        }

        return returnValue;
    }

    public static byte[] decompress(byte[] bytesToDecompress) throws IOException {
        InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(bytesToDecompress));
        ByteArrayOutputStream out = new ByteArrayOutputStream(bytesToDecompress.length * 5);
        shovelInToOut(in, out);
        out.close();
        in.close();

        return out.toByteArray();
    }

    public static byte[] compress(byte[] bytesToCompress) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytesToCompress);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = new DeflaterOutputStream(baos);
        shovelInToOut(in, out);
        in.close();
        out.close();
        return baos.toByteArray();
    }

    /**
     * Shovels all data from an input stream to an output stream.
     */
    private static void shovelInToOut(InputStream in, OutputStream out) throws IOException {
        IOUtils.copy(in, out, 1000);
    }
}
