/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaObject;
import org.faktorips.devtools.core.model.XmlSaxSupport;
import org.faktorips.devtools.core.model.ipsobject.ITimedIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;

public interface ITableContents extends IIpsMetaObject, ITimedIpsObject, XmlSaxSupport {

    public final static String PROPERTY_TABLESTRUCTURE = "tableStructure"; //$NON-NLS-1$
    public final static String PROPERTY_NUMOFCOLUMNS = "numOfColumns"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TABLECONTENTS-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the structure this content is based on can't be
     * found.
     */
    public final static String MSGCODE_UNKNWON_STRUCTURE = MSGCODE_PREFIX + "UnknownStructure"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the structure has a different number of columns than
     * this content.
     */
    public final static String MSGCODE_COLUMNCOUNT_MISMATCH = MSGCODE_PREFIX + "ColumncountMismatch"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unique violation.
     */
    public final static String MSGCODE_UNIQUE_KEY_VIOLATION = MSGCODE_PREFIX + "UniqueKeyViolation"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the from value is greater the to value. TODO joerg
     * validate 'from' <= 'to', and 'from'+'to' same datatype
     */
    public final static String MSGCODE_TWO_COLUMN_RANGE_FROM_GREATER_TO_VALUE = MSGCODE_PREFIX
            + "TwoColumnRangeFromGreaterToValue"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there are to many unique key violations.
     */
    public final static String MSGCODE_TO_MANY_UNIQUE_KEY_VIOLATIONS = MSGCODE_PREFIX + "ToManyUniqueKeyViolations"; //$NON-NLS-1$

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
     * @param ipsProject TODO
     * 
     * @throws CoreException if an exception occurs while searching for the table structure.
     */
    public ITableStructure findTableStructure(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the number of colums in the table contents. Note, that it is possible that this table
     * contents object contains more (or less) columns than the table structure it is based on. Of
     * course this is an error in the product definition, but the model must handle it.
     */
    public int getNumOfColumns();

    /**
     * Creates a new column. A new cell is added to each row in this object with the given default
     * value.
     * 
     * @return the index of the new column.
     */
    public int newColumn(String defaultValue);

    /**
     * Creates a new column at the given index (zero based), remaining columns are shifted to the
     * right.
     * 
     * @param index The index to insert the new column. Values less than zero lead to a new column
     *            at index zero. A value greater or equal to the index of the last existing column
     *            lead to a new column inserted after the last existing one.
     * @param defaultValue The new value to use for the new cells.
     */
    public void newColumnAt(int index, String defaultValue);

    /**
     * Deletes the column by removing the cell in each row.
     * 
     * @param columnIndex The column's index.
     * 
     * @throws IllegalArgumentException if this object does not contain a column with the indicated
     *             index.
     */
    public void deleteColumn(int columnIndex);

}
