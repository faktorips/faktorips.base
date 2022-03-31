/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FolderExplorerTest {

    private FolderExplorer indexer;

    @Before
    public void setUp() {
        indexer = new FolderExplorer();
    }

    @Test
    public void testNull() {
        assertTrue(indexer.getFolders(null).isEmpty());
        assertTrue(indexer.getFiles(null).isEmpty());
    }

    @Test
    public void testNoDirectory() {
        Path filePath = mock(Path.class);
        File fileFile = mock(File.class);

        when(fileFile.isDirectory()).thenReturn(false);
        when(filePath.toFile()).thenReturn(fileFile);

        assertTrue(indexer.getFolders(filePath).isEmpty());
        assertTrue(indexer.getFiles(filePath).isEmpty());
    }

    @Test
    public void testEmptyDirectory() {
        Path filePath = mock(Path.class);
        boolean isDirectory = true;

        File fileFile = createFile(isDirectory, "dir");

        when(fileFile.listFiles()).thenReturn(new File[0]);
        when(filePath.toFile()).thenReturn(fileFile);

        assertTrue(indexer.getFolders(filePath).isEmpty());
        assertTrue(indexer.getFiles(filePath).isEmpty());
    }

    private File createFile(boolean isDirectory, String name) {
        File fileFile = mock(File.class);
        when(fileFile.isDirectory()).thenReturn(isDirectory);
        when(fileFile.isFile()).thenReturn(!isDirectory);
        when(fileFile.getName()).thenReturn(name);

        return fileFile;
    }

    private File createFileIn(Path parent, boolean isDirectory, String name) {
        File fileFile = createFile(isDirectory, name);
        Path filePath = mock(Path.class);
        when(parent.resolve(name)).thenReturn(filePath);
        Path localfilePath = mock(Path.class);
        when(filePath.getFileName()).thenReturn(localfilePath);
        when(localfilePath.toString()).thenReturn(name);
        return fileFile;
    }

    @Test
    public void testFilesAndDirectory() {
        Path filePath = mock(Path.class);
        File fileFile = mock(File.class);

        when(fileFile.isDirectory()).thenReturn(true);
        File subDir = createFileIn(filePath, true, "subDir");
        File subFile = createFileIn(filePath, false, "subFile");
        File subFile2 = createFileIn(filePath, false, "subFile2");
        File subDir2 = createFileIn(filePath, true, "subDir2");

        File[] subFiles = { subDir, subFile, subFile2, subDir2 };

        when(fileFile.listFiles()).thenReturn(subFiles);
        doReturn(fileFile).when(filePath).toFile();

        List<Path> directories = indexer.getFolders(filePath);
        assertEquals(2, directories.size());
        assertEquals("subDir", directories.get(0).getFileName().toString());
        assertEquals("subDir2", directories.get(1).getFileName().toString());

        List<Path> files = indexer.getFiles(filePath);
        assertEquals(2, files.size());
        assertEquals("subFile", files.get(0).getFileName().toString());
        assertEquals("subFile2", files.get(1).getFileName().toString());
    }
}
