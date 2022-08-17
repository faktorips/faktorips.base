/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.Test;

public class DuplicateIpsSrcFileSearchTest extends AbstractIpsPluginTest {

    private DuplicateIpsSrcFileSearch search;

    @Test
    public void testProcessEntry_FoundDublicateIpsSourceFile() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        search = new DuplicateIpsSrcFileSearch(qnt);

        IIpsSrcFolderEntry entry = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(true);
        when(entry.findIpsSrcFile(qnt)).thenReturn(ipsSrcFile);

        search.processEntry(entry);
        assertFalse(search.isStopSearch());
        assertEquals(ipsSrcFile, search.getIpsSrcFile());
        assertFalse(search.foundDuplicateIpsSrcFile());

        IIpsSrcFolderEntry entry2 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        when(ipsSrcFile2.exists()).thenReturn(true);
        when(entry2.findIpsSrcFile(qnt)).thenReturn(ipsSrcFile2);
        search.processEntry(entry2);

        assertTrue(search.isStopSearch());
        assertEquals(ipsSrcFile, search.getIpsSrcFile());
        assertTrue(search.foundDuplicateIpsSrcFile());

    }

    @Test
    public void testProcessEntry_NoDublicateIpsSourceFile() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        search = new DuplicateIpsSrcFileSearch(qnt);

        IIpsSrcFolderEntry entry = mock(IIpsSrcFolderEntry.class);
        when(entry.findIpsSrcFile(qnt)).thenReturn(null);

        search.processEntry(entry);
        assertFalse(search.isStopSearch());
        assertNull(search.getIpsSrcFile());
        assertFalse(search.foundDuplicateIpsSrcFile());

        IIpsSrcFolderEntry entry2 = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFile ipsSrcFile2 = mock(IIpsSrcFile.class);
        when(ipsSrcFile2.exists()).thenReturn(true);
        when(entry2.findIpsSrcFile(qnt)).thenReturn(ipsSrcFile2);
        search.processEntry(entry2);
        assertFalse(search.isStopSearch());
        assertNotNull(search.getIpsSrcFile());
        assertEquals(ipsSrcFile2, search.getIpsSrcFile());
        assertFalse(search.foundDuplicateIpsSrcFile());
    }

    @Test
    public void testProcessEntry_NoIpsSourceFile() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        search = new DuplicateIpsSrcFileSearch(qnt);

        IIpsSrcFolderEntry entry = mock(IIpsSrcFolderEntry.class);
        when(entry.findIpsSrcFile(qnt)).thenReturn(null);

        search.processEntry(entry);
        assertFalse(search.isStopSearch());
        assertNull(search.getIpsSrcFile());
        assertFalse(search.foundDuplicateIpsSrcFile());

        IIpsSrcFolderEntry entry2 = mock(IIpsSrcFolderEntry.class);
        when(entry2.findIpsSrcFile(qnt)).thenReturn(null);
        search.processEntry(entry2);
        assertFalse(search.isStopSearch());
        assertNull(search.getIpsSrcFile());
        assertFalse(search.foundDuplicateIpsSrcFile());
    }
}
