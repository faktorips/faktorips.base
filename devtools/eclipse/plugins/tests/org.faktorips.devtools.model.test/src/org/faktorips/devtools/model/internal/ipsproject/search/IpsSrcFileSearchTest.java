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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsSrcFolderEntry;
import org.junit.Test;

public class IpsSrcFileSearchTest extends AbstractIpsPluginTest {

    private IpsSrcFileSearch search;

    @Test
    public void testProcessEntry_FoundIpsSourceFile() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        search = new IpsSrcFileSearch(qnt);

        IIpsSrcFolderEntry entry = mock(IIpsSrcFolderEntry.class);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class);
        when(ipsSrcFile.exists()).thenReturn(true);
        when(entry.findIpsSrcFile(qnt)).thenReturn(ipsSrcFile);

        search.processEntry(entry);
        assertTrue(search.isStopSearch());
        assertEquals(ipsSrcFile, search.getIpsSrcFile());
    }

    @Test
    public void testProcessEntry_NoFoundIpsSourceFile() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        search = new IpsSrcFileSearch(qnt);

        IIpsSrcFolderEntry entry = mock(IIpsSrcFolderEntry.class);
        when(entry.findIpsSrcFile(qnt)).thenReturn(null);

        search.processEntry(entry);
        assertFalse(search.isStopSearch());
        assertNull(search.getIpsSrcFile());
    }

}
