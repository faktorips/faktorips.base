/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.jdtcontainer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.model.internal.ipsproject.IpsArchiveEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPath;
import org.faktorips.devtools.model.internal.ipsproject.IpsObjectPathEntry;
import org.faktorips.devtools.model.internal.ipsproject.IpsProjectRefEntry;
import org.faktorips.devtools.model.internal.ipsproject.bundle.IpsBundleEntry;
import org.faktorips.devtools.model.internal.ipsproject.jdtcontainer.JdtClasspathEntryCreator.EntryCreator;
import org.faktorips.devtools.model.internal.ipsproject.jdtcontainer.JdtClasspathEntryCreator.ReferenceFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class JdtClasspathEntryCreatorTest {

    @Mock
    private IClasspathEntry entry;

    @Mock
    private IpsObjectPath ipsObjectPath;

    @Mock
    private IPath path;

    @Mock
    private ReferenceFactory referenceFactory;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProject refProject;

    @Mock
    private AProject project;

    @Mock
    private IpsProjectRefEntry expectedProjectReference;

    @Mock
    private IpsArchiveEntry expectedArchiveEntry;

    @Mock
    private IpsBundleEntry expectedBundleEntry;

    @Mock
    private MessageList archiveMessageList;

    @Mock
    private MessageList bundleMessageList;

    private EntryCreator entryCreator;

    @Before
    public void createJdtClasspathEntryCreator() throws Exception {
        entryCreator = new EntryCreator(entry, ipsObjectPath);
        entryCreator.setReferenceFactory(referenceFactory);
    }

    @Test
    public void testCreateIpsProjectRefEntry_notExisingProject() throws Exception {
        mockEntryAndPath();

        IIpsProjectRefEntry ipsProjectRefEntry = entryCreator.createIpsProjectRefEntry();

        assertNull(ipsProjectRefEntry);
    }

    @Test
    public void testCreateIpsProjectRefEntry_ipsProjectEntry() throws Exception {
        mockEntryAndPath();
        mockReferences();
        when(refProject.exists()).thenReturn(true);

        IIpsProjectRefEntry ipsProjectRefEntry = entryCreator.createIpsProjectRefEntry();

        assertEquals(expectedProjectReference, ipsProjectRefEntry);
    }

    @Test
    public void testCreateIpsProjectRefEntry_reexportFalse() throws Exception {
        mockEntryAndPath();
        mockReferences();
        when(refProject.exists()).thenReturn(true);

        entryCreator.createIpsProjectRefEntry();

        verify(expectedProjectReference).setReexported(false);
    }

    @Test
    public void testCreateLibraryEntry_invalidPath() throws Exception {
        mockEntryAndPath();
        mockReferences();
        when(archiveMessageList.containsErrorMsg()).thenReturn(true);
        when(bundleMessageList.containsErrorMsg()).thenReturn(true);

        IpsObjectPathEntry libraryEntry = entryCreator.createLibraryEntry();

        assertNull(libraryEntry);
    }

    @Test
    public void testCreateLibraryEntry_archive() throws Exception {
        mockEntryAndPath();
        mockReferences();

        IpsObjectPathEntry libraryEntry = entryCreator.createLibraryEntry();

        assertEquals(expectedArchiveEntry, libraryEntry);
    }

    @Test
    public void testCreateLibraryEntry_library() throws Exception {
        mockEntryAndPath();
        mockReferences();
        when(archiveMessageList.containsErrorMsg()).thenReturn(true);

        IpsObjectPathEntry libraryEntry = entryCreator.createLibraryEntry();

        assertEquals(expectedBundleEntry, libraryEntry);
    }

    private void mockReferences() {
        when(referenceFactory.createArchiveEntry()).thenReturn(expectedArchiveEntry);
        when(expectedArchiveEntry.validate()).thenReturn(archiveMessageList);
        when(referenceFactory.createIpsBundleEntry()).thenReturn(expectedBundleEntry);
        when(expectedBundleEntry.validate()).thenReturn(bundleMessageList);
        when(referenceFactory.createProjectRefEntry(refProject)).thenReturn(expectedProjectReference);
    }

    private void mockEntryAndPath() {
        when(entry.getPath()).thenReturn(path);
        File file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        Path filePath = mock(Path.class);
        when(file.toPath()).thenReturn(filePath);
        when(referenceFactory.getIpsProject(path)).thenReturn(refProject);
    }

}
