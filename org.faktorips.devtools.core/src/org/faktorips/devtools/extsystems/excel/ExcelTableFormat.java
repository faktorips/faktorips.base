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

package org.faktorips.devtools.extsystems.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.extsystems.AbstractExternalTableFormat;
import org.faktorips.util.message.MessageList;

/**
 * Table format for Microsoft's Excel.
 * 
 * @author Thorsten Guenther
 */
public class ExcelTableFormat extends AbstractExternalTableFormat {

	/**
	 * {@inheritDoc}
	 */
	public IWorkspaceRunnable getExportTableOperation(ITableContents contents, IPath filename, String nullRepresentationString, MessageList list) {
		return new ExcelTableExportOperation(contents, filename.toOSString(), this, nullRepresentationString, list);
	}

	/**
     * {@inheritDoc}
     */
    public IWorkspaceRunnable getImportTableOperation(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list) {
        return new ExcelTableImportOperation(structure, filename.toOSString(), targetGeneration, this,
                nullRepresentationString, ignoreColumnHeaderRow, list);
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean isValidImportSource(String source) {
        File file = new File(source);
        
        if (!file.canRead()) {
            return false;
        }
        
        FileInputStream fis = null;
		try {
            fis = new FileInputStream(file);
			new HSSFWorkbook(fis);
            return true;
		} catch (Exception e) {
			// if an exception occured, it is not a valid source, this exception can be ignored
		}
        finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // this is a serious problem, so report it.
                    IpsPlugin.log(e);
                }
            }
        }
		return false;
	}

}
