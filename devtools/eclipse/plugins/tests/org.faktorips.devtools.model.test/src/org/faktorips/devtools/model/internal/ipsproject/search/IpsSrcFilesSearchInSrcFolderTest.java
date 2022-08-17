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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.Test;

public class IpsSrcFilesSearchInSrcFolderTest extends AbstractIpsPluginTest {

    private IpsSrcFilesSearchInSrcFolder search;

    @Test
    public void testIpsSrcFilesSearch_NoParameter() {
        search = new IpsSrcFilesSearchInSrcFolder();
        assertTrue(search.getIpsObjectTypes().length > 0);
    }

    @Test
    public void testIpsSrcFilesSearch_OneParameter() {
        search = new IpsSrcFilesSearchInSrcFolder(IpsObjectType.ENUM_TYPE);
        assertTrue(search.getIpsObjectTypes().length == 1);
    }

    @Test
    public void testIpsSrcFilesSearch_MoreParameter() {
        search = new IpsSrcFilesSearchInSrcFolder(IpsObjectType.ENUM_TYPE, IpsObjectType.POLICY_CMPT_TYPE);
        assertTrue(search.getIpsObjectTypes().length == 2);
    }

    @Test
    public void testProcessEntry() throws Exception {
        search = new IpsSrcFilesSearchInSrcFolder(IpsObjectType.POLICY_CMPT_TYPE);
        IIpsSrcFolderEntry entry = mock(IIpsSrcFolderEntry.class);
        when(entry.getType()).thenReturn(IpsObjectPathEntry.TYPE_SRC_FOLDER);
        when(entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(
                new ArrayList<>(Arrays.asList(mock(IIpsSrcFile.class), mock(IIpsSrcFile.class))));

        search.processEntry(entry);
        List<IIpsSrcFile> ipsSrcFiles = search.getIpsSrcFiles();

        assertEquals(2, ipsSrcFiles.size());
        verify(entry, atLeastOnce()).findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Test
    public void testProcessEntry_NoSrcFolderEntry() throws Exception {
        search = new IpsSrcFilesSearchInSrcFolder(IpsObjectType.POLICY_CMPT_TYPE);
        IIpsArchiveEntry entry = mock(IIpsArchiveEntry.class);
        when(entry.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE)).thenReturn(
                new ArrayList<>(Arrays.asList(mock(IIpsSrcFile.class), mock(IIpsSrcFile.class))));
        when(entry.getType()).thenReturn(IpsObjectPathEntry.TYPE_ARCHIVE);
        search.processEntry(entry);
        List<IIpsSrcFile> ipsSrcFiles = search.getIpsSrcFiles();

        assertEquals(0, ipsSrcFiles.size());
        verify(entry, never()).findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
    }

    @Test
    public void testProcessEntry_noIndirectRefs() throws Exception {
        search = new IpsSrcFilesSearchInSrcFolder();

        assertFalse(search.isIncludeIndirect());
    }
}
