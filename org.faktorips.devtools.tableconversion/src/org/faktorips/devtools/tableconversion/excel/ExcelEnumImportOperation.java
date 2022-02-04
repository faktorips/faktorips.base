/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.enums.IEnumValueContainer;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.Messages;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.model.value.ValueType;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.values.LocalizedString;

/**
 * Operation to import IPS enum types or contents from an Excel file.
 * 
 * @author Roman Grutza, Alexander Weickmann
 */
public class ExcelEnumImportOperation extends AbstractExcelImportOperation {

    private final IEnumValueContainer valueContainer;

    public ExcelEnumImportOperation(IEnumValueContainer valueContainer, String filename, ExcelTableFormat format,
            String nullRepresentationString, boolean ignoreColumnHeaderRow, MessageList messageList,
            boolean importIntoExisting) {

        super(filename, format, nullRepresentationString, ignoreColumnHeaderRow, messageList, importIntoExisting);
        this.valueContainer = valueContainer;
        initDatatypes();
    }

    @Override
    protected void initDatatypes() {
        try {
            IEnumType enumType = valueContainer.findEnumType(valueContainer.getIpsProject());
            boolean includeLiteralName = valueContainer instanceof IEnumType;
            List<IEnumAttribute> enumAttributes = enumType.getEnumAttributesIncludeSupertypeCopies(includeLiteralName);
            datatypes = new Datatype[enumAttributes.size()];
            for (int i = 0; i < datatypes.length; i++) {
                IEnumAttribute enumAttribute = enumAttributes.get(i);
                ValueDatatype datatype = enumAttribute.findDatatype(enumAttribute.getIpsProject());
                datatypes[i] = datatype;
            }
        } catch (CoreRuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreRuntimeException {
        IProgressMonitor progressMonitor;
        if (monitor == null) {
            progressMonitor = new NullProgressMonitor();
        } else {
            progressMonitor = monitor;
        }
        try {
            initWorkbookAndSheet();
            progressMonitor.beginTask("Import file " + sourceFile, 2 + getNumberOfExcelRows(getSheet())); //$NON-NLS-1$

            // Update datatypes because the structure might be altered if this operation is reused.
            initDatatypes();
            progressMonitor.worked(1);
            fillEnum(valueContainer, getSheet(), progressMonitor);

            if (progressMonitor.isCanceled()) {
                valueContainer.getIpsObject().getIpsSrcFile().discardChanges();
            }
            progressMonitor.worked(1);
            progressMonitor.done();
        } catch (IOException e) {
            throw new CoreRuntimeException(new IpsStatus(
                    NLS.bind(Messages.AbstractXlsTableImportOperation_errRead, sourceFile), e));
        }
    }

    private int getNumberOfExcelRows(Sheet sheet) {
        int numberRows = 0;
        // Row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0 contains data.
        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        for (int i = startRow;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                // No more rows.
                break;
            }
            numberRows++;
        }
        return numberRows;
    }

    private void fillEnum(IEnumValueContainer valueContainer, Sheet sheet, IProgressMonitor monitor)
            throws CoreRuntimeException {

        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        IEnumType enumType = valueContainer.findEnumType(valueContainer.getIpsProject());
        int expectedFields = enumType.getEnumAttributesCountIncludeSupertypeCopies(enumType.isInextensibleEnum());
        for (int i = startRow;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (sheetRow == null) {
                // No more rows, we are finished whit this sheet.
                break;
            }
            IEnumValue enumValue = valueContainer.newEnumValue();
            for (int j = 0; j < expectedFields; j++) {
                Cell cell = sheetRow.getCell(j);
                IEnumAttributeValue enumAttributeValue = enumValue.getEnumAttributeValues().get(j);
                if (cell == null) {
                    Object[] objects = new Object[3];
                    objects[0] = Integer.valueOf(i);
                    objects[1] = Integer.valueOf(j);
                    objects[2] = IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
                    String msg = NLS.bind("In row {0}, column {1} no value is set - imported {2} instead.", objects); //$NON-NLS-1$
                    messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                    setValueAttribute(enumAttributeValue, null);
                } else {
                    setValueAttribute(enumAttributeValue, readCell(cell, datatypes[j]));
                }
            }

            if (monitor.isCanceled()) {
                return;
            }
            monitor.worked(1);
        }
    }

    private void setValueAttribute(IEnumAttributeValue enumAttribute, String value) {
        if (enumAttribute.getValueType().equals(ValueType.STRING)) {
            enumAttribute.setValue(ValueFactory.createStringValue(value));
        } else if (enumAttribute.getValueType().equals(ValueType.INTERNATIONAL_STRING)) {
            IValue<?> internationalStringValue = enumAttribute.getValue();
            IInternationalString content = (IInternationalString)internationalStringValue.getContent();
            content.add(new LocalizedString(getDefaultLanguage(enumAttribute.getIpsProject()), value));
        }
    }

    private Locale getDefaultLanguage(IIpsProject ipsProject) {
        return ipsProject.getReadOnlyProperties().getDefaultLanguage().getLocale();
    }
}
