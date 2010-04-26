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

package org.faktorips.devtools.core.ui.controls.spreadsheet;

import org.eclipse.jface.viewers.IStructuredContentProvider;

/**
 *
 */
public interface TableContentProvider extends IStructuredContentProvider {

    /**
     * Returns information about the columns.
     */
    public ColumnInfo[] getColumnInfos();

    /**
     * Creates a new row.
     */
    public Object newRow();

    /**
     * If the user leaves the last row in the table this method is called if the row has been
     * appended. If the method returns false, the row is automatically deleted from the model and
     * the table is refreshed.
     * 
     * @param input, the table input object
     * @param row, the row object to validate
     * @return true, if the row is valid, false otherwise.
     */
    public boolean deletePendingRow(Object row);

    /**
     * Deletes the indicated row.
     */
    public boolean deleteRow(Object row);

}
