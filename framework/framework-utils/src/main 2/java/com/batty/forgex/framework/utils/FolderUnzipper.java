package com.batty.forgex.framework.utils;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FolderUnzipper {

    public void unzip(Path zipFilePath, Path destDir) throws IOException {
        try (InputStream fis = Files.newInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                Path newPath = resolveZipEntry(destDir, zipEntry);

                if (zipEntry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    private Path resolveZipEntry(Path destDir, ZipEntry entry) throws IOException {
        Path resolvedPath = destDir.resolve(entry.getName()).normalize();
        if (!resolvedPath.startsWith(destDir)) {
            throw new IOException("Bad zip entry: " + entry.getName());
        }
        return resolvedPath;
    }
}
