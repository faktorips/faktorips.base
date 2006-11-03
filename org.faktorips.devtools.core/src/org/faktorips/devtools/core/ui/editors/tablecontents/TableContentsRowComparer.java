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

package org.faktorips.devtools.core.ui.editors.tablecontents;

import org.eclipse.jface.viewers.IElementComparer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
/**
 * Comparer for rows (<code>IRow</code>) used in the TableViewer 
 * 
 * @author Stefan Widmaier
 */
public class TableContentsRowComparer implements IElementComparer {

    /**
     * If the given objects are of type <code>IRow</code> the values for each column are compared.
     * Otherwise the Object#equals() method is called. 
     * <p>
     * Returns true if the tables of both rows have the same number of columns and if the values for each
     * column are equal in terms of string-equality.
     * {@inheritDoc}
     */
    public boolean equals(Object a, Object b) {
        if(a instanceof IRow && b instanceof IRow){
            IRow row= (IRow) a;
            IRow otherRow= (IRow) b;
            ITableContents table= (ITableContents) row.getIpsObject();
            ITableContents otherTable= (ITableContents) otherRow.getIpsObject();
            if(table.getNumOfColumns()!=otherTable.getNumOfColumns() || !row.getName().equals(otherRow.getName())){
                return false;
            }
            for(int i=0; i<table.getNumOfColumns(); i++){
                if(!getRowValueAt(row, i).equals(getRowValueAt(otherRow, i))){
                    return false;
                }
            }
            return true;
        }
        return a.equals(b);
    }

    /**
     * Returns the given objects hashcode if it is not of type <code>IRow</code>.
     * Otherwise returns the sum of all values' hashCodes in the given row.
     * {@inheritDoc}
     */
    public int hashCode(Object element) {
        if(element instanceof IRow){
            IRow row= (IRow) element;
            ITableContents table= (ITableContents) row.getIpsObject();
            int hashCode= 0;
            hashCode+= row.getName().hashCode();
            for (int i = 0; i < table.getNumOfColumns(); i++) {
                hashCode += getRowValueAt(row, i).hashCode();
            }   
            return hashCode;
        }
        return element.hashCode();
    }

    /**
     * Returns the value at the given column index in the given row. If the value retrieved from the
     * row is null the NULL-representation string (defined by the IpsPreferences) is returned. This
     * method thus never returns <code>null</code>.
     * 
     * @param row The row a value should be retrieved from.
     * @param columnIndex The column index the value should be retrieved from inside the row.
     * @return The value at the given index in the given row or the NULL-representation string
     *         (defined by the IpsPreferences) if the row returned <code>null</code> as a value.
     */
    private String getRowValueAt(IRow row, int columnIndex){
        String value= row.getValue(columnIndex);
        if(value==null){
            value= IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return value;
    }
}
