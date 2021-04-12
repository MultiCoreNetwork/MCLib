package it.multicoredev.mclib.misc;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Copyright © 2020 by Lorenzo Magni
 * This file is part of MCLib.
 * MCLib is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class ZipUtils {

    /**
     * Zip one or more files or directories to a compressed zip archive.
     * If the archive already exits it will be overwritten
     *
     * @param dst The destination archive
     * @param src The files or directories to be zipped
     * @throws IOException Thrown when zip process of file/directory or when file/directory creation fails
     */
    public static void zip(@NotNull File dst, @NotNull File... src) throws IOException {
        FileOutputStream os = new FileOutputStream(dst);
        ZipOutputStream zos = new ZipOutputStream(os);

        for (File file : src) {
            if (file.isFile()) {
                zipFile(file, zos);
            } else {
                zipDir(file, file.getName(), zos);
            }
        }

        zos.close();
        os.close();
    }

    /**
     * Unzip a compressed zip archive to destination directory
     *
     * @param src The archive to unzip
     * @param dst The destination directory
     * @throws IOException Thrown when unzip process of file/directory or when file/directory creation fails
     */
    public static void unzip(@NotNull File src, @NotNull File dst) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(src));
        ZipEntry entry = zis.getNextEntry();

        while (entry != null) {
            File dstFile = newFile(dst, entry);

            if (dstFile.isDirectory()) {
                entry = zis.getNextEntry();
                continue;
            }

            FileOutputStream os = new FileOutputStream(dstFile);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }

            os.close();
            entry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    /**
     * Add one or more files or directories to an existing compressed zip archive
     *
     * @param dst The destination file
     * @param src The files to be zipped
     * @throws IOException Thrown if zip process fails
     */
    public static void append(@NotNull File dst, @NotNull File... src) throws IOException {
        if (!dst.exists() || !dst.isFile()) throw new IllegalArgumentException("Source file must be a zip");

        File tmp = new File(UUID.randomUUID().toString().replace("-", ""));
        if (!tmp.mkdirs()) throw new InternalError("Cannot create temp directory");

        try {

            unzip(dst, tmp);

            List<File> files = new ArrayList<File>(Arrays.asList(src));
            File[] tmpFiles = tmp.listFiles();
            if (tmpFiles != null) files.addAll(Arrays.asList(tmpFiles));
            File[] srcFiles = new File[files.size()];
            files.toArray(srcFiles);

            zip(dst, srcFiles);
        } finally {
            if (!deleteDir(tmp)) throw new InternalError("Cannot delete temp directory");
        }
    }

    private static void zipFile(File file, ZipOutputStream zos) throws IOException {
        FileInputStream is = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);

        byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) >= 0) {
            zos.write(bytes, 0, len);
        }

        is.close();
    }

    private static void zipDir(File file, String name, ZipOutputStream zos) throws IOException {
        if (file.isHidden()) return;

        if (file.isDirectory()) {
            if (name.endsWith("/")) {
                zos.putNextEntry(new ZipEntry(name));
            } else {
                zos.putNextEntry(new ZipEntry(name + "/"));
            }
            zos.closeEntry();

            File[] children = file.listFiles();
            if (children == null || children.length == 0) return;

            for (File child : children) {
                zipDir(child, name + "/" + child.getName(), zos);
            }

            return;
        }

        FileInputStream is = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);

        byte[] bytes = new byte[1024];
        int len;
        while ((len = is.read(bytes)) >= 0) {
            zos.write(bytes, 0, len);
        }

        is.close();
    }

    private static File newFile(File dst, ZipEntry entry) throws IOException {
        File dstFile = new File(dst, entry.getName());

        String dirPath = dst.getCanonicalPath();
        String filePath = dstFile.getCanonicalPath();

        if (!filePath.startsWith(dirPath + File.separator))
            throw new IOException("Entry '" + entry.getName() + "' is outside of the target directory");

        if (entry.isDirectory()) {
            if (!dstFile.exists() || !dstFile.isDirectory()) {
                if (!dstFile.mkdirs()) throw new IOException("Cannot create '" + entry.getName() + "' directory");
            }
        }

        return dstFile;
    }

    private static boolean deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) return dir.delete();

        boolean success = true;
        for (File file : files) {
            if (file.isFile()) {
                success = file.delete();
            } else {
                success = deleteDir(file);
            }
        }

        if (success) return dir.delete();
        else return false;
    }
}
