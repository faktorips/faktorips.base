/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.core.internal.model.ipsproject.bundle.FolderExplorer;
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
        IPath filePath = mock(IPath.class);
        File fileFile = mock(File.class);

        when(fileFile.isDirectory()).thenReturn(false);
        when(filePath.toFile()).thenReturn(fileFile);

        assertTrue(indexer.getFolders(filePath).isEmpty());
        assertTrue(indexer.getFiles(filePath).isEmpty());
    }

    @Test
    public void testEmptyDirectory() {
        IPath filePath = mock(IPath.class);
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

    @Test
    public void testFilesAndDirectory() {
        Path filePath = spy(new Path("src"));
        File fileFile = mock(File.class);

        when(fileFile.isDirectory()).thenReturn(true);
        File subDir = createFile(true, "subDir");
        File subFile = createFile(false, "subFile");
        File subFile2 = createFile(false, "subFile2");
        File subDir2 = createFile(true, "subDir2");

        File[] subFiles = { subDir, subFile, subFile2, subDir2 };

        when(fileFile.listFiles()).thenReturn(subFiles);
        doReturn(fileFile).when(filePath).toFile();

        List<IPath> directories = indexer.getFolders(filePath);
        assertEquals(2, directories.size());
        assertEquals("subDir", directories.get(0).lastSegment());
        assertEquals("subDir2", directories.get(1).lastSegment());

        List<IPath> files = indexer.getFiles(filePath);
        assertEquals(2, files.size());
        assertEquals("subFile", files.get(0).lastSegment());
        assertEquals("subFile2", files.get(1).lastSegment());
    }
}
