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
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

/**
 * 
 * @author Thorsten Waertel
 */
public class TableExportOperation implements IWorkspaceRunnable {
    
    // The maximum number of rows allowed in an Excel sheet
    private static final int MAX_ROWS = 65535;
    
    private ITableContents contents;
    private String filename;

	/**
	 * 
	 */
	public TableExportOperation(ITableContents contents, String filename) {
		super();
        this.contents = contents;
        this.filename = filename;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
        
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        IIpsObjectGeneration[] gens = contents.getGenerations();
        if (gens.length == 0) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TableExportOperation_errNoGenerations, contents.getName())));
        }
        ITableContentsGeneration currentGeneration = (ITableContentsGeneration) gens[0]; // currently, there is only one generation per table contents
        
        monitor.beginTask(Messages.TableExportOperation_labelMonitorTitle, 2 + currentGeneration.getNumOfRows());
        
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(contents.getName());
        
        monitor.worked(1);
        
        ITableStructure structure = contents.findTableStructure();
        
        if (structure == null) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TableExportOperation_errStructureNotFound, contents.getTableStructure())));
        }
        
        createHeader(sheet, structure.getColumns());
        
        monitor.worked(1);
        
        createDataCells(sheet, currentGeneration, monitor);

        try {
            workbook.write(new FileOutputStream(new File(filename)));
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(Messages.TableExportOperation_errWrite, e));
        }
	}

    private void createHeader(HSSFSheet sheet, IColumn[] columns) throws CoreException {
        HSSFRow headerRow = sheet.createRow(0);
        if (columns.length > Short.MAX_VALUE) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TableExportOperation_errStructureTooMuchColumns,
                    new Object[] { new Integer(columns.length), contents.getTableStructure(), new Short(Short.MAX_VALUE) })));
        }
        if (contents.getNumOfColumns() > Short.MAX_VALUE) { // contents can have a differtent number of columns than defined in the structure
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TableExportOperation_errContentsTooMuchColumns,
                    new Object[] { new Integer(columns.length), contents.getName(), new Short(Short.MAX_VALUE) })));
        }
        for (int i = 0; i < columns.length; i++) {
            headerRow.createCell((short) i).setCellValue(columns[i].getName());
        }
    }
    
    private void createDataCells(HSSFSheet sheet, ITableContentsGeneration generation,
            IProgressMonitor monitor) throws CoreException {
        if (generation.getNumOfRows() > MAX_ROWS) {
            throw new CoreException(new IpsStatus(NLS.bind(Messages.TableExportOperation_errTooMuchRows,
                    new Object[] { new Integer(generation.getNumOfRows()), contents.getName(), new Integer(MAX_ROWS) })));
        }
        for (int i = 0; i < generation.getRows().length; i++) {
            HSSFRow sheetRow = sheet.createRow(i + 1); // row 0 already used for header
            IRow contentsRow = generation.getRows()[i];
            for (int j = 0; j < contents.getNumOfColumns(); j++) {
                sheetRow.createCell((short) j).setCellValue(contentsRow.getValue(j));
            }
            monitor.worked(1);
        }
    }
}
