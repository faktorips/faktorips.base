/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsContainerEntryTest {

    private static final String MY_ID = "myId";

    private static final String MY_OPTIONAL_PATH = "myOptionalPath";

    @Mock
    private IpsObjectPath path;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsModel ipsModel;

    private IpsContainerEntry ipsContainerEntry;

    @Before
    public void createIpsContainerEntry() throws Exception {
        ipsContainerEntry = new IpsContainerEntry(path);
        ipsContainerEntry.setContainerTypeId(MY_ID);
        ipsContainerEntry.setOptionalPath(MY_OPTIONAL_PATH);
        when(path.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getIpsModel()).thenReturn(ipsModel);
    }

    @Test
    public void testGetIpsObjectPathContainer() throws Exception {
        IIpsObjectPathContainer ipsObjectPathContainer = mockContainer();

        IIpsObjectPathContainer container = ipsContainerEntry.getIpsObjectPathContainer();

        assertEquals(ipsObjectPathContainer, container);
    }

    @Test
    public void testResolveEntries() throws Exception {
        IIpsObjectPathContainer container = mockContainer();
        List<IIpsObjectPathEntry> entries = new ArrayList<IIpsObjectPathEntry>();
        when(container.resolveEntries()).thenReturn(entries);

        List<IIpsObjectPathEntry> resolveEntries = ipsContainerEntry.resolveEntries();

        assertSame(entries, resolveEntries);
    }

    @Test
    public void testExists_empty() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        IIpsObjectPathContainer ipsObjectPathContainer = mock(IIpsObjectPathContainer.class);
        when(ipsModel.getIpsObjectPathContainer(ipsProject, MY_ID, MY_OPTIONAL_PATH))
                .thenReturn(ipsObjectPathContainer);

        boolean exists = ipsContainerEntry.exists(qnt);

        assertFalse(exists);
    }

    @Test
    public void testExists_existing() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IpsObjectPathEntry mockEntry = mockEntry(container);
        when(mockEntry.exists(qnt)).thenReturn(true);

        boolean exists = ipsContainerEntry.exists(qnt);

        assertTrue(exists);
    }

    @Test
    public void testFindIpsSrcFileInternal() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IpsObjectPathEntry mockEntry = mockEntry(container);
        HashSet<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();

        ipsContainerEntry.findIpsSrcFileInternal(qnt, visitedEntries);

        verify(mockEntry).findIpsSrcFileInternal(qnt, visitedEntries);
    }

    @Test
    public void testFindIpsSrcFilesInternal() throws Exception {
        IpsObjectType type = mock(IpsObjectType.class);
        String packageFragment = "myPackage";
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IpsObjectPathEntry mockEntry = mockEntry(container);
        HashSet<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();

        ipsContainerEntry.findIpsSrcFilesInternal(type, packageFragment, result, visitedEntries);

        verify(mockEntry).findIpsSrcFilesInternal(type, packageFragment, result, visitedEntries);
    }

    @Test
    public void testFindIpsSrcFilesStartingWithInternal() throws Exception {
        IpsObjectType type = mock(IpsObjectType.class);
        String prefix = "myPrefix";
        List<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IpsObjectPathEntry mockEntry = mockEntry(container);
        HashSet<IIpsObjectPathEntry> visitedEntries = new HashSet<IIpsObjectPathEntry>();

        ipsContainerEntry.findIpsSrcFilesStartingWithInternal(type, prefix, true, result, visitedEntries);

        verify(mockEntry).findIpsSrcFilesStartingWithInternal(type, prefix, true, result, visitedEntries);
    }

    @Test(expected = CoreRuntimeException.class)
    public void testGetResourceAsStream_throwCoreException() throws Exception {
        String resourcePath = "myResourcePath";
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        mockEntry(container);

        ipsContainerEntry.getResourceAsStream(resourcePath);
    }

    @Test
    public void testGetResourceAsStream_find() throws Exception {
        String resourcePath = "myResourcePath";
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IpsObjectPathEntry mockEntry = mockEntry(container);
        InputStream inputStream = mock(InputStream.class);
        when(mockEntry.containsResource(resourcePath)).thenReturn(true);
        when(mockEntry.getResourceAsStream(resourcePath)).thenReturn(inputStream);

        InputStream resourceAsStream = ipsContainerEntry.getResourceAsStream(resourcePath);

        assertEquals(inputStream, resourceAsStream);
    }

    @Test
    public void testContainsRsource_false() throws Exception {
        String resourcePath = "myResourcePath";
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        mockEntry(container);

        boolean containsResource = ipsContainerEntry.containsResource(resourcePath);

        assertFalse(containsResource);
    }

    @Test
    public void testContainsRsource_ture() throws Exception {
        String resourcePath = "myResourcePath";
        IIpsObjectPathContainer container = mock(IIpsObjectPathContainer.class);
        IpsObjectPathEntry entry = mockEntry(container);
        when(entry.containsResource(resourcePath)).thenReturn(true);

        boolean containsResource = ipsContainerEntry.containsResource(resourcePath);

        assertTrue(containsResource);
    }

    @Test
    public void testValidate() throws Exception {
        MessageList validate = ipsContainerEntry.validate();

        assertNotNull(validate.getMessageByCode(IpsContainerEntry.MSG_CODE_INVALID_CONTAINER_ENTRY));
    }

    @Test
    public void testValidate_validateContainer() throws Exception {
        IIpsObjectPathContainer container = mockContainer();

        ipsContainerEntry.validate();

        verify(container).validate();
    }

    @Test
    public void testGetResolvedEntry_empty() throws Exception {
        mockContainer();

        IIpsObjectPathEntry resolvedEntry = ipsContainerEntry.getResolvedEntry("myRootName");

        assertNull(resolvedEntry);
    }

    @Test
    public void testGetResolvedEntry_notEmpty() throws Exception {
        String myRootName = "myRootName";
        IIpsObjectPathContainer container = mockContainer();
        IpsObjectPathEntry entry = mockEntry(container);
        when(entry.getIpsPackageFragmentRootName()).thenReturn(myRootName);

        IIpsObjectPathEntry resolvedEntry = ipsContainerEntry.getResolvedEntry(myRootName);

        assertEquals(entry, resolvedEntry);
    }

    @Test
    public void testGetResolvedEntry_rootNull() throws Exception {
        String myRootName = "myRootName";
        IIpsObjectPathContainer container = mockContainer();
        IpsObjectPathEntry entry = mockEntry(container);
        when(entry.getIpsPackageFragmentRootName()).thenReturn(null);

        IIpsObjectPathEntry resolvedEntry = ipsContainerEntry.getResolvedEntry(myRootName);

        assertNull(resolvedEntry);
    }

    private IIpsObjectPathContainer mockContainer() {
        IIpsObjectPathContainer ipsObjectPathContainer = mock(IIpsObjectPathContainer.class);
        when(ipsModel.getIpsObjectPathContainer(ipsProject, MY_ID, MY_OPTIONAL_PATH))
                .thenReturn(ipsObjectPathContainer);
        return ipsObjectPathContainer;
    }

    private IpsObjectPathEntry mockEntry(IIpsObjectPathContainer container) {
        when(ipsModel.getIpsObjectPathContainer(ipsProject, MY_ID, MY_OPTIONAL_PATH)).thenReturn(container);
        List<IIpsObjectPathEntry> entries = new ArrayList<IIpsObjectPathEntry>();
        when(container.resolveEntries()).thenReturn(entries);
        IpsObjectPathEntry ipsObjectEntry = mock(IpsObjectPathEntry.class);
        IpsObjectPathEntry ipsObjectEntry2 = mock(IpsObjectPathEntry.class);
        entries.add(ipsObjectEntry2);
        entries.add(ipsObjectEntry);
        return ipsObjectEntry;
    }

}
