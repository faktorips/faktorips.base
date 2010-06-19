/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;

public class TableContentsCompareItemTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new TableContentsCompareItemCreator();
    private ITableContentsGeneration generation;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;
    private IIpsPackageFragmentRoot root;

    private TableContentsCompareItem compareItemRoot;
    private ITableContents table;
    private IRow row1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IIpsProject proj = newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        table = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table1");
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");

        GregorianCalendar calendar = new GregorianCalendar();
        generation = (ITableContentsGeneration)table.newGeneration(calendar);
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

    public void testGetType() {
        assertEquals("ipstablecontents", compareItemRoot.getType());
    }

    public void testEqualsObject() throws CoreException {
        // set Row values of default Table
        row1.setValue(0, "65");
        row1.setValue(1, "69");
        row1.setValue(2, "E69");
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));

        // create new table (and row) to avoid that both compareitems reference the same row
        // instance
        ITableContents table2 = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table2");
        table2.newColumn("1");
        table2.newColumn("2");
        table2.newColumn("3");
        GregorianCalendar calendar = new GregorianCalendar();
        ITableContentsGeneration generation2 = (ITableContentsGeneration)table2.newGeneration(calendar);
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
        // rows equal in contentString (ignored whitespace) but not in content
        assertEquals(rowItem1.getContentStringWithoutWhiteSpace(), rowItem2.getContentStringWithoutWhiteSpace());
        assertFalse(rowItem1.equals(rowItem2));

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
        assertEquals(rowItem1.getContentStringWithoutWhiteSpace(), rowItem2.getContentStringWithoutWhiteSpace());
        assertFalse(rowItem1.getIpsElement().getName().equals(rowItem2.getIpsElement().getName()));
        assertFalse(rowItem1.equals(rowItem2));

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
        assertFalse(rowItem1.getContentStringWithoutWhiteSpace().equals(rowItem2.getContentStringWithoutWhiteSpace()));
        assertFalse(rowItem1.equals(rowItem2));

        // add column
        table2.newColumn("4");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];
        // compare rows with differing columnNumber
        assertFalse(rowItem1.getContentStringWithoutWhiteSpace().equals(rowItem2.getContentStringWithoutWhiteSpace()));
        assertFalse(rowItem1.equals(rowItem2));
    }

    public void testHashCode() throws CoreException {
        // set Row values of default Table
        row1.setValue(0, "65");
        row1.setValue(1, "69");
        row1.setValue(2, "E69");
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));

        // create new table (and row) to avoid that both compareitems reference the same row
        // instance
        ITableContents table2 = (ITableContents)newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table2");
        table2.newColumn("1");
        table2.newColumn("2");
        table2.newColumn("3");
        GregorianCalendar calendar = new GregorianCalendar();
        ITableContentsGeneration generation2 = (ITableContentsGeneration)table2.newGeneration(calendar);
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
        // rows equal in contentString (ignored whitespace) but not in content
        assertEquals(rowItem1.getContentStringWithoutWhiteSpace().hashCode(), rowItem2
                .getContentStringWithoutWhiteSpace().hashCode());
        assertFalse(rowItem1.hashCode() == rowItem2.hashCode());

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
        assertEquals(rowItem1.getContentStringWithoutWhiteSpace().hashCode(), rowItem2
                .getContentStringWithoutWhiteSpace().hashCode());
        assertFalse(rowItem1.getIpsElement().getName().equals(rowItem2.getIpsElement().getName()));
        assertFalse(rowItem1.hashCode() == rowItem2.hashCode());

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
        assertFalse(rowItem1.getContentStringWithoutWhiteSpace().hashCode() == rowItem2
                .getContentStringWithoutWhiteSpace().hashCode());
        assertFalse(rowItem1.hashCode() == rowItem2.hashCode());

        // add column
        table2.newColumn("4");
        compareItemRoot2 = (TableContentsCompareItem)structureCreator
                .getStructure(new ResourceNode(correspondingFile2));
        tableItem2 = (TableContentsCompareItem)compareItemRoot2.getChildren()[0];
        genItem2 = (TableContentsCompareItem)tableItem2.getChildren()[0];
        rowItem2 = (TableContentsCompareItem)genItem2.getChildren()[0];
        // compare rows with differing columnNumber
        assertFalse(rowItem1.getContentStringWithoutWhiteSpace().hashCode() == rowItem2
                .getContentStringWithoutWhiteSpace().hashCode());
        assertFalse(rowItem1.hashCode() == rowItem2.hashCode());

        Differencer differencer = new Differencer();
        differencer.findDifferences(false, null, null, null, compareItemRoot2, compareItemRoot);
    }

}
