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

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.mobicage.rogerth.at.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

    public static File getFilesDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getFilesDir();
        } else {
            return getExternalFilesDirectory(context);
        }
    }

    public static File getExternalFilesDirectory(Context context) {
        return new File(Environment.getExternalStorageDirectory(),
                context.getString(R.string.app_name) + "-app");
    }

    public static boolean shouldCheckExternalStorageAvailable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        }
        fileOrDirectory.delete();
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            copyFile(sourceLocation, targetLocation);
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(target));
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(source));
            try {
                copy(in, out, 1024);
            } finally {
                in.close();
            }
        } finally {
            out.close();
        }
    }

    public static void copy(InputStream in, OutputStream out, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    public static List<String> readAllLinesFromFile(final File f) throws IOException {
        final List<String> lines = new ArrayList<String>();
        final BufferedReader br = new BufferedReader(new FileReader(f));
        try {
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        } finally {
            br.close();
        }
        return lines;
    }

    public static void writeToFile(final File f, final List<String> lines) throws IOException {
        final PrintWriter pw;
        pw = new PrintWriter(f, "UTF-8");
        try {
            for (String line : lines) {
                pw.println(line);
            }
        } finally {
            pw.close();
        }
    }

    public static String toString(final InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static byte[] digest(final MessageDigest digest, final File f) throws IOException {
        FileInputStream fis = new FileInputStream(f);

        byte[] dataBytes = new byte[1024];

        int read;
        while ((read = fis.read(dataBytes)) != -1) {
            digest.update(dataBytes, 0, read);
        }

        return digest.digest();
    }

}
