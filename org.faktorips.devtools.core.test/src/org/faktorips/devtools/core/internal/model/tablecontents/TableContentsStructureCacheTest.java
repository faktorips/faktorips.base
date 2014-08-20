/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsSrcFilesChangeListener;
import org.faktorips.devtools.core.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class TableContentsStructureCacheTest {

    private static final String TABLE_STRUCTURE = "myTableStructure";

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IIpsProject ipsProject1;

    @Mock
    private IIpsProject ipsProject2;

    private TableContentsStructureCache tableContentsStructureCache;

    @Mock
    private IIpsSrcFile tableStructure;

    @Mock
    private IIpsSrcFile tableContent1;

    @Mock
    private IIpsSrcFile tableContent2;

    private IIpsSrcFilesChangeListener changeListener;

    @Before
    public void setUp() {
        IIpsProject[] projects = new IIpsProject[2];
        projects[0] = ipsProject1;
        projects[1] = ipsProject2;
        when(ipsProject2.findReferencingProjectLeavesOrSelf()).thenReturn(new IIpsProject[] { ipsProject1 });
        when(ipsModel.getIpsProjects()).thenReturn(projects);
        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                changeListener = (IIpsSrcFilesChangeListener)invocation.getArguments()[0];
                return null;
            }
        }).when(ipsModel).addIpsSrcFilesChangedListener(any(IIpsSrcFilesChangeListener.class));
        tableContentsStructureCache = new TableContentsStructureCache(ipsModel);
    }

    @Before
    public void setUpsIpsSrcFiles() throws CoreException {
        when(tableStructure.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);
        when(tableStructure.getIpsProject()).thenReturn(ipsProject2);
        when(tableContent1.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent1.getIpsProject()).thenReturn(ipsProject1);
        when(tableContent1.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(TABLE_STRUCTURE);
        when(tableContent2.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent2.getIpsProject()).thenReturn(ipsProject1);
        when(tableContent2.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn("");
    }

    @Test
    public void testGetTableContents() throws Exception {
    }

    @Test
    public void testInit_noContent() throws Exception {
        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureInWrongProject1() throws Exception {
        setUpProjectWithTableContents(ipsProject1, tableContent1, tableContent2);
        doReturn(Collections.emptyList()).when(ipsProject2).findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);

        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureInWrongProject2() throws Exception {
        setUpProjectWithTableContents(ipsProject2, tableContent1, tableContent2);
        when(ipsProject1.findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS)).thenReturn(Arrays.asList(tableStructure));

        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureReferencedProject() throws Exception {
        setUpProjectWithTableContents(ipsProject2, tableContent1, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProject2);
        when(tableContent2.getIpsProject()).thenReturn(ipsProject2);
        setUpTableStructureIn(ipsProject2);

        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertEquals(1, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));
    }

    @Test
    public void testGetTableContents_structureWrongReferencedProject() throws Exception {
        setUpTableStructureIn(ipsProject1);
        // no contents
        setUpProjectWithTableContents(ipsProject1);
        setUpProjectWithTableContents(ipsProject2, tableContent1, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProject2);
        when(tableContent2.getIpsProject()).thenReturn(ipsProject2);

        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_testDefenceCopy() throws Exception {
        setUpProjectWithTableContents(ipsProject1, tableContent1);
        setUpTableStructureIn(ipsProject1);

        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);
        assertEquals(1, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));

        tableContents.add(tableContent2);

        List<IIpsSrcFile> tableContentsNew = tableContentsStructureCache.getTableContents(tableStructure);
        assertEquals(1, tableContentsNew.size());
        assertThat(tableContentsNew, hasItem(tableContent1));
    }

    @Test
    public void testChangeListener_contentAdded() throws Exception {
        setUpProjectWithTableContents(ipsProject1, tableContent1);
        setUpTableStructureIn(ipsProject1);
        setUpTableStructureNameFor(tableContent2, TABLE_STRUCTURE);
        // init
        tableContentsStructureCache.getTableContents(tableStructure);

        changeListener.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));
        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertEquals(2, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));
        assertThat(tableContents, hasItem(tableContent2));
    }

    @Test
    public void testChangeListener_contentRemoved() throws Exception {
        setUpProjectWithTableContents(ipsProject1, tableContent1, tableContent2);
        setUpTableStructureIn(ipsProject1);
        setUpTableStructureNameFor(tableContent2, TABLE_STRUCTURE);
        // init
        tableContentsStructureCache.getTableContents(tableStructure);

        changeListener.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.REMOVED));
        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertEquals(1, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));
    }

    @Test
    public void testChangeListener_structureRemoved() throws Exception {
        setUpProjectWithTableContents(ipsProject1, tableContent1, tableContent2);
        setUpTableStructureIn(ipsProject1);
        setUpTableStructureNameFor(tableContent2, TABLE_STRUCTURE);
        // init
        tableContentsStructureCache.getTableContents(tableStructure);

        changeListener.ipsSrcFilesChanged(newChangeEvent(tableStructure, IResourceDelta.REMOVED));
        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testChangeListener_structureChanged() throws Exception {
        setUpProjectWithTableContents(ipsProject1, tableContent1, tableContent2);
        setUpTableStructureNameFor(tableContent2, TABLE_STRUCTURE);
        when(tableStructure.getIpsObjectName()).thenReturn("");

        List<IIpsSrcFile> tableContents = tableContentsStructureCache.getTableContents(tableStructure);
        assertEquals(0, tableContents.size());

        setUpTableStructureIn(ipsProject1);
        changeListener.ipsSrcFilesChanged(newChangeEvent(tableStructure, IResourceDelta.CHANGED));
        tableContents = tableContentsStructureCache.getTableContents(tableStructure);

        assertEquals(2, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));
        assertThat(tableContents, hasItem(tableContent2));
    }

    private void setUpTableStructureNameFor(IIpsSrcFile tc, String name) throws CoreException {
        when(tc.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(name);
    }

    private void setUpProjectWithTableContents(IIpsProject project, IIpsSrcFile... files) {
        when(project.findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS)).thenReturn(Arrays.asList(files));
    }

    private void setUpTableStructureIn(IIpsProject project) throws CoreException {
        when(project.findIpsSrcFile(new QualifiedNameType(TABLE_STRUCTURE, IpsObjectType.TABLE_STRUCTURE))).thenReturn(
                tableStructure);
    }

    private IpsSrcFilesChangedEvent newChangeEvent(IIpsSrcFile ipsSrcFile, int kind) {
        Map<IIpsSrcFile, IResourceDelta> changedSrcFiles = new HashMap<IIpsSrcFile, IResourceDelta>();
        IResourceDelta delta = mock(IResourceDelta.class);
        when(delta.getKind()).thenReturn(kind);
        changedSrcFiles.put(ipsSrcFile, delta);
        return new IpsSrcFilesChangedEvent(changedSrcFiles);
    }
}
