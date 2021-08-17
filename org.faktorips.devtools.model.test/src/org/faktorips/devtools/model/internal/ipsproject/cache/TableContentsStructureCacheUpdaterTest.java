/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.cache;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResourceDelta;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IpsSrcFilesChangedEvent;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableContentsStructureCacheUpdaterTest {

    private static final String TABLE_STRUCTURE = "myTableStructure";

    @Mock
    private IIpsModel ipsModel;

    @Mock
    private IIpsProject ipsProjectBase;

    @Mock
    private IIpsProject ipsProjectA;

    @Mock
    private IIpsProject ipsProjectB;

    @Mock
    private IIpsProject ipsProjectC;

    @Mock
    private IIpsSrcFile tableStructure;

    @Mock
    private IIpsSrcFile tableContent1;

    @Mock
    private IIpsSrcFile tableContent2;

    @Mock
    private IIpsSrcFile tableContent3;

    @Mock
    private IIpsObjectPathEntry objectPathEntryC;

    @Mock
    private IIpsObjectPathEntry objectPathEntryB;

    @Mock
    private IIpsObjectPathEntry objectPathEntryA;

    @Mock
    private IIpsObjectPathEntry objectPathEntryBase;

    private TableContentsStructureCache tableContentsStructureCacheA;

    private TableContentsStructureCache tableContentsStructureCacheC;

    private TableContentsStructureCacheUpdater tableContentUpdaterA;

    private TableContentsStructureCacheUpdater tableContentUpdaterC;

    @Before
    public void setUp() {
        when(ipsProjectBase.getIpsModel()).thenReturn(ipsModel);
        when(ipsProjectA.getIpsModel()).thenReturn(ipsModel);
        when(ipsProjectB.getIpsModel()).thenReturn(ipsModel);
        when(ipsProjectC.getIpsModel()).thenReturn(ipsModel);

        tableContentsStructureCacheA = new TableContentsStructureCache(ipsProjectA);
        tableContentsStructureCacheC = new TableContentsStructureCache(ipsProjectC);
        tableContentUpdaterA = new TableContentsStructureCacheUpdater(tableContentsStructureCacheA, ipsProjectA);
        tableContentUpdaterC = new TableContentsStructureCacheUpdater(tableContentsStructureCacheC, ipsProjectC);
        when(ipsProjectBase.findReferencingProjectLeavesOrSelf()).thenReturn(
                new IIpsProject[] { ipsProjectA, ipsProjectB });
        when(ipsProjectBase.findReferencingProjects(true)).thenReturn(
                new IIpsProject[] { ipsProjectA, ipsProjectB, ipsProjectC });
        when(ipsProjectA.findReferencingProjects(true)).thenReturn(new IIpsProject[] { ipsProjectC });
        when(ipsProjectB.findReferencingProjects(true)).thenReturn(new IIpsProject[] {});
        when(ipsProjectC.findReferencingProjects(true)).thenReturn(new IIpsProject[] {});
        when(ipsProjectA.isReferencing(ipsProjectBase)).thenReturn(true);
        when(ipsProjectC.isReferencing(ipsProjectBase)).thenReturn(true);
        when(ipsProjectC.isReferencing(ipsProjectA)).thenReturn(true);

        when(ipsModel.getIpsProjects()).thenReturn(
                new IIpsProject[] { ipsProjectA, ipsProjectB, ipsProjectBase, ipsProjectC });
        when(tableContent1.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(TABLE_STRUCTURE);
        when(tableContent2.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(TABLE_STRUCTURE);
        when(tableContent3.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(TABLE_STRUCTURE);
        when(tableContent1.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent2.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent3.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent3.getIpsProject()).thenReturn(ipsProjectB);
        when(tableStructure.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);
        when(tableStructure.getIpsProject()).thenReturn(ipsProjectBase);
        when(tableStructure.getIpsObjectName()).thenReturn(TABLE_STRUCTURE);
        when(tableStructure.getQualifiedNameType()).thenReturn(
                new QualifiedNameType(TABLE_STRUCTURE, IpsObjectType.TABLE_STRUCTURE));

        IIpsPackageFragment fragment = mock(IIpsPackageFragment.class);
        when(tableContent1.getIpsPackageFragment()).thenReturn(fragment);
        when(tableContent2.getIpsPackageFragment()).thenReturn(fragment);
        when(tableContent3.getIpsPackageFragment()).thenReturn(fragment);
        when(tableStructure.getIpsPackageFragment()).thenReturn(fragment);

        IIpsPackageFragmentRoot rootFragment = mock(IIpsPackageFragmentRoot.class);
        when(fragment.getRoot()).thenReturn(rootFragment);
        when(rootFragment.getName()).thenReturn("FRAGMENT_NAME");

        IIpsObjectPath objectPathBase = mock(IIpsObjectPath.class);
        when(ipsProjectBase.getIpsObjectPath()).thenReturn(objectPathBase);

        IIpsObjectPath objectPathA = mock(IIpsObjectPath.class);
        when(ipsProjectA.getIpsObjectPath()).thenReturn(objectPathA);

        IIpsObjectPath objectPathB = mock(IIpsObjectPath.class);
        when(ipsProjectB.getIpsObjectPath()).thenReturn(objectPathB);

        IIpsObjectPath objectPathC = mock(IIpsObjectPath.class);
        when(ipsProjectC.getIpsObjectPath()).thenReturn(objectPathC);

        when(objectPathBase.getEntry(anyString())).thenReturn(objectPathEntryBase);
        when(objectPathEntryBase.isReexported()).thenReturn(true);

        when(objectPathA.getEntry(anyString())).thenReturn(objectPathEntryA);
        when(objectPathEntryA.isReexported()).thenReturn(true);

        when(objectPathB.getEntry(anyString())).thenReturn(objectPathEntryB);
        when(objectPathEntryB.isReexported()).thenReturn(true);

        when(objectPathC.getEntry(anyString())).thenReturn(objectPathEntryC);
        when(objectPathEntryC.isReexported()).thenReturn(true);
    }

    @Test
    public void testChangeListener_contentAdded_p1() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1);
        setUpTableStructureIn(ipsProjectA);
        initCache();

        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContents1 = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(2, tableContents1.size());
        assertThat(tableContents1, hasItem(tableContent1));
        assertThat(tableContents1, hasItem(tableContent2));

        tableContentUpdaterC.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContents3 = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(2, tableContents3.size());
        assertThat(tableContents3, hasItem(tableContent1));
        assertThat(tableContents3, hasItem(tableContent2));
    }

    @Test
    public void testChangeListener_contentAdded_p1_reexport_false() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1);
        setUpTableStructureIn(ipsProjectA);
        initCache();
        when(objectPathEntryA.isReexported()).thenReturn(false);

        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContents1 = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(2, tableContents1.size());
        assertThat(tableContents1, hasItem(tableContent1));
        assertThat(tableContents1, hasItem(tableContent2));

        tableContentUpdaterC.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContents3 = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(1, tableContents3.size());
        assertThat(tableContents3, hasItem(tableContent1));
    }

    @Test
    public void testChangeListener_contentAdded_ProjectC() throws Exception {
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectC);
        setUpProjectWithTableContents(ipsProjectA, tableContent1);
        setUpTableStructureIn(ipsProjectA);
        initCache();

        List<IIpsSrcFile> tableContentsC = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(1, tableContentsC.size());
        assertThat(tableContentsC, hasItem(tableContent1));

        tableContentUpdaterC.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));

        tableContentsC = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(2, tableContentsC.size());
        assertThat(tableContentsC, hasItem(tableContent1));
        assertThat(tableContentsC, hasItem(tableContent2));
    }

    @Test
    public void testChangeListener_contentAdded_ignoreAddOnOtherProjects() throws Exception {
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectC);
        setUpProjectWithTableContents(ipsProjectA, tableContent1);
        setUpTableStructureIn(ipsProjectA);
        initCache();

        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContentsA = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(1, tableContentsA.size());
        assertThat(tableContentsA, hasItem(tableContent1));
    }

    @Test
    public void testChangeListener_contentRemoved() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        setUpTableStructureIn(ipsProjectA);
        initCache();

        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.REMOVED));

        List<IIpsSrcFile> tableContents1 = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(1, tableContents1.size());
        assertThat(tableContents1, hasItem(tableContent1));

        tableContentUpdaterC.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.REMOVED));

        List<IIpsSrcFile> tableContents3 = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(1, tableContents3.size());
        assertThat(tableContents3, hasItem(tableContent1));
    }

    @Test
    public void testChangeListener_contentChanged() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        setUpTableStructureIn(ipsProjectA);
        initCache();

        when(tableContent2.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn("invalid");
        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.CHANGED));

        List<IIpsSrcFile> tableContentsA = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(1, tableContentsA.size());
        assertThat(tableContentsA, hasItem(tableContent1));

        tableContentUpdaterC.ipsSrcFilesChanged(newChangeEvent(tableContent2, IResourceDelta.CHANGED));

        List<IIpsSrcFile> tableContentsC = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(1, tableContentsC.size());
        assertThat(tableContentsC, hasItem(tableContent1));
    }

    @Test
    public void testChangeListener_structureRemoved() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        setUpTableStructureIn(ipsProjectA);
        initCache();

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(2, tableContents.size());

        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableStructure, IResourceDelta.REMOVED));

        tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testChangeListener_structureAdded() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        initCache();

        setUpTableStructureIn(ipsProjectA);
        tableContentUpdaterA.ipsSrcFilesChanged(newChangeEvent(tableStructure, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContentsA = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(2, tableContentsA.size());
        assertThat(tableContentsA, hasItem(tableContent1));
        assertThat(tableContentsA, hasItem(tableContent2));

        tableContentUpdaterC.ipsSrcFilesChanged(newChangeEvent(tableStructure, IResourceDelta.ADDED));

        List<IIpsSrcFile> tableContentsC = tableContentsStructureCacheC.getTableContents(tableStructure);
        assertEquals(2, tableContentsC.size());
        assertThat(tableContentsC, hasItem(tableContent1));
        assertThat(tableContentsC, hasItem(tableContent2));
    }

    private IpsSrcFilesChangedEvent newChangeEvent(IIpsSrcFile ipsSrcFile, int kind) {
        Map<IIpsSrcFile, IResourceDelta> changedSrcFiles = new HashMap<>();
        IResourceDelta delta = mock(IResourceDelta.class);
        when(delta.getKind()).thenReturn(kind);
        changedSrcFiles.put(ipsSrcFile, delta);
        return new IpsSrcFilesChangedEvent(changedSrcFiles);
    }

    private void setUpProjectWithTableContents(IIpsProject project, IIpsSrcFile... files) {
        IpsObjectType ipsObjectType = IpsObjectType.TABLE_CONTENTS;
        setUpProjectFindIpsSrcFiles(project, ipsObjectType, files);
    }

    private void setUpProjectFindIpsSrcFiles(IIpsProject project, IpsObjectType ipsObjectType, IIpsSrcFile... files) {
        setUpSingleProjectFindIpsSrcFiles(project, ipsObjectType, files);
        IIpsProject[] referencingProjects = project.findReferencingProjects(true);
        for (IIpsProject referencingProject : referencingProjects) {
            setUpSingleProjectFindIpsSrcFiles(referencingProject, ipsObjectType, files);
        }
    }

    void setUpSingleProjectFindIpsSrcFiles(IIpsProject project, IpsObjectType ipsObjectType, IIpsSrcFile... files) {
        List<IIpsSrcFile> oldIpsSrcFiles = project.findAllIpsSrcFiles(ipsObjectType);
        ArrayList<IIpsSrcFile> newResultingFiles = new ArrayList<>(oldIpsSrcFiles);
        newResultingFiles.addAll(Arrays.asList(files));
        when(project.findAllIpsSrcFiles(ipsObjectType)).thenReturn(newResultingFiles);
        setUpFindQNameType(project, files);
    }

    private void setUpTableStructureIn(IIpsProject project) {
        setUpProjectFindIpsSrcFiles(project, IpsObjectType.TABLE_STRUCTURE, tableStructure);
    }

    private void setUpFindQNameType(IIpsProject project, IIpsSrcFile... files) {
        for (IIpsSrcFile file : files) {
            when(project.findIpsSrcFile(file.getQualifiedNameType())).thenReturn(file);
        }
    }

    void initCache() {
        tableContentsStructureCacheA.getTableContents(tableStructure);
        tableContentsStructureCacheC.getTableContents(tableStructure);
    }

}
