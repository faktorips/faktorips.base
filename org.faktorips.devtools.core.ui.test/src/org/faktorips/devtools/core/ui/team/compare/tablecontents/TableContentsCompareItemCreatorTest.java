/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.junit.Before;
import org.junit.Test;

public class TableContentsCompareItemCreatorTest extends AbstractIpsPluginTest {

    private IStructureCreator structureCreator = new TableContentsCompareItemCreator();
    private ITableContentsGeneration generation;
    private IIpsSrcFile srcFile;
    private IFile correspondingFile;
    private IIpsPackageFragmentRoot root;

    private TableContentsCompareItem compareItemRoot;
    private ITableContents table;
    private IRow row1;
    private IRow row2;
    private IRow row3;
    private IRow row4;

    @Override
    @Before
    public void setUp() throws Exception {
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
        row2 = generation.newRow();
        row3 = generation.newRow();
        row4 = generation.newRow();

        srcFile = table.getIpsSrcFile();
        correspondingFile = srcFile.getCorrespondingFile();

        // initialized compareItem
        compareItemRoot = (TableContentsCompareItem)structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    @Test
    public void testGetStructure() {
        assertEquals(srcFile, compareItemRoot.getIpsElement());

        Object[] children = compareItemRoot.getChildren();
        TableContentsCompareItem compareItem = (TableContentsCompareItem)children[0];
        assertEquals(table, compareItem.getIpsElement());

        children = compareItem.getChildren();
        TableContentsCompareItem compareItemGen = (TableContentsCompareItem)children[0];

        assertEquals(generation, compareItemGen.getIpsElement());

        children = compareItemGen.getChildren();
        TableContentsCompareItem compareItemRow1 = (TableContentsCompareItem)children[0];
        TableContentsCompareItem compareItemRow2 = (TableContentsCompareItem)children[1];
        TableContentsCompareItem compareItemRow3 = (TableContentsCompareItem)children[2];
        TableContentsCompareItem compareItemRow4 = (TableContentsCompareItem)children[3];

        assertEquals(row1, compareItemRow1.getIpsElement());
        assertEquals(row2, compareItemRow2.getIpsElement());
        assertEquals(row3, compareItemRow3.getIpsElement());
        assertEquals(row4, compareItemRow4.getIpsElement());
    }

    @Test
    public void testGetContents() {
        Object[] children = compareItemRoot.getChildren();
        TableContentsCompareItem compareItem = (TableContentsCompareItem)children[0];

        String contentString = structureCreator.getContents(compareItemRoot, false);
        assertEquals(compareItemRoot.getContentString(), contentString);
        contentString = structureCreator.getContents(compareItem, false);
        assertEquals(compareItem.getContentString(), contentString);

        contentString = structureCreator.getContents(compareItemRoot, true);
        assertTrue(compareItemRoot.getContentStringWithoutWhiteSpace().equals(contentString));
        contentString = structureCreator.getContents(compareItem, true);
        assertTrue(compareItem.getContentStringWithoutWhiteSpace().equals(contentString));
    }

    @Test
    public void testGetName() {
        assertEquals(Messages.TableContentsCompareItemCreator_TableContentsStructureCompare, structureCreator.getName());
    }

    @Test
    public void testLocate() {
        assertNull(structureCreator.locate(null, compareItemRoot));
    }

}
