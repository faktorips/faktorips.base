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

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;

/**
 *
 */
public interface ITableContentsGeneration extends IIpsObjectGeneration {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TABLECONTENTSGENERATION-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists more than one enumeration value with the same id. The
     * validation only applies if the structure is a enum table structure.
     */
    public final static String MSGCODE_DOUBLE_ENUM_ID = MSGCODE_PREFIX + "DoubleEnumId"; //$NON-NLS-1$

    /**
     * Returns the rows that make up the table.
     */
    public IRow[] getRows();
    
    /**
     * Returns the row of the table at the given index (the first element has the index 0).
     * Returns null if the given index is out of bounds (less than zero or
     * greater or equal than the number of rows).
     */
    public IRow getRow(int rowIndex);

    /**
     * Returns the number of rows in the table.
     */
    public int getNumOfRows();
    
    /**
     * Creates a new row.
     */
    public IRow newRow();

    /**
     * Creates a new row after the given row index.<br>
     * If the index is greater than the number of rows the row will be added at the end.
     */
    public IRow insertRowAfter(int rowIndex);
    
    /**
     * removes all rows
     */
    public void clear();

}
