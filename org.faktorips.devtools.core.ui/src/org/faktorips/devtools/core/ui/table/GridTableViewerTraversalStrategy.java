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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.nebula.jface.gridviewer.GridTableViewer;

public class GridTableViewerTraversalStrategy extends TableTraversalStrategy {

    private GridTableViewer viewer;

    public GridTableViewerTraversalStrategy(IpsCellEditor cellEditor, GridTableViewer viewer, int colIndex) {
        super(cellEditor, colIndex);
        this.viewer = viewer;
    }

    @Override
    protected void editCell(int rowIndex, int columnIndex) {
        /**
         * Grid prevents further line-jumping using Enter, as it does not select the following line
         * in this case (even though a cell editor in the following line is activated).
         * <p>
         * Fix: Set selection to the following line. Caution this may have unexpected side effects.
         */
        if (columnIndex != getColumnIndex() || rowIndex != getCurrentRow()) {
            viewer.getGrid().setSelection(rowIndex);
            viewer.editElement(viewer.getElementAt(rowIndex), columnIndex);
            // System.out.println("Viewer#edit(" + rowIndex + ", " + columnIndex + ") called.");
        }
    }

    @Override
    protected int getColumnCount() {
        return viewer.getGrid().getColumnCount();
    }

    @Override
    protected int getCurrentRow() {
        return viewer.getGrid().getSelectionIndex();
    }

    @Override
    protected int getRowCount() {
        return viewer.getGrid().getItemCount();
    }
}
