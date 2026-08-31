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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecursiveCopyTest {

    private File tmpFile = null;
    private File tmpFile2 = null;
    private File tmpDir;
    private File tmpDir3;
    private File tmpDir2;

    @BeforeEach
    public void setUp() throws IOException {
        tmpDir = createTmpDir(null);
        tmpDir2 = createTmpDir(tmpDir);
        tmpDir3 = createTmpDir(null);
        tmpFile = File.createTempFile(this.getClass().getName() + "file", "");
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

    @Test
    public void testRecursiveDirCopy() {
        RecursiveCopy c = new RecursiveCopy();

        try {
            c.copyDir(tmpDir.toPath().toAbsolutePath(), tmpDir3.toPath().toAbsolutePath());
            File expectedDir = new File(tmpDir3, tmpDir2.getName());
            expectedDir.deleteOnExit();
            assertTrue(expectedDir.exists(), "expected directory does not exist");
            File expected = new File(expectedDir, tmpFile2.getName());
            expected.deleteOnExit();
            assertTrue(expected.exists(), "recursive dircopy failed");
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testCopyFile() {

        RecursiveCopy c = new RecursiveCopy();
        byte data[] = { '1', '2', '3' };

        try {
            FileOutputStream stream = new FileOutputStream(tmpFile);
            try (stream) {
                stream.write(data);
            } catch (IOException e) {
                throw e;
            }

            // copy file to dir => should throw an Exception
            assertThrows(Exception.class,
                    () -> c.copyFile(tmpFile.toPath().toAbsolutePath(), tmpDir.toPath().toAbsolutePath()));

            // copy file to file => should work
            try {
                File expectedCopy = new File(tmpDir, tmpFile.getName());
                expectedCopy.deleteOnExit();
                c.copyFile(tmpFile.toPath().toAbsolutePath(), expectedCopy.toPath().toAbsolutePath());
                assertTrue(expectedCopy.exists(), "copied file doesn't exist");
                assertEquals(tmpFile.length(), expectedCopy.length(), "filesize of copied file is incorrect. original:" + tmpFile.length() + " copy:"
                        + expectedCopy.length());
            } catch (Exception e) {
                fail(e.getMessage());
            }

        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    @AfterEach
    public void tearDown() {
        // just to be sure
        tmpFile.delete();
        tmpFile2.delete();
        tmpDir3.delete();
        tmpDir2.delete();
        tmpDir.delete();
    }

}
