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

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.faktorips.util.ArgumentCheck;

/**
 * The table sorter is the default sorter of the table control. It triggers a resort the the given
 * column depending on the comparator suitable for the type of the column.
 * 
 * @author Jacobi
 */
public class Sorter extends ViewerSorter {
    public static final int NATURAL = 0;
    public static final int ASCENDING = 1;
    public static final int DESCENDING = -1;

    // the order ascending/descending or natural (= no sorting)
    private int order = ASCENDING;

    // the currently sorted column.
    private int externalColumnIndex = -1;

    // the table control we are sorting.
    private SpreadsheetControl control = null;

    // the active comparator.
    private Comparator<Object> comparator = null;

    /**
     * Constructor
     */
    public Sorter(SpreadsheetControl control, int externalColumnIndex, Comparator<Object> comparator) {
        super();
        ArgumentCheck.notNull(control);
        this.control = control;
        this.externalColumnIndex = externalColumnIndex;
        this.comparator = comparator;
    }

    void setExternalColumn(int index) {
        externalColumnIndex = index;
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        // ensure the pending row is always the last row in the table.
        if (control.getPendingRow() != null) {
            if (e1 == control.getPendingRow()) {
                return 1;
            }
            if (e2 == control.getPendingRow()) {
                return -1;
            }
        }
        if (comparator == null) {
            // natural order.
            return 0;
        }

        // sort the values.
        Object v1 = control.getCellValue(e1, externalColumnIndex);
        Object v2 = control.getCellValue(e2, externalColumnIndex);

        return order * comparator.compare(v1, v2);
    }

    /**
     * Returns the currently sorted column
     */
    int getExternalColumnIndex() {
        return externalColumnIndex;
    }

    /**
     * Circulates through the orders ascending, descending, natural.
     */
    public int nextOrder() {
        switch (order) {
            case ASCENDING:
                order = DESCENDING;
                break;

            case DESCENDING:
                order = NATURAL;
                break;

            case NATURAL:
                order = ASCENDING;
                break;
        }

        return order;
    }

    /**
     * Get the current order of the sorter.
     * 
     * @return int, either <code>ASCENDING</code>, <code>DESCENDING</code>, <code>NATURAL</code>.
     */
    int getOrder() {
        return order;
    }

}
