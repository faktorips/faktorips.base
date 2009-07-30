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

package org.faktorips.devtools.tableconversion.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.tableconversion.AbstractExternalTableFormat;
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
    public boolean executeTableExport(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {
        try {
            ExcelTableExportOperation excelTableExportOperation = new ExcelTableExportOperation(contents, filename
                    .toOSString(), this, nullRepresentationString, exportColumnHeaderRow, list);
            excelTableExportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {

        try {
            ExcelTableImportOperation excelTableImportOperation = new ExcelTableImportOperation(structure, filename
                    .toOSString(), targetGeneration, this, nullRepresentationString, ignoreColumnHeaderRow, list,
                    importIntoExisting);
            excelTableImportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list) {

        try {
            ExcelEnumExportOperation enumExportOperation = new ExcelEnumExportOperation(valueContainer, filename
                    .toOSString(), this, nullRepresentationString, exportColumnHeaderRow, list);
            enumExportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) {

        try {
            ExcelEnumImportOperation enumImportOperation = new ExcelEnumImportOperation(valueContainer, filename
                    .toOSString(), this, nullRepresentationString, ignoreColumnHeaderRow, list, importIntoExisting);
            enumImportOperation.run(new NullProgressMonitor());
            return true;
        } catch (Exception e) {
            IpsPlugin.log(e);
            return false;
        }
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
            // if an exception occurred, it is not a valid source, this exception can be ignored
        } finally {
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

    public List getImportTablePreview(ITableStructure structure, IPath filename, int maxNumberOfRows) {
        // TODO rg: implement preview
        return Collections.EMPTY_LIST;
    }

    public List getImportEnumPreview(IEnumType structure, IPath filename, int maxNumberOfRows) {
        // TODO rg: implement preview
        return Collections.EMPTY_LIST;
    }
}
