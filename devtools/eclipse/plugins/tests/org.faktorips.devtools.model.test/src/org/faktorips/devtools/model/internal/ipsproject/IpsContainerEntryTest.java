/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathContainer;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.runtime.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
        when(ipsProject.getName()).thenReturn("ipsProject");

        new IpsObjectPathSearchContext(ipsProject);
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
        List<IIpsObjectPathEntry> entries = new ArrayList<>();
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
    public void testFindIpsSrcFile() throws Exception {
        QualifiedNameType qnt = mock(QualifiedNameType.class);
        assertNull(ipsContainerEntry.findIpsSrcFile(qnt));
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
        List<IIpsObjectPathEntry> entries = new ArrayList<>();
        when(container.resolveEntries()).thenReturn(entries);
        IpsObjectPathEntry ipsObjectEntry = mock(IpsObjectPathEntry.class);
        IpsObjectPathEntry ipsObjectEntry2 = mock(IpsObjectPathEntry.class);
        entries.add(ipsObjectEntry2);
        entries.add(ipsObjectEntry);
        return ipsObjectEntry;
    }

}
