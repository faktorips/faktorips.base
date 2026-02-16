/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.tableconversion;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.NamedDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.runtime.MessageList;

/**
 * Abstract operation to import <code>ITableContents</code> in an arbitrary format.
 * <p>
 * Implementors of a new table format should subclass this class and use the
 * <code>externalTableFormat</code> extension point to register the new table format.
 *
 * @author Roman Grutza
 */
public abstract class AbstractTableImportOperation implements ICoreRunnable {

    /**
     * The qualified name of the file to import from.
     */
    protected String sourceFile;

    /**
     * Data types for the columns. The data type at index 1 is the data type defined in the
     * structure for column at index 1.
     */
    protected Datatype[] datatypes;

    /**
     * The format which handles data conversion
     */
    protected ITableFormat format;

    /**
     * String representing <code>null</code>.
     */
    protected String nullRepresentationString;

    /**
     * List of messages describing problems occurred during import.
     */
    protected MessageList messageList;

    protected boolean importIntoExisting;

    /**
     * <code>true</code> if the first row contains column header and should be ignored
     * <code>false</code> if the to be imported content contains no column header row.
     */
    protected boolean ignoreColumnHeaderRow;

    public AbstractTableImportOperation(String sourceFile, ITableFormat format, String nullRepresentationString,
            boolean ignoreColumnHeaderRow, MessageList list, boolean importIntoExisting) {

        this.sourceFile = sourceFile;
        this.format = format;
        this.nullRepresentationString = nullRepresentationString;
        this.ignoreColumnHeaderRow = ignoreColumnHeaderRow;
        messageList = list;
        this.importIntoExisting = importIntoExisting;
    }

    @Override
    public abstract void run(IProgressMonitor monitor) throws IpsException;

    /**
     * Checks if the enum import with Name (ID) format option is enabled for the given datatype.
     *
     * @param datatype The datatype to check
     * @return true if the datatype is a NamedDatatype supporting names and the import option is
     *             enabled
     */
    protected boolean shouldParseEnumNameAndId(Datatype datatype) {
        String property = format.getProperty(AbstractExternalTableFormat.PROPERTY_ENUM_EXPORT_AS_NAME_AND_ID);
        return "true".equals(property) //$NON-NLS-1$
                && datatype instanceof NamedDatatype
                && ((NamedDatatype)datatype).isSupportingNames();
    }

    /**
     * Checks if a value matches the Name (ID) format pattern and if it does, validates that it is
     * actually a valid enum name and ID combination, rather than just an ID that happens to contain
     * parentheses.
     *
     * @param value The value to check
     * @param datatype The datatype to validate against
     * @return true if the value matches the Name (ID) format and the extracted ID is valid
     */
    protected boolean isNameAndIdFormat(String value, Datatype datatype) {
        if (value == null || !value.matches(".*\\(.*\\)$")) { //$NON-NLS-1$
            return false;
        }

        if (!(datatype instanceof NamedDatatype namedDatatype) || !namedDatatype.isSupportingNames()
                || !(datatype instanceof ValueDatatype valueDatatype)) {
            return false;
        }

        String extractedId = extractIdFromNameAndIdFormat(value);
        Object parsedValue = valueDatatype.getValue(extractedId);

        return parsedValue != null;
    }

    /**
     * Extracts the ID from a value in Name (ID) format.
     * <p>
     * This method extracts the content from the outermost parentheses at the end of the value by
     * properly handling nested parentheses. For example:
     * <ul>
     * <li>"Payment Mode (monthly)" → "monthly"</li>
     * <li>"foo foo (foo (bar))" → "foo (bar)"</li>
     * </ul>
     *
     * @param value The value in format "Name (ID)"
     * @return The extracted ID, or the original value if the pattern doesn't match
     */
    protected String extractIdFromNameAndIdFormat(String value) {
        if (value == null || !value.endsWith(")")) {
            return value;
        }
        int depth = 0;
        for (int i = value.length() - 1; i >= 0; i--) {
            char c = value.charAt(i);
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                depth--;
                if (depth == 0) {
                    return value.substring(i + 1, value.length() - 1);
                }
            }
        }
        return value;
    }

}
