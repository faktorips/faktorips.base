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

package org.faktorips.devtools.tableconversion;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValueContainer;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

public interface ITableFormat {

    /**
     * Returns the human readable name of this external table format.
     */
    public String getName();

    /**
     * Set the (human readable) name of this external table format. This name might be used to
     * identify this in the UI.
     * 
     * @param name The name to use.
     */
    public void setName(String name);

    /**
     * Set the default extension to use if a proposal for the name of the file to export ist
     * generated. If the file is, for example, an excel-file, the default-extension is ".XLS" (note
     * the included dot as first char).
     * 
     * @param extension The new default-extension.
     */
    public void setDefaultExtension(String extension);

    /**
     * Returns the default extension used for the proposal of a filename as export-target.
     */
    public String getDefaultExtension();

    /**
     * Adds a converter to transform external values to internal values (and vice versa). This
     * method also sets the table format in the given converter via its
     * {@link IValueConverter#setTableFormat(ITableFormat)} method.
     * 
     * @param converter The additional converter.
     */
    public void addValueConverter(IValueConverter converter);

    /**
     * Returns a string representing the given external value which can be parsed by the given data
     * type.
     * 
     * @param externalValue The external representation of the value.
     * @param datatype The data type for the given external value.
     * @param messageList A list for messages to add if anything happens that should be reported to
     *            the user. If this list does not contains an error-message before you call this
     *            method and do contain an error-message after the call, the conversion failed.
     */
    public String getIpsValue(Object externalValue, Datatype datatype, MessageList messageList);

    /**
     * Returns the external representation for the given string respecting the given data type.
     * 
     * @param ipsValue The string-representation of a value.
     * @param datatype The data type the given string is a value for.
     * @param messageList A list for messages to add if anything happens that should be reported to
     *            the user. If this list does not contains an error-message before you call this
     *            method and do contain an error-message after the call, the conversion failed.
     */
    public Object getExternalValue(String ipsValue, Datatype datatype, MessageList messageList);

    /**
     * Returns <code>true</code> if the export operation was successful, <code>false</code>
     * otherwise.
     * 
     * @param contents The contents of the table to export.
     * @param filename The name of the file to export to. The file can exist already and might or
     *            might not be overwritten, the choice is up to the runnable.
     * @param nullRepresentationString The string to use to replace <code>null</code>. This value
     *            can be used for systems with no own <code>null</code>-representation (MS-Excel,
     *            for example).
     * @param exportColumnHeaderRow <code>true</code> if the header names should be exported or
     *            <code>false</code> if only the content should be exported without the header names
     * @param list A list for messages describing any problems occurred during the export. If no
     *            messages of severity ERROR are contained in this list, the export is considered
     *            successful.
     */
    public boolean executeTableExport(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list);

    /**
     * @param structure The structure for the imported table
     * @param filename The name of the file to import from.
     * @param targetGeneration The generation to insert the data into.
     * @param nullRepresentationString The string to use to replace <code>null</code>. This value
     *            can be used for systems with no own <code>null</code>-representation (MS-Excel,
     *            for example).
     * @param ignoreColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be imported content contains no
     *            column header row.
     * @param list A list for messages describing any problems occurred during the import. If no
     *            messages of severity ERROR are contained in this list, the import is considered
     *            successful.
     * @param importIntoExisting <tt>true</tt> if the import method chosen was replace or append
     *            (the file won't be saved in this case after the import).
     * 
     * @throws CoreException If the file could not be imported. This can happen for instance if the
     *             file referenced by <code>filename</code> is an Excel file and one tries to import
     *             it using an <code>CSVTableFormat</code> instead of <code>ExcelTableformat</code>.
     * 
     */
    public void executeTableImport(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) throws CoreException;

