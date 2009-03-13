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

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

public interface ITableFormat {

    /**
     * @return The human readable name of this external table format.
     */
    public abstract String getName();

    /**
     * Set the (human readable) name of this external table format. This name
     * might be used to identify this in the ui.
     * 
     * @param name
     *            The name to use.
     */
    public abstract void setName(String name);

    /**
     * Set the default extension to use if a proposal for the name of the file
     * to export ist generated. If the file is, for example, an excel-file, the
     * default-extension is ".xls" (note the included dot as first char).
     * 
     * @param extension
     *            The new default-extension.
     */
    public abstract void setDefaultExtension(String extension);

    /**
     * @return Returns the default extension used for the proposal of a filename
     *         as export-target.
     */
    public abstract String getDefaultExtension();

    /**
     * Add a converter to tranform external values to internal values (and vice
     * versa).
     * 
     * @param converter
     *            The additional converter.
     */
    public abstract void addValueConverter(IValueConverter converter);

    /**
     * @param externalValue
     *            The external representation of the value.
     * @param datatype
     *            The datatype for the given external value.
     * @param messageList 
     *            A list for messages to add if anything happens that should be 
     *            reported to the user. If this list does not contains an error-message
     *            before you call this method and do contain an error-message after the
     *            call, the conversion failed.
     *            
     * @return A string representing the given external value which can be
     *         parsed by the given datatype.
     */
    public abstract String getIpsValue(Object externalValue, Datatype datatype, MessageList messageList);

    /**
     * @param ipsValue
     *            The string-representation of a value.
     * @param datatype
     *            The datatype the given string is a value for.
     * @param messageList 
     *            A list for messages to add if anything happens that should be 
     *            reported to the user. If this list does not contains an error-message
     *            before you call this method and do contain an error-message after the
     *            call, the conversion failed.
     *            
     * @return Returns the external representation for the given string
     *         respecting the given datatype.
     */
    public abstract Object getExternalValue(String ipsValue, Datatype datatype, MessageList messageList);

    /**
     * @param contents
     *            The contents of the table to export.
     * @param filename
     *            The name of the file to export to. The file can exist already
     *            and might or might not be overwritten, the choice is up to the
     *            runnable.
     * @param nullRepresentationString
     *            The string to use to replace <code>null</code>. This value
     *            can be used for systems with no own <code>null</code>-representation
     *            (MS-Excel, for example).
     * @param exportColumnHeaderRow 
     *            <code>true</code> if the header names should be exported or <code>false</code> if only
     *            the content should be exported without the header names
     * @param list
     *            A list for messages describing any problems occurred during the
     *            export. If no messages of severity ERROR are contained in this
     *            list, the export is considered successful.
     *            
     * @return Returns the runnable to use to export a table.
     */
    public abstract IWorkspaceRunnable getExportTableOperation(ITableContents contents,
            IPath filename,
            String nullRepresentationString,
            boolean exportColumnHeaderRow,
            MessageList list);

    /**
     * @param structure
     *            The structure for the imported table
     * @param filename
     *            The name of the file to import from. 
     * @param targetGeneration
     *            The generation to insert the data into.
     * @param nullRepresentationString
     *            The string to use to replace <code>null</code>. This value
     *            can be used for systems with no own <code>null</code>-representation
     *            (MS-Excel, for example).
     * @param ignoreColumnHeaderRow
     *            <code>true</code> if the first row contains column header and should be ignored
     *            <code>false</code> if the to be imported content contains no column header row.
     * @param list
     *            A list for messages describing any problems occurred during the
     *            import. If no messages of severity ERROR are contained in this
     *            list, the import is considered successful.
     *            
     * @return The runnable to use to import a table.
     */
    public abstract IWorkspaceRunnable getImportTableOperation(ITableStructure structure,
            IPath filename,
            ITableContentsGeneration targetGeneration,
            String nullRepresentationString,
            boolean ignoreColumnHeaderRow,
            MessageList list);

    /**
     * @param source The identification of the resource to check (for example, a qualified filename).
     * @return <code>true</code> if the given resource is a valid source for import, <code>false</code> otherwise.
     */
    public abstract boolean isValidImportSource(String source);

    /**
     * Retrieve a table format specific property using the given property name.
     * 
     * @param propertyName The name of the property to be retrieved.
     * @return The property value if defined, or null if the property could not be found.
     * @throws NullPointerException if the given propertyName is null.
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
     * Creates the Composite registered for this table format to configure table format specific parameters.
     * The Composite will most likely be embedded as part of Dialogs, WizardPages and the like.
     * 
     * @param parent the parent of the Composite to create.
     * @return Composite with widgets to configure the table formats parameters.
     */
    // TODO: this introduces a dependency of core, which has nothing to do with UI, to SWT.
    //  Maybe it would be a better idea to move this method to the TableFormatPlugin class.
    public Composite createTableFormatConfigurationControl(Composite parent);
    
    /**
     * Computes a preview for the table to be imported.
     * 
     * @param structure The table structure to use for the preview.
     * @param filename The filename of the file to be previewed.
     * @param maxNumberOfRows Limit the number of returned rows to maxNumberOfRows.
     * @return A <code>List</code> containing a <code>String[]</code> for each row, 
     *   or Collections.EMPTY_LIST if the preview could not be computed or the file contains no entries.
     */
    public abstract List getImportTablePreview(ITableStructure structure, IPath filename, int maxNumberOfRows);

}
