/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.junit.Before;
import org.junit.Test;

public class TableContentsCompareItemTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new TableContentsCompareItemCreator();
    private ITableRows generation;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;
    private IIpsPackageFragmentRoot root;

    private TableContentsCompareItem compareItemRoot;
    private ITableContents table;
    private IRow row1;
    private IIpsProject proj;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        proj = newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        ITableStructure structure = (ITableStructure)newIpsObject(proj, IpsObjectType.TABLE_STRUCTURE,
                "StructureTable");
        structure.newColumn();
        structure.newColumn();
        structure.newColumn();
        table = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table1");
        generation = table.newTableRows();
        table.setTableStructure(structure.getQualifiedName());
        table.newColumn("1", "");
        table.newColumn("2", "");
        table.newColumn("3", "");

        row1 = generation.newRow();
        row1.setValue(0, "r1_c1");
        row1.setValue(1, "r1_c2");
        row1.setValue(2, "r1_c3");
        generation.newRow();
        generation.newRow();
        generation.newRow();

        srcFile = table.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        // initialized compareItem
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testInit() {
        // create uninitialized tree of compareitems
        TableContentsCompareItem compareItemSrcFile = new TableContentsCompareItem(null, srcFile);
        TableContentsCompareItem compareItemTable = new TableContentsCompareItem(compareItemSrcFile, table);
        TableContentsCompareItem compareItemGeneration = new TableContentsCompareItem(compareItemTable, generation);
        TableContentsCompareItem compareItemRow1 = new TableContentsCompareItem(compareItemGeneration, row1);

        // test content/name before initialization
        assertNull(compareItemSrcFile.getContentString());
        assertNull(compareItemSrcFile.getContentStringWithoutWhiteSpace());
        assertNull(compareItemSrcFile.getName());
        assertNull(compareItemSrcFile.getDocument());

        // init
        compareItemSrcFile.init();

        // test content/name after initialization
        assertNotNull(compareItemSrcFile.getContentString());
        assertNotNull(compareItemSrcFile.getContentStringWithoutWhiteSpace());
        assertNotNull(compareItemSrcFile.getName());
        assertNotNull(compareItemSrcFile.getDocument());

        // test initialization of children
        assertNotNull(compareItemRow1.getContentString());
        assertNotNull(compareItemRow1.getContentStringWithoutWhiteSpace());
        assertNotNull(compareItemRow1.getName());
        assertNotNull(compareItemRow1.getDocument());
    }

    @Test
    public void testGetType() {
        assertEquals("ipstablecontents", compareItemRoot.getType());
    }

    @Test
    public void testEqualsObject() throws CoreException {
        // set Row values of default Table
        row1.setValue(0, "65");
        row1.setValue(1, "69");
        row1.setValue(2, "E69");
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));

        // create new table (and row) to avoid that both compareitems reference the same row
        // instance
        ITableContents table2 = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table2");
        ITableStructure structure2 = (ITableStructure)newIpsObject(proj, IpsObjectType.TABLE_STRUCTURE,
                "StructureTable2");
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();

        ITableRows generation2 = table2.newTableRows();
        table2.setTableStructure(structure2.getQualifiedName());
        table2.newColumn("1", "");
        table2.newColumn("2", "");
        table2.newColumn("3", "");
        IRow row2 = generation2.newRow();
        row2.setValue(0, "6");
        row2.setValue(1, "569");
        row2.setValue(2, "E69");
        IRow row2b = generation2.newRow();
        generation2.newRow();
        generation2.newRow();
        IIpsSrcFile srcFile2 = table2.getIpsSrcFile();
        IFile correspondingFile2 = srcFile2.getCorrespondingFile();
        TableContentsCompareItem compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));

        TableContentsCompareItem tableItem1 = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem1 = (TableContentsCompareItem)tableItem1.getChildren()[0];
        TableContentsCompareItem rowItem1 = (TableContentsCompareItem)genItem1.getChildren()[0];
        TableContentsCompareItem tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        TableContentsCompareItem genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        TableContentsCompareItem rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];

        // fill row (different ID) with same contents
        row2b.setValue(0, "65");
        row2b.setValue(1, "69");
        row2b.setValue(2, "E69");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[1]; // second row (row2b)
        // compare rows with same content and differing rownumber
        assertEquals(rowItem1.getContentString(), rowItem2.getContentString());
        assertFalse(rowItem1.getIpsElement().getName().equals(rowItem2.getIpsElement().getName()));
        assertTrue(rowItem1.equals(rowItem2));

        // change contents
        row2.setValue(0, "x");
        row2.setValue(1, "xx");
        row2.setValue(2, "xXx");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];
        // compare rows with differing content
        assertFalse(rowItem1.getContentString().equals(rowItem2.getContentString()));
        assertFalse(rowItem1.equals(rowItem2));

        // add column
        structure2.newColumn();
        table2.newColumn("4", "");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];
        // compare rows with differing columnNumber
        assertFalse(rowItem1.getContentString().equals(rowItem2.getContentString()));
        assertFalse(rowItem1.equals(rowItem2));
    }

    @Test
    public void testHashCode() throws CoreException {
        // set Row values of default Table
        row1.setValue(0, "65");
        row1.setValue(1, "69");
        row1.setValue(2, "E69");
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));

        // create new table (and row) to avoid that both compareitems reference the same row
        // instance
        ITableContents table2 = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table2");
        ITableStructure structure2 = (ITableStructure)newIpsObject(proj, IpsObjectType.TABLE_STRUCTURE,
                "StructureTable2");
        structure2.newColumn();
        structure2.newColumn();
        structure2.newColumn();
        ITableRows generation2 = table2.newTableRows();
        table2.setTableStructure(structure2.getQualifiedName());
        table2.newColumn("1", "");
        table2.newColumn("2", "");
        table2.newColumn("3", "");
        IRow row2 = generation2.newRow();
        row2.setValue(0, "6");
        row2.setValue(1, "569");
        row2.setValue(2, "E69");
        IRow row2b = generation2.newRow();
        generation2.newRow();
        generation2.newRow();
        IIpsSrcFile srcFile2 = table2.getIpsSrcFile();
        IFile correspondingFile2 = srcFile2.getCorrespondingFile();
        TableContentsCompareItem compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));

        TableContentsCompareItem tableItem1 = (TableContentsCompareItem)compareItemRoot.getChildren()[0];
        TableContentsCompareItem genItem1 = (TableContentsCompareItem)tableItem1.getChildren()[0];
        TableContentsCompareItem rowItem1 = (TableContentsCompareItem)genItem1.getChildren()[0];
        TableContentsCompareItem tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        TableContentsCompareItem genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        TableContentsCompareItem rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];

        // fill row (different rownumber) with same contents
        row2b.setValue(0, "65");
        row2b.setValue(1, "69");
        row2b.setValue(2, "E69");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[1]; // second row (row2b)
        // compare rows with same content and different ID
        assertEquals(rowItem1.getContentString().hashCode(), rowItem2.getContentString().hashCode());
        assertFalse(rowItem1.getIpsElement().getName().equals(rowItem2.getIpsElement().getName()));
        assertTrue(rowItem1.hashCode() == rowItem2.hashCode());

        // change contents
        row2.setValue(0, "x");
        row2.setValue(1, "xx");
        row2.setValue(2, "xXx");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];
        // compare rows with differing content
        assertFalse(rowItem1.getContentString().hashCode() == rowItem2.getContentString().hashCode());
        assertFalse(rowItem1.hashCode() == rowItem2.hashCode());

        // add column
        structure2.newColumn();
        table2.newColumn("4", "");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];
        // compare rows with differing columnNumber
        assertFalse(rowItem1.getContentString().hashCode() == rowItem2.getContentString().hashCode());
        assertFalse(rowItem1.hashCode() == rowItem2.hashCode());

        Differencer differencer = new Differencer();
        differencer.findDifferences(false, null, null, null, compareItemRoot2, compareItemRoot);
    }

}
