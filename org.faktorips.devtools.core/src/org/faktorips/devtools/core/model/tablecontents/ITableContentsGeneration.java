/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.tablecontents;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;

public interface ITableContentsGeneration extends IIpsObjectGeneration {

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "TABLECONTENTSGENERATION-"; //$NON-NLS-1$

    /**
     * Returns the rows that make up the table.
     */
    public IRow[] getRows();

    /**
     * Returns the row of the table at the given index (the first element has the index 0). Returns
     * null if the given index is out of bounds (less than zero or greater or equal than the number
     * of rows).
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
