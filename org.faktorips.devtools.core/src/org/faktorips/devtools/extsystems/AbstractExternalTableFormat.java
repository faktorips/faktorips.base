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

package org.faktorips.devtools.extsystems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IPath;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

/**
 * @author Thorsten Guenther
 */
public abstract class AbstractExternalTableFormat {

	/**
	 * The human readable name of this external table format.
	 */
	private String name;

	/**
	 * The extension of the files this external table format usually uses. Can
	 * be the empty string, but not <code>null</code>.
	 */
	private String defaultExtension;

	/**
	 * Converter to be used if no other matches
	 */
	private final IValueConverter defaultValueConverter = new DefaultValueConverter();

	/**
	 * List of all converters this external table format is configured with.
	 */
	private List converter = new ArrayList();

    /**
     * Returns the given Double as string. If the Double has no decimal places
     * then the Double will be returned as intg string. 
     * Example: "1.0" will be returned as "1" but "1.2" will be returned as "1.2"
     */
    public static String doubleToStringWithoutDecimalPlaces(Double externalDataValue){
        // Workaround: if the external data is double without decimal places (n.0),
        // e.g. if the external data represents the numeric id of an enum value set,
        // then it is important to import the value as int value ("1.0" will be "1"),
        // otherwise the id couldn't be mapped correctly to the enum identifier.
        // The extenal data interprets a "1" always as "1.0", 
        // even if the column or cell is formatted as text
        int intValue = externalDataValue.intValue();
        if (Double.valueOf(""+intValue).equals(externalDataValue)){ //$NON-NLS-1$
            return "" + intValue; //$NON-NLS-1$
        }
        return externalDataValue.toString();
    }

	/**
	 * @return The human readable name of this external table format.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the (human readable) name of this external table format. This name
	 * might be used to identify this in the ui.
	 * 
	 * @param name
	 *            The name to use.
	 */
	public void setName(String name) {
		this.name = name;

		if (this.name == null) {
			this.name = ""; //$NON-NLS-1$
		}
	}

	/**
	 * Set the default extension to use if a proposal for the name of the file
	 * to export ist generated. If the file is, for example, an excel-file, the
	 * default-extension is ".xls" (note the included dot as first char).
	 * 
	 * @param extension
	 *            The new default-extension.
	 */
	public void setDefaultExtension(String extension) {
		this.defaultExtension = extension;

		if (this.defaultExtension == null) {
			defaultExtension = ""; //$NON-NLS-1$
		}
	}

	/**
	 * @return Returns the default extension used for the proposal of a filename
	 *         as export-target.
	 */
	public String getDefaultExtension() {
		return defaultExtension;
	}

	/**
	 * Add a converter to tranform external values to internal values (and vice
	 * versa).
	 * 
	 * @param converter
	 *            The additional converter.
	 */
	public void addValueConverter(IValueConverter converter) {
		this.converter.add(converter);
	}

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
	public String getIpsValue(Object externalValue, Datatype datatype, MessageList messageList) {
		return getConverter(datatype).getIpsValue(externalValue, messageList);
	}

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
	public Object getExternalValue(String ipsValue, Datatype datatype, MessageList messageList) {
		return getConverter(datatype).getExternalDataValue(ipsValue, messageList);
	}

	private IValueConverter getConverter(Datatype datatype) {
		for (Iterator iter = converter.iterator(); iter.hasNext();) {
			IValueConverter valueConverter = (IValueConverter) iter.next();

			if (valueConverter.getSupportedDatatype().equals(datatype)) {
				return valueConverter;
			}
		}
		return defaultValueConverter;
	}

	/**
	 * @param contents
	 *            The contents of the table to export.
	 * @param filename
	 *            The name of the file to export to. The file can exist allready
	 *            and might or might not be overwritten, the choice is up to the
	 *            runnable.
	 * @param nullRepresentationString
	 *            The string to use to replace <code>null</code>. This value
	 *            can be used for systems with no own <code>null</code>-representation
	 *            (MS-Excel, for example).
	 * @param list
	 *            A list for messages describing any problems occured during the
	 *            export. If no messages of severity ERROR are contained in this
	 *            list, the export is considered successfull.
	 *            
	 * @return Returns the runnable to use to export a table.
	 */
	public abstract IWorkspaceRunnable getExportTableOperation(
			ITableContents contents, IPath filename,
			String nullRepresentationString, MessageList list);

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
	 *            A list for messages describing any problems occured during the
	 *            import. If no messages of severity ERROR are contained in this
	 *            list, the import is considered successfull.
	 *            
	 * @return The runnable to use to import a table.
	 */
	public abstract IWorkspaceRunnable getImportTableOperation(
			ITableStructure structure, IPath filename,
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
	 * Implementation of IValueConverter to be used if no other converter was
	 * found.
	 * 
	 * @author Thorsten Guenther
	 */
	private class DefaultValueConverter implements IValueConverter {

		/**
		 * {@inheritDoc}
		 */
		public Class getSupportedClass() {
			return Object.class;
		}

		/**
		 * {@inheritDoc}
		 */
		public Datatype getSupportedDatatype() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getIpsValue(Object externalDataValue, MessageList messageList) {
            if (externalDataValue instanceof Double){
                return doubleToStringWithoutDecimalPlaces((Double)externalDataValue);
            }
			return externalDataValue.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getExternalDataValue(String ipsValue, MessageList messageList) {
			return ipsValue;
		}

	}
}
