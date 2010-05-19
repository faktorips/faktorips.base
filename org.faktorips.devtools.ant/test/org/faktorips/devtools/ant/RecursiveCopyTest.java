/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.faktorips.devtools.ant.RecursiveCopy;

import junit.framework.TestCase;

public class RecursiveCopyTest extends TestCase {

    private File tmpFile = null;
    private File tmpFile2 = null;
    private File tmpDir = new File(System.getProperty("java.io.tmpdir") + "/org.faktorips.devtools.ant.util.CopyTest/");
    private File tmpDir3 = new File(System.getProperty("java.io.tmpdir")
            + "/org.faktorips.devtools.ant.util.CopyTest2/");
    private File tmpDir2 = new File(tmpDir.getAbsolutePath() + "/2/");

    @Override
    public void setUp() throws IOException {
        tmpDir.mkdir();
        tmpFile = File.createTempFile(this.getName() + "file", "");
        tmpFile2 = new File(tmpDir2.getAbsolutePath() + "/temp");
        tmpFile.createNewFile();
        tmpDir2.mkdir();
        tmpDir3.mkdir();
        tmpFile2.createNewFile();

        tmpFile.deleteOnExit();
        tmpFile2.deleteOnExit();
        tmpDir.deleteOnExit();
        tmpDir2.deleteOnExit();
        tmpDir3.deleteOnExit();

    }

    public void testRecursiveDirCopy() {
        RecursiveCopy c = new RecursiveCopy();

        try {
            c.copyDir(tmpDir.getAbsolutePath(), tmpDir3.getAbsolutePath());
            File expected = new File(tmpDir3.getAbsolutePath() + "/2/" + tmpFile2.getName());
            if (!expected.exists()) {
                fail("recursive dircopy failed");
            }

        }
        catch (Exception e) {
            fail(e.getMessage());
        }

    }

    public void testCopyFile() {

        RecursiveCopy c = new RecursiveCopy();
        byte data[] = { '1', '2', '3' };

        try {
            FileOutputStream stream = new FileOutputStream(tmpFile);
            try {
                stream.write(data);
            }
            catch (IOException e) {
                throw e;
            }
            finally {
                stream.close();
            }

            // copy file to dir => should throw an Exception
            try {
                c.copyFile(tmpFile.getAbsolutePath(), tmpDir.getAbsolutePath());
                fail("copy from file to dir was sucessfull. should throw an Exception");
            }
            catch (Exception e) {
                // expected
            }

            // copy file to file => should work
            try {
                c.copyFile(tmpFile.getAbsolutePath(), tmpDir.getAbsolutePath() + tmpFile.getName());
            }
            catch (Exception e) {
                fail(e.getMessage());
            }

            File expectedCopy = new File(tmpDir.getAbsolutePath() + tmpFile.getName());

            if (!expectedCopy.exists()) {
                fail("copied file doesn't exist");
            }
            else if (tmpFile.length() != expectedCopy.length()) {
                fail("filesize of copied file is incorrect. original:" + tmpFile.length() + " copy:"
                        + expectedCopy.length());
            }

        }
        catch (IOException e) {
            fail(e.getMessage());
        }

    }

    @Override
    public void tearDown() {
        // just to be sure
        tmpFile.delete();
        tmpFile2.delete();
        tmpDir3.delete();
        tmpDir2.delete();
        tmpDir.delete();
    }

}
