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

package org.faktorips.devtools.core.ui.table;

import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

public class GridTableViewerTraversalStrategy extends TableTraversalStrategy {

    private GridTableViewer viewer;

    public GridTableViewerTraversalStrategy(IpsCellEditor cellEditor, GridTableViewer viewer, int colIndex) {
        super(cellEditor, colIndex);
        this.viewer = viewer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.keyCode == SWT.CR) {
            editNextRow();
            e.doit = false;
        }
    }

    @Override
    protected void editCell(int rowIndex, int columnIndex) {
        if (columnIndex != getColumnIndex() || rowIndex != getCurrentRow()) {
            viewer.editElement(viewer.getElementAt(rowIndex), columnIndex);
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
