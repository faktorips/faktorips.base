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

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IFile;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.util.StringUtil;

public class XlsTableImportIntoExistingOperationTest extends AbstractIpsPluginTest {
    
    private IFile file;
    private IIpsProject project;
    private IIpsPackageFragment pack;
    private HSSFWorkbook workbook;

    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("test");
        file = project.getProject().getFile("Test.xls");
        file.create(getClass().getResourceAsStream("Test.xls"), true, null);
        pack = project.getSourceIpsPackageFragmentRoots()[0].createPackageFragment("test", true, null);
        workbook = new HSSFWorkbook(new FileInputStream(file.getRawLocation().toOSString()));
    }
    
    /*
     * Test method for 'org.faktorips.devtools.core.model.tablecontents.TableImportOperation.run(IProgressMonitor)'
     */
    public void testRun() throws Exception {
        XlsTableImportIntoNewOperation op = new XlsTableImportIntoNewOperation(file.getRawLocation().toOSString(), pack, "Test", "Test");
        op.run(null);
        ITableContents contents = (ITableContents)project.findIpsObject(IpsObjectType.TABLE_CONTENTS, StringUtil.qualifiedName(pack.getName(), "Test"));
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
}
