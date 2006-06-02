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
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;

public abstract class AbstractXlsTableImportOperation implements IWorkspaceRunnable {
    
    private String filename;

    protected abstract ITableContentsGeneration getImportGeneration(short numberOfCols, IProgressMonitor monitor) throws CoreException;
    
    public AbstractXlsTableImportOperation(String filename) {
        this.filename = filename;
    }
    
    /**
     * {@inheritDoc}
     */
    public void run(IProgressMonitor monitor) throws CoreException {
        try {
            File importFile = new File(filename);
            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(importFile));
            HSSFSheet sheet = workbook.getSheetAt(0);
            short numberOfCols = getNumberOfCols(sheet);
            ITableContentsGeneration generation = getImportGeneration(numberOfCols, monitor);
            fillGeneration(generation, sheet, monitor);
            generation.getIpsObject().getIpsSrcFile().save(true, monitor);
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.AbstractXlsTableImportOperation_errRead, filename), e));
        }
    }

    private void fillGeneration(ITableContentsGeneration generation, HSSFSheet sheet, IProgressMonitor monitor) {
        for (int i = 1; ; i++) {
            HSSFRow sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                break;
            }
            IRow genRow = generation.newRow();
            for (short j = 0; ; j++) {
                HSSFCell cell = sheetRow.getCell(j);
                if (cell == null) {
                    break;
                }
                genRow.setValue(j, cell.getStringCellValue());
            }
        }
    }

    private short getNumberOfCols(HSSFSheet sheet) throws CoreException {
        if (sheet == null) {
            throw new CoreException(new IpsStatus(Messages.AbstractXlsTableImportOperation_errNoSheets));
        }
        HSSFRow header = sheet.getRow(0);
        if (header == null) {
            throw new CoreException(new IpsStatus(Messages.AbstractXlsTableImportOperation_errNoRows));
        }
        for (short i = 0; ; i++) {
            if (header.getCell(i) == null) {
                return i;
            }
        }
    }

}
