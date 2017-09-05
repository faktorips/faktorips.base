/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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

public class RecursiveCopy {

    /**
     * Do a recursive Directory-Copy
     * 
     * @param fromDir Source Directory as String
     * @param toDir Target Directory as String
     * @throws IOException
     */
    public void copyDir(String fromDir, String toDir) throws IOException {
        File dirFile = new File(fromDir);
        if (!dirFile.exists()) {
            return;
        }

        File toDirFile = new File(toDir);
        if (!toDirFile.exists()) {
            toDirFile.mkdir();
        }
        String[] fileList = dirFile.list();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                String name = fileList[i];
                String from = fromDir + File.separator + name;
                String to = toDir + File.separatorChar + name;
                File file = new File(from);
                if (file.isDirectory()) {
                    copyDir(from, to);
                } else {
                    copyFile(from, to);
                }
            }
        }
    }

    /**
     * Copy a single File
     * 
     * @param from - Path to the Sourcefile as String
     * @param to - Path to the Targetfile as String
     */
    public void copyFile(String from, String to) throws IOException {
        mkdirs(to);
        InputStream input = null;
        OutputStream output = null;
        int c;
        try {
            input = new BufferedInputStream(new FileInputStream(from));
            output = new BufferedOutputStream(new FileOutputStream(to));
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
     * Create a Directory. Supports creating multiple Directories at once. Example:
     * mkdir("/path/to/a/new/dir") will create all subdirs
     * 
     * @param dir - Directory-Name as String
     */
    private void mkdir(String dir) {
        new File(dir).mkdirs();
    }

    /**
     * Create multiple directories recursive
     * 
     * @param file - new Path as String
     */
    private void mkdirs(String file) {
        file = file.replace('/', File.separatorChar);
        int pos = file.lastIndexOf(File.separatorChar);
        if (pos != -1) {
            String dir = file.substring(0, pos);
            mkdir(dir);
        }
    }

}
