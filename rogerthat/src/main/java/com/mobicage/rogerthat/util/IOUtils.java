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

package com.mobicage.rogerthat.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.mobicage.rogerth.at.R;
import com.mobicage.rogerthat.plugins.messaging.BrandingFailureException;
import com.mobicage.rogerthat.util.logging.L;
import com.mobicage.rogerthat.util.system.T;
import com.mobicage.rogerthat.util.ui.ImageHelper;
import com.soundcloud.android.crop.CropUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

    public static void createDirIfNotExists(Context context, File file) throws IOException {
        T.dontCare();
        if (!file.exists()) {
            if (!file.mkdirs())
                throw new IOException(context.getString(R.string.failed_to_create_directory, file.getAbsolutePath()));
        }
    }

    public static void createDirIfNotExistsBranding(Context context, File file) throws BrandingFailureException {
        T.dontCare();
        if (!file.exists()) {
            if (!file.mkdirs())
                throw new BrandingFailureException(context.getString(R.string.failed_to_create_directory, file.getAbsolutePath()));
        }
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

    public static void copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) throws IOException {
        String[] files = assetManager.list(fromAssetPath);
        if (files.length > 0) {
            new File(toPath).mkdirs();
            for (String file : files) {
                copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
            }
        } else {
            copyAsset(assetManager, fromAssetPath, toPath);
        }
    }

    private static void copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) throws IOException {
        InputStream in = assetManager.open(fromAssetPath);
        try {
            new File(toPath).createNewFile();
            OutputStream out = new FileOutputStream(toPath);
            try {
                copy(in, out, 1024);
                out.flush();
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    public static List<String> readAllLinesFromFile(final File f) throws IOException {
        return readAllLines(new FileReader(f));
    }

    public static List<String> readAllLines(final InputStreamReader reader) throws IOException {
        final List<String> lines = new ArrayList<String>();
        final BufferedReader br = new BufferedReader(reader);
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

    public static void compressPicture(Uri uri, long maxSize) {
        try {
            File file = new File(uri.getPath());
            long size = file.length();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            Bitmap imgBitmap;
            if (maxSize == 0) {
                maxSize = size;
            }
            int sampleSize = Math.round((size / maxSize));
            L.d("compressPicture 100% size: " + size);

            if (size > maxSize) {
                imgBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                FileOutputStream compressfOut = new FileOutputStream(file);
                try {
                    imgBitmap.compress(Bitmap.CompressFormat.JPEG, 80, compressfOut);
                } finally {
                    compressfOut.close();
                }
                size = file.length();
                L.d("compressPicture 80% size: " + size);
            }

            if (size > maxSize && sampleSize > 1) {
                double cs;
                do {
                    sampleSize = (int) Math.pow(2, Math.ceil(Math.log(sampleSize) / Math.log(2)));
                    bounds.inSampleSize = sampleSize;
                    imgBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
                    sampleSize++;
                    cs = bounds.outWidth * bounds.outHeight;
                } while (cs > maxSize + maxSize / 2);
            } else {
                bounds.inSampleSize =  1;
                imgBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
            }

            final int exifRotation = CropUtil.getExifRotation(file);
            imgBitmap = ImageHelper.rotateBitmap(imgBitmap, exifRotation);

            FileOutputStream savefOut = new FileOutputStream(file);
            try {
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, savefOut);
            } finally {
                savefOut.close();
            }

            L.d("compressPicture final size: " + file.length());

        } catch (Exception e) {
            L.e("compressPicture exception", e);
        }
    }

}
