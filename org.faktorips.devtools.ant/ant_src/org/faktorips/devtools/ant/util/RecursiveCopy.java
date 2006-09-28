/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.ant.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecursiveCopy {

    /**
     * Do a recursive Directory-Copy
     * 
     * @param fromDir Source Directory as String
     * @param toDir   Target Directory as String
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
                }
                else {
                    copyFile(from, to);
                }
            }
        }
    }

    /**
     * Copy a single File
     * 
     * @param from - Path to the Sourcefile as String
     * @param to   - Path to the Targetfile as String
     */
    public void copyFile(String from, String to) throws FileNotFoundException, IOException {
        mkdirs(to);
        InputStream input = new BufferedInputStream(new FileInputStream(from));
        OutputStream output = new BufferedOutputStream(new FileOutputStream(to));
        int c;
        try{
            while ((c = input.read()) != -1) {
                output.write(c);
            }
        }catch (IOException e){
            throw e;
        }finally{
            if ((input != null) && (output != null))
            input.close();
            output.close();
        }

    }

    /**
     * Create a Directory.
     * Supports creating multiple Directories at once.
     * Example: mkdir("/path/to/a/new/dir") will create all subdirs
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
