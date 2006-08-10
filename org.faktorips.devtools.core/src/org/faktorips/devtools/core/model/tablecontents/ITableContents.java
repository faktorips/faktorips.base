/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ITimedIpsObject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;


/**
 *
 */
public interface ITableContents extends ITimedIpsObject {

    public final static String PROPERTY_TABLESTRUCTURE = "tableStructure"; //$NON-NLS-1$
    
    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TABLECONTENTS-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the structure this content is based on can't be found.
     */
    public final static String MSGCODE_UNKNWON_STRUCTURE = MSGCODE_PREFIX + "UnknownStructure"; //$NON-NLS-1$

    /**
     * Returns the qualified name of the table structure this table contents
     * is based on.
     */
    public String getTableStructure();
    
    /**
     * Sets the qualified name of the table structure this table contents
     * is based on.
     * 
     * @throws IllegalArgumentException if qName is <code>null</code>.
     */
    public void setTableStructure(String qName);
    
    /**
     * Searches the table structure this contents is based on and returns
     * it. Returns <code>null</code> if the structure can't be found.
     * 
     * @throws CoreException if an exception occurs while searching for
     * the table structure.
     */
    public ITableStructure findTableStructure() throws CoreException;
    
    /**
     * Returns the number of colums in the table contents. Note, that it is possible
     * that this table contents object contains more (or less) columns than the
     * table structure it is based on. Of course this is an error in the
     * product definition, but the model must handle it.  
     */
    public int getNumOfColumns();

    /**
     * Creates a new column. A new cell is added to each row in this object
     * with the given default value. 
     * 
     * @return the index of the new column.
     */
    public int newColumn(String defaultValue);
    
    /**
     * Deletes the column by removing the cell in each row.
     * 
     * @param columnIndex The column's index.
     * 
     * @throws IllegalArgumentException if this object does not contain a column
     * with the indicated index.
     */
    public void deleteColumn(int columnIndex);
}
