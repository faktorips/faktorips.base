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
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableContentsStructureCacheTest {

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

    private TableContentsStructureCache tableContentsStructureCacheBase;

    private TableContentsStructureCache tableContentsStructureCacheA;

    private TableContentsStructureCache tableContentsStructureCacheB;

    private TableContentsStructureCache tableContentsStructureCacheC;

    @Before
    public void setUp() {
        when(ipsProjectBase.getIpsModel()).thenReturn(ipsModel);
        when(ipsProjectA.getIpsModel()).thenReturn(ipsModel);
        when(ipsProjectB.getIpsModel()).thenReturn(ipsModel);
        when(ipsProjectC.getIpsModel()).thenReturn(ipsModel);

        when(ipsProjectBase.findReferencingProjectLeavesOrSelf()).thenReturn(
                new IIpsProject[] { ipsProjectA, ipsProjectB });
        when(ipsProjectBase.findReferencingProjects(true)).thenReturn(
                new IIpsProject[] { ipsProjectA, ipsProjectB, ipsProjectC });
        when(ipsProjectA.findReferencingProjects(true)).thenReturn(new IIpsProject[] { ipsProjectC });
        when(ipsProjectB.findReferencingProjects(true)).thenReturn(new IIpsProject[] {});
        when(ipsProjectC.findReferencingProjects(true)).thenReturn(new IIpsProject[] {});
        when(ipsModel.getIpsProjects()).thenReturn(
                new IIpsProject[] { ipsProjectA, ipsProjectB, ipsProjectBase, ipsProjectC });
        tableContentsStructureCacheBase = new TableContentsStructureCache(ipsProjectBase);
        tableContentsStructureCacheA = new TableContentsStructureCache(ipsProjectA);
        tableContentsStructureCacheB = new TableContentsStructureCache(ipsProjectB);
        tableContentsStructureCacheC = new TableContentsStructureCache(ipsProjectC);
    }

    @Before
    public void setUpsIpsSrcFiles() {
        when(tableStructure.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_STRUCTURE);
        when(tableStructure.getIpsProject()).thenReturn(ipsProjectBase);
        when(tableContent1.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent1.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(TABLE_STRUCTURE);
        when(tableContent2.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent2.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn("");
        when(tableContent3.getIpsObjectType()).thenReturn(IpsObjectType.TABLE_CONTENTS);
        when(tableContent3.getIpsProject()).thenReturn(ipsProjectB);
        when(tableContent3.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(TABLE_STRUCTURE);
    }

    @Test
    public void testGetTableContents() throws Exception {
    }

    @Test
    public void testInit_noContent_base() throws Exception {
        List<IIpsSrcFile> tableContents = tableContentsStructureCacheBase.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testInit_noContent_p1() throws Exception {
        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testInit_noContent_p2() throws Exception {
        List<IIpsSrcFile> tableContents = tableContentsStructureCacheB.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureInWrongProject_projectA() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        doReturn(Collections.emptyList()).when(ipsProjectBase).findAllIpsSrcFiles(IpsObjectType.TABLE_CONTENTS);

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureInWrongProject_base() throws Exception {
        setUpProjectWithTableContents(ipsProjectBase, tableContent1, tableContent2);
        when(ipsProjectA.findAllIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE)).thenReturn(Arrays.asList(tableStructure));

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureReferencedProject_base() throws Exception {
        setUpProjectWithTableContents(ipsProjectBase, tableContent1, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectBase);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectBase);
        setUpTableStructureIn(ipsProjectBase);

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheBase.getTableContents(tableStructure);

        assertEquals(1, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));
    }

    @Test
    public void testGetTableContents_structureReferencedProject_projectA() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectA);
        setUpTableStructureIn(ipsProjectBase);

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);

        assertEquals(1, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));
    }

    @Test
    public void testGetTableContents_structureReferencedProject_projectB() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectA);
        setUpTableStructureIn(ipsProjectBase);

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheB.getTableContents(tableStructure);

        assertEquals(0, tableContents.size());
    }

    @Test
    public void testGetTableContents_structureWrongReferencedProject() throws Exception {
        setUpTableStructureIn(ipsProjectA);
        // no contents
        setUpProjectWithTableContents(ipsProjectA);
        setUpProjectWithTableContents(ipsProjectBase, tableContent1, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectBase);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectBase);

        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);

        assertTrue(tableContents.isEmpty());
    }

    @Test
    public void testGetTableContents_structureReferencedProject_projectC() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1);
        setUpProjectWithTableContents(ipsProjectC, tableContent2);
        when(tableContent1.getIpsProject()).thenReturn(ipsProjectA);
        when(tableContent2.getIpsProject()).thenReturn(ipsProjectC);
        setUpTableStructureIn(ipsProjectBase);
        setUpTableStructureNameFor(tableContent2, TABLE_STRUCTURE);

        List<IIpsSrcFile> tableContents1 = tableContentsStructureCacheA.getTableContents(tableStructure);
        List<IIpsSrcFile> tableContents3 = tableContentsStructureCacheC.getTableContents(tableStructure);

        assertEquals(1, tableContents1.size());
        assertThat(tableContents1, hasItem(tableContent1));
        assertEquals(2, tableContents3.size());
        assertThat(tableContents3, hasItems(tableContent1, tableContent2));
    }

    @Test
    public void testGetTableContents_testDefenceCopy() throws Exception {
        setUpProjectWithTableContents(ipsProjectA, tableContent1);
        setUpTableStructureIn(ipsProjectA);
        List<IIpsSrcFile> tableContents = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(1, tableContents.size());
        assertThat(tableContents, hasItem(tableContent1));

        tableContents.add(tableContent2);

        List<IIpsSrcFile> tableContentsNew = tableContentsStructureCacheA.getTableContents(tableStructure);
        assertEquals(1, tableContentsNew.size());
        assertThat(tableContentsNew, hasItem(tableContent1));
    }

    @Test
    public void testInit() {
        assertTrue(tableContentsStructureCacheA.isNew());

        tableContentsStructureCacheA.getTableContents(tableStructure);
        assertTrue(tableContentsStructureCacheA.isInitialized());

        tableContentsStructureCacheA.clear();
        assertTrue(tableContentsStructureCacheA.isNew());
    }

    private void setUpTableStructureNameFor(IIpsSrcFile tc, String name) {
        when(tc.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE)).thenReturn(name);
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
        when(tableStructure.getQualifiedNameType()).thenReturn(
                new QualifiedNameType(TABLE_STRUCTURE, IpsObjectType.TABLE_STRUCTURE));
        setUpProjectFindIpsSrcFiles(project, IpsObjectType.TABLE_STRUCTURE, tableStructure);
    }

    private void setUpFindQNameType(IIpsProject project, IIpsSrcFile... files) {
        for (IIpsSrcFile file : files) {
            when(project.findIpsSrcFile(file.getQualifiedNameType())).thenReturn(file);
        }
    }

}
