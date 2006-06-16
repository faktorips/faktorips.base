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

package org.faktorips.devtools.core.model.tablecontents;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;

public class XlsTableImportIntoNewOperationTest extends AbstractIpsPluginTest {
    
    private IFile file;
    private IIpsProject project;
    private HSSFWorkbook workbook;
    private ITableContents contents;

    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("test");
        file = project.getProject().getFile("Test.xls");
        InputStream is = null;
        try{
            is = getClass().getResourceAsStream("Test.xls"); 
            file.create(is, true, null);
        }
        finally{
            if(is != null){
                is.close();
            }
        }
        FileInputStream excelFile = null;
        try{
            excelFile = new FileInputStream(file.getRawLocation().toOSString());
            workbook = new HSSFWorkbook(excelFile);
        }
        finally{
            excelFile.close();
        }
        contents = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "Test");
        contents.setTableStructure("Test");
        contents.newColumn("AAA");
        contents.newColumn("BBB");
        ITableContentsGeneration gen = (ITableContentsGeneration)contents.newGeneration();
        gen.newRow();
        gen.newRow();
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.model.tablecontents.TableImportOperation.run(IProgressMonitor)'
     */
    public void testRunReplace() throws Exception {
        XlsTableImportIntoExistingOperation op = new XlsTableImportIntoExistingOperation(file.getRawLocation().toOSString(), false, project, "Test");
        op.run(null);
        ITableContentsGeneration gen = (ITableContentsGeneration)contents.getGenerations()[0];
        IRow[] rows = gen.getRows();
        HSSFSheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < rows.length; i++) {
            IRow contentRow = rows[i];
            HSSFRow sheetRow = sheet.getRow(i + 1); // first row is the header
            if (sheetRow == null) {
                fail();
            }
            for (short j = 0; j < (short)contents.getNumOfColumns(); j++) {
                assertEquals(contentRow.getValue(j), sheetRow.getCell(j).getStringCellValue());
            }
        }
        
        assertEquals("Test", contents.getTableStructure());
    }

    /*
     * Test method for 'org.faktorips.devtools.core.model.tablecontents.TableImportOperation.run(IProgressMonitor)'
     */
    public void testRunAppend() throws Exception {
        XlsTableImportIntoExistingOperation op = new XlsTableImportIntoExistingOperation(file.getRawLocation().toOSString(), true, project, "Test");
        op.run(null);
        ITableContentsGeneration gen = (ITableContentsGeneration)contents.getGenerations()[0];
        IRow[] rows = gen.getRows();
        HSSFSheet sheet = workbook.getSheetAt(0);
        for (int i = 2; i < rows.length; i++) {
            IRow contentRow = rows[i];
            HSSFRow sheetRow = sheet.getRow(i - 1); // first row is the header
            if (sheetRow == null) {
                fail();
            }
            for (short j = 0; j < (short)contents.getNumOfColumns(); j++) {
                assertEquals(contentRow.getValue(j), sheetRow.getCell(j).getStringCellValue());
            }
        }
        
        assertEquals("Test", contents.getTableStructure());
    }
}
