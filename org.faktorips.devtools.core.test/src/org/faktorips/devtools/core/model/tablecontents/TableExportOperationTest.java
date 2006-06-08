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

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public class TableExportOperationTest extends AbstractIpsPluginTest {
    
    String filename;
    TableExportOperation op;
    ITableContents table;
    
    protected void setUp() throws Exception {
        IIpsProject project = newIpsProject("test");
        ITableStructure structure = (ITableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestSructure");
        structure.newColumn().setName("X");
        structure.newColumn().setName("Y");
        structure.getIpsSrcFile().save(true, null);
        
        table = (ITableContents)newIpsObject(project, IpsObjectType.TABLE_CONTENTS, "TestTable");
        table.setTableStructure(structure.getQualifiedName());
        table.newColumn("A");
        table.newColumn("1");
        ITableContentsGeneration gen = (ITableContentsGeneration)table.newGeneration();
        gen.newRow();
        gen.newRow();
        filename = table.getName() + ".xls";
        op = new TableExportOperation(table, filename);
    }

    /*
     * Test method for 'org.faktorips.devtools.core.model.tablecontents.TableExportOperation.run(IProgressMonitor)'
     */
    public void testRun() throws Exception {
        op.run(new NullProgressMonitor());
        HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(new File(filename)));
        HSSFSheet sheet = workbook.getSheetAt(0);
        ITableContentsGeneration generation = (ITableContentsGeneration)table.getGenerations()[0];
        IRow[] contentRows = generation.getRows();
        for (int i = 1; i < contentRows.length; i++) { // first row is the header
            HSSFRow sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                fail();
            }
            for (short j = 0; j < table.getNumOfColumns(); j++) {
                assertEquals(contentRows[i].getValue(j), sheetRow.getCell(j).getStringCellValue());
            }
        }
    }
}