    /**
     * The file to import can either contain attributes of an enumeration (therefore defining a
     * structure) or enumeration values. In case of enumeration values the decision where to store
     * them is based on {@link IEnumType#isContainingValues()}.
     * 
     * @param valueContainer The destination of the import.
     * @param filename The name of the file to import from.
     * @param nullRepresentationString The string to use to replace <code>null</code>. This value
     *            can be used for systems with no own <code>null</code>-representation (MS-Excel,
     *            for example).
     * @param ignoreColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be imported content contains no
     *            column header row.
     * @param list A list for messages describing any problems occurred during the import. If no
     *            messages of severity ERROR are contained in this list, the import is considered
     *            successful.
     * @param importIntoExisting <tt>true</tt> if the import method chosen was replace or append
     *            (the file won't be saved in this case after the import).
     * 
     * @throws CoreException If the file could not be imported. This can happen for instance if the
     *             file referenced by <code>filename</code> is an Excel file and one tries to import
     *             it using an <code>CSVTableFormat</code> instead of <code>ExcelTableformat</code>.
     * 
     * @since 2.3
     * 
     * @see IEnumValueContainer
     * @see IEnumType
     * @see IEnumContent
     */
    public void executeEnumImport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list,
            boolean importIntoExisting) throws CoreException;

    /**
     * The file to export can either contain attributes of an enumeration (therefore defining a
     * structure) or enumeration values. In case of enumeration values the decision where to store
     * them is based on {@link IEnumType#isContainingValues()}.
     * 
     * @param valueContainer The destination of the export.
     * @param filename The name of the file to export from.
     * @param nullRepresentationString The string to use to replace <code>null</code>. This value
     *            can be used for systems with no own <code>null</code>-representation (MS-Excel,
     *            for example).
     * @param exportColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be exported content contains no
     *            column header row.
     * @param list A list for messages describing any problems occurred during the export. If no
     *            messages of severity ERROR are contained in this list, the export is considered
     *            successful.
     * 
     * @since 2.3
     * 
     * @see IEnumValueContainer
     * @see IEnumType
     * @see IEnumContent
     */
    public boolean executeEnumExport(IEnumValueContainer valueContainer,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list);

    /**
     * Returns <code>true</code> if the given resource is a valid source for import,
     * <code>false</code> otherwise.
     * 
     * @param source The identification of the resource to check (for example, a qualified
     *            filename).
     */
    public boolean isValidImportSource(String source);

    /**
     * Retrieves a table format specific property using the given property name. Returns the
     * property value if defined, or <tt>null</tt> if the property could not be found.
     * 
     * @param propertyName The name of the property to be retrieved.
     * 
     * @throws NullPointerException if the given <tt>propertyName</tt> is <tt>null</tt>.
     */
    public String getProperty(String propertyName);

    /**
     * Changes a property or defines a new property for this table format.
     * 
     * @param propertyName The name of the property to be set/changed.
     * @param propertyValue The value to set for the given <code>propertyName</code>.
     */
    public void setProperty(String propertyName, String propertyValue);

    /**
     * Computes a preview for the table to be imported.
     * 
     * @param structure The table structure to use for the preview.
     * @param filename The filename of the file to be previewed.
     * @param maxNumberOfRows Limit the number of returned rows to maxNumberOfRows.
     * @param ignoreColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be imported content contains no
     *            column header row.
     * @param nullRepresentationString The string to use to replace <code>null</code>.
     * 
     * @return A <code>List</code> containing a <code>String[]</code> for each row, or
     *         <tt>Collections.EMPTY_LIST</tt> if the preview could not be computed or the file
     *         contains no entries.
     */
    public List<String[]> getImportTablePreview(ITableStructure structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentationString);

    /**
     * Computes a preview for the enumeration to be imported and returns <code>List</code>
     * containing a <code>String[]</code> for each row, or <tt>Collections.EMPTY_LIST</tt> if the
     * preview could not be computed or the file contains no entries.
     * 
     * @param structure The enumeration type to use (derive data types) for the preview.
     * @param filename The filename of the file to be previewed.
     * @param maxNumberOfRows Limit the number of returned rows to maxNumberOfRows.
     * @param ignoreColumnHeaderRow <code>true</code> if the first row contains column header and
     *            should be ignored <code>false</code> if the to be imported content contains no
     *            column header row.
     * @param nullRepresentationString The string to use to replace <code>null</code>.
     */
    public List<String[]> getImportEnumPreview(IEnumType structure,
            IPath filename,
            int maxNumberOfRows,
            boolean ignoreColumnHeaderRow,
            String nullRepresentationString);

}
