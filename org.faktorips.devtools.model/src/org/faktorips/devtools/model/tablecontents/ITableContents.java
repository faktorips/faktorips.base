/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablecontents;

import java.util.List;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsMetaObject;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.XmlSaxSupport;
import org.faktorips.devtools.model.ipsobject.IDeprecation;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public interface ITableContents extends IIpsMetaObject, XmlSaxSupport {

    public static final String PROPERTY_TABLESTRUCTURE = "tableStructure"; //$NON-NLS-1$
    public static final String PROPERTY_NUMOFCOLUMNS = "numOfColumns"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TABLECONTENTS-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the structure this content is based on can't be
     * found.
     */
    public static final String MSGCODE_UNKNWON_STRUCTURE = MSGCODE_PREFIX + "UnknownStructure"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unique violation.
     */
    public static final String MSGCODE_UNIQUE_KEY_VIOLATION = MSGCODE_PREFIX + "UniqueKeyViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the from value is greater the to value.
     */
    // TODO joerg validate 'from' &le; 'to', and 'from'+'to' same datatype
    public static final String MSGCODE_TWO_COLUMN_RANGE_FROM_GREATER_TO_VALUE = MSGCODE_PREFIX
            + "TwoColumnRangeFromGreaterToValue"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are to many unique key violations.
     */
    public static final String MSGCODE_TOO_MANY_UNIQUE_KEY_VIOLATIONS = MSGCODE_PREFIX + "TooManyUniqueKeyViolations"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are to many contents for a single content
     * table structure.
     */
    public static final String MSGCODE_TOO_MANY_CONTENTS_FOR_SINGLETABLESTRUCTURE = MSGCODE_PREFIX
            + "TooManyContentsForSingleTableStructure"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the ordering of the referenced {@link IColumn
     * IColumns} as stored in this {@link ITableContents} does not match the ordering of the
     * {@link IColumn IColumns} as defined in the base {@link ITableStructure}.
     */
    public static final String MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_ORDERING_INVALID = MSGCODE_PREFIX
            + "TableContentsReferencedColumnOrderingInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the number of referenced {@link IPartReference
     * IPartReferences} does not correspond to the number of {@link IColumn IColumns} defined in the
     * referenced {@link ITableStructure}.
     */
    public static final String MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMNS_COUNT_INVALID = MSGCODE_PREFIX
            + "TableContentsReferencedColumnCountInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the names of the referenced {@link IColumn IColumns}
     * as stored in this {@link ITableContents} do not match the names of the {@link IColumn} s as
     * defined in the base {@link ITableStructure}.
     */
    public static final String MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_NAMES_INVALID = MSGCODE_PREFIX
            + "TableContentReferencedColumnNamesInvalid"; //$NON-NLS-1$

    /**
     * Validation message code that indicates that the table structure is deprecated. A
     * replacement/migration strategy should be documented in its
     * {@link IDeprecation#getDescriptions() deprecation descriptions}.
     */
    public static final String MSGCODE_DEPRECATED_TABLE_STRUCTURE = MSGCODE_PREFIX + "DeprecatedTableStructure"; //$NON-NLS-1$

    /**
     * Returns the qualified name of the table structure this table contents is based on.
     */
    public String getTableStructure();

    /**
     * Sets the qualified name of the table structure this table contents is based on.
     * 
     * @throws IllegalArgumentException if qName is <code>null</code>.
     */
    public void setTableStructure(String qName);

    /**
     * Searches the table structure this contents is based on and returns it. Returns
     * <code>null</code> if the structure can't be found.
     * 
     * @throws IpsException if an exception occurs while searching for the table structure.
     */
    public ITableStructure findTableStructure(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the number of columns in the table contents. Note, that it is possible that this
     * table contents object contains more (or less) columns than the table structure it is based
     * on. Of course this is an error in the product definition, but the model must handle it.
     */
    public int getNumOfColumns();

    /**
     * Creates a new column. A new cell is added to each row in this object with the given default
     * value.
     * 
     * @param defaultValue the default value to use for cells in the new column
     * @param name the name of the new column
     * @return the index of the new column
     */
    public int newColumn(String defaultValue, String name);

    /**
     * Creates a new column at the given index (zero based), remaining columns are shifted to the
     * right.
     * 
     * @param index the index to insert the new column. Values less than zero lead to a new column
     *            at index zero. A value greater or equal to the index of the last existing column
     *            lead to a new column inserted after the last existing one
     * @param defaultValue The default value to use for cells in the new column
     * @param name the name of the new column
     */
    public void newColumnAt(int index, String defaultValue, String name);

    /**
     * Deletes the column by removing the cell in each row and the corresponding table column
     * reference.
     * 
     * @param columnIndex the column's index
     * 
     * @throws IllegalArgumentException if this object does not contain a column with the indicated
     *             index
     */
    public void deleteColumn(int columnIndex);

    /**
     * Migrates the missing columnReferences in old {@link ITableContents}
     */
    public void migrateColumnReferences();

    /**
     * Creates a new {@link ITableRows}
     */
    public ITableRows newTableRows();

    /**
     * Returns the table content.
     * <p>
     * Reads the whole Content only on the first call of this method.
     */
    public ITableRows getTableRows();

    /**
     * Returns a list containing all {@link IPartReference IPartReferences} that belong to this
     * {@link ITableContents}.
     * <p>
     * Returns an empty list if there are none, never returns {@code null}.
     */
    public List<IPartReference> getColumnReferences();

    /**
     * Returns the number of {@link IColumn IColumns} that are currently referenced by this
     * {@link ITableContents}.
     */
    public int getColumnReferencesCount();

    /**
     * Adds a new ColumnReference to {@link ITableContents}. Only used for initializing
     * {@link ITableContents} with SAXHandler
     */
    public void createColumnReferenceSaxHandler(String referenceID);

    /**
     * Returns {@code true} if this {@link ITableContents} is inconsistent with the model and needs
     * to be fixed by the user, {@code false} otherwise.
     */
    public boolean isFixToModelRequired();

    /**
     * Sorts the table column references in the right order corresponding to the
     * {@link ITableStructure}.
     * <p>
     * Note: This is a workaround since there is no method to notice the {@code ContentPage} that
     * just the order of the references changed without adding or deleting any columns.
     */
    public void fixColumnReferences();

}
