/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.tablecontents;

import java.util.GregorianCalendar;

import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureCreator;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProject;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
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
    
    protected void setUp() throws Exception {
        super.setUp();
        
        IIpsProject proj= (IpsProject)newIpsProject("TestProject");
        root = proj.getIpsPackageFragmentRoots()[0];
        table = (ITableContents) newIpsObject(root, IpsObjectType.TABLE_CONTENTS, "Table1");
        table.newColumn("1");
        table.newColumn("2");
        table.newColumn("3");
        
        GregorianCalendar calendar= new GregorianCalendar();
        generation = (ITableContentsGeneration) table.newGeneration(calendar);
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
        compareItemRoot = (TableContentsCompareItem) structureCreator.getStructure(new ResourceNode(correspondingFile));
    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.tablecontents.TableContentsCompareItem.init()'
     */
    public void testInit() {
        // create uninitialized tree of compareitems
        TableContentsCompareItem compareItemSrcFile= new TableContentsCompareItem(null, srcFile);
        TableContentsCompareItem compareItemTable= new TableContentsCompareItem(compareItemSrcFile, table);
        TableContentsCompareItem compareItemGeneration= new TableContentsCompareItem(compareItemTable, generation);
        TableContentsCompareItem compareItemRow1= new TableContentsCompareItem(compareItemGeneration, row1);

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

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.tablecontents.TableContentsCompareItem.getType()'
     */
    public void testGetType() {
        assertEquals("ipstablecontents", compareItemRoot.getType());
    }

    
    
    
    /* ****************************************************************
     * protected Methods
     */
    
    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem.initContentString()'
     */
//    public void testInitContentString() {
//        TableContentsCompareItem tableItem= (TableContentsCompareItem) compareItemRoot.getChildren()[0];
//        TableContentsCompareItem genItem= (TableContentsCompareItem) tableItem.getChildren()[0];
//        assertEquals("", genItem.initContentString());        
//        
//        TableContentsCompareItem rowItem= (TableContentsCompareItem) genItem.getChildren()[0];
//        TableContentsCompareItem rowItem2= (TableContentsCompareItem) genItem.getChildren()[1];
//        TableContentsCompareItem rowItem3= (TableContentsCompareItem) genItem.getChildren()[2];
//        TableContentsCompareItem rowItem4= (TableContentsCompareItem) genItem.getChildren()[3];
//        assertEquals("0:\t\tr1_c1\t\tr1_c2\t\tr1_c3\t\t", rowItem.initContentString());
//        assertEquals("1:\t\t1\t\t\t2\t\t\t3\t\t\t", rowItem2.initContentString());
//        assertEquals("2:\t\t1\t\t\t2\t\t\t3\t\t\t", rowItem3.initContentString());
//        assertEquals("3:\t\t1\t\t\t2\t\t\t3\t\t\t", rowItem4.initContentString());
//    }

    /*
     * Test method for 'org.faktorips.devtools.core.ui.team.compare.AbstractCompareItem.initName()'
     */
//    public void testInitName() {
//        DateFormat dateFormat = IpsPlugin.getDefault().getIpsPreferences().getValidFromFormat();
//        
//        TableContentsCompareItem tableItem= (TableContentsCompareItem) compareItemRoot.getChildren()[0];
//        String name= tableItem.getIpsElement().getName();
//        assertEquals(Messages.TableContentsCompareItem_TableContents+": \""+name+"\"", tableItem.initContentString());
//        
//        TableContentsCompareItem genItem= (TableContentsCompareItem) tableItem.getChildren()[0];
//        Date date= ((IIpsObjectGeneration)genItem.getIpsElement()).getValidFrom().getTime();
//        assertEquals(Messages.TableContentsCompareItem_Generation+": \""+dateFormat.format(date)+"\"", genItem.initContentString());        
//        
//        TableContentsCompareItem rowItem= (TableContentsCompareItem) genItem.getChildren()[0];
//        TableContentsCompareItem rowItem2= (TableContentsCompareItem) genItem.getChildren()[1];
//        TableContentsCompareItem rowItem3= (TableContentsCompareItem) genItem.getChildren()[2];
//        TableContentsCompareItem rowItem4= (TableContentsCompareItem) genItem.getChildren()[3];
//        assertEquals(Messages.TableContentsCompareItem_Row+": 0", rowItem.initName());
//        assertEquals(Messages.TableContentsCompareItem_Row+": 1", rowItem2.initName());
//        assertEquals(Messages.TableContentsCompareItem_Row+": 2", rowItem3.initName());
//        assertEquals(Messages.TableContentsCompareItem_Row+": 3", rowItem4.initName());
//    }
    
//    public void testGetColumnWidths(){
//        // test columnwidth before init
//        assertNull(compareItemRoot.getColumnWidths());
//        
//        // set cell content to force a 9 tabs column width in the second column and reinit artificially
//        row3.setValue(1, "thisStringNeedsEightTabsToFit");
//        compareItemRoot.init();
//
//        // test columnwidth after init
//        int[] columnWidths= compareItemRoot.getColumnWidths();
//        assertNotNull(columnWidths);
//        // the rowNumber column is two tabs wide
//        assertEquals(2, columnWidths[0]);
//        // the first column is two tabs wide (because of "r1_c1")
//        assertEquals(3, columnWidths[1]);
//        // the first column is nine tabs wide (because of "thisStringNeedsEightTabsToFit")
//        assertEquals(9, columnWidths[2]);
//        // the first column is two tabs wide (because of "r1_c3")
//        assertEquals(3, columnWidths[3]);
//        
//        // test subclasses
//        TableContentsCompareItem tableItem= (TableContentsCompareItem) compareItemRoot.getChildren()[0];
//        TableContentsCompareItem genItem= (TableContentsCompareItem) tableItem.getChildren()[0];
//        TableContentsCompareItem rowItem= (TableContentsCompareItem) genItem.getChildren()[0];
//        TableContentsCompareItem rowItem2= (TableContentsCompareItem) genItem.getChildren()[1];
//        TableContentsCompareItem rowItem3= (TableContentsCompareItem) genItem.getChildren()[2];
//        TableContentsCompareItem rowItem4= (TableContentsCompareItem) genItem.getChildren()[3];
//        assertSame(columnWidths, tableItem.getColumnWidths());
//        assertSame(columnWidths, genItem.getColumnWidths());
//        assertSame(columnWidths, rowItem.getColumnWidths());
//        assertSame(columnWidths, rowItem2.getColumnWidths());
//        assertSame(columnWidths, rowItem3.getColumnWidths());
//        assertSame(columnWidths, rowItem4.getColumnWidths());
//    }
}
