/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.tableconversion.excel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.InternationalString;
import org.faktorips.devtools.core.internal.model.LocalizedString;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.Messages;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

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
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {
        // TODO AW: ProgressMonitor is not shown to the user somehow.
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        try {
            initWorkbookAndSheet();
            monitor.beginTask("Import file " + sourceFile, 2 + getNumberOfExcelRows(sheet)); //$NON-NLS-1$

            // Update datatypes because the structure might be altered if this operation is reused.
            initDatatypes();
            monitor.worked(1);
            fillEnum(valueContainer, sheet, monitor);

            if (monitor.isCanceled()) {
                valueContainer.getIpsObject().getIpsSrcFile().discardChanges();
            }
            monitor.worked(1);
            monitor.done();
        } catch (IOException e) {
            throw new CoreException(new IpsStatus(
                    NLS.bind(Messages.AbstractXlsTableImportOperation_errRead, sourceFile), e));
        }
    }

    private int getNumberOfExcelRows(Sheet sheet) {
        int numberRows = 0;
        // Row 0 is the header if ignoreColumnHeaderRow is true, otherwise row 0 contains data.
        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        for (int i = startRow;; i++) {
            Row sheetRow = sheet.getRow(i);
            if (sheetRow == null) { // No more rows.
                break;
            }
            numberRows++;
        }
        return numberRows;
    }

    private void fillEnum(IEnumValueContainer valueContainer, Sheet sheet, IProgressMonitor monitor)
            throws CoreException {

        int startRow = ignoreColumnHeaderRow ? 1 : 0;
        IEnumType enumType = valueContainer.findEnumType(valueContainer.getIpsProject());
        int expectedFields = enumType.getEnumAttributesCountIncludeSupertypeCopies(enumType
                .isCapableOfContainingValues());
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
                    objects[0] = new Integer(i);
                    objects[1] = new Integer(j);
                    objects[2] = nullRepresentationString;
                    String msg = NLS.bind("In row {0}, column {1} no value is set - imported {2} instead.", objects); //$NON-NLS-1$
                    messageList.add(new Message("", msg, Message.WARNING)); //$NON-NLS-1$
                    setValueAttribute(enumAttributeValue, nullRepresentationString);
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
            IValue<?> InternationalStringValue = enumAttribute.getValue();
            InternationalString content = (InternationalString)InternationalStringValue.getContent();
            content.add(new LocalizedString(getDefaultLanguage(enumAttribute.getIpsProject()), value));
        }
    }

    private Locale getDefaultLanguage(IIpsProject ipsProject) {
        return ipsProject.getProperties().getDefaultLanguage().getLocale();
    }
}
