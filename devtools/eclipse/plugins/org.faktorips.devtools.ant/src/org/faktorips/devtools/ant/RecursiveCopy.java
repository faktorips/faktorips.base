/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class RecursiveCopy {

    /**
     * Do a recursive directory copy
     * 
     * @param fromDir source directory
     * @param toDir target directory
     * @throws IOException
     */
    public void copyDir(Path fromDir, Path toDir) throws IOException {
        File dirFile = fromDir.toFile();
        if (!dirFile.exists()) {
            return;
        }

        File toDirFile = toDir.toFile();
        if (!toDirFile.exists()) {
            toDirFile.mkdir();
        }
        String[] fileList = dirFile.list();
        if (fileList != null) {
            for (String name : fileList) {
                Path from = fromDir.resolve(name);
                Path to = toDir.resolve(name);
                File file = from.toFile();
                if (file.isDirectory()) {
                    copyDir(from, to);
                } else {
                    copyFile(from, to);
                }
            }
        }
    }

    /**
     * Copy a single {@link File}
     * 
     * @param from path to the source file
     * @param to path to the target file
     */
    public void copyFile(Path from, Path to) throws IOException {
        mkdirs(to);
        InputStream input = null;
        OutputStream output = null;
        int c;
        try {
            input = new BufferedInputStream(new FileInputStream(from.toFile()));
            output = new BufferedOutputStream(new FileOutputStream(to.toFile()));
            while ((c = input.read()) != -1) {
                output.write(c);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * Create a directory. Supports creating multiple directories at once. Example:
     * mkdir("/path/to/a/new/dir") will create all subdirs
     * 
     * @param dir directory name
     */
    private void mkdir(Path dir) {
        dir.toFile().mkdirs();
    }

    /**
     * Create multiple directories recursively
     * 
     * @param file new path
     */
    private void mkdirs(Path file) {
        Path dir = file.getParent();
        if (dir != null) {
            mkdir(dir);
        }
    }

}
