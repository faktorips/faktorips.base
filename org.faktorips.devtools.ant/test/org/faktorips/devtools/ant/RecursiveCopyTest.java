/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class RecursiveCopyTest extends TestCase {

    private File tmpFile = null;
    private File tmpFile2 = null;
    private File tmpDir;
    private File tmpDir3;
    private File tmpDir2;

    public void setUp() throws IOException {
        tmpDir = createTmpDir(null);
        tmpDir2 = createTmpDir(tmpDir);
        tmpDir3 = createTmpDir(null);
        tmpFile = File.createTempFile(this.getName() + "file", "");
        tmpFile2 = new File(tmpDir2, "/temp");
        tmpFile.createNewFile();
        tmpFile2.createNewFile();

        tmpFile.deleteOnExit();
        tmpFile2.deleteOnExit();
    }

    private static File createTmpDir(File inDir) throws IOException {
        File dir = File.createTempFile("fipsAntTest", "dir", inDir);
        dir.delete();
        dir.mkdir();
        dir.deleteOnExit();
        return dir;
    }

    public void testRecursiveDirCopy() {
        RecursiveCopy c = new RecursiveCopy();

        try {
            c.copyDir(tmpDir.getAbsolutePath(), tmpDir3.getAbsolutePath());
            File expectedDir = new File(tmpDir3, tmpDir2.getName());
            expectedDir.deleteOnExit();
            assertTrue("expected directory does not exists", expectedDir.exists());
            File expected = new File(expectedDir, tmpFile2.getName());
            expected.deleteOnExit();
            assertTrue("recursive dircopy failed", expected.exists());
        } catch (Exception e) {
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
            } catch (IOException e) {
                throw e;
            } finally {
                stream.close();
            }

            // copy file to dir => should throw an Exception
            try {
                c.copyFile(tmpFile.getAbsolutePath(), tmpDir.getAbsolutePath());
                fail("copy from file to dir was sucessfull. should throw an Exception");
            } catch (Exception e) {
                // expected
            }

            // copy file to file => should work
            try {
                File expectedCopy = new File(tmpDir, tmpFile.getName());
                expectedCopy.deleteOnExit();
                c.copyFile(tmpFile.getAbsolutePath(), expectedCopy.getAbsolutePath());
                assertTrue("copied file doesn't exist", expectedCopy.exists());
                assertEquals("filesize of copied file is incorrect. original:" + tmpFile.length() + " copy:"
                        + expectedCopy.length(), tmpFile.length(), expectedCopy.length());
            } catch (Exception e) {
                fail(e.getMessage());
            }

        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    public void tearDown() {
        // just to be sure
        tmpFile.delete();
        tmpFile2.delete();
        tmpDir3.delete();
        tmpDir2.delete();
        tmpDir.delete();
    }

}
