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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TraverseEvent;

/**
 * Supports the navigation in a <tt>Table</tt> / <tt>TableViewer</tt> using the
 * <tt>SWT.TRAVERSE_ESCAPE</tt>, <tt>SWT.TRAVERSE_RETURN</tt>, <tt>SWT.TRAVERSE_TAB_NEXT</tt>,
 * <tt>SWT.TRAVERSE_TAB_PREVIOUS</tt>, <tt>SWT.ARROW_DOWN</tt>, <tt>SWT.ARROW_UP</tt> keys.
 * 
 * @author Stefan Widmaier, Alexander Weickmann
 */
public abstract class TableTraversalStrategy extends AbstractTraversalStrategy {
    /** Index of the table column this <tt>CellEditor</tt> was created for. */
    private final int columnIndex;

    public TableTraversalStrategy(IpsCellEditor cellEditor, int colIndex) {
        super(cellEditor);
        columnIndex = colIndex;
    }

    @Override
    public void keyTraversed(TraverseEvent e) {
        if (e.detail == SWT.TRAVERSE_ESCAPE) {
            getCellEditor().deactivate();
            e.doit = false;
        } else if (e.detail == SWT.TRAVERSE_RETURN) {
            editNextRow();
            e.doit = false;
        } else if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
            editNextColumn();
            e.doit = false;
        } else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
            editPreviousColumn();
            e.doit = false;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // FS#1585: if this cell editor is a ComboCellEditor then the arrow down and up
        // feature to create or delete rows are not supported, because otherwise the
        // selection of a new value inside the drop down doesn't work correctly
        if (getCellEditor() instanceof ComboCellEditor) {
            return;
        }
        if (e.keyCode == SWT.ARROW_DOWN) {
            editNextRow();
            e.doit = false;
        } else if (e.keyCode == SWT.ARROW_UP) {
            editPreviousRow();
            e.doit = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        fireApplyEditorValue();
    }

    /**
     * Edits the previous row relative to the current selection of the <tt>TableViewer</tt> this
     * <tt>TableCellEditor</tt> is used in. Does nothing if the first row of the table is selected.
     */
    private void editPreviousRow() {
        fireApplyEditorValue();
        editCell(getPreviousRow(), columnIndex);
    }

    /**
     * Edits the previous column relative to the column this <tt>TableCellEditor</tt> is used for.
     * If there is no previous column, the last cell of the previous row is edited. If in turn the
     * previous row does not exist the first cell of the topmost row of the table is edited (first
     * cell of the table).
     */
    private void editPreviousColumn() {
        if (getPreviousColumn() > columnIndex) {
            fireApplyEditorValue();
            editCell(getPreviousRow(), getPreviousColumn());
        } else {
            editCell(getCurrentRow(), getPreviousColumn());
        }
    }

    /**
     * Edits the next row relative to the current selection of the <tt>TableViewer</tt> this
     * <tt>TableCellEditor</tt> is used in. If no following row exists, this method does nothing.
     */
    protected void editNextRow() {
        if (!isAtNewRow()) {
            fireApplyEditorValue();
            editCell(getNextRow(), columnIndex);
        }
    }

    /**
     * Edits the next column relative to the column this <tt>TableCellEditor</tt> is used for. If
     * there is no next column, the first cell of the next row is edited. If in turn no following
     * row exists this method does nothing.
     */
    protected void editNextColumn() {
        if (isAtNewColumn()) {
            fireApplyEditorValue();
            editCell(getNextRow(), getNextColumn());
        } else {
            editCell(getCurrentRow(), getNextColumn());
        }
    }

    /**
     * Returns the index of the next row. Returns the current row index if no following row exists.
     */
    protected int getNextRow() {
        if (isAtNewRow()) {
            return getCurrentRow();
        }
        return getCurrentRow() + 1;
    }

    /**
     * Returns the index of the next column. If no following row exists the current column index
     * will be returned.
     */
    protected int getNextColumn() {
        int nextColumn = (isAtNewColumn()) ? columnIndex : columnIndex + 1;
        return nextColumn;
    }

    /**
     * Returns the index of the previous row. If the first row is currently selected the index of
     * the first row (0) will be returned.
     */
    protected int getPreviousRow() {
        if (getCurrentRow() - 1 < 0) {
            return 0;
        }
        return getCurrentRow() - 1;
    }

    /**
     * Returns the index of the previous column. If there is no previous column there are two
     * possible behaviors:
     * <ul>
     * <li>If the first row is being edited the first column (0) will be returned.
     * <li>For any other row the highest column index will be returned.
     * </ul>
     * .
     */
    protected int getPreviousColumn() {
        int previousColumn = columnIndex - 1;
        if (previousColumn < 0) {
            previousColumn = (getCurrentRow() == 0) ? 0 : getColumnCount() - 1;
        }
        return previousColumn;
    }

    /** Returns <tt>true</tt> if the next row does not exist, <tt>false</tt> otherwise. */
    private boolean isAtNewRow() {
        return getCurrentRow() + 1 == getRowCount();
    }

    /**
     * Returns <tt>true</tt> if the next column does not exist, <tt>false</tt> otherwise.
     */
    protected boolean isAtNewColumn() {
        int nextColIndex = columnIndex + 1;
        return nextColIndex >= getColumnCount();
    }

    /**
     * Edits the table cell in the given column of the given row. Expects valid values for
     * <tt>rowIndex</tt> and <tt>columnIndex</tt>. Out-of-bound values will cause the table viewer
     * to loose focus.
     * <p>
     * For optimization reasons this method only informs the table viewer of a cell edit if the cell
     * has changed.
     * 
     * @param rowIndex The index of the table row that shall be edited.
     * @param columnIndex The index of the table column that shall be edited.
     */
    protected abstract void editCell(int rowIndex, int columnIndex);

    /** Returns the index of the row that is currently being edited. */
    protected abstract int getCurrentRow();

    protected abstract int getRowCount();

    protected abstract int getColumnCount();

    protected int getColumnIndex() {
        return columnIndex;
    }

}
